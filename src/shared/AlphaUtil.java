package shared;

import com.almasb.fxgl.parser.tiled.TMXParser;
import com.almasb.fxgl.parser.tiled.TiledMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class AlphaUtil {

    public static TiledMap parseWorld(String file) throws FileNotFoundException {
        File initialFile = new File(file);
        InputStream targetStream = new FileInputStream(initialFile);


        TMXParser parser = new TMXParser();
        TiledMap map = parser.parse(targetStream);
//        map.getLayerByName("Treetop").setDraworder("topdown");

        return map;
    }

    public static TiledMap parseWorld(InputStream stream) {
        TMXParser parser = new TMXParser();
        TiledMap map = parser.parse(stream);
//        map.getLayerByName("Treetop").setDraworder("topdown");

        return map;
    }


    public static int ensureRange(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }



}
