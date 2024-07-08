package Tanks;

import Tanks.App;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Timeout;
import processing.core.PApplet;
import processing.core.PImage;
import processing.event.KeyEvent;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.util.*;

import javax.lang.model.type.NullType;

public class AppTest {
    private App app;
    private App app1;
    private TankSprite testTank;
    private tankTurret testTurret;
    private MapCell[][] initialMapCells;
    private Map<TankSprite, Boolean> initialLargeShells;

//    @BeforeEach
//    public void setup() {
//        app = new App();
//        PApplet.runSketch(new String[] { "App" }, app);
//        app.delay(1000); // 确保初始化完成
//
//        // 使用现有的坦克实例
//        testTank = app.getTestTank();
//        testTurret = app.getTestTurret();
//        assertNotNull(testTank, "测试坦克实例应该存在");
//        initialLargeShells = new HashMap<>(app.getLargeShellActivated());
//        initialMapCells = app.mapCells;
//    }


//    @AfterEach
//    public void tearDown() {
//        app.dispose();
//        app = null;
//    }
    // To avoid out of heap memory, I wrote several functions testing inside one test case instead of separate them in different test cases
    // For each test case will open an individual app window, if many app windows are running at one time, it'll surely have memory issue

    @Test
    public void testTankMovement_TurretPower_TurretAngleTurn() throws InterruptedException {
        app = new App();
        PApplet.runSketch(new String[] { "App" }, app);
        app.delay(1000); // 确保初始化完成

        // Get a tank instance from app class
        testTank = app.getTestTank().get(0);
        testTurret = app.getTestTurret().get(0);

        int initialX = testTank.getX();

        // Test left movement
        app.keyCode = 37;

        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 37));

        int expectedX = initialX - 2;
        assertEquals(expectedX, testTank.getX(), "坦克左移后，X 坐标应该减少 2");

        // Test right movement
        int initialX1 = testTank.getX();
        app.keyCode = 39;

        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 39));

        int expectedX1 = initialX1 + 2;
        assertEquals(expectedX1, testTank.getX(), "坦克右移后，X 坐标应该增加 2");

        // Test conflict movement right, when a tank meets another tank, it should jump over to the x coordinate next to that conflict tank, and this tank itself will not be deleted
        testTank.setScore(60);
        app.keyCode = 70;
        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 70));
        app.keyCode = 70;
        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 70));
        for (int i = 0; i < 600; i++) {
            app.keyCode = 39;
            app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 39));
            Thread.sleep(10);
        }
        boolean tankStillExists = testTank != null;
        assertTrue(tankStillExists, "If conflict happens, tank should still exists");

        // Test conflict movement left, when a tank meets another tank, it should jump over to the x coordinate next to that conflict tank, and this tank itself will not be deleted
        app.currentTurn = 3;
        TankSprite testTank1 = app.getTestTank().get(3);
        testTank1.setScore(60);
        app.keyCode = 70;
        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 70));
        app.keyCode = 70;
        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 70));
        for (int i = 0; i < 600; i++) {
            app.keyCode = 37;
            app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 39));
            Thread.sleep(10);
        }
        boolean tankStillExists1 = testTank != null;
        assertTrue(tankStillExists1, "If conflict happens, tank should still exists");
        app.currentTurn = 0;

        // Test power up
        int initialPower = testTurret.getPower();
        app.keyCode = 87;
        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 87));
        int expectedPower = initialPower + 1;
        assertEquals(expectedPower, testTurret.getPower(), "Power should add 1");
        // Test power down
        int initialPower1 = testTurret.getPower();
        app.keyCode = 83;
        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 83));
        int expectedPower1 = initialPower1 - 1;
        assertEquals(expectedPower1, testTurret.getPower(), "Power should minus 1");
        // Test turret turn left
        float initialAngle = testTurret.getAngle();
        app.keyCode = 38;
        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 38));
        float expectedAngle = initialAngle - 3;
        assertEquals(expectedAngle, testTurret.getAngle(), "Angle should minus 3");
        // Test turret turn right
        float initialAngle1 = testTurret.getAngle();
        app.keyCode = 40;
        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 40));
        float expectedAngle1 = initialAngle1 + 3;
        assertEquals(expectedAngle1, testTurret.getAngle(), "Angle should add 3");


    }

//    @Test
//    public void testTankMovementRight() {
//        app = new App();
//        PApplet.runSketch(new String[] { "App" }, app);
//        app.delay(1000); // 确保初始化完成
//
//        // 使用现有的坦克实例
//        testTank = app.getTestTank();
//        testTurret = app.getTestTurret();
//
//
//    }

