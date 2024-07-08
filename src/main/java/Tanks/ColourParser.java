package Tanks;

import processing.data.JSONObject;

import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static Tanks.App.random;
/**
 * This class can parse tank's colour from JSON Objects or Strings.
 */
public class ColourParser {
    public static String[][] parsePlayerColours(JSONObject playerColours) {
        String[] keys = new String[playerColours.size()];
        String[] colours = new String[playerColours.size()];
        int index = 0;

        for (Object key : playerColours.keys()) {
            keys[index] = (String) key;
            colours[index] = playerColours.getString((String) key);
            index++;
        }

        return new String[][] { keys, colours };
    }
    public static Color parseColor(String colorStr) {
        if ("random".equals(colorStr)) {
            return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        } else {
            String[] rgb = colorStr.split(",");
            return new Color(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]));
        }
    }

}
