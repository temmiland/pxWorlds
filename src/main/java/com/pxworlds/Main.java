package com.pxworlds;

import java.io.File;

import com.pxworlds.util.SharedLibraryLoader;

public class Main {

    public static void main(String[] args) {
        System.setProperty("org.lwjgl.librarypath", new File("natives").getAbsolutePath());
        SharedLibraryLoader.load();
        new Bootstrap().onEnable();
    }

}
