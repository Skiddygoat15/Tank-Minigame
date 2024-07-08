package Tanks;

import javafx.scene.input.KeyCode;
import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.awt.*;

import java.util.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
//Please note!! In computer graphics, the xy zero is in the upper left corner, so when using mapCell, be aware that you may have to invert xy, that is, mapCell[y][x] like this

/**
 * This class is the main class
 * Game starts from here
 */

public class App extends PApplet {

    public static final int CELLSIZE = 1;
    public static int WIDTH = 864; //CELLSIZE*BOARD_WIDTH;
    public static int HEIGHT = 640; //BOARD_HEIGHT*CELLSIZE+TOPBAR;


    public static final int FPS = 30;
    /**
     * This field stores the original terrain data.
     */
    int[][] terrainMatrix;
    /**
     * This field stores the amplified terrain data.
     * Moving averages are only possible by amplify the raw data
     */
    int[][] amplifiedTerrain;   // Amplified Terrain Matrix
    /**
     * This field stores the map data.
     */
    MapCell[][] mapCells;

    JSONObject config;
    PImage background;
    PImage trees;
    PImage FuelTank;
    PImage Parachute;
    PImage WindPositive;
    PImage WindNegative;
    Color foregroundColor;
    /**
     * This field stores the tanks list
     * Most in-game operations on the tank itself are realised through this list
     */
    List<TankSprite> tanks = new ArrayList<>();
    private List<tankTurret> turrets = new ArrayList<>();
    private List<List<Shell>> shells = new ArrayList<>();
    private boolean showArrow = true;
    private int arrowDisplayFrames = 0;
    private int WinnerDisplayFrames = 0;
    private double globalWind = 0;
    public static Random random = new Random();
    private boolean afterInitialisation = false;
    private Map<TankSprite, Integer> tankScores = new HashMap<>();
    /**
     * Indicates curren turn, Also used to get the current tank/turret in the tanks/turrets list.
     */
    public int currentTurn = 0;
    /**
     * This list stores all occurring explosions.
     */
    List<Explosion> explosions = new ArrayList<>();
    public String configPath;
    private Map<Explosion, Shell> explosionSource = new HashMap<>();// Used to figure out who made the falling damage
    private boolean allLevelEnded = false;
    private Map<TankSprite, Boolean> largeShellActivated = new HashMap<>();

	
	// Feel free to add any additional methods or attributes you want. Please put classes in different files.

    public App() {
        this.configPath = "config.json";
    }

