package com.pxworlds;


public class Main {

    public static void main(String[] args) {
        SharedLibraryLoader.load();
        new pxWorlds(1280, 720).run();
    }

}
