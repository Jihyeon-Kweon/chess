package websocket.commands;

import chess.ChessMove;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MakeMoveCommand extends UserGameCommand {

    @Expose
    @SerializedName("move")
    private ChessMove move;

    public MakeMoveCommand(){
        super(CommandType.MAKE_MOVE, null, null);
    }

    public MakeMoveCommand(CommandType commandType, String authToken, Integer gameID, ChessMove move) {
        super(commandType, authToken, gameID);
        this.move = move;
    }

    public ChessMove getMove() {
        return move;
    }
}