    /**
     * Initialise the setting of the window size.
     */
	@Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }
    int levelIndex = 0;

    /**
     * Load all resources such as images. Initialise the elements such as the player and map elements.
     */
	@Override
    public void setup() {
        frameRate(FPS);
        this.config = loadJSONObject("config.json");
        loadLevel();
        applyMovingAverageToTerrain();
        applyMovingAverageToTerrain();
        newTreesPosition();
        newTankPosition();
        initializeMapCells();

        initialiseTankScore();

        for (int i = 0; i < tanks.size(); i++) {
            shells.add(new ArrayList<>());  // Initialize the shell list for each tank
        }
        afterInitialisation = false;
        updateWind();
    }

    String[][] PlayerTankColour;
    /**
     * The loadLevel class will load map resources from config.json .
     */
    private void loadLevel() {

        JSONArray levels = config.getJSONArray("levels");
        JSONObject level = levels.getJSONObject(levelIndex); // Load Level
        JSONObject PlayerColour = config.getJSONObject("player_colours");
        PlayerTankColour = ColourParser.parsePlayerColours(PlayerColour);
        for (int i = 0; i < PlayerTankColour.length; i++){
            for (int j = 0; j < PlayerTankColour[0].length; j++){
                System.out.println("PlayerTankColour: " + PlayerTankColour[i][j]);
            }
        }
        String backgroundPath = level.getString("background");
        String foregroundColorStr = level.getString("foreground-colour");
        String layoutPath = level.getString("layout");
        String workingDir = System.getProperty("user.dir");
        if (levelIndex == 0 || levelIndex == 2){
            String treesPath = level.getString("trees");
            trees = loadImage(this.getClass().getResource(treesPath).getPath().toLowerCase(Locale.ROOT).replace("%20", " "));
        }
        System.out.println("Current working path is:" + workingDir);

        background = loadImage(this.getClass().getResource(backgroundPath).getPath().toLowerCase(Locale.ROOT).replace("%20", " "));
        FuelTank = loadImage(this.getClass().getResource("fuel.png").getPath().toLowerCase(Locale.ROOT).replace("%20", " "));
        Parachute = loadImage(this.getClass().getResource("parachute.png").getPath().toLowerCase(Locale.ROOT).replace("%20", " "));
        WindPositive = loadImage(this.getClass().getResource("wind.png").getPath().toLowerCase(Locale.ROOT).replace("%20", " "));
        WindNegative = loadImage(this.getClass().getResource("wind-1.png").getPath().toLowerCase(Locale.ROOT).replace("%20", " "));
        System.out.println(this.getClass().getResource(backgroundPath));

        foregroundColor = ColourParser.parseColor(foregroundColorStr);
        parseTerrain(layoutPath);
    }

    private void initialiseTankScore() {
        Map<TankSprite, Integer> newTankScores = new HashMap<>();
        if (levelIndex == 0) {
            // Initial level, set initial score to 0 for each tank
            for (TankSprite tank : tanks) {
                newTankScores.put(tank, 0);
            }
        } else {
            // Non-initial levels, copying scores from old mappings
            for (TankSprite tank : tanks) {
                // Find the tank with the same playerId as the current tank and copy its score
                for (TankSprite oldTank : tankScores.keySet()) {
                    if (oldTank.getPlayerId() == tank.getPlayerId()) {
                        newTankScores.put(tank, tankScores.get(oldTank));
                        tank.setScore(tankScores.get(oldTank));
                        break;
                    }
                }
            }
        }
        // Replacing old tankScores mapping
        tankScores = newTankScores;
    }

    int lineSize = 0;
    /**
     * This class will parse terrain data.
     */
    private void parseTerrain(String filePath) {
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get(filePath));
            if (lines.isEmpty()) {
                System.out.println("Level file is empty.");
                return;
            }
        } catch (Exception e) {
            System.err.println("Unable to load level files" + e.getMessage());
            return;
        }

        // Find the length of the longest line
        int maxLineLength = lines.stream().mapToInt(String::length).max().orElse(0);
        this.lineSize = lines.size();
        System.out.println("maxLineLength: " + maxLineLength);
        System.out.println(lineSize);
        // Initialize the terrain matrix
        terrainMatrix = new int[lineSize][maxLineLength];

        // Parses each character and fills the terrain matrix
        terrainRectifier.fillOriginalTerrainMatrix(terrainMatrix, lineSize, lines);

        amplifiedTerrain = new int[740][900];

        // Parses the terrain and fills the enlarged terrain matrix
        terrainRectifier.terrainAmplifier(terrainMatrix, amplifiedTerrain, lineSize, lines);
    }
    /**
     * This class will assign new trees position.
     */
    private void newTreesPosition() {
        int[][] newAmplifiedTrees = new int[amplifiedTerrain.length][amplifiedTerrain[0].length];
        for (int i = 0; i < amplifiedTerrain.length; i++) {
            System.arraycopy(amplifiedTerrain[i], 0, newAmplifiedTrees[i], 0, amplifiedTerrain[i].length);
        }
        double [] treesX = new double[40];
        double [] treesY = new double[40];
        int index = 0;
        int indexFor = 0;
        // Fuzzy localization based on relative position in the original array
        int [][] newAmplifiedTrees1 = terrainRectifier.newPosition(26.5, terrainMatrix, newAmplifiedTrees, index, indexFor, treesX, treesY, 3, 2);
        // Precise positioning over the terrain
        this.amplifiedTerrain = terrainRectifier.positionRectifier(newAmplifiedTrees1, 2, 1 , 3);
    }
    /**
     * This class will assign new tanks position.
     */
    private void newTankPosition(){
        int [] Tanks = CheckPlayers.getPlayers(terrainMatrix);
        int index = 0;
        int TanksLength = 0;
        while (Tanks[index] != 0){
            TanksLength++;
            index++;
        }
        double [] tanksX = new double[20];
        double [] tanksY = new double[20];
        int index1 = 0;
        int indexFor = 0;
        int [][] newAmlifiedTanks = new int[amplifiedTerrain.length][amplifiedTerrain[0].length];
        for (int i = 0; i < amplifiedTerrain.length; i++) {
            System.arraycopy(amplifiedTerrain[i], 0, newAmlifiedTanks[i], 0, amplifiedTerrain[i].length);
        }
        System.out.println("TanksLength: " + TanksLength);
        int [] TanksIdentity = new int[TanksLength];
        System.arraycopy(Tanks, 0, TanksIdentity, 0, TanksLength);
        System.out.println("Tank identity: " + Arrays.toString(TanksIdentity));
        for (int i = 0; i < TanksLength; i++){
            terrainRectifier.newPosition(27, terrainMatrix, newAmlifiedTanks, index1, indexFor, tanksX, tanksY, 3, TanksIdentity[i]);
            terrainRectifier.positionRectifier(newAmlifiedTanks, TanksIdentity[i], 1, 3);
        }

        amplifiedTerrain = newAmlifiedTanks;


    }

    /**
     * Receive key pressed signal from the keyboard.
     */
	@Override
    public void keyPressed(KeyEvent event){
        if (keyCode == LEFT) {
            // Players can't move beyond the boundaries of the x-axis, and at the same time can't move when falling
            if (!tanks.isEmpty() && !allLevelEnded && !levelSwitchOccurring && tanks.get(currentTurn).getFuels() > 0 && tanks.get(currentTurn).getX() > 5 && !tanks.get(currentTurn).isAnimating())
                moveTank(-2);
//            System.out.println("left pressed");
        } else if (keyCode == RIGHT) {
            if (!tanks.isEmpty() && !allLevelEnded && !levelSwitchOccurring && tanks.get(currentTurn).getFuels() > 0 && tanks.get(currentTurn).getX() < 859 && !tanks.get(currentTurn).isAnimating())
                moveTank(2);
//            System.out.println("right pressed");
        } else if (keyCode == UP) {
            if (!turrets.isEmpty() && !allLevelEnded && !levelSwitchOccurring ){
                float currentAngle = turrets.get(currentTurn).getAngle();
                if (currentAngle > -90)
                    turrets.get(currentTurn).setAngle(currentAngle - 3);
            }
        } else if (keyCode == DOWN) {
            if (!turrets.isEmpty() && !allLevelEnded && !levelSwitchOccurring ){
                float currentAngle = turrets.get(currentTurn).getAngle();
                if (currentAngle < 90)
                    turrets.get(currentTurn).setAngle(currentAngle + 3);
            }
        } else if (keyCode == 32) {
            if (!turrets.isEmpty() && !allLevelEnded && !levelSwitchOccurring ){
                updateWind();
                Shell newShell = turrets.get(currentTurn).fireAndSetShell(turrets.get(currentTurn).getPower());
                newShell.setTank(turrets.get(currentTurn).getTurretTank());
                shells.get(currentTurn).add(newShell);
                endTurn();
            }
        } else if (keyCode == 87){
            if (!turrets.isEmpty() && !allLevelEnded && !levelSwitchOccurring && turrets.get(currentTurn).getPower() < 100 && turrets.get(currentTurn).getPower() < tanks.get(currentTurn).getHealth()){
                turrets.get(currentTurn).powerUp();
            }
        } else if (keyCode == 83) {
            if (!turrets.isEmpty() && !allLevelEnded && !levelSwitchOccurring && turrets.get(currentTurn).getPower() > 0){
                turrets.get(currentTurn).powerDown();
            }
        } else if (keyCode == 82) {
            if (!allLevelEnded && !levelSwitchOccurring && tanks.get(currentTurn) != null && tanks.get(currentTurn).getScore() > 19 && tanks.get(currentTurn).getHealth() < 100) {
                tanks.get(currentTurn).minusScore(20);
                tanks.get(currentTurn).healthKit();
            }
            if (allLevelEnded){
                allLevelEnded = false;
                Iterator<tankTurret> turretIterator = turrets.iterator();
                while (turretIterator.hasNext()) {
                    tankTurret turret = turretIterator.next();
                    if (turret != null) {
                        turret.getTurretTank().getLocation().setTankSprite(null);
                        tanks.remove(turret.getTurretTank());
                        turretIterator.remove();
                    }
                }

                // Clear all shells
                shells.clear();
                explosionSource.clear();

                levelIndex = (levelIndex + 1) % 3;  // Cycle between three levels
                globalWind = 0;
                WinnerDisplayFrames = 0;
                setup();  // Reload the level
            }
        } else if (keyCode == 80) {
            if (!allLevelEnded && !levelSwitchOccurring && tanks.get(currentTurn) != null && tanks.get(currentTurn).getScore() > 14){
                tanks.get(currentTurn).minusScore(15);
                tanks.get(currentTurn).addParachute();

            }
        } else if (keyCode == 88) {
            TankSprite currentTank = tanks.get(currentTurn);
            if (!allLevelEnded && !levelSwitchOccurring && currentTank != null && currentTank.getScore() > 19 && !largeShellActivated.containsKey(currentTank)) {
                currentTank.minusScore(20);
                largeShellActivated.put(currentTank, true);
            }
        } else if (keyCode == 70) {
            TankSprite currentTank = tanks.get(currentTurn);
            if (!allLevelEnded && !levelSwitchOccurring && currentTank != null && currentTank.getScore() > 9){
                currentTank.minusScore(10);
                currentTank.fuelKit();
            }
        }
    }
    /**
     * Change current turn and indicates player arrow should be displayed.
     */
    public void endTurn() {
        // Update the tank index for the current turn, move to the next tank

        currentTurn = (currentTurn + 1) % tanks.size();
        this.showArrow = true; // Reset arrow display
        this.arrowDisplayFrames = 0; // Reset frame count
    }


    // To my future self, please don't change this moveTank, it took me 3 hours to write it in order to realize the move function
    /**
     * This method allows the tank to move to the next available terrain surface by detecting a suitable terrain surface.
     * @param dx represents the distance need to traveled
     */
    public void moveTank(int dx) {
        int newX = tanks.get(currentTurn).getX() + dx;
//        System.out.println("firstTank.getX() + dx: " + newX);

        boolean conflictRight = false;
        boolean conflictLeft = false;

        // Y-direction scanning range
        int scanRange = 128;


        // Scanning the terrain below
        for (int offset = 1; offset <= scanRange && (tanks.get(currentTurn).getY() + offset - 2) < mapCells.length; offset++) {
//            System.out.println("below running");
            int newY = tanks.get(currentTurn).getY() + offset - 2;
//            System.out.println("Original firstTank X: " + firstTank.getX());
//            System.out.println("firstTank.getX() + dx: " + newX);
//            System.out.println("firstTank.getY() + offset: " + newY);
            while (isConflict(newX, newY)) {
                if (dx > 0){
                    newX++; // If there is a conflict, try moving to the left
                    conflictRight = true;
                } else if (dx < 0) {
                    newX--;
                    conflictLeft = true;
                }

            }
            if (canMoveTo(newX, newY)) {
//                firstTank.move(dx, offset - 2);
                // Clear current position
                mapCells[tanks.get(currentTurn).getY()][tanks.get(currentTurn).getX()].setTankSprite(null);
//                if (mapCells[tanks.get(currentTurn).getY()][tanks.get(currentTurn).getX()].getSprite() == null)
//                    System.out.println("Seems like previous location's tank is cleared");


                // Updated tank positions
                if (conflictRight){
                    tanks.get(currentTurn).move(dx + 1, offset - 2);
                    tanks.get(currentTurn).fuelConsumption();
                    conflictRight = false;
                } else if (conflictLeft) {
                    tanks.get(currentTurn).move(dx - 1, offset - 2);
                    tanks.get(currentTurn).fuelConsumption();
                    conflictLeft = false;
                } else {
                    tanks.get(currentTurn).move(dx, offset - 2);
                    tanks.get(currentTurn).fuelConsumption();
                }

                // Setting the new position
                mapCells[newY][newX].setTankSprite(tanks.get(currentTurn));
                return; // Terminate the loop when movable terrain found
            }
        }

        // Scanning the terrain above
        for (int offset = 0; offset <= scanRange; offset++) {
            int newY = tanks.get(currentTurn).getY() - offset + 2;
            while (isConflict(newX, newY)) {
                if (dx > 0){
                    newX++; // If there is a conflict, try moving to the left
                    conflictRight = true;
                } else if (dx < 0) {
                    newX--;
                    conflictLeft = true;
                }

            }
            if (canMoveTo(newX, newY)) {
//                firstTank.move(dx, -offset + 2);
                mapCells[tanks.get(currentTurn).getY()][tanks.get(currentTurn).getX()].setTankSprite(null);
//                if (mapCells[tanks.get(currentTurn).getY()][tanks.get(currentTurn).getX()].getSprite() == null)
//                    System.out.println("Seems like previous location's tank is cleared");

                if (conflictRight){
                    tanks.get(currentTurn).move(dx + 1, -offset + 2);
                    tanks.get(currentTurn).fuelConsumption();
                    conflictRight = false;
                } else if (conflictLeft) {
                    tanks.get(currentTurn).move(dx - 1, -offset + 2);
                    tanks.get(currentTurn).fuelConsumption();
                    conflictLeft = false;
                } else {
                    tanks.get(currentTurn).move(dx, -offset + 2);
                    tanks.get(currentTurn).fuelConsumption();
                }

                mapCells[newY][newX].setTankSprite(tanks.get(currentTurn));
//                if (mapCells[newY][newX].getSprite() != null)
//                    System.out.println("Seems like new location is allocated");
//                System.out.println("possible move to " + newY);
                return; // Terminate the loop when you find movable terrain
            }
        }

        // If no moveable terrain is found, the tank stays in place
        System.out.println("No valid move found.");
    }

    /**
     * This method detects if the tank can move to the next position.
     * @param x stands for the detecting x coordinate.
     * @param y stands for the detecting y coordinate.
     * @return Whether this tank can move to new position
     */
    public boolean canMoveTo(int x, int y) {
//        System.out.println("canMoveTo running");
        // Ensure that tanks do not leave the map boundary
        if (x < 0 || y < 0 || x >= 900 || y >= 740) return false;

        // Ensure that the terrain at the new location is passable
        if (mapCells[y][x].getTerrainType() == 1) {
            return true;
        }
        return false;
    }
    /**
     * This method detects if the tank's next position is conflict with another tank.
     * @param x stands for the detecting x coordinate.
     * @param y stands for the detecting y coordinate.
     * @return Whether it's a conflicted position
     */
    public boolean isConflict(int x, int y) {
        if (y < mapCells.length && mapCells[y][x].getSprite() != null) {
            return true;
        }
        return false;
    }
    /**
     * Receive key released signal from the keyboard.
     */
	@Override
    public void keyReleased(){
        
    }

    @Override
    public void mousePressed(MouseEvent e) {
        //TODO - powerups, like repair and extra fuel and teleport


    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    /**
     * Draw all elements in the game by current frame.
     */
	@Override
    public void draw() {
        background(background);
        displayTerrain();
        displayTankParachute();
        displayTurrets();
        displayTanks();
        updateWindPerFrame();
        displayShellsAndExplosions();
        updateTankFall();
        displayTurn();
        displayFuel();
        displayParachute();
        displayPower();
        displayHealth();
        if (showArrow && arrowDisplayFrames < 60) {
            dispayArrow();
            arrowDisplayFrames++;
        } else {
            showArrow = false;
        }
        displayWind();
        displayLeaderBoard();
        displayTankExplosions();
        syncScores();
        checkLevelSwitch();
        if ((tanks.size() == 1 || tanks.isEmpty()) && levelIndex == 2) {
            displayWinner();
            WinnerDisplayFrames++;
        }
//        displayWinnerTest();
        displayLargerShellGUI();


        //----------------------------------
        //display HUD:
        //----------------------------------
        //TODO

        //----------------------------------
        //display scoreboard:
        //----------------------------------
        //TODO
        
		//----------------------------------
        //----------------------------------

        //TODO: Check user action
    }
//      Tank Drops
    /**
     * Update falling tank's position and start animation.
     */
    public void updateTankFall() {
        // Updated position and status of all tanks
        if (!allLevelEnded && !levelSwitchOccurring)
            for (TankSprite tank : tanks) {
                tank.update(mapCells, tank);
            }

        // Check and start tank descent animation
        Explosion.checkAndAnimateTanks(tanks, mapCells, explosionSource);
    }
    /**
     * Apply wind effects to all shells.
     */
    private void updateWind() {
        // Apply wind speed to all shells
        if (!afterInitialisation) {
            globalWind = globalWind + random.nextDouble() * 70 - 35; // Random values from -35 to +35
            afterInitialisation = true;
//            globalWind += 35;
        } else {
            globalWind = globalWind + random.nextDouble() * 10 - 5; // Random values from -5 to +5
//            globalWind -= 5;
        }
    }
    /**
     * Display wind value on interface.
     */
    private void updateWindPerFrame () {
        for (List<Shell> shellList : shells) {
            for (Shell shell : shellList) {
                shell.applyWind(globalWind);
            }
        }
    }
    private void displayTerrain() {
        for (int i = 0; i < mapCells.length; i++) {
            for (int j = 0; j < mapCells[i].length; j++) {
                mapCells[i][j].draw(this, j, i, CELLSIZE);
            }
        }
    }
    public void displayTurrets() {
        for (tankTurret turret : turrets){
            turret.draw(this);
        }
    }

    public void displayTanks() {
        for (int i = 0; i < mapCells.length; i++) {
            for (int j = 0; j < mapCells[i].length; j++) {
                if ( mapCells[i][j].getSprite() != null){
                    mapCells[i][j].drawTank(this, j , i , CELLSIZE);
//                    System.out.println("Tank ID on map: " + mapCells[i][j].getSprite().getPlayerId());
                }
            }
        }
    }
    public void displayShellsAndExplosions() {
        for (int i = 0; i < shells.size(); i++) {
            Iterator<Shell> it = shells.get(i).iterator();
            while (it.hasNext()) {
                Shell shell = it.next();
                shell.draw(this);
                double sX = shell.getX();
                double sY = shell.getY();
                int radius = 30; // Default projectile radius
                if (largeShellActivated.containsKey(shell.getTank())) {
                    radius = 60; // Large projectile radius
                }

                if ((sY > 0 && sY < mapCells.length && sX < mapCells[0].length &&  sX > 0 && mapCells[(int) sY][(int) sX].isSolid()) || (sX <= 10 || sX >= mapCells[0].length - 11) || sY > 871) {
                    Explosion explosion = new Explosion(sX, sY, radius, shell);
                    explosions.add(explosion);
                    explosionSource.put(explosion, shell);
                    Explosion.applyExplosionDamage(sX, sY, radius, mapCells, shell);
                    it.remove();
                    largeShellActivated.remove(shell.getTank()); // Remove activation after use
                }
            }
        }

        // Showing explosions frame by frame
        Iterator<Explosion> expIt = explosions.iterator();
        while (expIt.hasNext()) {
            Explosion explosion = expIt.next();
            if (!explosion.update(0.1f, mapCells)) {
                explosionSource.remove(explosion);
                expIt.remove();
            } else {
                explosion.draw(this);
            }
        }
    }

    public void displayTurn() {
        if (!tanks.isEmpty()) {
            int playerId = tanks.get(currentTurn).getPlayerId(); // Get current player ID
            String displayId = CheckPlayers.playerIdToDisplay(playerId); // Getting the display ID using the conversion method
            String turnText = "Player " + displayId + "'s Turn";

            fill(0, 0, 0);
            textSize(20);
            text(turnText, 20, 30);
        }
    }

    public void displayFuel() {
        if (!tanks.isEmpty()){
            int fuelLevel = tanks.get(currentTurn).getFuels(); // Get the current player's fuel level

            float newWidth = FuelTank.width * 0.1f;
            float newHeight = FuelTank.height * 0.1f;
            image(FuelTank, 175, 4, newWidth, newHeight);

            fill(0, 0, 0);
            textSize(20);

            text(fuelLevel, 210, 30);
        }
        }

    public void displayParachute() {
        float newWidthP = Parachute.width * 0.5f;
        float newHeightP = Parachute.height * 0.5f;
        image(Parachute, 175,40, newWidthP, newHeightP);
        fill(0, 0, 0);
        textSize(20);
        if (!tanks.isEmpty())
            text(tanks.get(currentTurn).getParachutes(), 210, 65);
    }

    public void displayTankParachute() {
        for (TankSprite tank : tanks) {
            if (tank.isAnimating() && tank.getParachutes() > 0) {
                float newWidthP = Parachute.width * 0.5f;
                float newHeightP = Parachute.height * 0.5f;
                image(Parachute, tank.getX() - 16, tank.getY() - 33, newWidthP, newHeightP);
            }
        }
    }
    public void displayPower() {
        if (!turrets.isEmpty()){
            if (turrets.get(currentTurn).getPower() > tanks.get(currentTurn).getHealth()) {
                turrets.get(currentTurn).setPower(tanks.get(currentTurn).getHealth());
            }
            fill(0, 0, 0);
            textSize(20);
            text("Power: " + turrets.get(currentTurn).getPower(), 320, 65);
        }
    }

    public void displayHealth() {
        if (!tanks.isEmpty() && !turrets.isEmpty()){
            int health = tanks.get(currentTurn).getHealth();
            int power = turrets.get(currentTurn).getPower();
            int maxHealth = 100;
            int maxPower = 100;

            int barWidth = 200;
            int barHeight = 25;
            int x = 320;
            int y = 10;
            // Drawing Health Bar
            fill(255);
            rect(x, y, barWidth, barHeight);
            stroke(0);
            strokeWeight(3);
            fill(tanks.get(currentTurn).getBodyColour().getRGB());
            float healthWidth = map(health, 0, maxHealth, 0, barWidth);
            rect(x, y, healthWidth, barHeight);

            // Drawing the launch strength indicator
            float powerPosition = map(power, 0, maxPower, 0, barWidth);
            stroke(137, 127 ,145);
            strokeWeight(3);
            noFill();
            rect(x, y, powerPosition, barHeight);
            stroke(0);
            strokeWeight(1);
            line(x + powerPosition, y - 3, x + powerPosition, y + barHeight + 3);

            fill(0);
            textSize(20);
            text(health, x + 205, y + 20);
        }

    }

    public void dispayArrow() {
        if (!tanks.isEmpty()){
            int x = tanks.get(currentTurn).getX();
            int y = tanks.get(currentTurn).getY();
            strokeWeight(2);
            line(x - 1, y - 40, x - 1, y - 85);
            line(x - 1, y - 40, x + 6, y - 55);
            line(x - 1, y - 40, x - 8, y - 55);
        }
    }

    public void displayWind() {
        fill(0, 0, 0);
        textSize(20);
        int wind = (int)globalWind;
        if (wind < 0)
            wind = Math.abs(wind);
        if (globalWind > 0)
            image(WindPositive, 740,0);
        else
            image(WindNegative, 740,0);

        text((wind), 810, 40);
    }


    public void displayLeaderBoard() {
        fill(0, 0, 0);
        textSize(20);
        int yPosition = 140;

        // Get a list of tanks and sort them by playerId
        List<TankSprite> sortedTanks = new ArrayList<>(tankScores.keySet());
        Collections.sort(sortedTanks, Comparator.comparingInt(TankSprite::getPlayerId));

        // Iterate through the sorted list of tanks
        for (TankSprite tank : sortedTanks) {
            String displayId = CheckPlayers.playerIdToDisplay(tank.getPlayerId());
            int score = tankScores.get(tank);
            fill(tank.getBodyColour().getRGB());
            text("Player " + displayId, 690, yPosition);
            fill(0);
            text( score, 790, yPosition);
            yPosition += 30; // Update the display position of the next player
        }
        stroke(0);
        strokeWeight(2);
        line(660, 80, 660, 250);
        line(845, 80, 845, 250);
        line(660, 80, 845, 80);
        line(660, 110, 845, 110);
        line(660, 250, 845, 250);
        textSize(22);
        stroke(0);
        strokeWeight(2);
        text("Score", 725, 103);
    }

    public void displayTankExplosions() {
        // Check if the tank should explode
        Iterator<TankSprite> tankIt = tanks.iterator();
        Iterator<tankTurret> turretIt = turrets.iterator();
        int index = 0; // Index for tracking tanks
        while (tankIt.hasNext() && turretIt.hasNext()) {
            TankSprite tank = tankIt.next();
            tankTurret turret = turretIt.next();
            if (tank.getHealth() == 0 || tank.getY() >= 645) {
                int explosionRadius = tank.getY() >= 645 ? 30 : 15;
                Explosion explosion = new Explosion(tank.getX(), tank.getY(), explosionRadius, null);
                explosions.add(explosion);  // Adding explosions to the list
                System.out.println("Tank should explode now!");
                mapCells[tank.getY()][tank.getX()].setTankSprite(null);  // Clearing tanks from the map

                if (index == currentTurn - 1) {
                    // If the deleted tank is a tank from the current turn, handle currentTurn
                    currentTurn--;
                }

                tankIt.remove();  // Remove tanks from the list
                turretIt.remove();  // Remove the corresponding turret at the same time
            }
            index++; // Update Index
        }

        if (currentTurn >= tanks.size()) {
            currentTurn = 0;  // If currentTurn is out of range, reset to 0
        }
        if (currentTurn == -1)
            currentTurn++ ;
    }
    /**
     * Make sure tank scores are always updated and correctly assigned.
     */
    public void syncScores() {
        for (TankSprite tank : tanks) {
            if (tankScores.containsKey(tank)) {
                    tankScores.put(tank, tank.getScore());

            }
        }
    }

    private int delayStartTime = 0;
    private boolean levelSwitchDelay = false;
    private boolean levelSwitchOccurring = false;
    /**
     * Check if game should switch to another level.
     */
    public void checkLevelSwitch() {
        if (levelIndex < 2) {
            // Check if there is only one tank left in the frame-by-frame method
            if (tanks.size() == 1 || tanks.isEmpty()) {
                if (!levelSwitchDelay) {
                    // Start the delay timer
                    delayStartTime = millis();
                    levelSwitchDelay = true;
                    levelSwitchOccurring = true;
                } else if (millis() - delayStartTime >= 1000) {
                    // Delay has passed, perform the level switch
                    Iterator<tankTurret> turretIterator = turrets.iterator();
                    while (turretIterator.hasNext()) {
                        tankTurret turret = turretIterator.next();
                        if (turret != null) {
                            turret.getTurretTank().getLocation().setTankSprite(null); // Unreferencing Tanks in Map Cells
                            tanks.remove(turret.getTurretTank()); // Delete tanks from the tank list
                            turretIterator.remove(); // Safely removing turrets from the turret list
                        }
                    }

                    shells.clear();
                    explosionSource.clear();
                    explosions.clear();

                    levelIndex = (levelIndex + 1) % 3;
                    globalWind = 0;
                    setup();

                    // Reset delay variables
                    levelSwitchDelay = false;
                    levelSwitchOccurring = false;
                    delayStartTime = 0;
                    return;
                }
            } else {
                // Reset delay if condition is not met
                levelSwitchDelay = false;
                levelSwitchOccurring = false;
                delayStartTime = 0;
            }
        } else if (levelIndex == 2) {
            if (tanks.size() == 1 || tanks.isEmpty()) {
                allLevelEnded = true;
            }
        }
    }
    public void displayWinner() {
        fill(237,137,157);
        rect(240, 155, 360, 165);
        noFill();
        stroke(0);
        strokeWeight(2);
        // Left frame
        line(240, 155, 240, 320);
        // Right frame
        line(600, 155, 600, 320);
        // upper frame
        line(240, 155, 600, 155);
        // center frame
        line(240, 195, 600, 195);
        // lower border
        line(240, 320, 600, 320);


        textSize(22);
        stroke(0);
        strokeWeight(2);
        fill(0);

        // Determining the winner
        TankSprite winner = null;
        int maxScore = -1;
        for (Map.Entry<TankSprite, Integer> entry : tankScores.entrySet()) {
            if (entry.getValue() > maxScore || (entry.getValue() == maxScore && (winner == null || entry.getKey().getPlayerId() < winner.getPlayerId()))) {
                winner = entry.getKey();
                maxScore = entry.getValue();
            }
        }

        fill(winner.getBodyColour().getRGB());
        text("Player " + CheckPlayers.playerIdToDisplay(winner.getPlayerId()) + " wins!", 260, 140);
        fill(0);
        text("Final Scores", 260, 182);
        if (WinnerDisplayFrames > 30) {
            // Shows scores of all players, sorted by score
            List<TankSprite> sortedTanks = new ArrayList<>(tankScores.keySet());
            Collections.sort(sortedTanks, (a, b) -> tankScores.get(b).compareTo(tankScores.get(a)));
            int yPos = 220;  // Starting y position
            for (TankSprite tank : sortedTanks) {
                if (WinnerDisplayFrames > 30 * (sortedTanks.indexOf(tank) + 1)) {
                    fill(tank.getBodyColour().getRGB());
                    text("Player " + CheckPlayers.playerIdToDisplay(tank.getPlayerId()), 260, yPos);
                    fill(0);
                    text(tankScores.get(tank), 540, yPos);
                    yPos += 30;
                }
                if (WinnerDisplayFrames > 150) {
                    fill(0);
                    text("Press 'r' to Restart!", 260, 380);
                }
            }
        }
    }
    public void displayLargerShellGUI() {
        if (!tanks.isEmpty() && tanks.get(currentTurn) != null) {
            TankSprite currentTank = tanks.get(currentTurn);
            if (largeShellActivated.containsKey(currentTank)) {
                fill(0);
                textSize(20);
                text("Large Shell Ready!", 20, 105);
            }
        }
    }




    private void applyMovingAverageToTerrain() {
        int[][] newAmplifiedTerrain = new int[amplifiedTerrain.length][amplifiedTerrain[0].length];

        // Copy the original array to the new one
        for (int i = 0; i < amplifiedTerrain.length; i++) {
            System.arraycopy(amplifiedTerrain[i], 0, newAmplifiedTerrain[i], 0, amplifiedTerrain[i].length);
        }

        amplifiedTerrain = terrainRectifier.MovingAverage(amplifiedTerrain, newAmplifiedTerrain);
    }


    /**
     * This method ensures that the tanks are passed into the list of tanks in order from smallest to largest x coordinate values.
     */
    public void sortTanksByX() {
        List<TankSprite> sortedTanks = tanks.stream()
                .sorted(Comparator.comparingInt(TankSprite::getX))
                .collect(Collectors.toList());
        this.tanks = sortedTanks;
    }
    /**
     * This method initialises MapCell, make sure each pixel have corresponding attribution.
     */
    public void initializeMapCells() {
        mapCells = new MapCell[amplifiedTerrain.length][amplifiedTerrain[0].length];
        for (int i = 0; i < amplifiedTerrain.length; i++) {
            for (int j = 0; j < amplifiedTerrain[0].length; j++) {
                int cellData = amplifiedTerrain[i][j];
                int terrainType;
                if (cellData >= 10 && cellData <= 29) {  // Player ID Range for Tanks
                    terrainType = 0;
                    int playerId = cellData - 10;  // Converts values in an array to indexes 0-19
                    String colorKey = playerId < 10 ? String.valueOf((char)('A' + playerId)) : String.valueOf(playerId - 10);
                    Color color = getPlayerColor(colorKey);
                    System.out.println("Color: " + color);
                    TankSprite tank = new TankSprite(color, playerId, j, i);
                    tanks.add(tank);
                    mapCells[i][j] = new MapCell(j, i, terrainType, trees, foregroundColor);
                    mapCells[i][j].setTankSprite(tank);
                } else {
                    terrainType = cellData;
                    mapCells[i][j] = new MapCell(j, i, terrainType, trees, foregroundColor);
                }
            }
        }
        sortTanksByX(); // Sort tanks by their x-coordinate
        // Create a turret for each of the sorted tanks
        for (TankSprite tank : tanks) {
            tankTurret turret = new tankTurret(tank, 15);
            turrets.add(turret);
        }
        for (TankSprite tank : tanks) {
            System.out.println("Tank playerId: " + tank.getPlayerId() + ", x: " + tank.getX() + ", y: " + tank.getY());
        }
        // Initialize solid terrain, i.e. pixels below the surface of the terrain
        for (int x = 0; x < mapCells[0].length; x++){
            boolean foundTerrain = false;
            for (int y = 0; y < mapCells.length; y++){
                if (mapCells[y][x].getTerrainType() == 1){// 1 stands for terrain surface
                    foundTerrain = true;
                    mapCells[y][x].setSolid(true);// Starting from this point, mark this point and all points below it in the current column as terrain
                    continue;
                }
                if (foundTerrain) {
                    mapCells[y][x].setSolid(true);  // Mark all locations under this position as terrain blocks
                }
            }
        }
        if (mapCells[367][64].getTerrainType() == 1)
            System.out.println("368 64 is terrain!");
    }
    /**
     * This method can get player colour.
     * @param playerId stands for the id of tank.
     * @return The parsed colour
     */
    public Color getPlayerColor(String playerId) {
        JSONObject playerColors = config.getJSONObject("player_colours");
        String playerColorStr = playerColors.getString(playerId);
        return ColourParser.parseColor(playerColorStr);
    }
    /**
     * Used for testing.
     * @return tanks list
     */
    public List<TankSprite> getTestTank() {
        if (!tanks.isEmpty()) {
            return tanks; // This tank will be used for JUnit testing
        }
        return null;
    }
    /**
     * Used for testing.
     * @return  turrets list
     */
    public List<tankTurret> getTestTurret() {
        if (!turrets.isEmpty()) {
            return turrets; // This turret will be used for JUnit testing
        }
        return null;
    }

    /**
     * Used for testing.
     * @return largeShellActivated map
     */
    public Map<TankSprite, Boolean> getLargeShellActivated() {
        return largeShellActivated;
    }

    public static void main(String[] args) {
        PApplet.main("Tanks.App");

    }

}