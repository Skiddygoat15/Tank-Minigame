package Tanks;

import processing.core.PApplet;
import processing.core.PImage;

import java.awt.*;
/**
 * This class handle Map data logics.
 */
public class MapCell {
    private int x, y;
    private int terrainType;
    private boolean isSolid;
    private TankSprite tank;
    private PImage trees;
    private Color terrainColor;

    public MapCell(int x, int y, int terrainType, PImage trees, Color terrainColor) {
        this.x = x;
        this.y = y;
        this.terrainType = terrainType;
        this.trees = trees;
        this.terrainColor = terrainColor;
    }

    public void setTerrainType(int type) {
        this.terrainType = type;
    }

    public int getTerrainType() {
        return this.terrainType;
    }

    public void setSolid(boolean is){
        this.isSolid = is;
    }

    public boolean isSolid() {
        return isSolid;
    }

    public void setTankSprite(TankSprite t) {
        this.tank = t;
        if (t != null) {
            t.setLocation(this);
        }
    }

    public TankSprite getSprite() {
        return this.tank;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

        public void draw(PApplet app, int x, int y, int cellSize) {
            if (isSolid){
                app.fill(terrainColor.getRGB());
                app.noStroke();
                app.rect(x, y, cellSize, cellSize);
            }
        // Draw trees
        if (terrainType == 2) {  // terrainType == 2 means this cell contains tree
            if (trees != null) {
                app.image(trees, x - 16, y - 29, cellSize * 32, cellSize * 32);
            }
        }

    }

    public void drawTank(PApplet app, int x, int y, int cellSize) {
        if (tank != null) {
            tank.draw(app, x, y, cellSize);
        }
    }


}
