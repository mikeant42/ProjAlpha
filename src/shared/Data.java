package shared;


import com.almasb.fxgl.core.math.Vec2;

public class Data {

    static public class MovementState {
        public static int STANDING           = 0;
        public static int RUNNING_FORWARD    = 1;
        public static int RUNNING_RIGHT      = 2;
        public static int RUNNING_LEFT       = 3;
        public static int RUNNING_BACK       = 4;
    }

    static public class PlayerConstants {
        public static int MAX_HEALTH = 100;
        public static int MAX_MANA   = 100;
    }

    static public class Shield {
        public static int NONE    = -1;
        public static int GRAVITY = 0;
    }

    static public class AlphaGameState {
        public static int RESTORING   = 0;
        public static int FIGHTING    = 1;

        public static String stateToString(int state) {
            String val = "";
            switch (state) {
                case 0:
                    val = "RESTORING";
                    break;
                case 1:
                    val = "FIGHTING";
                    break;
            }
            return val;
        }
    }

}