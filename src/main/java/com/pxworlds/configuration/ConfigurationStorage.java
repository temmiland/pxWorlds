package com.pxworlds.configuration;

import com.pxworlds.Bootstrap;
import com.pxworlds.Constants;
import jdk.internal.dynalink.support.BottomGuardingDynamicLinker;

import javax.script.ScriptEngine;
import java.io.File;
import java.io.IOException;

public class ConfigurationStorage {

    private ScreenConfiguration screenConfiguration;

    public void init() {
        if (!Bootstrap.getInstance().getJsonConfig().isConfigurationExisting(Bootstrap.getInstance().getJsonConfig().generateConfigName("screen_configuration"))) {
            File file = new File(Constants.CONFIG_DIRECTORY_PATH);
            try {
                file.mkdirs();
                file = new File(Constants.CONFIG_DIRECTORY_PATH, "screen_configuration.json");
                file.createNewFile();
                ScreenConfiguration screenConfiguration = new ScreenConfiguration().setFullscreen(true);
                Bootstrap.getInstance().getJsonConfig().saveConfig(screenConfiguration, Bootstrap.getInstance().getJsonConfig().generateConfigName("screen_configuration"));
                System.out.println("Set file?");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        setScreenConfiguration(Bootstrap.getInstance().getJsonConfig().<ScreenConfiguration>readConfiguration(Bootstrap.getInstance().getJsonConfig().generateConfigName("screen_configuration")));
    }

    public ScreenConfiguration getScreenConfiguration() {
        return screenConfiguration;
    }

    public ConfigurationStorage setScreenConfiguration(ScreenConfiguration screenConfiguration) {
        this.screenConfiguration = screenConfiguration;
        return this;
    }
}
