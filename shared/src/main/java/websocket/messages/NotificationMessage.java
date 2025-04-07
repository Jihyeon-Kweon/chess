package websocket.messages;

import com.google.gson.annotations.Expose;

public class NotificationMessage extends ServerMessage {
    @Expose
    private final String message;

    public NotificationMessage(String message) {
        super(ServerMessageType.NOTIFICATION);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
