package shared;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import server.GameMap;

// This class is a convenient place to keep things common to both the client and server.

/**
 * There are two different types of ids
 * - The connection id
 * - The npc id assigned to a non Connection networtk entity
 */
public class Network {
    static public final int port = 54555;

    // This registers objects that are going to be sent over the network.
    // TODO - organize
    static public void register (EndPoint endPoint) {
        Kryo kryo = endPoint.getKryo();
        kryo.register(Login.class);
        kryo.register(RegistrationRequired.class);
        kryo.register(Register.class);
        kryo.register(AddCharacter.class);
        kryo.register(UpdateCharacter.class);
        kryo.register(RemoveCharacter.class);
        kryo.register(CharacterPacket.class);
        kryo.register(MoveCharacter.class);
        kryo.register(LoginSuccess.class);
        kryo.register(WorldQuery.class);
        kryo.register(EntityType.class);
        kryo.register(NPCPacket.class);
        kryo.register(UpdateNPC.class);
        kryo.register(GameObject.class);
        kryo.register(RemoveGameObject.class);
        kryo.register(Inventory.class);
        kryo.register(AddInventoryItem.class);
        kryo.register(RemoveInventoryItem.class);
        kryo.register(GameObject[].class);
        kryo.register(ObjectUseHandler.class);
    }

    static public class Login {
        public String name;
        public String pass;
    }

    static public class RegistrationRequired {
    }

    static public class Register {
        public String name;
        public String otherStuff;
    }

    static public class UpdateCharacter {
        public int id;
        public double x, y;
        public int moveState;
    }

    static public class AddCharacter {
        public CharacterPacket character;
    }

    static public class RemoveCharacter {
        public int id;
    }

    static public class MoveCharacter {
        public int x, y;
    }

    static public class LoginSuccess {
        public boolean success;
        public CharacterPacket packet;
    }

    static public class NPCPacket {
        public float x;
        public float y;
        public int id;
        public EntityType type;
        public int moveState;
    }

    static public class UpdateNPC {
        public float x;
        public float y;
        public int id;
        public int moveState;
    }

    static public class RemoveGameObject {
        public int uid;
    }

    static public class RemoveInventoryItem {
        public GameObject object;
    }

    static public class AddInventoryItem {
        public GameObject object;
    }

//    static public class GameObjectPacket {
//        public int id;
//        public int itemLevel;
//        public String name;
//        public String desc;
//        public float x,y;
//    }

    static public class WorldQuery {
        public int map;
        public int cid; // With this we can grab the info of the character connected
    }
}