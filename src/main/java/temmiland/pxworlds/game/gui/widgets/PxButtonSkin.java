package temmiland.pxworlds.game.gui.widgets;

import temmiland.rollercoaster.gui.widgets.button.ButtonSkin;
import temmiland.rollercoaster.gui.widgets.button.ButtonState;
import temmiland.rollercoaster.gui.GuiPrimitives;

/**
 * pxWorlds-Erscheinungsbild eines Buttons: 9-Slice aus dem GUI-Tilesheet plus zentrierte,
 * schattierte Beschriftung. Implementiert den Engine-Vertrag {@link ButtonSkin}.
 *
 * <p>Zustandslos und über {@link #INSTANCE} geteilt; komponiert ausschließlich aus den
 * {@link GuiPrimitives}.</p>
 */
public final class PxButtonSkin implements ButtonSkin {

    /** Geteilte, zustandslose Instanz. */
    public static final PxButtonSkin INSTANCE = new PxButtonSkin();

    /** Spalten-Versatz pro Button-Zustand im Tilesheet. */
    private static final int STATE_STRIDE = 3;
    /** Eckengröße in GUI-Einheiten. */
    private static final float CORNER = 16f;
    /** Randgröße in GUI-Einheiten. */
    private static final float BORDER = 16f;
    /** Skalierung der Beschriftung. */
    private static final float LABEL_SCALE = 2.5f;
    /** Beschriftungsfarbe (weiß). */
    private static final float WHITE = 1.0f;

    /**
     * 9-Slice-Kachelbelegung im IDLE-Zustand. {@code baseCol} ist die Spalte bei IDLE,
     * je Zustand kommt {@code ordinal * STATE_STRIDE} hinzu; {@code row} ist fest.
     */
    private enum Tile {
        /** Mittelfüllung. */
        FILL(1, 1),
        /** Oberer Rand. */
        TOP(1, 0),
        /** Unterer Rand. */
        BOTTOM(1, 2),
        /** Linker Rand. */
        LEFT(0, 1),
        /** Rechter Rand. */
        RIGHT(2, 1),
        /** Ecke oben-links. */
        CORNER_TOP_LEFT(0, 0),
        /** Ecke oben-rechts. */
        CORNER_TOP_RIGHT(2, 0),
        /** Ecke unten-links. */
        CORNER_BOTTOM_LEFT(0, 2),
        /** Ecke unten-rechts. */
        CORNER_BOTTOM_RIGHT(2, 2);

        /** Basisspalte im IDLE-Zustand. */
        private final int baseCol;
        /** Feste Zeile im Tilesheet. */
        private final int row;

        Tile(final int newBaseCol, final int newRow) {
            this.baseCol = newBaseCol;
            this.row = newRow;
        }

        int col(final ButtonState state) {
            return baseCol + state.ordinal() * STATE_STRIDE;
        }
    }

    @Override
    public void draw(final GuiPrimitives p, final String label, final float cx, final float cy,
                     final float hw, final float hh, final ButtonState state) {
        tile(p, Tile.FILL, state, cx, cy, hw, hh);

        tile(p, Tile.TOP, state, cx, cy + hh - BORDER, hw, BORDER);
        tile(p, Tile.BOTTOM, state, cx, cy - hh + BORDER, hw, BORDER);
        tile(p, Tile.LEFT, state, cx - hw + BORDER, cy, BORDER, hh);
        tile(p, Tile.RIGHT, state, cx + hw - BORDER, cy, BORDER, hh);

        tile(p, Tile.CORNER_TOP_LEFT, state, cx - hw + CORNER, cy + hh - CORNER, CORNER, CORNER);
        tile(p, Tile.CORNER_TOP_RIGHT, state, cx + hw - CORNER, cy + hh - CORNER, CORNER, CORNER);
        tile(p, Tile.CORNER_BOTTOM_LEFT, state, cx - hw + CORNER, cy - hh + CORNER, CORNER, CORNER);
        tile(p, Tile.CORNER_BOTTOM_RIGHT, state, cx + hw - CORNER, cy - hh + CORNER, CORNER, CORNER);

        if (label != null && !label.isEmpty()) {
            p.drawText(label, cx, cy, LABEL_SCALE, WHITE, WHITE, WHITE, true);
        }
    }

    private static void tile(final GuiPrimitives p, final Tile tile, final ButtonState state,
                             final float cx, final float cy, final float hw, final float hh) {
        p.drawTile(tile.col(state), tile.row, cx, cy, hw, hh);
    }
}
