package com.pxworlds.configuration.profile;

import com.pxworlds.configuration.Configuration;

import java.util.UUID;

public class ProfileConfiguration extends Configuration {

    /** The name of the profile. */
    private String profileName = "";
    /** The name of the user. */
    private String userName    = "";
    /** The unique identifier for the profile. */
    private UUID   uuid        = UUID.randomUUID();

    public ProfileConfiguration(String profileName, String userName, UUID uuid) {
        this.profileName = profileName;
        this.userName = userName;
        this.uuid = uuid;
    }

    public String getProfileName() {
        return profileName;
    }

    public ProfileConfiguration setProfileName(String profileName) {
        this.profileName = profileName;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public ProfileConfiguration setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public UUID getUuid() {
        return uuid;
    }

    public ProfileConfiguration setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }
}
