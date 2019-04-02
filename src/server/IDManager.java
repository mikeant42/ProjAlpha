package server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class IDManager {
    private static int objectLimit = 100000; // leave room for projectiles, who's uid gets de-allocated when finished
    private static List<Integer> uniques;

    public static void init() {
        uniques = new ArrayList<>();
    }

    public static void deAllocateId(int id) {
        synchronized (uniques) {
            uniques.remove(new Integer(id));
        }
    }

    // IT ASSIGNED DUPLICATES!!!!!!!!!!!!!!!!!!!!!!!!!!!!! FIX FIX
    // if you gen too many ids this will cause a stackoverflow
    public static int assignUniqueId() {
        synchronized (uniques) {
            int num = ThreadLocalRandom.current().nextInt(AlphaServer.PLAYER_COUNT, objectLimit + 1);

            if (uniques.contains(num)) {
                num = assignUniqueId();
            } else {
                uniques.add(num);
            }

            return num;
        }
    }
}
