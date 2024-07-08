package Tanks;

import processing.core.PApplet;
/**
 * This class handle shell related logics.
 */
public class Shell {
    private double x, y; // Turret's current position
    private double vx, vy; // Acceleration of the shell in the x and y directions
    TankSprite tank;
    private double windEffect = 0; // Initial wind

    public Shell(float x, float y, float angle, float initialSpeed) {
        this.x = x;
        this.y = y;
        // Calculate the initial velocity component
        if (initialSpeed > 5){
            this.vx = initialSpeed * Math.sin(Math.toRadians(angle)) * 3 * 0.16;
            this.vy = -initialSpeed * Math.cos(Math.toRadians(angle)) * 4 * 0.16;
        } else if (initialSpeed <= 5 ) {
            this.vx = 5 * Math.sin(Math.toRadians(angle)) * 3 * 0.16;
            this.vy = -5 * Math.cos(Math.toRadians(angle)) * 4 * 0.16;
        }

    }

    public void setTank(TankSprite tank) {
        this.tank = tank;
    }

    public void applyWind(double wind) {
        this.windEffect = wind;
    }
    public double getWindEffect() {
        return windEffect;
    }
    public TankSprite getTank() {
        return tank;
    }

    public void update(double deltaTime) {

        // Update Location
        x += (vx + windEffect * 0.3) * deltaTime ;
        // Adding the effects of gravity
        vy += 3.6 * deltaTime; // Gravity acceleration
        y += vy * deltaTime;
//        System.out.println("x now is: " + x);
//        System.out.println("y now is: " + y);
    }

    public void draw(PApplet app) {
        app.fill(tank.getBodyColour().getRGB());
        update(0.3);
        app.ellipse((float) x, (float) y, 9, 9);
    }

    // Getters for x, y for collision detection
    public double getX() { return x; }
    public double getY() { return y; }
}
