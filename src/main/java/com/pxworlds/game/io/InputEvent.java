package com.pxworlds.game.io;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;

/**
 * Immutable data-transfer object für ein einzelnes Input-Event.
 * Wird von {@link InputHandler} via GLFW-Callbacks erzeugt und
 * von {@link InputProcessor} pro Tick verarbeitet.
 */
public final class InputEvent {

    /** Typ des Input-Events. */
    public enum Type {
        /** Eine Taste wurde gedrückt. */
        KEY_DOWN,
        /** Eine Taste wurde losgelassen. */
        KEY_UP,
        /** Eine Maustaste wurde gedrückt. */
        MOUSE_PRESS,
        /** Eine Maustaste wurde losgelassen. */
        MOUSE_RELEASE,
        /** Der Mauszeiger wurde bewegt. */
        MOUSE_MOVE,
        /**
         * Das Fenster hat den Fokus verloren.
         * {@link com.pxworlds.game.io.InputProcessor} löscht beim Empfang alle
         * gedrückt-gehaltenen Tasten und Maustasten, damit keine Keys „stecken" bleiben.
         */
        FOCUS_LOST,
        /** Ein Unicode-Zeichen wurde getippt (via GLFW-Char-Callback, für Texteingaben). */
        CHAR_TYPED
    }

    /** Typ dieses Events. */
    private final Type type;
    /** GLFW-Keycode, oder {@code GLFW_KEY_UNKNOWN} bei Nicht-Tasten-Events. */
    private final int keyCode;
    /** GLFW-Maustaste, oder {@code -1} bei Nicht-Mausbutton-Events. */
    private final int mouseButton;
    /** Cursor-X in Bildschirmkoordinaten (nur bei Maus-Events relevant). */
    private final double mouseX;
    /** Cursor-Y in Bildschirmkoordinaten (nur bei Maus-Events relevant). */
    private final double mouseY;
    /** Unicode-Codepoint des getippten Zeichens (nur bei CHAR_TYPED relevant). */
    private final int codepoint;

    private InputEvent(
            final Type eventType,
            final int eventKeyCode,
            final int eventMouseButton,
            final double eventMouseX,
            final double eventMouseY,
            final int eventCodepoint) {
        this.type = eventType;
        this.keyCode = eventKeyCode;
        this.mouseButton = eventMouseButton;
        this.mouseX = eventMouseX;
        this.mouseY = eventMouseY;
        this.codepoint = eventCodepoint;
    }

    // --- Factory-Methoden ---

    /**
     * Erzeugt ein KEY_DOWN-Event.
     * @param eventKeyCode GLFW-Keycode der gedrückten Taste
     * @return neues {@code InputEvent}
     */
    public static InputEvent keyDown(final int eventKeyCode) {
        return new InputEvent(Type.KEY_DOWN, eventKeyCode, -1, 0, 0, 0);
    }

    /**
     * Erzeugt ein KEY_UP-Event.
     * @param eventKeyCode GLFW-Keycode der losgelassenen Taste
     * @return neues {@code InputEvent}
     */
    public static InputEvent keyUp(final int eventKeyCode) {
        return new InputEvent(Type.KEY_UP, eventKeyCode, -1, 0, 0, 0);
    }

    /**
     * Erzeugt ein MOUSE_PRESS-Event.
     * @param button GLFW-Maustaste
     * @param eventMouseX Cursor-X beim Klick
     * @param eventMouseY Cursor-Y beim Klick
     * @return neues {@code InputEvent}
     */
    public static InputEvent mousePress(
            final int button,
            final double eventMouseX,
            final double eventMouseY) {
        return new InputEvent(Type.MOUSE_PRESS, GLFW_KEY_UNKNOWN, button, eventMouseX, eventMouseY, 0);
    }

    /**
     * Erzeugt ein MOUSE_RELEASE-Event.
     * @param button GLFW-Maustaste
     * @param eventMouseX Cursor-X beim Loslassen
     * @param eventMouseY Cursor-Y beim Loslassen
     * @return neues {@code InputEvent}
     */
    public static InputEvent mouseRelease(
            final int button,
            final double eventMouseX,
            final double eventMouseY) {
        return new InputEvent(Type.MOUSE_RELEASE, GLFW_KEY_UNKNOWN, button, eventMouseX, eventMouseY, 0);
    }

    /**
     * Erzeugt ein MOUSE_MOVE-Event.
     * @param eventMouseX neue Cursor-X-Position
     * @param eventMouseY neue Cursor-Y-Position
     * @return neues {@code InputEvent}
     */
    public static InputEvent mouseMove(final double eventMouseX, final double eventMouseY) {
        return new InputEvent(Type.MOUSE_MOVE, GLFW_KEY_UNKNOWN, -1, eventMouseX, eventMouseY, 0);
    }

    /**
     * Erzeugt ein FOCUS_LOST-Event.
     * Wird von {@link InputHandler} ausgelöst wenn das Fenster den Fokus verliert,
     * damit der {@link InputProcessor} alle gedrückten Tasten zurücksetzen kann.
     * @return neues {@code InputEvent}
     */
    public static InputEvent focusLost() {
        return new InputEvent(Type.FOCUS_LOST, GLFW_KEY_UNKNOWN, -1, 0, 0, 0);
    }

    /**
     * Erzeugt ein CHAR_TYPED-Event.
     * @param codepoint Unicode-Codepoint des getippten Zeichens
     * @return neues {@code InputEvent}
     */
    public static InputEvent charTyped(final int codepoint) {
        return new InputEvent(Type.CHAR_TYPED, GLFW_KEY_UNKNOWN, -1, 0, 0, codepoint);
    }

    // --- Getter ---

    /**
     * @return Typ dieses Events
     */
    public Type getType() {
        return type;
    }

    /**
     * @return GLFW-Keycode, oder {@code GLFW_KEY_UNKNOWN} bei Nicht-Tasten-Events
     */
    public int getKeyCode() {
        return keyCode;
    }

    /**
     * @return GLFW-Maustaste, oder {@code -1} bei Nicht-Mausbutton-Events
     */
    public int getMouseButton() {
        return mouseButton;
    }

    /**
     * @return Cursor-X in Bildschirmkoordinaten
     */
    public double getMouseX() {
        return mouseX;
    }

    /**
     * @return Cursor-Y in Bildschirmkoordinaten
     */
    public double getMouseY() {
        return mouseY;
    }

    /**
     * @return Unicode-Codepoint des getippten Zeichens (nur bei CHAR_TYPED relevant)
     */
    public int getCodepoint() {
        return codepoint;
    }

    @Override
    public String toString() {
        return "InputEvent{type=" + type
                + ", keyCode=" + keyCode
                + ", mouseButton=" + mouseButton
                + ", mouseX=" + mouseX
                + ", mouseY=" + mouseY + "}";
    }
}
