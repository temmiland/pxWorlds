package com.pxworlds.worlds;


import com.pxworlds.game.rendering.tiles.TileLayer;

import java.util.ArrayList;
import java.util.Map;

public class World {

    /** The name of the world. */
    private final String worldName;
    /** The map of locations to tile layers. */
    private Map<Location, ArrayList<TileLayer>> locationTileLayersHashMap;

    public World(String worldName) {
        this.worldName = worldName;
        //this.locationTileLayersHashMap = loadWorldFromConfiguration();
    }

    public String getWorldName() {
        return worldName;
    }

    public Map<Location, ArrayList<TileLayer>> getLocationTileLayersHashMap() {
        return locationTileLayersHashMap;
    }

    //public Map<Location, ArrayList<TileLayer>> loadWorldFromConfiguration() {
    //
    //}

    public World setLocationTileLayersHashMap(Map<Location, ArrayList<TileLayer>> locationTileLayersHashMap) {
        this.locationTileLayersHashMap = locationTileLayersHashMap;
        return this;
    }
}
