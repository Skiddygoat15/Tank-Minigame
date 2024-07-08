package Tanks;
/**
 * This class can get tank's playerId and its corresponding display character.
 */
public class CheckPlayers {

    public static int [] getPlayers(int [][] terrainMatrix) {
        int [] Players = new int[20];
        int index = 0;
        for (int i = 0; i < terrainMatrix.length; i++){
            for (int j = 0; j < terrainMatrix[0].length; j++){
                if (terrainMatrix[i][j] == 10) {
                    Players[index] = 10;
                    index++;
                }
                if (terrainMatrix[i][j] == 11){
                    Players[index] = 11;
                    index++;
                }
                if (terrainMatrix[i][j] == 12){
                    Players[index] = 12;
                    index++;
                }
                if (terrainMatrix[i][j] == 13){
                    Players[index] = 13;
                    index++;
                }
                if (terrainMatrix[i][j] == 14){
                    Players[index] = 14;
                    index++;
                }
                if (terrainMatrix[i][j] == 15){
                    Players[index] = 15;
                    index++;
                }
                if (terrainMatrix[i][j] == 16){
                    Players[index] = 16;
                    index++;
                }
                if (terrainMatrix[i][j] == 17){
                    Players[index] = 17;
                    index++;
                }
                if (terrainMatrix[i][j] == 18){
                    Players[index] = 18;
                    index++;
                }
                if (terrainMatrix[i][j] == 20){
                    Players[index] = 20;
                    index++;
                }
                if (terrainMatrix[i][j] == 21){
                    Players[index] = 21;
                    index++;
                }
                if (terrainMatrix[i][j] == 22){
                    Players[index] = 22;
                    index++;
                }
                if (terrainMatrix[i][j] == 23){
                    Players[index] = 23;
                    index++;
                }
                if (terrainMatrix[i][j] == 24){
                    Players[index] = 24;
                    index++;
                }
                if (terrainMatrix[i][j] == 25){
                    Players[index] = 25;
                    index++;
                }
                if (terrainMatrix[i][j] == 26){
                    Players[index] = 26;
                    index++;
                }
                if (terrainMatrix[i][j] == 27){
                    Players[index] = 27;
                    index++;
                }
                if (terrainMatrix[i][j] == 28){
                    Players[index] = 28;
                    index++;
                }
                if (terrainMatrix[i][j] == 29){
                    Players[index] = 29;
                    index++;
                }

            }
        }
        return Players;
    }
    public static String playerIdToDisplay(int playerId) {
        if (playerId >= 0 && playerId <= 8) {
            // Convert 0-8 to A-I
            return String.valueOf((char)('A' + playerId));
        } else if (playerId >= 20 && playerId <= 29) {
            // Convert 20-29 to 0-9
            return String.valueOf(playerId - 20);
        }
        return "Invalid ID";
    }
}
