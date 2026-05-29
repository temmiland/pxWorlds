package com.pxworlds;

public class Main {

    public static void main(String[] args) {
        // LWJGL 3 extrahiert und lädt Natives automatisch aus dem Classpath
        new Bootstrap().onEnable();
    }

}
