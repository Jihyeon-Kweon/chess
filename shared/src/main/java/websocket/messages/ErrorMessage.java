package websocket.messages;

import com.google.gson.annotations.Expose;

public class ErrorMessage extends ServerMessage {
    @Expose
    private final String errorMessage;

    public ErrorMessage(String errorMessage) {
        super(ServerMessageType.ERROR);
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
