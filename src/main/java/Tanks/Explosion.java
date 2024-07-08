package Tanks;

import processing.core.PApplet;

import java.util.List;
import java.util.Map;
/**
 * Implementation of explosion related logic
 */
class Explosion {
    /**
     * Location of the center of the explosion.
     */
    double x, y;
    /**
     * Explosion duration in seconds.
     */
    float duration = 0.2f;
    /**
     * Time elapsed since the explosion.
     */
    float elapsedTime = 0;
    /**
     * Explosion radius.
     */
    float maxRadius;
    /**
     * Corresponding shell.
     */
    Shell shell;

    public Explosion(double x, double y, float maxRadius, Shell shell) {
        this.x = x;
        this.y = y;
        this.maxRadius = maxRadius;
        this.shell = shell;
    }
    public float getMaxRadius() {
        return maxRadius;
    }

    public void setShell(Shell shell){
        this.shell = shell;
    }

    public Shell getShell() {
        return this.shell;
    }
    /**
     * Apply Explosion impact to the terrain.
     * @param centerX The center X of explosion
     * @param centerY The center Y of explosion
     * @param radius The explosion radius
     * @param mapCells Contains map data
     */
    public void applyExplosionImpact(double centerX, double centerY, float radius, MapCell[][] mapCells) {
        int startX = Math.max(0, (int) (centerX - radius));
        int endX = Math.min(mapCells[0].length - 1, (int) (centerX + radius));
        int startY = Math.max(0, (int) (centerY - radius));
        int endY = Math.min(mapCells.length - 1, (int) (centerY + radius));

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                double distance = Math.sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY));
                if (distance <= radius) {
                    mapCells[y][x].setSolid(false);  // Set isSolid to false to simulate destruction
                }
            }
        }
        // Update solid block, make sure terrain will not float
        for (int x = 0; x < mapCells[0].length; x++){
            for (int y = mapCells.length - 1 ; y >= 0; y--){
                if (mapCells[y][x].isSolid() == true){
                    if (mapCells[y - 1][x].isSolid() == false){
                        for (int m = y - 2; m >= 0; m--){
                            if (mapCells[m][x].isSolid() == true){
                                mapCells[y - 1][x].setSolid(true);
                                mapCells[m][x].setSolid(false);
                                break;
                            }
                        }
                    }
                }
            }
        }
        // Double check solid block, make sure terrain will not float
        for (int x = 0; x < mapCells[0].length; x++){
            for (int y = 0 ; y < mapCells.length; y++){
                if (mapCells[y][x].isSolid() == true){
                    if (y + 64 < mapCells.length && mapCells[y + 64][x].isSolid() == false){
                        mapCells[y][x].setSolid(false);
                    }
                }
            }
        }
        // Clear original terrain
        for (int x = 0; x < mapCells[0].length; x++){
            for (int y = 0; y < mapCells.length; y++){
                if (mapCells[y][x].getTerrainType() == 1){
                    mapCells[y][x].setTerrainType(0);
                }
            }
        }
        // Update new terrain
        for (int x = 0; x < mapCells[0].length; x++){
            for (int y = 0; y < mapCells.length; y++){
                if (mapCells[y][x].isSolid() == true){
                    mapCells[y][x].setTerrainType(1);
                    break;
                }
            }
        }
        // Update tree position
        for (int x = 0; x < mapCells[0].length; x++){
            for (int y = 0; y < mapCells.length; y++){
                if (mapCells[y][x].getTerrainType() == 2){
                    for (int m = 1; y + m < mapCells.length; m++){
                        if (mapCells[y + m][x].getTerrainType() == 1){
                            mapCells[y][x].setTerrainType(0);
                            mapCells[y + m - 2][x].setTerrainType(2);
                            break;
                        }
                    }
                }
            }
        }
        for (int x = 0; x < mapCells[0].length; x++){
            for (int y = 0; y < mapCells.length; y++){
                if (mapCells[y][x].getTerrainType() == 2 && y + 5 < mapCells.length && !mapCells[y + 5][x].isSolid()){
                    mapCells[y][x].setTerrainType(0);
                }
            }
        }
    }
    /**
     * Apply Explosion impact to the terrain.
     * @param centerX The center X of explosion
     * @param centerY The center Y of explosion
     * @param radius The explosion radius
     * @param mapCells Contains map data
     * @param shell Indicates which shell caused damage
     */
    public static void applyExplosionDamage(double centerX, double centerY, float radius, MapCell[][] mapCells, Shell shell) {
        int startX = Math.max(0, (int) (centerX - radius));
        int endX = Math.min(mapCells[0].length - 1, (int) (centerX + radius));
        int startY = Math.max(0, (int) (centerY - radius));
        int endY = Math.min(mapCells.length - 1, (int) (centerY + radius));

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                double distance = Math.sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY));
                if (distance <= radius && distance > 7) {
                    int damage = calculateDamage(distance, radius);
                    TankSprite tank = mapCells[y][x].getSprite();
                    if (tank != null) {
                        int newHealth = Math.max(tank.getHealth() - damage, 0);  // Ensure health does not go below zero
                        int previousHealth = tank.getHealth();
                        tank.setHealth(newHealth);
                        System.out.println("newHealth: " + newHealth);
                        if (!tank.equals(shell.getTank())){
                            System.out.println("Previous Health: " + previousHealth + "Who's getting damaged: " + tank.getPlayerId());
                            shell.getTank().addScore(previousHealth - newHealth);
                        }
                    }
                } else if (distance >= 0 && distance <= 7) {
                    TankSprite tank = mapCells[y][x].getSprite();
                    if (tank != null){
                        int newHealth = Math.max(tank.getHealth() - 40, 0);
                        int previousHealth1 = tank.getHealth();
                        tank.setHealth(newHealth);
                        System.out.println("newHealth: " + newHealth);
                        if (!tank.equals(shell.getTank())){
                            System.out.println("Previous Health: " + previousHealth1 + "Who's getting damaged: " + tank.getPlayerId());
                            shell.getTank().addScore(previousHealth1 - newHealth);
                        }
                    }
                }
            }
        }
    }
    // Calculates distance-based damage, which decreases linearly with distance
    /**
     * Calculate explosion damage.
     * @param distance Whoever distance to center explosion position
     * @param maxDistance Indicates the max distance with corresponding radius
     */
    private static int calculateDamage(double distance, double maxDistance) {
        // When the distance is maxDistance, the damage is 1, when the distance is 0, the damage is 40
        if (distance >= maxDistance) return 0; // Ensures no damage when out of range
        return (int) (41 - (distance / maxDistance) * 40); // Damage decreases with distance
    }
    // Updated tank drop animation
    /**
     * Check if a tank should fall.
     * @param tanks The tanks list, used for looping detection of which tank should fall
     * @param mapCells Used for looping detection of which tank should fall
     * @param explosionSources With this parameter the program can pass this map to startTankDescentAnimation
     */
    public static void checkAndAnimateTanks(List<TankSprite> tanks, MapCell[][] mapCells, Map<Explosion, Shell> explosionSources) {
        for (TankSprite tank : tanks) {
            int x = tank.getX();
            int y = tank.getY();
            int initialY = tank.getY();

            // 检查坦克下方两个像素是否为空
            if (y + 2 < mapCells.length && !mapCells[y + 2][x].isSolid()) {
                // 搜索下方最近的地形表面
                int newY = y + 2;
                while (newY + 2 < mapCells.length && !mapCells[newY][x].isSolid()) {
                    newY++;
                }
                if (newY > 645)
                    newY = 645;
                // 启动下降动画
                startTankDescentAnimation(tank, newY, initialY, explosionSources);
            }
        }
    }
    /**
     * Start falling animation.
     * @param tank The tank that should start falling animation
     * @param targetY Indicates which y value this tank should fall onto
     * @param initialY Indicates the initial y value when tank starts to fall
     * @param explosionSources Indicates which shell caused this tank's falling action, so that the game can know which tank should get added score
     */
    public static void startTankDescentAnimation(TankSprite tank, int targetY, int initialY, Map<Explosion, Shell> explosionSources) {
        int newInitialY = 0;
        if (!tank.isAnimating()) {  // Set the initial Y value only when the animation has not started.
            tank.setInitialFallY(initialY);  // Setting the initial drop Y value
            newInitialY = tank.getInitialFallY();
            System.out.println("Target Y and InitialFallY:" + targetY + " "+ initialY);
            tank.setAnimating(true);  // Start animation
            for (Map.Entry<Explosion, Shell> entry : explosionSources.entrySet()) {
                Explosion explosion = entry.getKey();
                Shell shell = entry.getValue();
                if (explosion.getShell() == shell && shell.getTank() != tank && tank.getParachutes() == 0) {
                    tank.setCausingFallShell(shell);
                    break;
                }
            }
        }
        tank.setTargetY(targetY);  // Set the target Y coordinate, this can be updated if the animation has already started

    }
    // Update explosion animation
    /**
     * Update explosion animation.
     * @param deltaTime Fixed time increment
     * @param mapCells This can be passed to applyExplosionImpact method
     * @return Whether animation should continue
     */
    public boolean update(float deltaTime, MapCell[][] mapCells) {
        elapsedTime += deltaTime;
        if (elapsedTime > duration) {
            return false; // Returns false if the animation ends
        }
        if (elapsedTime == deltaTime) {  // Application explosion impact on first update
            applyExplosionImpact(x, y, maxRadius, mapCells);
        }
        return true;
    }

    /**
     * Draw the explosion.
     * @param app App class instance
     */
    public void draw(PApplet app) {
        float progress = elapsedTime / duration;
        float radius1 = maxRadius * progress;
        float radius2 = maxRadius * 0.5f * progress;
        float radius3 = maxRadius * 0.2f * progress;

        app.stroke(255, 0, 0);
        app.fill(255, 0, 0);
        app.ellipse((float) x, (float) y, radius1 * 2, radius1 * 2);
        app.stroke(255, 165, 0);
        app.fill(255, 165, 0);
        app.ellipse((float) x, (float) y, radius2 * 2, radius2 * 2);
        app.stroke(255, 255, 0);
        app.fill(255, 255, 0);
        app.ellipse((float) x, (float) y, radius3 * 2, radius3 * 2);
    }
}
