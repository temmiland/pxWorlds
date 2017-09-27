package com.pxworlds.game.screens;

import com.pxworlds.game.states.GameStateManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

public class GamePanel extends JPanel implements Runnable, KeyListener {

    public static int WIDTH;
    public static int HEIGHT;
    public static double SCALE;

    private Thread thread;
    private boolean running;
    private int FPS = 60;
    private long targetTime = 1000 / FPS;

    private long timeBefore;
    private int fpsCount;
    private int fps;

    private BufferedImage image;
    private Graphics2D g;

    private GameStateManager gsm;

    public GamePanel(int width, int height, double scale) {
        super();

        this.WIDTH = width;
        this.HEIGHT = height;
        this.SCALE = scale;

        setFocusable(true);
        requestFocus();

    }

    public void addNotify() {
        super.addNotify();
        if(thread == null) {
            thread = new Thread(this);
            addKeyListener(this);
            thread.start();
        }
    }

    private void init() {

        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        g = (Graphics2D) image.getGraphics();


        running = true;

        gsm = new GameStateManager();
        this.timeBefore = System.currentTimeMillis();
    }

    private void update() {
        gsm.update();
    }

    private void draw() {
        gsm.draw(g);
        g.drawString("FPS: " + fps, 50, 50);
        fpsCount++;
        long timeNow = System.currentTimeMillis();
        long diff = timeNow - timeBefore;
        if(diff >= 1000) {
            fps = fpsCount;
            fpsCount = 0;
            timeBefore = timeNow;
        }
    }

    private void drawToScreen() {
        Graphics2D g2 = (Graphics2D) getGraphics();
        g2.drawImage(image, 0,0, (int)(WIDTH * SCALE), (int)(HEIGHT * SCALE), null);
        g2.dispose();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        gsm.keyPressed(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        gsm.keyReleased(e.getKeyCode());
    }

    @Override
    public void run() {
        init();

        long start;
        long elapsed;
        long wait;

        while(running) {


            start = System.nanoTime();


            update();
            draw();
            drawToScreen();

            elapsed = System.nanoTime() - start;
            wait = Math.abs(targetTime - elapsed / 1000000);

            try {
                Thread.sleep(wait);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
