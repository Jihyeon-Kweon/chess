package chess;

import java.util.Collection;
import java.util.HashSet;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor currentTurn;

    public ChessGame() {
        this.board = new ChessBoard();
        this.currentTurn = TeamColor.WHITE;
        board.resetBoard();
    }

    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    public void setTeamTurn(TeamColor team) {
        this.currentTurn = team;
    }

    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        return piece.pieceMoves(board, startPosition);
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null || piece.getTeamColor() != currentTurn) {
            throw new InvalidMoveException("Invalid move: No piece at start position or wrong turn.");
        }
        board.addPiece(move.getEndPosition(), piece);
        board.addPiece(move.getStartPosition(), null);  // Remove from start position
        currentTurn = (currentTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    public boolean isInCheck(TeamColor teamColor) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public boolean isInStalemate(TeamColor teamColor) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    public ChessBoard getBoard() {
        return board;
    }

    public enum TeamColor {
        WHITE, BLACK
    }
}
