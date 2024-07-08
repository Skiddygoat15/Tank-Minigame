package Tanks;

import processing.core.PApplet;
/**
 * This class handle turret related logics.
 */
public class tankTurret {
    private float angle; // Turret rotate angle
    private float length; // Turret length
    private TankSprite tank; // Corresponding tank
    private int Power = 50;

    public TankSprite getTurretTank() {
        return this.tank;
    }

    public tankTurret(TankSprite tank, float length) {
        this.tank = tank;
        this.length = length;
        this.angle = 0; // initial angle
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public float getAngle() {
        return angle;
    }

    public int getPower() {
        return Power;
    }
    public void powerUp() {
        this.Power++;
    }
    public void powerDown() {
        this.Power--;
    }
    public void setPower(int power) {
        this.Power = power;
    }

    public Shell fireAndSetShell(float initialSpeed) {
        float turretEndX = tank.getX() - 1;
        float turretEndY = tank.getY() - 1;
//        System.out.println("Angle now: " + angle);
        return new Shell(turretEndX, turretEndY, angle, initialSpeed);
    }
    public void draw(PApplet app) {
        float turretEndX = tank.getX() - 1 + length * PApplet.sin(PApplet.radians(angle));
        float turretEndY = tank.getY() - 1 - length * PApplet.cos(PApplet.radians(angle));
        app.stroke(0);
        app.strokeWeight(3);
        app.line(tank.getX() - 1, tank.getY() - 1, turretEndX, turretEndY);
    }

}
