package com.pxworlds.game.gui;

import com.pxworlds.game.debug.DebugRenderer;
import com.pxworlds.game.io.InputAction;
import com.pxworlds.game.io.InputProcessor;
import com.pxworlds.game.rendering.Camera;
import org.joml.Vector2f;
import org.lwjgl.stb.STBEasyFont;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glUseProgram;

/**
 * Einfaches Text-Eingabe-Widget für GUI-Screens.
 *
 * <p>Zeichen werden via GLFW-Char-Callback (Unicode-Codepoints aus
 * {@link InputProcessor#getTypedCodepoints()}) eingelesen.
 * Nur ASCII-Zeichen {@code [A-Za-z0-9_-]} werden akzeptiert.
 * Backspace löscht das letzte Zeichen; Enter löst den konfigurierten
 * {@code onConfirm}-Callback aus.</p>
 *
 * <p>Das Widget ist kein {@link Gui}-Element — es muss manuell in
 * {@code update()} und {@code render()} des betreffenden Gui-Screens
 * aufgerufen werden.</p>
 */
public class TextInput {

    /**
     * Maximale Anzahl erlaubter Zeichen.
     * Entspricht der sicheren Anzeigebreite in {@link WorldSelectGui} —
     * längere Namen würden den Button-Rahmen überschreiten.
     */
    public static final int MAX_LENGTH = 18;

    private static final float FONT_SCALE  = 2.5f;
    private static final float FONT_HEIGHT = 7.0f;
    /** Cursor-Blinkintervall in Sekunden. */
    private static final float BLINK_INTERVAL = 0.5f;

    /** Hintergrundfarbe des Felds. */
    private static final float BG_R = 0.15f, BG_G = 0.15f, BG_B = 0.15f;
    /** Rahmenfarbe (aktiv). */
    private static final float BORDER_R = 0.9f, BORDER_G = 0.9f, BORDER_B = 0.9f;
    /** Rahmendicke in virtuellen Einheiten. */
    private static final float BORDER = 2f;

    private final Vector2f position;
    private final Vector2f halfExtent;
    private final StringBuilder value = new StringBuilder();
    private float blinkTimer;
    private boolean cursorVisible = true;

    /** Callback bei Enter-Taste (Eingabe bestätigen). */
    private final Runnable onConfirm;

    /**
     * @param position   Mittelpunkt des Felds in GUI-Koordinaten
     * @param halfExtent Halbe Breite/Höhe des Felds
     * @param onConfirm  Callback bei Enter – erhält den aktuellen Wert via {@link #getValue()}
     */
    public TextInput(final Vector2f position, final Vector2f halfExtent, final Runnable onConfirm) {
        this.position   = position;
        this.halfExtent = halfExtent;
        this.onConfirm  = onConfirm;
    }

    /**
     * Verarbeitet Tastatureingaben für diesen Tick.
     *
     * @param input  aktueller {@link InputProcessor}
     * @param delta  Tick-Delta in Sekunden (für Cursor-Blinken)
     */
    public void update(final InputProcessor input, final double delta) {
        blinkTimer += (float) delta;
        if (blinkTimer >= BLINK_INTERVAL) {
            blinkTimer -= BLINK_INTERVAL;
            cursorVisible = !cursorVisible;
        }

        // Getippte Zeichen anfügen
        for (final int cp : input.getTypedCodepoints()) {
            if (value.length() >= MAX_LENGTH) {
                break;
            }
            if (isAllowed(cp)) {
                value.appendCodePoint(cp);
                resetCursor();
            }
        }

        // Backspace
        if (input.wasPressed(InputAction.BACKSPACE) && !value.isEmpty()) {
            value.deleteCharAt(value.length() - 1);
            resetCursor();
        }

        // Enter → bestätigen
        if (input.wasPressed(InputAction.CONFIRM) && onConfirm != null) {
            onConfirm.run();
        }
    }

    /**
     * Zeichnet Hintergrund, Rahmen, Text und blinkenden Cursor.
     *
     * @param camera GUI-Kamera
     */
    public void render(final Camera camera) {
        final int vw = camera.getVirtualWidth();
        final int vh = camera.getVirtualHeight();

        final float cx = position.x;
        final float cy = position.y;
        final float hw = halfExtent.x;
        final float hh = halfExtent.y;

        glUseProgram(0);
        glDisable(GL_TEXTURE_2D);
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        // Standard-Fensterkoordinaten (0,0 = oben links)
        glOrtho(0, vw, vh, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        // GUI → Fensterkoordinaten (Y invertieren, Mitte verschieben)
        final float screenX = cx - hw + vw / 2.0f;
        final float screenY = -cy - hh + vh / 2.0f;
        final float screenW = hw * 2;
        final float screenH = hh * 2;

        // Rahmen
        glColor3f(BORDER_R, BORDER_G, BORDER_B);
        glBegin(GL_QUADS);
        glVertex2f(screenX - BORDER,         screenY - BORDER);
        glVertex2f(screenX + screenW + BORDER, screenY - BORDER);
        glVertex2f(screenX + screenW + BORDER, screenY + screenH + BORDER);
        glVertex2f(screenX - BORDER,         screenY + screenH + BORDER);
        glEnd();

        // Hintergrund
        glColor3f(BG_R, BG_G, BG_B);
        glBegin(GL_QUADS);
        glVertex2f(screenX,          screenY);
        glVertex2f(screenX + screenW, screenY);
        glVertex2f(screenX + screenW, screenY + screenH);
        glVertex2f(screenX,          screenY + screenH);
        glEnd();

        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();

        // Text + Cursor mit FONT_SCALE rendern
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, vw, vh, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();
        glScalef(FONT_SCALE, FONT_SCALE, 1f);

        final String display = value.toString() + (cursorVisible ? "|" : " ");
        final float scaledH  = FONT_HEIGHT * FONT_SCALE;
        // Vertical center within the field
        final float textY = (-cy - scaledH / 2.0f + vh / 2.0f) / FONT_SCALE;
        final float textX = (-hw + 8 + cx + vw / 2.0f) / FONT_SCALE;

        DebugRenderer.drawStringShadowed(display, textX, textY);

        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();

        glEnable(GL_TEXTURE_2D);
    }

    /** @return aktueller Eingabetext */
    public String getValue() {
        return value.toString();
    }

    /** Löscht den Inhalt des Felds. */
    public void clear() {
        value.setLength(0);
        resetCursor();
    }

    /** Setzt den Inhalt des Felds; erlaubte Zeichen werden gefiltert. */
    public void setValue(final String text) {
        value.setLength(0);
        text.codePoints()
            .filter(TextInput::isAllowed)
            .limit(MAX_LENGTH)
            .forEach(value::appendCodePoint);
        resetCursor();
    }

    private void resetCursor() {
        blinkTimer   = 0f;
        cursorVisible = true;
    }

    private static boolean isAllowed(final int cp) {
        return (cp >= 'a' && cp <= 'z')
                || (cp >= 'A' && cp <= 'Z')
                || (cp >= '0' && cp <= '9')
                || cp == '_'
                || cp == '-';
    }

    /** Pixel-Breite des aktuellen Textes in STBEasyFont-Einheiten × FONT_SCALE. */
    @SuppressWarnings("unused")
    public float getTextWidth() {
        return STBEasyFont.stb_easy_font_width(value.toString()) * FONT_SCALE;
    }
}
