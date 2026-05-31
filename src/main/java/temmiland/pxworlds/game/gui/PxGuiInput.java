package temmiland.pxworlds.game.gui;

import temmiland.pxworlds.game.io.InputAction;
import temmiland.rollercoaster.gui.GuiInput;
import temmiland.rollercoaster.platform.Window;
import org.joml.Vector2f;
import temmiland.rollercoaster.platform.io.InputProcessor;

import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

/**
 * pxWorlds-Implementierung von {@link GuiInput}. Adaptiert den {@link InputProcessor}
 * des Fensters auf das render-/koordinatenunabhängige GUI-Eingabe-Interface.
 *
 * <p>Rechnet die Mausposition aus Fenster-Pixeln in den virtuellen GUI-Raum um
 * (Skalierung {@code virtuell / Fenster}), passend zur zentrierten GUI-Kamera des
 * zugehörigen {@link PxGuiRenderer}.</p>
 */
public final class PxGuiInput implements GuiInput {

    /** Das Fenster (liefert InputProcessor, Mausposition und aktuelle Größe). */
    private final Window window;
    /** Input */
    private final InputProcessor inputProcessor;
    /** Der zugehörige Renderer (liefert die virtuellen GUI-Maße). */
    private final PxGuiRenderer renderer;

    /**
     * @param newWindow   das Fenster
     * @param newRenderer der zugehörige Renderer (für die virtuelle GUI-Größe)
     */
    public PxGuiInput(final Window newWindow, final PxGuiRenderer newRenderer) {
        this.window = newWindow;
        this.renderer = newRenderer;
        this.inputProcessor = window.getInputProcessor();
    }

    @Override
    public Vector2f mousePosition() {
        final Vector2f mouse = window.getMousePosition();
        final float scaleX = (float) renderer.virtualWidth() / window.getWidth();
        final float scaleY = (float) renderer.virtualHeight() / window.getHeight();
        return new Vector2f(mouse.x * scaleX, mouse.y * scaleY);
    }

    @Override
    public boolean mousePressed() {
        return inputProcessor.wasMouseButtonPressed(GLFW_MOUSE_BUTTON_LEFT);
    }

    @Override
    public boolean mouseDown() {
        return inputProcessor.isMouseButtonDown(GLFW_MOUSE_BUTTON_LEFT);
    }

    @Override
    public List<Integer> typedCodepoints() {
        return inputProcessor.getTypedCodepoints();
    }

    @Override
    public boolean backspacePressed() {
        return inputProcessor.wasPressed(InputAction.BACKSPACE.getDefaultKey());
    }

    @Override
    public boolean confirmPressed() {
        return inputProcessor.wasPressed(InputAction.CONFIRM.getDefaultKey());
    }
}
