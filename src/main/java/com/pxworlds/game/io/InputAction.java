package com.pxworlds.game.io;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F3;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F4;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;

/**
 * Hochsprachige Spiel-Aktionen mit ihrem Standard-Keybinding.
 * Alle Game-States und Entities verwenden ausschließlich diese Enum – keine rohen GLFW-Keycodes.
 */
public enum InputAction {
    /** ESC – Pause-Menü öffnen/schließen oder Spiel beenden. */
    PAUSE(GLFW_KEY_ESCAPE),
    /** A – Spieler nach links bewegen. */
    MOVE_LEFT(GLFW_KEY_A),
    /** D – Spieler nach rechts bewegen. */
    MOVE_RIGHT(GLFW_KEY_D),
    /** W – Spieler nach oben bewegen. */
    MOVE_UP(GLFW_KEY_W),
    /** S – Spieler nach unten bewegen. */
    MOVE_DOWN(GLFW_KEY_S),
    /** LEFT_SHIFT – Sprint aktivieren. */
    SPRINT(GLFW_KEY_LEFT_SHIFT),
    /** F3 – Debug-Overlay umschalten. */
    DEBUG_TOGGLE(GLFW_KEY_F3),
    /** F4 – Debug-UI umschalten (nur Dev-Mode). */
    DEBUG_UI_TOGGLE(GLFW_KEY_F4),
    /** BACKSPACE – letztes Zeichen löschen (Texteingabe). */
    BACKSPACE(GLFW_KEY_BACKSPACE),
    /** ENTER – Eingabe bestätigen. */
    CONFIRM(GLFW_KEY_ENTER);

    /** Standard-GLFW-Keycode für diese Aktion. */
    private final int defaultKey;

    InputAction(final int key) {
        this.defaultKey = key;
    }

    /** @return Standard-GLFW-Keycode für diese Aktion */
    public int getDefaultKey() {
        return defaultKey;
    }
}
