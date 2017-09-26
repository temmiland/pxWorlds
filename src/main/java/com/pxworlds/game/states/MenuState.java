package com.pxworlds.game.states;

import com.pxworlds.game.tilemap.Button;
import com.pxworlds.game.tilemap.ButtonType;
import com.pxworlds.game.tilemap.Image;

import java.awt.*;
import java.awt.event.KeyEvent;


public class MenuState extends GameState {

    private Image bg;
    private Image cloud3;
    private Image cloud2;
    private Image cloud1;
    private Image opacityFrame;
    private Image logo;

    private Button playButton = new Button(ButtonType.START);
    private Button quitButton = new Button(ButtonType.QUIT);

    private int currentChoice = 0;
    private Button[] buttons = {
            playButton,
            quitButton
    };

    private Font font;

    public MenuState(GameStateManager gsm) {
        this.gsm = gsm;

        try {
            font = new Font("Century Gothic", Font.PLAIN, 20);

            bg = new Image("/assets/textures/gui/title/background.png", 1);
            cloud3 = new Image("/assets/textures/gui/title/cloud3.png", 1);
            cloud3.setVector(-3, 0);
            cloud2 = new Image("/assets/textures/gui/title/cloud2.png", 1);
            cloud2.setVector(-2.5, 0);
            cloud1 = new Image("/assets/textures/gui/title/cloud1.png", 1);
            cloud1.setVector(-2, 0);
            opacityFrame = new Image("/assets/textures/gui/title/opacityFrame.png", 1);

            logo = new Image("/assets/textures/gui/title/logo.png", 1);
            logo.setPosition(120, 80);

            playButton.setSelectTexture(true);

            int buttonHeight = 0;
            for(Button i : buttons) {
                buttonHeight = buttonHeight + 65;
                i.setPosition(270, 250 + buttonHeight);
                i.setInnerPosition(270, 250 + buttonHeight);
            }


        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init() {

    }

    @Override
    public void update() {
        bg.update();
        cloud3.update();
        cloud2.update();
        cloud1.update();
        opacityFrame.update();
        logo.update();
        for(Button i : buttons) {
            i.update();
        }
    }

    @Override
    public void draw(Graphics2D g) {
        g.setFont(font);

        bg.draw(g);
        cloud3.draw(g);
        cloud2.draw(g);
        cloud1.draw(g);
        opacityFrame.draw(g);
        logo.draw(g);

        for(Button i : buttons) {
            i.draw(g);
            g.drawString(i.getButtonType().getDisplayName(), i.getInnerPositionX() + 102, i.getInnerPositionY() + 30);
        }
    }

    private void select(Button b) {
        if(b.getButtonType() == ButtonType.QUIT) {
            System.exit(0);
        }
    }

    @Override
    public void keyPressed(int k) {


        if(k == KeyEvent.VK_ENTER) {
            select(buttons[currentChoice]);
        }

        if(k == KeyEvent.VK_UP) {
            currentChoice--;

            if(currentChoice == -1) {
                currentChoice = buttons.length - 1;
            }
            for(Button b : buttons) b.setSelectTexture(false);
            buttons[currentChoice].setSelectTexture(true);
        }

        if(k == KeyEvent.VK_DOWN) {
            currentChoice++;
            if(currentChoice == buttons.length) {
                currentChoice = 0;
            }
            for(Button b : buttons) b.setSelectTexture(false);
            buttons[currentChoice].setSelectTexture(true);
        }

    }

    @Override
    public void keyReleased(int k) {

    }
}
