package com.pxworlds.game.tilemap;


public class Button extends Image {

    private ButtonType buttonType;
    private int innerPositionX;
    private int innerPositionY;

    public Button(ButtonType buttonType) {
        super("/assets/textures/gui/emptyButton.png", 1);
        this.buttonType = buttonType;
    }

    public void setInnerPosition(int posX, int posY) {
        this.innerPositionX = posX;
        this.innerPositionY = posY;
    }

    public int getInnerPositionX() {
        return this.innerPositionX;
    }

    public int getInnerPositionY() {
        return this.innerPositionY;
    }

    public ButtonType getButtonType() {
        return this.buttonType;
    }

    public void setSelectTexture(boolean selected) {
        if (selected) {
            setImage("/assets/textures/gui/emptyButtonSelected.png", 1);
        } else {
            setImage("/assets/textures/gui/emptyButton.png", 1);
        }
    }

}