package com.pxworlds.configuration.profile;

import com.pxworlds.configuration.Configuration;

import java.util.UUID;

public class ProfileConfiguration extends Configuration {

    /** Name des Profils. */
    private String profileName = "";
    /** Benutzername. */
    private String userName    = "";
    /** Eindeutiger Bezeichner des Profils. */
    private UUID   uuid        = UUID.randomUUID();

    public ProfileConfiguration(String newProfileName, String newUserName, UUID newUuid) {
        this.profileName = newProfileName;
        this.userName = newUserName;
        this.uuid = newUuid;
    }

    public String getProfileName() {
        return profileName;
    }

    public ProfileConfiguration setProfileName(String newProfileName) {
        this.profileName = newProfileName;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public ProfileConfiguration setUserName(String newUserName) {
        this.userName = newUserName;
        return this;
    }

    public UUID getUuid() {
        return uuid;
    }

    public ProfileConfiguration setUuid(UUID newUuid) {
        this.uuid = newUuid;
        return this;
    }
}
