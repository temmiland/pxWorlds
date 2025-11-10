package com.pxworlds.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public enum Hash {

    /** MD5 hash algorithm. */
    MD5("MD5"),
    /** SHA1 hash algorithm. */
    SHA1("SHA1"),
    /** SHA256 hash algorithm. */
    SHA256("SHA-256"),
    /** SHA512 hash algorithm. */
    SHA512("SHA-512");

    /** The buffer size for hashing. */
    private static final int BUFFER_SIZE = 4096;

    /** The name of the hash algorithm. */
    private String name;

    Hash(String hashName) {
        this.name = hashName;
    }

    public String getName() {
        return name;
    }

    public byte[] checksum(File input) {
        try (InputStream in = new FileInputStream(input)) {
            final MessageDigest digest = MessageDigest.getInstance(getName());
            final byte[] block = new byte[BUFFER_SIZE];
            int length;
            while ((length = in.read(block)) > 0) {
                digest.update(block, 0, length);
            }
            return digest.digest();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
