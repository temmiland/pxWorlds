package land.temmi.pxworlds.gui.widgets;

import land.temmi.rollercoaster.gui.GuiPrimitives;
import land.temmi.rollercoaster.gui.widgets.textfield.TextFieldSkin;

/**
 * pxWorlds appearance of a text input field: a border, a dark background,
 * centered shadowed text and a blinking bar cursor. Implements the engine
 * contract {@link TextFieldSkin}.
 *
 * <p>Stateless and shared via {@link #INSTANCE}; composed solely from the
 * {@link GuiPrimitives}.</p>
 */
public enum PxTextFieldSkin implements TextFieldSkin {

    /** Shared, stateless instance. */
    INSTANCE;

    /** Font scale. */
    private static final float FONT_SCALE = 2.5f;
    /** Background gray value. */
    private static final float BG = 0.15f;
    /** Border gray value. */
    private static final float BORDER_COLOR = 0.9f;
    /** Border thickness in GUI units. */
    private static final float BORDER = 2f;
    /** Text color (white). */
    private static final float WHITE = 1.0f;

    @Override
    public void draw(final GuiPrimitives p, final String text, final float cx, final float cy,
                     final float hw, final float hh, final boolean cursorVisible) {
        p.drawQuad(cx, cy, hw + BORDER, hh + BORDER, BORDER_COLOR, BORDER_COLOR, BORDER_COLOR);
        p.drawQuad(cx, cy, hw, hh, BG, BG, BG);

        final String cursor;
        if (cursorVisible) {
            cursor = "|";
        } else {
            cursor = " ";
        }
        p.drawText(text + cursor, cx, cy, FONT_SCALE, WHITE, WHITE, WHITE, true);
    }
}
