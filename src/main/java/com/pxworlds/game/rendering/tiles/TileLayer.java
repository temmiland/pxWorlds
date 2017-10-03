package com.pxworlds.game.rendering.tiles;

public class TileLayer {

    private TileLayerType tileLayerType;
    private Tile tile;

    public TileLayer(TileLayerType tileLayerType, Tile tile) {
        this.tileLayerType = tileLayerType;
        this.tile = tile;
    }

    public TileLayerType getTileLayerType() {
        return tileLayerType;
    }

    public TileLayer setTileLayerType(TileLayerType tileLayerType) {
        this.tileLayerType = tileLayerType;
        return this;
    }

    public Tile getTile() {
        return tile;
    }

    public TileLayer setTile(Tile tiles) {
        this.tile = tiles;
        return this;
    }
}
