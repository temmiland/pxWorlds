package land.temmi.pxworlds.gui;

import land.temmi.pxworlds.io.InputAction;
import land.temmi.rollercoaster.gui.GuiInput;
import land.temmi.rollercoaster.platform.Window;
import land.temmi.rollercoaster.platform.input.InputProcessor;
import land.temmi.rollercoaster.platform.input.Key;
import org.joml.Vector2f;

import java.util.List;

/**
 * pxWorlds implementation of {@link GuiInput}. Adapts the window's
 * {@link InputProcessor} to the render-/coordinate-independent GUI input
 * interface.
 *
 * <p>Converts the mouse position from window pixels into the virtual GUI space
 * (scale {@code virtual / window}), matching the centered GUI camera of the
 * associated {@link PxGuiRenderer}.</p>
 */
public final class PxGuiInput implements GuiInput {

    /** The window (provides InputProcessor, mouse position and current size). */
    private final Window window;
    /** Input processor of the window. */
    private final InputProcessor inputProcessor;
    /** The associated renderer (provides the virtual GUI dimensions). */
    private final PxGuiRenderer renderer;

    /**
     * @param window   the window
     * @param renderer the associated renderer (for the virtual GUI size)
     */
    public PxGuiInput(final Window window, final PxGuiRenderer renderer) {
        this.window = window;
        this.renderer = renderer;
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
        return inputProcessor.wasPressed(Key.MOUSE_LEFT);
    }

    @Override
    public boolean mouseDown() {
        return inputProcessor.isHeld(Key.MOUSE_LEFT);
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
