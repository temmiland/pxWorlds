package com.pxworlds.game.io;

import org.joml.Vector2f;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;

/**
 * Verarbeitet rohe {@link InputEvent}s aus der Queue und übersetzt sie
 * in GLFW-freie, hochsprachige Spiel-Aktionen.
 *
 * <p>Wird einmal pro Tick in {@link Window#update()} aufgerufen —
 * bevor die Game-States updaten. Entities (z.B. {@link com.pxworlds.game.entity.Player})
 * und GUI-Elemente fragen diesen Processor für den aktuellen Eingabezustand ab
 * und haben keine direkte GLFW-Abhängigkeit.</p>
 *
 * <p>Interne Key-Zuordnung (WASD + Shift):
 * <ul>
 *   <li>A → {@link #isMoveLeft()}</li>
 *   <li>D → {@link #isMoveRight()}</li>
 *   <li>W → {@link #isMoveUp()}</li>
 *   <li>S → {@link #isMoveDown()}</li>
 *   <li>LEFT_SHIFT → {@link #isSprinting()}</li>
 * </ul>
 * </p>
 */
public class InputProcessor {

    /** Aktuell gedrückt gehaltene Tasten als GLFW-Keycodes. */
    private final Set<Integer> heldKeys = new HashSet<>();
    /** Tasten, die in diesem Tick das erste Mal gedrückt wurden (KEY_DOWN-Events). */
    private final Set<Integer> pressedThisTick = new HashSet<>();
    /** Aktuell gedrückt gehaltene Maustasten als GLFW-Mausbutton-Codes. */
    private final Set<Integer> heldMouseButtons = new HashSet<>();

    /** Letzte bekannte Cursor-X-Position in GLFW-Bildschirmkoordinaten (von oben links). */
    private double rawMouseX;
    /** Letzte bekannte Cursor-Y-Position in GLFW-Bildschirmkoordinaten (von oben links). */
    private double rawMouseY;

    /** Bewegung nach links (A). */
    private boolean moveLeft;
    /** Bewegung nach rechts (D). */
    private boolean moveRight;
    /** Bewegung nach oben (W). */
    private boolean moveUp;
    /** Bewegung nach unten (S). */
    private boolean moveDown;
    /** Sprint-Modus (LEFT_SHIFT). */
    private boolean sprint;

    /**
     * Leert die Event-Queue und aktualisiert den internen Aktionszustand.
     * Muss genau einmal pro Tick aufgerufen werden, bevor Entities den Zustand lesen.
     *
     * @param eventQueue die Queue aus {@link InputHandler}
     */
    public void processEvents(final Queue<InputEvent> eventQueue) {
        pressedThisTick.clear();

        InputEvent event;
        while ((event = eventQueue.poll()) != null) {
            switch (event.getType()) {
                case KEY_DOWN:
                    heldKeys.add(event.getKeyCode());
                    pressedThisTick.add(event.getKeyCode());
                    break;
                case KEY_UP:
                    heldKeys.remove(event.getKeyCode());
                    break;
                case MOUSE_PRESS:
                    heldMouseButtons.add(event.getMouseButton());
                    break;
                case MOUSE_RELEASE:
                    heldMouseButtons.remove(event.getMouseButton());
                    break;
                case MOUSE_MOVE:
                    rawMouseX = event.getMouseX();
                    rawMouseY = event.getMouseY();
                    break;
                default:
                    break;
            }
        }

        // Gedrückte Tasten auf benannte Aktionen abbilden
        moveLeft  = heldKeys.contains(GLFW_KEY_A);
        moveRight = heldKeys.contains(GLFW_KEY_D);
        moveUp    = heldKeys.contains(GLFW_KEY_W);
        moveDown  = heldKeys.contains(GLFW_KEY_S);
        sprint    = heldKeys.contains(GLFW_KEY_LEFT_SHIFT);
    }

    // --- One-Shot-Abfragen ---

    /**
     * Gibt {@code true} zurück, wenn die Taste in diesem Tick das erste Mal gedrückt wurde.
     * Ideal für einmalige Aktionen (Menü öffnen, Debug-Toggle etc.).
     *
     * @param keyCode GLFW-Keycode
     * @return {@code true} nur im Tick des ersten Tastendrucks
     */
    public boolean wasPressed(final int keyCode) {
        return pressedThisTick.contains(keyCode);
    }

    // --- Maus-Abfragen ---

    /**
     * Gibt zurück, ob die angegebene Maustaste aktuell gedrückt gehalten wird.
     *
     * @param button GLFW-Mausbutton-Code (z.B. {@code GLFW_MOUSE_BUTTON_LEFT = 0})
     * @return {@code true} solange der Button gehalten wird
     */
    public boolean isMouseButtonDown(final int button) {
        return heldMouseButtons.contains(button);
    }

    /**
     * Berechnet die Mausposition relativ zur Fenstermitte, Y-Achse nach oben.
     * Entspricht dem Koordinatensystem der GUI-Kamera.
     *
     * @param windowWidth  aktuelle Fensterbreite in Pixeln
     * @param windowHeight aktuelle Fensterhöhe in Pixeln
     * @return Mausposition im GUI-Koordinatensystem
     */
    public Vector2f getMousePosition(final int windowWidth, final int windowHeight) {
        final float centeredX = (float) rawMouseX - (windowWidth / 2.0f);
        final float centeredY = -((float) rawMouseY - (windowHeight / 2.0f));
        return new Vector2f(centeredX, centeredY);
    }

    // --- Bewegungs-Abfragen (GLFW-freie API für Entities) ---

    /**
     * @return {@code true}, wenn A gedrückt gehalten wird
     */
    public boolean isMoveLeft() {
        return moveLeft;
    }

    /**
     * @return {@code true}, wenn D gedrückt gehalten wird
     */
    public boolean isMoveRight() {
        return moveRight;
    }

    /**
     * @return {@code true}, wenn W gedrückt gehalten wird
     */
    public boolean isMoveUp() {
        return moveUp;
    }

    /**
     * @return {@code true}, wenn S gedrückt gehalten wird
     */
    public boolean isMoveDown() {
        return moveDown;
    }

    /**
     * @return {@code true}, wenn LEFT_SHIFT gedrückt gehalten wird
     */
    public boolean isSprinting() {
        return sprint;
    }
}
