package com.pxworlds;

import com.pxworlds.util.SharedLibraryLoader;

public class Main {

    public static void main(String[] args) {
        SharedLibraryLoader.load();
        new Bootstrap().onEnable();
    }

}
