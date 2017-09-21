package com.pxworlds.configuration;

import com.pxworlds.Bootstrap;
import com.pxworlds.Constants;

import java.io.File;
import java.io.IOException;

public class ConfigurationStorage {

    private ScreenConfiguration screenConfiguration;

    public void init() {
        createScreenConfigurationIfNotExists();
    }

    public void createConfigDirectoryIfNotExisting() {
        File file = new File(Constants.CONFIG_DIRECTORY_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public void createScreenConfigurationIfNotExists() {
        createConfigDirectoryIfNotExisting();
        File file = new File(Constants.CONFIG_DIRECTORY_PATH, "screen_configuration.json");
        if (!file.exists()) {
            try {
                file.createNewFile();
                ScreenConfiguration screenConfiguration = new ScreenConfiguration();
                Bootstrap.getInstance().getJsonConfig().saveConfig(screenConfiguration, Bootstrap.getInstance().getJsonConfig().generateConfigName("screen_configuration"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        setScreenConfiguration(Bootstrap.getInstance().getJsonConfig().<ScreenConfiguration>readConfiguration(Bootstrap.getInstance().getJsonConfig().generateConfigName("screen_configuration"), Configuration.ConfigurationType.SCREEN_CONFIGURATION.getType()));
    }

    public ScreenConfiguration getScreenConfiguration() {
        return screenConfiguration;
    }

    public ConfigurationStorage setScreenConfiguration(ScreenConfiguration screenConfiguration) {
        this.screenConfiguration = screenConfiguration;
        return this;
    }

    public ConfigurationStorage saveAllConfigurtations() {
        saveScreenConfiguration();
        return this;
    }

    public ConfigurationStorage saveScreenConfiguration() {
        createScreenConfigurationIfNotExists();
        Bootstrap.getInstance().getJsonConfig().saveConfig(getScreenConfiguration(), Bootstrap.getInstance().getJsonConfig().generateConfigName("screen_configuration"));
        return this;
    }
}
