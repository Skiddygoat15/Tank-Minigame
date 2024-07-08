package Tanks;

import processing.core.PApplet;

import java.awt.*;
import java.util.Objects;

/**
 * This class handle tank logics.
 */
public class TankSprite {
    private Color bodyColour;
    private int playerId;
    private int x, y;
    private MapCell location;
    private boolean isAnimating;
    private int targetY;
    private int Fuels = 250;
    private int parachutes = 3;
    private int Health = 100;
    private int initialFallY = 0;
    private int score = 0;
    Shell causingFallShell = null;



    public TankSprite(Color s, int playerId, int x, int y){
        this.bodyColour = s;
        this.playerId = playerId;
        this.x = x;
        this.y = y;
    }

    public void setAnimating(boolean animating) {
        isAnimating = animating;
    }

    public boolean isAnimating() {
        return isAnimating;
    }

    public void addScore(int score) {
        this.score += score;
    }

    public void minusScore(int score) {
        this.score -= score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return this.score;
    }

    public void setFuels(int fuels) {
        Fuels = fuels;
    }

    public int getFuels() {
        return Fuels;
    }

    public void setCausingFallShell(Shell shell) {
        this.causingFallShell = shell;
    }

    public Shell getCausingFallShell() {
        return this.causingFallShell;
    }

    public int getParachutes() {
        return parachutes;
    }


    public void setInitialFallY(int initialFallY) {
        this.initialFallY = initialFallY;
    }

    public int getInitialFallY() {
        return initialFallY;
    }

    public void resetInitialFallY() {
        initialFallY = 0;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TankSprite that = (TankSprite) obj;
        return Objects.equals(this.getPlayerId(), that.getPlayerId());  // Assume each tank has an unique id
    }

    public void setHealth(int health) {
        Health = health;
    }

    public int getHealth() {
        return Health;
    }
    public void healthKit() {
        if (Health + 20 <= 100){
            this.Health += 20;
        } else if (Health + 20 > 100) {
            this.Health = 100;
        }
    }

    public void parachuteUsed() {
        if (this.parachutes > 0)
            this.parachutes--;
    }

    public void addParachute() {
        this.parachutes++;
    }

    public void fuelConsumption() {
        this.Fuels--;
    }
    public void fuelKit() {
        this.Fuels = Fuels + 200;
    }

    public void setTargetY(int y){
        targetY = y;
    }

    public int getTargetY() {
        return targetY;
    }

    public void setLocation (MapCell loc){
        this.location = loc;
    }
    public MapCell getLocation () {
        return this.location;
    }

    public void move(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    public void directMove (int x, int y){
        this.x = x;
        this.y = y;
    }
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    public int getPlayerId() {
        return playerId;
    }

    public Color getBodyColour() {
        return bodyColour;
    }
    // Update animation for falling tanks
    /**
     * Update animation for falling tanks.
     * @param mapCells Used to reassign or clear tank location, so that falling tanks can be drawn by the program
     * @param tank Falling tank
     */
    public void update(MapCell[][] mapCells, TankSprite tank) {
        if (isAnimating) {
            if (Math.abs(y - targetY) <= 1) {
                mapCells[y][x].setTankSprite(null);
                directMove((int)x, targetY);
                mapCells[targetY][x].setTankSprite(tank);
                isAnimating = false;
                if (parachutes == 0){
                    int damage = targetY - initialFallY + 3;
                    int initialHealth = this.Health;
                    this.Health = Math.max(this.Health - damage, 0);
                    resetInitialFallY();
                    // If tank is falling because of its own shell, then no score add
                    if (this.causingFallShell != null && this.causingFallShell.getTank() != this) {
                        this.causingFallShell.getTank().addScore(initialHealth - this.Health);
                        this.setCausingFallShell(null);
                    }
                }
                parachuteUsed();
            } else {
                int newY = (int)(y + (parachutes > 0 ? 1 : 2));
                mapCells[y][x].setTankSprite(null);
                directMove((int)x, newY);
                mapCells[newY][x].setTankSprite(tank);
            }
        }
    }

    public void draw(PApplet app, int x, int y, int cellSize) {
        app.fill(bodyColour.getRGB());
        app.noStroke();
        float rectHeight = cellSize * 11 * 0.4f;
        float cornerRadius = 3;
        app.rect(x - (float) (cellSize * 20) /2, y + cellSize * 32 - rectHeight - 28, cellSize * 20, rectHeight, cornerRadius);


        float topRectWidth = cellSize * 22 * 2 / 3;
        float topRectX = x + (cellSize * 16 - topRectWidth) / 2 + 1.7f;
        app.rect(topRectX - (float) (cellSize * 20) /2, y + cellSize * 13 - 2 * rectHeight + 16 - 25, topRectWidth, rectHeight, cornerRadius);
    }


}