//    @Test
//    public void testTankPowerUp() {
//        app = new App();
//        PApplet.runSketch(new String[] { "App" }, app);
//        app.delay(1000); // 确保初始化完成
//
//        // 使用现有的坦克实例
//        testTank = app.getTestTank();
//        testTurret = app.getTestTurret();
//
//        int initialPower = testTurret.getPower();
//        app.keyCode = 87;
//        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 87));
//        int expectedPower = initialPower + 1;
//        assertEquals(expectedPower, testTurret.getPower(), "Power should add 1");
//    }

//    @Test
//    public void testTankPowerDown() {
//        app = new App();
//        PApplet.runSketch(new String[] { "App" }, app);
//        app.delay(1000); // 确保初始化完成
//
//        // 使用现有的坦克实例
//        testTank = app.getTestTank();
//        testTurret = app.getTestTurret();
//
//        int initialPower = testTurret.getPower();
//        app.keyCode = 83;
//        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 83));
//        int expectedPower = initialPower - 1;
//        assertEquals(expectedPower, testTurret.getPower(), "Power should minus 1");
//    }

    @Test
    public void testTankHealthKit_FuelKit_Parachute_LargeProjectile() throws InterruptedException {
        app = new App();
        PApplet.runSketch(new String[] { "App" }, app);
        app.delay(1000);

        testTank = app.getTestTank().get(0);
        testTurret = app.getTestTurret().get(0);

        // Test health kit purchase
        testTank.setHealth(80);
        int initialHealth = testTank.getHealth();
        testTank.setScore(20);
        int initialScore = testTank.getScore();
        app.keyCode = 82;
        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 82));
        int expectedHealth = initialHealth + 20;
        int expectedScore = initialScore - 20;
        assertEquals(expectedHealth, testTank.getHealth(), "Health should add 20");
        assertEquals(expectedScore, testTank.getScore(), "Score should minus 20");

        // Test parachute purchase
        int initialParachute = testTank.getParachutes();
        testTank.setScore(20);
        int initialScore1 = testTank.getScore();
        app.keyCode = 80;
        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 80));
        int expectedParachute = initialParachute + 1;
        int expectedScore1 = initialScore1 - 15;
        assertEquals(expectedParachute, testTank.getParachutes(), "Parachute should add 1");
        assertEquals(expectedScore1, testTank.getScore(), "Score should minus 15");

        // Test fuel kit purchase
        int initialFuel = testTank.getFuels();
        testTank.setScore(20);
        int initialScore2 = testTank.getScore();
        app.keyCode = 70;
        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 70));
        int expectedFuel = initialFuel + 200;
        int expectedScore2 = initialScore2 - 10;
        assertEquals(expectedFuel, testTank.getFuels(), "Fuel should add 200");
        assertEquals(expectedScore2, testTank.getScore(), "Score should minus 10");

        // Test large projectile
        testTank.setScore(20);
        int initialScore3 = testTank.getScore();
        // Simulate user input to make turret angle to be horizontal
        for (int i = 0; i < 30; i++) {
            app.keyCode = 40;
            app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 40));
            Thread.sleep(10);
        }

        // Activate large shell
        app.keyCode = 88;
        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 88));
        Thread.sleep(500);

        int expectedScore3 = initialScore3 - 20;
        boolean largeShellChanged = initialLargeShells != app.getLargeShellActivated();

        // Simulate large projectile shots
        app.keyCode = 32;
        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 32));

        // Wait until projectile explodes, and this explosion should have an explosion radius of 60
        boolean RadiusIs60 = false;
        for (int i = 0; i < 100; i++) {
            for (Explosion explosion : app.explosions) {
                if (explosion.getMaxRadius() == 60.0) {
                    RadiusIs60 = true;
                    break;
                }
            }
            if (RadiusIs60) {
                break;
            }
            Thread.sleep(50);
        }

        assertTrue(largeShellChanged, "Large shell state should change after pressing 'X'");
        assertTrue(RadiusIs60, "Explosion radius should be 60");
        assertEquals(expectedScore3, testTank.getScore(), "Score should minus 20");

    }
