package land.temmi.pxworlds.io;

import land.temmi.rollercoaster.platform.input.Key;

/**
 * High-level game actions with their default key binding.
 *
 * <p>All game states and entities use only this enum — never raw GLFW key
 * codes.</p>
 */
public enum InputAction {
    /** ESC – open/close the pause menu or quit the game. */
    PAUSE(Key.ESCAPE),
    /** A – move the player left. */
    MOVE_LEFT(Key.A),
    /** D – move the player right. */
    MOVE_RIGHT(Key.D),
    /** W – move the player up. */
    MOVE_UP(Key.W),
    /** S – move the player down. */
    MOVE_DOWN(Key.S),
    /** LEFT_SHIFT – enable sprinting. */
    SPRINT(Key.LEFT_SHIFT),
    /** F3 – toggle the debug overlay. */
    DEBUG_TOGGLE(Key.F3),
    /** F4 – toggle the debug UI (dev mode only). */
    DEBUG_UI_TOGGLE(Key.F4),
    /** BACKSPACE – delete the last character (text input). */
    BACKSPACE(Key.BACKSPACE),
    /** ENTER – confirm input. */
    CONFIRM(Key.ENTER);

    /** Default GLFW key code for this action. */
    private final Key defaultKey;

    /**
     * @param key the default key for this action
     */
    InputAction(final Key key) {
        this.defaultKey = key;
    }

    /** @return the default GLFW key code for this action */
    public Key getDefaultKey() {
        return defaultKey;
    }
}
