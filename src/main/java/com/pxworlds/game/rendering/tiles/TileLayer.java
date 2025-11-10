package com.pxworlds.game.rendering.tiles;

public class TileLayer {

    /** The type of the tile layer. */
    private TileLayerType tileLayerType;
    /** The tile in this layer. */
    private Tile tile;

    public TileLayer(TileLayerType newTileLayerType, Tile newTile) {
        this.tileLayerType = newTileLayerType;
        this.tile = newTile;
    }

    public TileLayerType getTileLayerType() {
        return tileLayerType;
    }

    public TileLayer setTileLayerType(TileLayerType newTileLayerType) {
        this.tileLayerType = newTileLayerType;
        return this;
    }

    public Tile getTile() {
        return tile;
    }

    public TileLayer setTile(Tile newTile) {
        this.tile = newTile;
        return this;
    }
}