//    @Test
//    public void testTankParachuteKit() {
//        app = new App();
//        PApplet.runSketch(new String[] { "App" }, app);
//        app.delay(1000); // 确保初始化完成
//
//        // 使用现有的坦克实例
//        testTank = app.getTestTank();
//        testTurret = app.getTestTurret();
//
//        int initialParachute = testTank.getParachutes();
//        testTank.setScore(20);
//        int initialScore = testTank.getScore();
//        app.keyCode = 80;
//        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 80));
//        int expectedParachute = initialParachute + 1;
//        int expectedScore = initialScore - 15;
//        assertEquals(expectedParachute, testTank.getParachutes(), "Parachute should add 1");
//        assertEquals(expectedScore, testTank.getScore(), "Score should minus 15");
//    }
//
//    @Test
//    public void testTankFuelKit() {
//        app = new App();
//        PApplet.runSketch(new String[] { "App" }, app);
//        app.delay(1000); // 确保初始化完成
//
//        // 使用现有的坦克实例
//        testTank = app.getTestTank();
//        testTurret = app.getTestTurret();
//
//        int initialFuel = testTank.getFuels();
//        testTank.setScore(20);
//        int initialScore = testTank.getScore();
//        app.keyCode = 70;
//        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 70));
//        int expectedFuel = initialFuel + 200;
//        int expectedScore = initialScore - 10;
//        assertEquals(expectedFuel, testTank.getFuels(), "Fuel should add 200");
//        assertEquals(expectedScore, testTank.getScore(), "Score should minus 10");
//    }

//    @Test
//    public void testTurretAngleLeft() {
//        app = new App();
//        PApplet.runSketch(new String[] { "App" }, app);
//        app.delay(1000); // 确保初始化完成
//
//        // 使用现有的坦克实例
//        testTank = app.getTestTank();
//        testTurret = app.getTestTurret();
//
//        float initialAngle = testTurret.getAngle();
//        app.keyCode = 38;
//        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 38));
//        float expectedAngle = initialAngle - 3;
//        assertEquals(expectedAngle, testTurret.getAngle(), "Angle should minus 3");
//    }
//
//    @Test
//    public void testTurretAngleRight() {
//        app = new App();
//        PApplet.runSketch(new String[] { "App" }, app);
//        app.delay(1000); // 确保初始化完成
//
//        // 使用现有的坦克实例
//        testTank = app.getTestTank();
//        testTurret = app.getTestTurret();
//
//        float initialAngle = testTurret.getAngle();
//        app.keyCode = 40;
//        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 40));
//        float expectedAngle = initialAngle + 3;
//        assertEquals(expectedAngle, testTurret.getAngle(), "Angle should add 3");
//    }

    // Test normal projectile shots
    @Test
    public void testProjectile() throws InterruptedException {
        PImage Null = null;
        Color color = Color.BLACK;
        app = new App();
        PApplet.runSketch(new String[] { "App" }, app);
        app.delay(1000);
        // If projectile hits terrain, terrain surface and terrain block will be changed
        initialMapCells = new MapCell[app.mapCells.length][app.mapCells[0].length];
        for (int i = 0; i < app.mapCells.length; i++) {
            for (int j = 0; j < app.mapCells[i].length; j++) {
                initialMapCells[i][j] = new MapCell(app.mapCells[i][j].getX(), app.mapCells[i][j].getY(), app.mapCells[i][j].getTerrainType(), null, color);
                initialMapCells[i][j].setSolid(app.mapCells[i][j].isSolid());
                initialMapCells[i][j].setTerrainType(app.mapCells[i][j].getTerrainType());
            }
        }

        for (int i = 0; i < 30; i++) {
            app.keyCode = 40;
            app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 40));
            Thread.sleep(10);
        }

        app.keyCode = 32;
        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 32));

        Thread.sleep(5000);

        boolean solidTerrainChanged = false;
        boolean terrainSurfaceAndTreesChanged = false;
        for (int i = 0; i < app.mapCells.length; i++) {
            for (int j = 0; j < app.mapCells[i].length; j++) {
                if (app.mapCells[i][j].isSolid() != initialMapCells[i][j].isSolid()) {
                    solidTerrainChanged = true;
                    break;
                }
                if (app.mapCells[i][j].getTerrainType() != initialMapCells[i][j].getTerrainType()){
                    terrainSurfaceAndTreesChanged = true;
                }
            }
        }

        assertTrue(solidTerrainChanged, "Once the projectile hits terrain, terrain should change");
        assertTrue(terrainSurfaceAndTreesChanged, "Once the projectile hits terrain, terrain should change");
    }

    @Test
    public void testTerrainDestroyedTankMovement() throws InterruptedException {
        app = new App();
        PApplet.runSketch(new String[] { "App" }, app);
        app.delay(1000);
        for (int i = 0; i < 12; i++) {
            app.keyCode = 32;
            app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 32));
            Thread.sleep(1000);
        }
        Thread.sleep(7000);
        app.currentTurn = 0;
        app.getTestTank().get(0).setScore(20);
        app.keyCode = 70;
        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 70));
        app.keyCode = 70;
        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 70));
        for (int i = 0; i < 240; i++) {
            app.keyCode = 39;
            app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 39));
            Thread.sleep(10);
        }
        for (int i = 0; i < 240; i++) {
            app.keyCode = 37;
            app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 37));
            Thread.sleep(10);
        }
        app.currentTurn = 1;
        app.getTestTank().get(1).setScore(20);
        app.keyCode = 70;
        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 70));
        app.keyCode = 70;
        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 70));
        for (int i = 0; i < 240; i++) {
            app.keyCode = 39;
            app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 39));
            Thread.sleep(10);
        }
        for (int i = 0; i < 240; i++) {
            app.keyCode = 37;
            app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 37));
            Thread.sleep(10);
        }
        app.currentTurn = 2;
        app.getTestTank().get(2).setScore(20);
        app.keyCode = 70;
        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 70));
        app.keyCode = 70;
        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 70));
        for (int i = 0; i < 240; i++) {
            app.keyCode = 39;
            app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 39));
            Thread.sleep(10);
        }
        for (int i = 0; i < 240; i++) {
            app.keyCode = 37;
            app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 37));
            Thread.sleep(10);
        }
        app.currentTurn = 3;
        app.getTestTank().get(3).setScore(20);
        app.keyCode = 70;
        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 70));
        app.keyCode = 70;
        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 70));
        for (int i = 0; i < 240; i++) {
            app.keyCode = 39;
            app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 39));
            Thread.sleep(10);
        }
        for (int i = 0; i < 240; i++) {
            app.keyCode = 37;
            app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 37));
            Thread.sleep(10);
        }
        TankSprite testTank1 = app.getTestTank().get(0);
        boolean testTank1Exists = testTank1 != null;
        TankSprite testTank2 = app.getTestTank().get(1);
        boolean testTank2Exists = testTank2 != null;
        TankSprite testTank3 = app.getTestTank().get(2);
        boolean testTank3Exists = testTank3 != null;
        TankSprite testTank4 = app.getTestTank().get(3);
        boolean testTank4Exists = testTank4 != null;
        assertTrue(testTank1Exists, "tank1 should exist");
        assertTrue(testTank2Exists, "tank2 should exist");
        assertTrue(testTank3Exists, "tank3 should exist");
        assertTrue(testTank4Exists, "tank4 should exist");




    }
