package com.pxworlds.worlds;

/**
 * Created by tompi on 06.10.2017.
 */
public enum Worlds {

    W01_01("01_01", "test.json", "FD8C6D0659A423407FDC113290DD49BABC50AD30F54E41CC64F01107D4AF32FFD12C4EEB26C69E0F26F6E8CC2CED7A32AAB1CD629D3BADBC5DEB0CBBAF945045"),
    W01_02("01_02", "hallo.json", "checksum");

    private String worldName = "";
    private String worldFileName = "";
    private String worldChecksum = "";

    Worlds(String worldName, String worldFileName, String worldChecksum) {
        this.worldName = worldName;
        this.worldChecksum = worldChecksum;
        this.worldFileName = worldFileName;
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
