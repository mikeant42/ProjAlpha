package server;



public class Message {
    private int id;
    private Object content;
    private boolean wait;
    private boolean sendToAll;
    private int excludeID = 0;


    public Message(Object content, boolean wait) {
        this(0, content, wait, true);
    }

    public Message(int id, Object content, boolean wait) {
        this(id, content, wait, false);
    }

    public Message(int id, Object content, boolean wait, boolean sendToAll) {
        this.id = id;
        this.content = content;
        this.wait = wait;
        this.sendToAll = sendToAll;
        this.excludeID = 0;
    }

    // bug - sendtoallexcept does not work
    // solution - make a ExcludingMessage class extending Message, modify alpha messaging queue
    public void send(AlphaServer server) {
        if (sendToAll) {
            if (excludeID != 0) {
                server.sendToAllExceptTCP(excludeID, content);
            } else {
                server.sendToAllReady(content, wait);
            }
        } else {
            server.sendWithQueue(id, content, wait);
        }
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
