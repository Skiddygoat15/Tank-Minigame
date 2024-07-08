package Tanks;

import java.util.Arrays;
import java.util.List;

/**
 * This class mainly used for terrain correction.
 */
public class terrainRectifier {
    /**
     * This method fills original terrain array (terrainMatrix) for subsequent terrain handle process.
     * @param terrainMatrix Initialised terrainMatrix to be processed
     * @param lineSize Initial lineSize
     * @param lines Original lines list, loaded from txt terrain file
     * @return Processed terrainMatrix
     */
    public static int [][] fillOriginalTerrainMatrix(int [][] terrainMatrix, int lineSize, List<String> lines){
        for (int i = 0; i < lineSize; i++) {
            for (int j = 0; j < lines.get(i).length(); j++) {
                char c = lines.get(i).charAt(j);
                if (c == 'X') {
                    terrainMatrix[i][j] = 1;//1 stands for terrain height
                }
                if (c == 'T'){
                    terrainMatrix[i][j] = 2;//2 stands for trees
                }
                if (c == 'A'){
                    terrainMatrix[i][j] = 10;//1 stands for User A
                }
                if (c == 'B'){
                    terrainMatrix[i][j] = 11;//1 stands for User B
                }
                if (c == 'C'){
                    terrainMatrix[i][j] = 12;//1 stands for User C
                }
                if (c == 'D'){
                    terrainMatrix[i][j] = 13;//So forth....
                }
                if (c == 'E'){
                    terrainMatrix[i][j] = 14;
                }
                if (c == 'F'){
                    terrainMatrix[i][j] = 15;
                }
                if (c == 'G'){
                    terrainMatrix[i][j] = 16;
                }
                if (c == 'H'){
                    terrainMatrix[i][j] = 17;
                }
                if (c == 'I'){
                    terrainMatrix[i][j] = 18;
                }
                if (c == '0'){
                    terrainMatrix[i][j] = 20;
                }
                if (c == '1'){
                    terrainMatrix[i][j] = 21;
                }
                if (c == '2'){
                    terrainMatrix[i][j] = 22;
                }
                if (c == '3'){
                    terrainMatrix[i][j] = 23;
                }
                if (c == '4'){
                    terrainMatrix[i][j] = 24;
                }
                if (c == '5'){
                    terrainMatrix[i][j] = 25;
                }
                if (c == '6'){
                    terrainMatrix[i][j] = 26;
                }
                if (c == '7'){
                    terrainMatrix[i][j] = 27;
                }
                if (c == '8'){
                    terrainMatrix[i][j] = 28;
                }
                if (c == '9'){
                    terrainMatrix[i][j] = 29;
                }
            }
        }
        System.out.println(Arrays.deepToString(terrainMatrix));
        return terrainMatrix;
    }
    /**
     * This method amplifies terrainMatrix array, so that it can be used for moving average.
     * @param terrainMatrix Processed terrainMatrix
     * @param amplifiedTerrain Initialised amplifiedTerrain array to be processed
     * @param lineSize New lineSize
     * @param lines Original lines list, loaded from txt terrain file
     * @return Processed amplifiedTerrain array
     */
    public static int [][] terrainAmplifier(int [][] terrainMatrix, int [][] amplifiedTerrain, int lineSize, List<String> lines){
        for (int i = 0; i < lineSize; i++) {
            for (int j = 0; j < lines.get(i).length(); j++) {
                int topRowIndex = i * 32;
                if (terrainMatrix[i][j] == 1) {
                    for (int ampli_j = j * 32; ampli_j < (j + 1) * 32; ampli_j++) {
                        amplifiedTerrain[topRowIndex][ampli_j] = 1;
                    }
                }
            }
        }
        return amplifiedTerrain;
    }
    /**
     * This method can be used to assign new tree or tank position, but the assigned positions are not precise, need to be further processed.
     * @param terrainMatrix Processed terrainMatrix
     * @param newAmplifiedTrees Initialised newAmplifiedTrees array to be processed, its name sounds like it's used for trees, but it actually can be used for tanks also
     * @param division This double value affects the new position of tanks and trees, setting this field means some positions may need to be adjusted manually
     * @param index This is only used for loop, nothing else
     * @param indexFor same as index
     * @param X This is the initialised array for tanks or trees X coordinates, all elements in this array will have a corresponding Y in Y array after processing, say X[1] corresponds to second trees X value, and Y[1] corresponds to second trees Y value
     * @param Y Same as X array
     * @param identity This indicates which entity we need to assign, in this case it means either tree or tank
     * @param temporay This is a temporary value, setting this is to avoid original values get override
     * @return Processed newAmplifiedTrees array
     */
    public static int[][] newPosition(double division, int[][] terrainMatrix, int[][] newAmplifiedTrees, int index, int indexFor, double [] X, double [] Y, int temporay , int identity){
        int treenum1 = 0;
        for (int i = 0; i < terrainMatrix.length; i++){
            for (int j = 0; j < terrainMatrix[0].length; j++){
                if (terrainMatrix[i][j] == identity)
                    treenum1++;
            }
        }
        System.out.println("treesnum before positioned: " + treenum1);

        for (int i = 0; i < terrainMatrix.length; i++){
            for (int j = 0; j < terrainMatrix[0].length; j++){
                if (terrainMatrix[i][j] == identity){
                    if (index - 1 >= 0 && ((double) j /division)*864 == X[index - 1])
                        X[index] = ((double) j /division)*864 + 1;
                    else if (((double) j /division)*864 == 0.0)
                        X[index] = ((double) j /division)*864 + 9;
                    else
                        X[index] = ((double) j /division)*864;
                    System.out.println("X: " + X[index]);
                    if (index - 1 >= 0 && ((double)i / 20.0) *640 == Y[index - 1])
                        Y[index] = ((double)i / 20.0) *640 + 1;
                    else if (((double)i / 20.0) *640 == 0.0)
                        Y[index] = ((double)i / 20.0) *640 + 0.1;
                    else
                        Y[index] = ((double)i / 20.0) *640;
                    System.out.println("Y: " + Y[index]);
                    System.out.println("________________________");
                    index++;
                }

            }
        }
        int treeCoordsX = 0;
        int treeCoordsY = 0;
        for (int k = 0; k < X.length; k++){
            if (X[k] != 0.0)
                treeCoordsX++;
        }
        for (int k = 0; k < Y.length; k++){
            if (Y[k] != 0.0)
                treeCoordsY++;
        }
        System.out.println("tressX have " + treeCoordsX + "values");
        System.out.println(Arrays.toString(X));
        System.out.println("tressY have " + treeCoordsY + "values");
        while(X[indexFor] != 0.0){
            System.out.println("Y: " + (int) Y[indexFor]);
            System.out.println("X: " + (int) X[indexFor]);
            newAmplifiedTrees[(int) Y[indexFor]][(int) X[indexFor]] = temporay;
            indexFor++;
        }
        int treenum = 0;
        for (int i = 0; i < newAmplifiedTrees.length; i++){
            for (int j = 0; j < newAmplifiedTrees[0].length; j++){
                if (newAmplifiedTrees[i][j] == temporay)
                    treenum++;
            }
        }
        System.out.println("treesnum After positioned: " + treenum);
        return newAmplifiedTrees;
    }
    /**
     * This method will precisely assign tanks or trees position onto terrain surface.
     * @param a Initialised array to be processed
     * @param identity This indicates which entity we need to assign, in this case it means either tree or tank
     * @param attachTo Indicates which terrain type trees or tanks need to be on top of, in this case it means terrain surface
     * @param temporary This is a temporary value, setting this is to avoid original values get override
     * @return Processed array
     */
    public static int[][] positionRectifier(int[][] a, int identity, int attachTo, int temporary){
        for (int l = 0; l < a.length; l++) {
            for (int p = 0; p < a[0].length; p++) {
                if (a[l][p] == temporary) {
                    boolean found = false;

                    // Scan below
                    for (int z = 1; z < a.length; z++) {
                        if (l + z < a.length && a[l + z][p] == attachTo) {
                            if (l + z - 2 >= 0) {
                                a[l + z - 2][p] = identity;
                                a[l][p] = 0;
                                System.out.println("Terrain pixel found! Below");
                                found = true;
                                break;
                            }
                        }
                    }

                    // If no terrain pixel found, scan above
                    if (!found) {
                        for (int g = 1; g < a.length; g++) {
                            if (l - g >= 0 && a[l - g][p] == attachTo) {
                                if (l - g - 2 >= 0) {
                                    a[l - g - 2][p] = identity;
                                    a[l][p] = 0;
                                    System.out.println("Terrain pixel found! Above");
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return a;
    }
    /**
     * This method will implement moving average with 32 steps.
     * @param a Array to be processed
     * @param b Array that has been processed
     * @return Processed moving average array
     */
    public static int[][] MovingAverage(int [][] a, int [][] b){
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                if (a[i][j] == 1) { // found a terrain
                    int sumRowIndex = i;
                    int count = 1;
                    boolean isValid = true;

                    for (int k = 1; k <= 31; k++) {
                        if (j + k < a[i].length) {
                            if (a[i][j + k] == 1) {
                                sumRowIndex += i;
                                count++;
                            } else {
                                boolean found = false;
                                for (int m = 1; m < a.length && !found; m++) {
                                    if (i + m < a.length && a[i + m][j + k] == 1) {
                                        sumRowIndex += (i + m);
                                        count++;
                                        found = true;
                                    }
                                    if (i - m >= 0 && a[i - m][j + k] == 1) {
                                        sumRowIndex += (i - m);
                                        count++;
                                        found = true;
                                    }
                                }
                                if (!found) {
                                    isValid = false;
                                    break;
                                }
                            }
                        } else {
                            break;
                        }
                    }

                    if (isValid && count > 1) {
                        int averageRowIndex = sumRowIndex / count;  // Calculate the average row index
                        if (averageRowIndex < a.length) {
                            b[i][j] = 0;  // Clear original position
                            b[averageRowIndex][j] = 1;
                        }
                    }
                }
            }
        }
        return b;
    }
}
