package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor currentTurn;

    // Constructor
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

        List<ChessMove> validMoves = new ArrayList<>();
        Collection<ChessMove> potentialMoves = piece.pieceMoves(board, startPosition);

        for (ChessMove move : potentialMoves) {
            ChessBoard tempBoard = new ChessBoard(board);
            tempBoard.addPiece(move.getEndPosition(), piece);
            tempBoard.addPiece(move.getStartPosition(), null);

            ChessGame tempGame = new ChessGame();
            tempGame.setBoard(tempBoard);
            if (!tempGame.isInCheck(piece.getTeamColor())) {
                validMoves.add(move);
            }
        }

        return validMoves;
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());

        if (piece == null || piece.getTeamColor() != currentTurn) {
            throw new InvalidMoveException("Invalid.");
        }

        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());
        if (!validMoves.contains(move)) {
            throw new InvalidMoveException("Invalid.");
        }

        ChessPiece newPiece = piece;
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN && move.getPromotionPiece() != null) {
            newPiece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
        }

        board.addPiece(move.getEndPosition(), newPiece);
        board.addPiece(move.getStartPosition(), null);

        currentTurn = (currentTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    public boolean isInCheck(ChessGame.TeamColor teamColor) {
        ChessPosition kingPosition = findKingPosition(teamColor);

        if (kingPosition == null) {
            throw new IllegalStateException("King not found on the board.");
        }

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() != teamColor) {
                    for (ChessMove move : piece.pieceMoves(board, position)) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private ChessPosition findKingPosition(TeamColor teamColor) {
        for (int row = 1; row <= 8; row ++){
            for (int col = 1; col <= 8; col++){
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor){
                    return position;
                }
            }
        }
        return null;
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)){
            return false;
        }

        for (int row =1; row<=8;row++){
            for (int col =1;col<=8;col++){
                ChessPosition position = new ChessPosition(row,col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor()==teamColor){
                    for (ChessMove move: validMoves(position)){

                        ChessBoard tempBoard = new ChessBoard(board);
                        tempBoard.addPiece(move.getEndPosition(), piece);
                        tempBoard.addPiece(move.getStartPosition(), null);

                        ChessGame tempGame = new ChessGame();
                        tempGame.setBoard(tempBoard);
                        tempGame.setTeamTurn(teamColor);

                        if (!tempGame.isInCheck(teamColor)){
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)){
            return false;
        }

        for (int row=1; row<=8;row++){
            for (int col=1;col<=8;col++){
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if(piece != null && piece.getTeamColor()==teamColor){
                    Collection<ChessMove> validMoves = validMoves(position);

                    if (validMoves != null && !validMoves.isEmpty()){
                        return false;
                    }
                }
            }
        }
        return true;
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
