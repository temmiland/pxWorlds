package land.temmi.pxworlds.gui.widgets;

import land.temmi.pxworlds.tiles.Tile;
import land.temmi.pxworlds.tiles.tile.ButtonTiles;
import land.temmi.rollercoaster.gui.GuiPrimitives;
import land.temmi.rollercoaster.gui.widgets.button.ButtonSkin;
import land.temmi.rollercoaster.gui.widgets.button.ButtonState;

/**
 * pxWorlds appearance of a button: a 9-slice drawn from registered {@link Tile}s
 * plus a centered, shadowed label.
 *
 * <p>Stateless and shared via {@link #INSTANCE}.</p>
 */
public final class PxButtonSkin implements ButtonSkin {

    /** Shared, stateless instance. */
    public static final PxButtonSkin INSTANCE = new PxButtonSkin();

    /** Corner / border size in GUI units. */
    private static final float CORNER = 16f;
    /** Scale of the label. */
    private static final float LABEL_SCALE = 2.5f;
    /** Label color (white). */
    private static final float WHITE = 1.0f;

    /** 9-slice tile sets per button state, indexed by {@link ButtonState#ordinal()}. */
    private static final Tile[][] SLICES = {
        // IDLE
        {
            ButtonTiles.TOP_LEFT,     ButtonTiles.TOP_CENTER,     ButtonTiles.TOP_RIGHT,
            ButtonTiles.CENTER_LEFT,  ButtonTiles.CENTER_CENTER,  ButtonTiles.CENTER_RIGHT,
            ButtonTiles.BOTTOM_LEFT,  ButtonTiles.BOTTOM_CENTER,  ButtonTiles.BOTTOM_RIGHT,
        },
        // HOVERED
        {
            ButtonTiles.HOVERED_TOP_LEFT,     ButtonTiles.HOVERED_TOP_CENTER,     ButtonTiles.HOVERED_TOP_RIGHT,
            ButtonTiles.HOVERED_CENTER_LEFT,  ButtonTiles.HOVERED_CENTER_CENTER,  ButtonTiles.HOVERED_CENTER_RIGHT,
            ButtonTiles.HOVERED_BOTTOM_LEFT,  ButtonTiles.HOVERED_BOTTOM_CENTER,  ButtonTiles.HOVERED_BOTTOM_RIGHT,
        },
        // CLICKED
        {
            ButtonTiles.CLICKED_TOP_LEFT,     ButtonTiles.CLICKED_TOP_CENTER,     ButtonTiles.CLICKED_TOP_RIGHT,
            ButtonTiles.CLICKED_CENTER_LEFT,  ButtonTiles.CLICKED_CENTER_CENTER,  ButtonTiles.CLICKED_CENTER_RIGHT,
            ButtonTiles.CLICKED_BOTTOM_LEFT,  ButtonTiles.CLICKED_BOTTOM_CENTER,  ButtonTiles.CLICKED_BOTTOM_RIGHT,
        },
    };

    /** Slice index: top-left corner. */
    private static final int TOP_LEFT      = 0;
    /** Slice index: top-center edge. */
    private static final int TOP_CENTER    = 1;
    /** Slice index: top-right corner. */
    private static final int TOP_RIGHT     = 2;
    /** Slice index: center-left edge. */
    private static final int CENTER_LEFT   = 3;
    /** Slice index: center fill. */
    private static final int CENTER_CENTER = 4;
    /** Slice index: center-right edge. */
    private static final int CENTER_RIGHT  = 5;
    /** Slice index: bottom-left corner. */
    private static final int BOTTOM_LEFT   = 6;
    /** Slice index: bottom-center edge. */
    private static final int BOTTOM_CENTER = 7;
    /** Slice index: bottom-right corner. */
    private static final int BOTTOM_RIGHT  = 8;

    @Override
    public void draw(final GuiPrimitives p, final String label, final float cx, final float cy,
                     final float hw, final float hh, final ButtonState state) {
        final Tile[] s = SLICES[state.ordinal()];

        tile(p, s[CENTER_CENTER], cx,              cy,              hw,     hh);
        tile(p, s[TOP_CENTER],    cx,              cy + hh - CORNER, hw,   CORNER);
        tile(p, s[BOTTOM_CENTER], cx,              cy - hh + CORNER, hw,   CORNER);
        tile(p, s[CENTER_LEFT],   cx - hw + CORNER, cy,            CORNER,   hh);
        tile(p, s[CENTER_RIGHT],  cx + hw - CORNER, cy,            CORNER,   hh);
        tile(p, s[TOP_LEFT],      cx - hw + CORNER, cy + hh - CORNER, CORNER, CORNER);
        tile(p, s[TOP_RIGHT],     cx + hw - CORNER, cy + hh - CORNER, CORNER, CORNER);
        tile(p, s[BOTTOM_LEFT],   cx - hw + CORNER, cy - hh + CORNER, CORNER, CORNER);
        tile(p, s[BOTTOM_RIGHT],  cx + hw - CORNER, cy - hh + CORNER, CORNER, CORNER);

        if (label != null && !label.isEmpty()) {
            p.drawText(label, cx, cy, LABEL_SCALE, WHITE, WHITE, WHITE, true);
        }
    }

    private static void tile(final GuiPrimitives p, final Tile t,
                              final float cx, final float cy, final float hw, final float hh) {
        p.drawTile(t.getTexture(), cx, cy, hw, hh);
    }
}
