package com.pxworlds.configuration;

import com.pxworlds.Bootstrap;
import com.pxworlds.Constants;
import com.pxworlds.configuration.profile.ProfilesConfiguration;
import com.pxworlds.configuration.screen.ScreenConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigurationStorage {

        private ScreenConfiguration screenConfiguration;
        private ProfilesConfiguration profilesConfiguration;

        public void init() {
            createScreenConfigurationIfNotExists();
            createProfilesConfigurationIfNotExists();
        }

        public void createConfigDirectoryIfNotExists() {
            File file = new File(Constants.CONFIG_DIRECTORY_PATH);
            if (!file.exists()) file.mkdirs();
        }

        public void createScreenConfigurationIfNotExists() {
            createConfigDirectoryIfNotExists();
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

        public void createProfilesConfigurationIfNotExists() {
            createConfigDirectoryIfNotExists();
            File file = new File(Constants.CONFIG_DIRECTORY_PATH, "profiles.json");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                    ProfilesConfiguration profilesConfiguration = new ProfilesConfiguration();
                    Bootstrap.getInstance().getJsonConfig().saveConfig(profilesConfiguration, Bootstrap.getInstance().getJsonConfig().generateConfigName("profiles"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            setProfilesConfiguration(Bootstrap.getInstance().getJsonConfig().<ProfilesConfiguration>readConfiguration(Bootstrap.getInstance().getJsonConfig().generateConfigName("profiles"), Configuration.ConfigurationType.PROFILES_CONFIGURATION.getType()));
        }

        public ScreenConfiguration getScreenConfiguration() {
            return screenConfiguration;
        }

        public ConfigurationStorage setScreenConfiguration(ScreenConfiguration screenConfiguration) {
            this.screenConfiguration = screenConfiguration;
            return this;
        }

        public ProfilesConfiguration getProfilesConfiguration() {
            return profilesConfiguration;
        }

        public ConfigurationStorage setProfilesConfiguration(ProfilesConfiguration profilesConfiguration) {
            this.profilesConfiguration = profilesConfiguration;
            return this;
        }

        public ConfigurationStorage saveAllConfigurations() {
            saveScreenConfiguration();
            saveProfilesConfiguration();
            return this;
        }

        public ConfigurationStorage saveScreenConfiguration() {
            createScreenConfigurationIfNotExists();
            Bootstrap.getInstance().getJsonConfig().saveConfig(getScreenConfiguration(), Bootstrap.getInstance().getJsonConfig().generateConfigName("screen_configuration"));
            return this;
        }

        public ConfigurationStorage saveProfilesConfiguration() {
            createProfilesConfigurationIfNotExists();
            Bootstrap.getInstance().getJsonConfig().saveConfig(getProfilesConfiguration(), Bootstrap.getInstance().getJsonConfig().generateConfigName("profiles"));
            return this;
        }
}