package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

public class ChessGame {

    @Expose
    private ChessBoard board;
    @Expose
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

        List<ChessMove> validMoves = new ArrayList<>();
        Collection<ChessMove> potentialMoves = piece.pieceMoves(board, startPosition);

        for (ChessMove move : potentialMoves) {
            if (!wouldStillBeInCheck(piece.getTeamColor(), move)) {
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

        ChessPiece newPiece = (piece.getPieceType() == ChessPiece.PieceType.PAWN && move.getPromotionPiece() != null)
                ? new ChessPiece(piece.getTeamColor(), move.getPromotionPiece())
                : piece;

        board.addPiece(move.getEndPosition(), newPiece);
        board.addPiece(move.getStartPosition(), null);

        currentTurn = (currentTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    public boolean isInCheck(ChessGame.TeamColor teamColor) {
        ChessPosition kingPosition = findKingPosition(teamColor);
        if (kingPosition == null) {
            throw new IllegalStateException("King not found on the board.");
        }
        return isThreatened(kingPosition, teamColor);
    }

    private boolean isThreatened(ChessPosition position, TeamColor teamColor) {
        return getAllEnemyMoves(teamColor).stream().anyMatch(move -> move.getEndPosition().equals(position));
    }

    private Collection<ChessMove> getAllEnemyMoves(TeamColor teamColor) {
        List<ChessMove> allMoves = new ArrayList<>();
        iterateBoard((pos, piece) -> {
            if (piece != null && piece.getTeamColor() != teamColor) {
                allMoves.addAll(piece.pieceMoves(board, pos));
            }
        });
        return allMoves;
    }

    private ChessPosition findKingPosition(TeamColor teamColor) {
        return findPiecePosition(teamColor, ChessPiece.PieceType.KING);
    }

    private ChessPosition findPiecePosition(TeamColor teamColor, ChessPiece.PieceType type) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getPieceType() == type && piece.getTeamColor() == teamColor) {
                    return position;
                }
            }
        }
        return null;
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && !canEscapeCheck(teamColor);
    }

    private boolean canEscapeCheck(TeamColor teamColor) {
        return getAllMovesForTeam(teamColor).stream().anyMatch(move -> !wouldStillBeInCheck(teamColor, move));
    }

    private Collection<ChessMove> getAllMovesForTeam(TeamColor teamColor) {
        List<ChessMove> allMoves = new ArrayList<>();
        iterateBoard((pos, piece) -> {
            if (piece != null && piece.getTeamColor() == teamColor) {
                allMoves.addAll(validMoves(pos));
            }
        });
        return allMoves;
    }

    private boolean wouldStillBeInCheck(TeamColor teamColor, ChessMove move) {
        ChessBoard tempBoard = new ChessBoard(board);
        ChessPiece piece = board.getPiece(move.getStartPosition());

        tempBoard.addPiece(move.getEndPosition(), piece);
        tempBoard.addPiece(move.getStartPosition(), null);

        ChessGame tempGame = new ChessGame();
        tempGame.setBoard(tempBoard);
        tempGame.setTeamTurn(teamColor);

        return tempGame.isInCheck(teamColor);
    }

    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && getAllMovesForTeam(teamColor).isEmpty();
    }

    private void iterateBoard(BoardIterator iterator) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                iterator.process(position, board.getPiece(position));
            }
        }
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

    @FunctionalInterface
    private interface BoardIterator {
        void process(ChessPosition pos, ChessPiece piece);
    }

    private boolean gameOver = false;
    public void setGameOver(boolean over) { this.gameOver = over; }
    public boolean isGameOver() { return gameOver; }

}
