package com.pxworlds.configuration;

import com.google.gson.reflect.TypeToken;
import com.pxworlds.Bootstrap;

import java.lang.reflect.Type;

public class Configuration {

    public String serializeToJson() {
        return Bootstrap.getInstance().getGson().toJson(this, this.getClass());
    }

    public enum ConfigurationType {
        SCREEN_CONFIGURATION(new TypeToken<ScreenConfiguration>(){}.getType());

        private Type type;

        ConfigurationType(Type type) {
            this.type = type;
        }

        public Type getType() {
            return type;
        }
    }

}
