package com.pxworlds.configuration.world;

import com.pxworlds.Bootstrap;
import com.pxworlds.configuration.Configuration;
import com.pxworlds.worlds.Location;
import com.pxworlds.game.rendering.tiles.TileLayer;

import java.util.ArrayList;
import java.util.HashMap;

public class WorldConfiguration extends Configuration {

    private String worldName;
    private HashMap<Location, ArrayList<TileLayer>> locationTileLayerHashMap;

    public WorldConfiguration() {
    }

    public String getWorldName() {
        return worldName;
    }

    public WorldConfiguration setWorldName(String worldName) {
        this.worldName = worldName;
        return this;
    }

    public HashMap<Location, ArrayList<TileLayer>> getLocationTileLayerHashMap() {
        return locationTileLayerHashMap;
    }

    public WorldConfiguration setLocationTileLayerHashMap(HashMap<Location, ArrayList<TileLayer>> tileLayers) {
        this.locationTileLayerHashMap = tileLayers;
        return this;
    }

    @Override
    public String toString() {
        return "WorldConfiguration{" +
                "worldName=" + worldName +
                ", tileLayers=" + Bootstrap.getInstance().getGson().toJson(locationTileLayerHashMap) +
                '}';
    }

}