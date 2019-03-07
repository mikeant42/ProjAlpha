package server;

enum MessageType {
    TO_ALL, TO_ONE
}

public class Message {
    private int id;
    private Object content;
    private boolean wait;
    private boolean sendToAll;
    private int excludeID;

    public Message(int id, Object content, boolean wait, boolean sendToAll) {
        this.id = id;
        this.content = content;
        this.wait = wait;
        this.sendToAll = sendToAll;
        this.excludeID = 0;
    }

    public int getId() {
        return id;
    }

    public Object getContent() {
        return content;
    }

    public boolean isWait() {
        return wait;
    }

    public boolean isSendToAll() {
        return sendToAll;
    }

    public int getExcludeID() {
        return excludeID;
    }

    public void setExcludeID(int excludeID) {
        this.excludeID = excludeID;
    }
}
