package websocket.messages;

import chess.ChessGame;
import com.google.gson.annotations.Expose;

public class LoadGameMessage extends ServerMessage {

    @Expose
    private final ChessGame game;

    public LoadGameMessage(ChessGame game) {
        super(ServerMessageType.LOAD_GAME);
        this.game = game;
    }

    public ChessGame getGame() {
        return game;
    }
}
