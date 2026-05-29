package com.pxworlds.game.gui;

import com.pxworlds.game.io.InputProcessor;
import com.pxworlds.game.io.Window;
import com.pxworlds.game.rendering.Camera;
import org.joml.Vector2f;
import org.lwjgl.stb.STBEasyFont;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glUseProgram;

/**
 * GUI-Screen zum Erstellen einer neuen Welt.
 *
 * <p>Zeigt ein {@link TextInput}-Feld für den Weltnamen sowie Buttons
 * zum Bestätigen und Zurückgehen. Fehlermeldungen (z.B. doppelter Name,
 * leeres Feld) werden unter dem Textfeld angezeigt.</p>
 */
public class NewWorldGui extends Gui {

    /** Tilesheet-Name für das GUI. */
    private static final String GUI_TILESHEET   = "menustate";
    /** Anzahl Tiles im Tilesheet. */
    private static final int    TILESHEET_TILES  = 21;

    /** Y-Position des Titels. */
    private static final float TITLE_Y     = 100f;
    /** Y-Position des Textfelds. */
    private static final float INPUT_Y     = 20f;
    /** Halbe Breite des Textfelds. */
    private static final float INPUT_HW    = 150f;
    /** Halbe Höhe des Textfelds. */
    private static final float INPUT_HH    = 22f;
    /** Y-Position der Fehlermeldung. */
    private static final float ERROR_Y     = -30f;
    /** X-Position des Erstellen-Buttons. */
    private static final float CREATE_X    = -115f;
    /** X-Position des Zurück-Buttons. */
    private static final float BACK_X      =  115f;
    /** Y-Position der Aktions-Buttons. */
    private static final float ACTION_Y    = -100f;
    /** Halbe Breite der Aktions-Buttons. */
    private static final float ACTION_HW   = 100f;
    /** Halbe Höhe der Aktions-Buttons. */
    private static final float ACTION_HH   = 22f;

    /** Skalierung des Titeltexts. */
    private static final float TITLE_SCALE = 2.5f;
    /** Skalierung der Fehlermeldung. */
    private static final float ERROR_SCALE = 1.8f;
    /** Fehlerfarbe Rot. */
    private static final float ERROR_R     = 1.0f;
    /** Fehlerfarbe Grün. */
    private static final float ERROR_G     = 0.3f;
    /** Fehlerfarbe Blau. */
    private static final float ERROR_B     = 0.3f;

    /** Texteingabefeld für den Weltnamen. */
    private final TextInput nameInput;
    /** Aktuell angezeigte Fehlermeldung (leer = keine). */
    private String errorMessage = "";

    public NewWorldGui(
            final Window window,
            final Runnable onCreate,
            final Runnable onBack) {
        super(window, GUI_TILESHEET, TILESHEET_TILES);
        this.nameInput = new TextInput(
                new Vector2f(0, INPUT_Y),
                new Vector2f(INPUT_HW, INPUT_HH),
                onCreate);
        registerButtons(
                new Button(new Vector2f(CREATE_X, ACTION_Y), new Vector2f(ACTION_HW, ACTION_HH),
                        "Create", onCreate),
                new Button(new Vector2f(BACK_X, ACTION_Y), new Vector2f(ACTION_HW, ACTION_HH),
                        "Back", onBack));
    }

    /**
     * Aktualisiert Buttons und TextInput.
     * Muss mit dem regulären Gui-Update kombiniert werden.
     */
    @Override
    public void render() {
        super.render();
        drawLabel("New World", 0, TITLE_Y, TITLE_SCALE, 1f, 1f, 1f);
        nameInput.render(camera);
        if (!errorMessage.isEmpty()) {
            drawLabel(errorMessage, 0, ERROR_Y, ERROR_SCALE, ERROR_R, ERROR_G, ERROR_B);
        }
    }

    public void update(final InputProcessor input, final Vector2f mousePos,
                       final int windowWidth, final int windowHeight,
                       final double delta) {
        super.update(input, mousePos, windowWidth, windowHeight);
        nameInput.update(input, delta);
    }

    /** @return aktuellen Eingabetext (Weltname). */
    public String getWorldName() {
        return nameInput.getValue();
    }

    /**
     * Setzt die Fehlermeldung (leer = keine Anzeige).
     * @param message anzuzeigende Fehlermeldung oder {@code null}
     */
    public void setError(final String message) {
        if (message == null) {
            this.errorMessage = "";
        } else {
            this.errorMessage = message;
        }
    }

    /** Löscht das Eingabefeld und die Fehlermeldung. */
    public void reset() {
        nameInput.clear();
        errorMessage = "";
    }

    private void drawLabel(final String text, final float guiX, final float guiY,
                           final float scale, final float r, final float g, final float b) {
        final int vw = camera.getVirtualWidth();
        final int vh = camera.getVirtualHeight();
        final float fontH = 7f * scale;
        final float textW = STBEasyFont.stb_easy_font_width(text) * scale;
        final float drawX = (guiX - textW / 2.0f + vw / 2.0f) / scale;
        final float drawY = (-guiY - fontH / 2.0f + vh / 2.0f) / scale;

        glUseProgram(0);
        glDisable(GL_TEXTURE_2D);
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, vw, vh, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();
        glScalef(scale, scale, 1f);
        glColor3f(r, g, b);
        com.pxworlds.game.debug.DebugRenderer.drawString(text + " ", drawX, drawY);
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
        glEnable(GL_TEXTURE_2D);
    }
}
