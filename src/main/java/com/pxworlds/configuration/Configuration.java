package com.pxworlds.configuration;

import com.google.gson.reflect.TypeToken;
import com.pxworlds.configuration.profile.ProfilesConfiguration;
import com.pxworlds.configuration.screen.ScreenConfiguration;

import java.lang.reflect.Type;

public class Configuration {

    public enum ConfigurationType {
        /** Konfigurationstyp für die Bildschirm-Einstellungen. */
        SCREEN_CONFIGURATION(new TypeToken<ScreenConfiguration>() {
        }.getType()),
        /** Konfigurationstyp für die Profile. */
        PROFILES_CONFIGURATION(new TypeToken<ProfilesConfiguration>() {
        }.getType());

        /** Typ-Token für Gson-Deserialisierung. */
        private Type type;

        ConfigurationType(Type newType) {
            this.type = newType;
        }

        public Type getType() {
            return type;
        }
    }

}
