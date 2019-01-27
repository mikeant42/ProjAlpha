package shared;


import com.almasb.fxgl.core.math.Vec2;

public class Data {

    static public class Input {
        public boolean UP = false;
        public boolean DOWN = false;
        public boolean LEFT = false;
        public boolean RIGHT = false;
    }

    static public class State {
        public State() {}
        public Vec2 position;
        public Vec2 velocity;
    }
}