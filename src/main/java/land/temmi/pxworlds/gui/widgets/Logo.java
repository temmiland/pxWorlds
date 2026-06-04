package land.temmi.pxworlds.gui.widgets;

import land.temmi.pxworlds.tiles.Tile;
import land.temmi.pxworlds.tiles.tile.LogoTiles;

import land.temmi.rollercoaster.gui.GuiContext;

/**
 * Game-specific pxWorlds logo composed from registered {@link Tile}s.
 *
 * <p>A pure layout helper: draws the 11-column × 3-row logo via
 * {@link GuiContext#sprite} and holds no state of its own.</p>
 */
public final class Logo {

    /** Vertical offset (in half-heights) between the three logo rows. */
    private static final int ROW_OFFSET = 2;

    /** Index of the horizontal-offset-factor in a column entry. */
    private static final int COL_FACTOR = 0;
    /** Index of the top tile in a column entry. */
    private static final int COL_TOP    = 1;
    /** Index of the center tile in a column entry. */
    private static final int COL_MID    = 2;
    /** Index of the bottom tile in a column entry. */
    private static final int COL_BOT    = 3;

    /**
     * Per column: {horizontal-offset-factor, top-tile, center-tile, bottom-tile}.
     * The x position is {@code cx - hw * factor}.
     */
    private static final Object[][] COLUMNS = {
        { 10, LogoTiles.COL_0_TOP,  LogoTiles.COL_0_CENTER,  LogoTiles.COL_0_BOTTOM  },
        {  8, LogoTiles.COL_1_TOP,  LogoTiles.COL_1_CENTER,  LogoTiles.COL_1_BOTTOM  },
        {  6, LogoTiles.COL_2_TOP,  LogoTiles.COL_2_CENTER,  LogoTiles.COL_2_BOTTOM  },
        {  4, LogoTiles.COL_3_TOP,  LogoTiles.COL_3_CENTER,  LogoTiles.COL_3_BOTTOM  },
        {  2, LogoTiles.COL_4_TOP,  LogoTiles.COL_4_CENTER,  LogoTiles.COL_4_BOTTOM  },
        {  0, LogoTiles.COL_5_TOP,  LogoTiles.COL_5_CENTER,  LogoTiles.COL_5_BOTTOM  },
        { -2, LogoTiles.COL_6_TOP,  LogoTiles.COL_6_CENTER,  LogoTiles.COL_6_BOTTOM  },
        { -4, LogoTiles.COL_7_TOP,  LogoTiles.COL_7_CENTER,  LogoTiles.COL_7_BOTTOM  },
        { -6, LogoTiles.COL_8_TOP,  LogoTiles.COL_8_CENTER,  LogoTiles.COL_8_BOTTOM  },
        { -8, LogoTiles.COL_9_TOP,  LogoTiles.COL_9_CENTER,  LogoTiles.COL_9_BOTTOM  },
        {-10, LogoTiles.COL_10_TOP, LogoTiles.COL_10_CENTER, LogoTiles.COL_10_BOTTOM },
    };

    private Logo() { }

    /**
     * Draws the logo centered on {@code (cx, cy)}.
     *
     * @param g  the GUI context
     * @param cx center X (GUI coordinates)
     * @param cy center Y (GUI coordinates)
     * @param hw half width of a tile
     * @param hh half height of a tile
     */
    public static void draw(final GuiContext g, final float cx, final float cy,
                            final float hw, final float hh) {
        for (final Object[] col : COLUMNS) {
            final float x   = cx - hw * (int) col[COL_FACTOR];
            final Tile  top = (Tile) col[COL_TOP];
            final Tile  mid = (Tile) col[COL_MID];
            final Tile  bot = (Tile) col[COL_BOT];
            g.sprite(top.getTexture(), x, cy + hh * ROW_OFFSET, hw, hh);
            g.sprite(mid.getTexture(), x, cy,                    hw, hh);
            g.sprite(bot.getTexture(), x, cy - hh * ROW_OFFSET,  hw, hh);
        }
    }
}