//    @Test
//    public void testLargeShell() throws InterruptedException {
//        app = new App();
//        PApplet.runSketch(new String[] { "App" }, app);
//        app.delay(1000);
//        testTank = app.getTestTank();
//        testTurret = app.getTestTurret();
//
//    }


//    @Test
//    public void testTankMovement() {
//        App app = new App();
//        app.loop();
//        PApplet.runSketch(new String[] { "App" }, app);
//        app.setup();
//        app.delay(1000); // to give time to initialise stuff before drawing begins
//
//        //app.keyPressed(new KeyEvent(null, 0, 0, 0, ... ' ', 37)) // all that matters is keyCode if this is all you check in your code
//        //check velocity of tank is as expected
//
//        //assertEquals(expected, app.getCurrentPlayer().getVelocity());
//    }
//
//    // Test when the user presses a key, then the powerup activates
//    @Test
//    public void testPowerup() {
//        App app = new App();
//        app.loop();
//        PApplet.runSketch(new String[] { "App" }, app);
//        app.setup();
//        app.delay(1000); // to give time to initialise stuff before drawing begins
//        //check powerup had an effect
//
//        //assert scoreboard value decreased as expected
//    }
//
//    @Test
//    public void testProjectile() {
//        //perform key press action - space bar
//        int initialframes = app.frameCount;
//        Thread.sleep(1000); //1 second passes, check game state was changed in expected way
//        //projectile moves through the air
//
//        int endframes = app.frameCount;
//        //do we expect the projectile to have hit the target? - if time is too short, then assert projectile still exists
//
//        //if expected projectile to have hit terrain - assert terrain is destroyed
//
//        //however, the user has to wait 1 second for the testcase to complete
//        // - to avoid this - use app.noLoop()
//
//        //simulate 3 second of time in the game
//        for (int i = 0; i < App.FPS*3; i++) {
//            app.draw(); //simulate frame
//            //or if a seperate method is used for game logic tick, eg. app.tick()
//        }
//    }

//}
}

// gradle run						Run the program
// gradle test						Run the testcases

// Please ensure you leave a comments in your testcases explaining what the testcase is testing.
// Your mark will be based off the average of branches and instructions code coverage.
// To run the testcases and generate the jacoco code coverage report:
// gradle test jacocoTestReport