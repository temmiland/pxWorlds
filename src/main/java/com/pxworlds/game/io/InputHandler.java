package com.pxworlds.game.io;

import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWWindowFocusCallback;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwSetCharCallback;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowFocusCallback;

/**
 * Registriert GLFW-Callbacks und schreibt {@link InputEvent}-Objekte in eine Queue.
 * Die Queue wird einmal pro Tick von {@link InputProcessor} geleert.
 *
 * <p>GLFW-Callbacks laufen auf dem gleichen Thread wie {@code glfwPollEvents()},
 * sodass keine Race-Conditions entstehen — {@link ConcurrentLinkedQueue} bietet
 * dennoch thread-sichere Guarantees für zukünftige Erweiterungen.</p>
 */
public class InputHandler {

    /** Thread-sichere Queue für rohe GLFW-Events. */
    private final ConcurrentLinkedQueue<InputEvent> eventQueue = new ConcurrentLinkedQueue<>();

    /** GLFW-Fenster-Handle, wird für das Detachen der Callbacks in {@link #free()} benötigt. */
    private long windowHandle;

    /**
     * Keyboard-Callback — Referenz muss gehalten werden, damit der GC sie nicht freigibt.
     */
    private GLFWKeyCallback keyCallback;
    /**
     * Mausbutton-Callback — Referenz muss gehalten werden, damit der GC sie nicht freigibt.
     */
    private GLFWMouseButtonCallback mouseButtonCallback;
    /**
     * Cursor-Callback — Referenz muss gehalten werden, damit der GC sie nicht freigibt.
     */
    private GLFWCursorPosCallback cursorPosCallback;
    /**
     * Fenster-Fokus-Callback — feuert FOCUS_LOST wenn das Fenster den Fokus verliert,
     * damit der InputProcessor stuck keys zurücksetzen kann.
     */
    private GLFWWindowFocusCallback windowFocusCallback;
    /**
     * Char-Callback — liefert Unicode-Codepoints für Texteingaben (TextInput-Widget).
     * Wird nur für printbare Zeichen gefeuert, nicht für Sondertasten wie Backspace.
     */
    private GLFWCharCallback charCallback;

    /**
     * Registriert die GLFW-Callbacks für das angegebene Fenster-Handle.
     * Muss nach {@code glfwCreateWindow()} aufgerufen werden.
     *
     * @param handle GLFW-Fenster-Handle
     */
    public void registerCallbacks(final long handle) {
        this.windowHandle = handle;

        keyCallback = GLFWKeyCallback.create((win, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                eventQueue.add(InputEvent.keyDown(key));
            } else if (action == GLFW_RELEASE) {
                eventQueue.add(InputEvent.keyUp(key));
            }
            // GLFW_REPEAT wird ignoriert — gedrückt-gehaltener Zustand wird
            // in InputProcessor über das heldKeys-Set verfolgt.
        });

        mouseButtonCallback = GLFWMouseButtonCallback.create((win, button, action, mods) -> {
            final double[] xArr = new double[1];
            final double[] yArr = new double[1];
            glfwGetCursorPos(win, xArr, yArr);
            if (action == GLFW_PRESS) {
                eventQueue.add(InputEvent.mousePress(button, xArr[0], yArr[0]));
            } else if (action == GLFW_RELEASE) {
                eventQueue.add(InputEvent.mouseRelease(button, xArr[0], yArr[0]));
            }
        });

        cursorPosCallback = GLFWCursorPosCallback.create((win, x, y) ->
            eventQueue.add(InputEvent.mouseMove(x, y))
        );

        // Fix: Bei Fokusverlust FOCUS_LOST in die Queue schreiben, damit
        // der InputProcessor heldKeys/heldMouseButtons leert → keine stuck keys.
        windowFocusCallback = GLFWWindowFocusCallback.create((win, focused) -> {
            if (!focused) {
                eventQueue.add(InputEvent.focusLost());
            }
        });

        charCallback = GLFWCharCallback.create((win, codepoint) ->
            eventQueue.add(InputEvent.charTyped(codepoint))
        );

        glfwSetKeyCallback(handle, keyCallback);
        glfwSetMouseButtonCallback(handle, mouseButtonCallback);
        glfwSetCursorPosCallback(handle, cursorPosCallback);
        glfwSetWindowFocusCallback(handle, windowFocusCallback);
        glfwSetCharCallback(handle, charCallback);
    }

    /**
     * Gibt die rohe Event-Queue zurück.
     * Wird von {@link InputProcessor#processEvents(Queue)} pro Tick geleert.
     *
     * @return die Event-Queue
     */
    public Queue<InputEvent> getEventQueue() {
        return eventQueue;
    }

    /**
     * Detacht alle Callbacks von GLFW und gibt die LWJGL-Wrapper-Objekte frei.
     *
     * <p>Muss vor {@code glfwFreeCallbacks(window)} aufgerufen werden, da
     * {@code glfwFreeCallbacks} andernfalls dieselben Callbacks ein zweites Mal
     * freigibt (Double-Free).</p>
     */
    public void free() {
        // Erst von GLFW detachen, damit glfwFreeCallbacks() sie nicht nochmals freed
        glfwSetKeyCallback(windowHandle, null);
        glfwSetMouseButtonCallback(windowHandle, null);
        glfwSetCursorPosCallback(windowHandle, null);
        glfwSetWindowFocusCallback(windowHandle, null);
        glfwSetCharCallback(windowHandle, null);

        // Dann die LWJGL-Wrapper-Objekte freigeben
        if (keyCallback != null) {
            keyCallback.free();
            keyCallback = null;
        }
        if (mouseButtonCallback != null) {
            mouseButtonCallback.free();
            mouseButtonCallback = null;
        }
        if (cursorPosCallback != null) {
            cursorPosCallback.free();
            cursorPosCallback = null;
        }
        if (windowFocusCallback != null) {
            windowFocusCallback.free();
            windowFocusCallback = null;
        }
        if (charCallback != null) {
            charCallback.free();
            charCallback = null;
        }
    }
}
