package com.pxworlds.configuration;

import com.google.gson.reflect.TypeToken;
import com.pxworlds.Bootstrap;
import com.pxworlds.configuration.profile.ProfilesConfiguration;
import com.pxworlds.configuration.screen.ScreenConfiguration;

import java.lang.reflect.Type;

public class Configuration {

    public String serializeToJson() {
        return Bootstrap.getInstance().getGson().toJson(this, this.getClass());
    }

    public enum ConfigurationType {
        /** Screen configuration type. */
        SCREEN_CONFIGURATION(new TypeToken<ScreenConfiguration>() {
        }.getType()),
        /** Profiles configuration type. */
        PROFILES_CONFIGURATION(new TypeToken<ProfilesConfiguration>() {
        }.getType());

        /** The type of configuration. */
        private Type type;

        ConfigurationType(Type newType) {
            this.type = newType;
        }

        public Type getType() {
            return type;
        }
    }

}
