package com.pxworlds.configuration;

import com.pxworlds.Constants;
import com.pxworlds.configuration.profile.ProfilesConfiguration;
import com.pxworlds.configuration.screen.ScreenConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigurationStorage {

        /** The JSON config used for reading/writing configurations. */
        private final JsonConfig jsonConfig;
        /** The screen configuration. */
        private ScreenConfiguration screenConfiguration;
        /** The profiles configuration. */
        private ProfilesConfiguration profilesConfiguration;

        public ConfigurationStorage(JsonConfig jsonConfig) {
            this.jsonConfig = jsonConfig;
        }

        public void init() {
            createScreenConfigurationIfNotExists();
            createProfilesConfigurationIfNotExists();
        }

        public void createConfigDirectoryIfNotExists() {
            final File file = new File(Constants.CONFIG_DIRECTORY_PATH);
            if (!file.exists()) {
                file.mkdirs();
            }
        }

        public void createScreenConfigurationIfNotExists() {
            createConfigDirectoryIfNotExists();
            final File file = new File(Constants.CONFIG_DIRECTORY_PATH, "screen_configuration.json");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                    final ScreenConfiguration newScreenConfiguration = new ScreenConfiguration();
                    jsonConfig.saveConfig(newScreenConfiguration, jsonConfig.generateConfigName("screen_configuration"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            setScreenConfiguration(jsonConfig.<ScreenConfiguration>readConfiguration(jsonConfig.generateConfigName("screen_configuration"), Configuration.ConfigurationType.SCREEN_CONFIGURATION.getType()));
        }

        public void createProfilesConfigurationIfNotExists() {
            createConfigDirectoryIfNotExists();
            final File file = new File(Constants.CONFIG_DIRECTORY_PATH, "profiles.json");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                    final ProfilesConfiguration newProfilesConfiguration = new ProfilesConfiguration();
                    jsonConfig.saveConfig(newProfilesConfiguration, jsonConfig.generateConfigName("profiles"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            setProfilesConfiguration(jsonConfig.<ProfilesConfiguration>readConfiguration(jsonConfig.generateConfigName("profiles"), Configuration.ConfigurationType.PROFILES_CONFIGURATION.getType()));
        }

        public ScreenConfiguration getScreenConfiguration() {
            return screenConfiguration;
        }

        public ConfigurationStorage setScreenConfiguration(ScreenConfiguration newScreenConfiguration) {
            this.screenConfiguration = newScreenConfiguration;
            return this;
        }

        public ProfilesConfiguration getProfilesConfiguration() {
            return profilesConfiguration;
        }

        public ConfigurationStorage setProfilesConfiguration(ProfilesConfiguration newProfilesConfiguration) {
            this.profilesConfiguration = newProfilesConfiguration;
            return this;
        }

        public ConfigurationStorage saveAllConfigurations() {
            saveScreenConfiguration();
            saveProfilesConfiguration();
            return this;
        }

        public ConfigurationStorage saveScreenConfiguration() {
            createConfigDirectoryIfNotExists();
            jsonConfig.saveConfig(getScreenConfiguration(), jsonConfig.generateConfigName("screen_configuration"));
            return this;
        }

        public ConfigurationStorage saveProfilesConfiguration() {
            createConfigDirectoryIfNotExists();
            jsonConfig.saveConfig(getProfilesConfiguration(), jsonConfig.generateConfigName("profiles"));
            return this;
        }
}
