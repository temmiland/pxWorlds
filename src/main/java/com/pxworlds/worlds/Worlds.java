package com.pxworlds.worlds;

/**
 * Created by temmiland on 06.10.2017.
 */
public enum Worlds {

    /** World 01_01. */
    W01_01("01_01", "test.json", "FD8C6D0659A423407FDC113290DD49BABC50AD30F54E41CC64F01107D4AF32FFD12C4EEB26C69E0F26F6E8CC2CED7A32AAB1CD629D3BADBC5DEB0CBBAF945045"),
    /** World 01_02. */
    W01_02("01_02", "hallo.json", "checksum");

    /** The name of the world. */
    private String worldName = "";
    /** The filename of the world. */
    private String worldFileName = "";
    /** The checksum of the world. */
    private String worldChecksum = "";

    Worlds(String newWorldName, String newWorldFileName, String newWorldChecksum) {
        this.worldName = newWorldName;
        this.worldChecksum = newWorldChecksum;
        this.worldFileName = newWorldFileName;
    }

    public String getWorldName() {
        return worldName;
    }

    public String getWorldChecksum() {
        return worldChecksum;
    }

    public String getWorldFileName() {
        return worldFileName;
    }
}
