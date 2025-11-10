package com.pxworlds.configuration.profile;

import com.pxworlds.configuration.Configuration;

import java.util.ArrayList;
import java.util.List;

public class ProfilesConfiguration extends Configuration {

    /** The list of profile configurations. */
    private List<ProfileConfiguration> profileConfigurations = new ArrayList<>();

    public ProfilesConfiguration() {
    }

    public List<ProfileConfiguration> getProfileConfigurations() {
        return profileConfigurations;
    }

    public ProfilesConfiguration setProfileConfigurations(List<ProfileConfiguration> newProfileConfigurations) {
        this.profileConfigurations = newProfileConfigurations;
        return this;
    }

    public ProfilesConfiguration addProfileConfiguration(ProfileConfiguration profileConfiguration) {
        this.profileConfigurations.add(profileConfiguration);
        return this;
    }

    public ProfilesConfiguration removeProfileConfiguration(ProfileConfiguration profileConfiguration) {
        this.profileConfigurations.remove(profileConfiguration);
        return this;
    }
}
