package com.pxworlds.game.io;

import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

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
    /** Maustasten, die in diesem Tick das erste Mal gedrückt wurden. */
    private final Set<Integer> pressedMouseButtonsThisTick = new HashSet<>();
    /** Unicode-Zeichen die in diesem Tick getippt wurden (für TextInput-Widgets). */
    private final List<Integer> typedCodepoints = new ArrayList<>();

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
        pressedMouseButtonsThisTick.clear();
        typedCodepoints.clear();

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
                    pressedMouseButtonsThisTick.add(event.getMouseButton());
                    break;
                case MOUSE_RELEASE:
                    heldMouseButtons.remove(event.getMouseButton());
                    break;
                case MOUSE_MOVE:
                    rawMouseX = event.getMouseX();
                    rawMouseY = event.getMouseY();
                    break;
                case FOCUS_LOST:
                    heldKeys.clear();
                    heldMouseButtons.clear();
                    pressedMouseButtonsThisTick.clear();
                    break;
                case CHAR_TYPED:
                    typedCodepoints.add(event.getCodepoint());
                    break;
                default:
                    break;
            }
        }

        // Gedrückte Tasten auf benannte Aktionen abbilden
        moveLeft  = isHeld(InputAction.MOVE_LEFT);
        moveRight = isHeld(InputAction.MOVE_RIGHT);
        moveUp    = isHeld(InputAction.MOVE_UP);
        moveDown  = isHeld(InputAction.MOVE_DOWN);
        sprint    = isHeld(InputAction.SPRINT);
    }

    // --- One-Shot-Abfragen ---

    /**
     * Gibt {@code true} zurück, wenn die der Aktion zugeordnete Taste in diesem Tick
     * das erste Mal gedrückt wurde. Ideal für einmalige Aktionen (Pause, Debug-Toggle etc.).
     *
     * @param action die zu prüfende {@link InputAction}
     * @return {@code true} nur im Tick des ersten Tastendrucks
     */
    public boolean wasPressed(final InputAction action) {
        return pressedThisTick.contains(action.getDefaultKey());
    }

    /**
     * Gibt {@code true} zurück, wenn die der Aktion zugeordnete Taste aktuell gehalten wird.
     *
     * @param action die zu prüfende {@link InputAction}
     * @return {@code true} solange die Taste gehalten wird
     */
    public boolean isHeld(final InputAction action) {
        return heldKeys.contains(action.getDefaultKey());
    }

    // --- Maus-Abfragen ---

    /**
     * Gibt {@code true} zurück, wenn die Maustaste in diesem Tick das erste Mal gedrückt wurde.
     *
     * @param button GLFW-Mausbutton-Code (z.B. {@code GLFW_MOUSE_BUTTON_LEFT = 0})
     * @return {@code true} nur im Tick des ersten Mausklicks
     */
    public boolean wasMouseButtonPressed(final int button) {
        return pressedMouseButtonsThisTick.contains(button);
    }

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

    // --- Texteingabe ---

    /**
     * Gibt die Liste der in diesem Tick getippten Unicode-Codepoints zurück.
     * Wird pro Tick von {@link #processEvents} geleert und neu befüllt.
     * Nur printbare Zeichen (via GLFW-Char-Callback) sind enthalten —
     * Sondertasten wie Backspace erscheinen hier nicht.
     *
     * @return unveränderliche Sicht auf die Codepoint-Liste für diesen Tick
     */
    public List<Integer> getTypedCodepoints() {
        return typedCodepoints;
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
