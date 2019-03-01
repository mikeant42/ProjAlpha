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
}