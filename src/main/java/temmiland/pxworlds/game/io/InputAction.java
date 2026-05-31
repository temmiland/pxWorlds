package temmiland.pxworlds.game.io;

import temmiland.rollercoaster.platform.io.Key;

/**
 * Hochsprachige Spiel-Aktionen mit ihrem Standard-Keybinding.
 * Alle Game-States und Entities verwenden ausschließlich diese Enum – keine rohen GLFW-Keycodes.
 */
public enum InputAction {
    /** ESC – Pause-Menü öffnen/schließen oder Spiel beenden. */
    PAUSE(Key.ESCAPE),
    /** A – Spieler nach links bewegen. */
    MOVE_LEFT(Key.A),
    /** D – Spieler nach rechts bewegen. */
    MOVE_RIGHT(Key.D),
    /** W – Spieler nach oben bewegen. */
    MOVE_UP(Key.W),
    /** S – Spieler nach unten bewegen. */
    MOVE_DOWN(Key.S),
    /** LEFT_SHIFT – Sprint aktivieren. */
    SPRINT(Key.LEFT_SHIFT),
    /** F3 – Debug-Overlay umschalten. */
    DEBUG_TOGGLE(Key.F3),
    /** F4 – Debug-UI umschalten (nur Dev-Mode). */
    DEBUG_UI_TOGGLE(Key.F4),
    /** BACKSPACE – letztes Zeichen löschen (Texteingabe). */
    BACKSPACE(Key.BACKSPACE),
    /** ENTER – Eingabe bestätigen. */
    CONFIRM(Key.ENTER);

    /** Standard-GLFW-Keycode für diese Aktion. */
    private final Key defaultKey;

    InputAction(final Key key) {
        this.defaultKey = key;
    }

    /** @return Standard-GLFW-Keycode für diese Aktion */
    public Key getDefaultKey() {
        return defaultKey;
    }
}
