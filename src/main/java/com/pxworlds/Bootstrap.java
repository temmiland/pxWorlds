package com.pxworlds;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pxworlds.configuration.ConfigurationStorage;
import com.pxworlds.configuration.JsonConfig;
import com.pxworlds.game.PxWorlds;
import com.pxworlds.worlds.WorldManager;

import static com.pxworlds.Constants.TITLE;

public class Bootstrap {

    /** Die Gson-Instanz für JSON-Verarbeitung. */
    private Gson                 gson;
    /** Die JSON-Konfiguration. */
    private JsonConfig           jsonConfig;
    /** Der Konfigurationsspeicher. */
    private ConfigurationStorage configurationStorage;
    /** Der World-Manager. */
    private WorldManager         worldManager;
    /** Der API-Schlüssel. */
    private String               apiKey;

    /**
     * Einstiegspunkt nach {@code main()} — initialisiert alle Subsysteme und startet das Spiel.
     */
    public void onEnable() {
        this.apiKey = "abc";

        initGson();
        initJsonConfig();
        initConfigurationStorage();
        initWorldManager();

        getConfigurationStorage().init();
        getWorldManager().init();

        final int width = getConfigurationStorage().getScreenConfiguration().getLastWidth();
        final int height = getConfigurationStorage().getScreenConfiguration().getLastHeight();
        final int frameRate = getConfigurationStorage().getScreenConfiguration().getFrameRate();
        final boolean fullscreen = getConfigurationStorage().getScreenConfiguration().isFullscreen();

        new PxWorlds(TITLE, width, height, fullscreen, frameRate, configurationStorage).run();

    }

    public void initGson() {
        gson = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create();
    }

    public void initJsonConfig() {
        jsonConfig = new JsonConfig(gson);
    }

    public void initConfigurationStorage() {
        configurationStorage = new ConfigurationStorage(jsonConfig);
    }

    public void initWorldManager() {
        worldManager = new WorldManager();
    }

    public Gson getGson() {
        return gson;
    }

    public JsonConfig getJsonConfig() {
        return jsonConfig;
    }

    public ConfigurationStorage getConfigurationStorage() {
        return configurationStorage;
    }

    public WorldManager getWorldManager() {
        return worldManager;
    }

    public String getApiKey() {
        return apiKey;
    }
}
