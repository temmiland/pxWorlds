package com.pxworlds.configuration;

import com.pxworlds.Bootstrap;

public class Configuration {

    public String serializeToJson() {
        return Bootstrap.getInstance().getGson().toJson(this, this.getClass());
    }

}
