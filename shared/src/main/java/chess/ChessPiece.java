package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();

        switch (type) {
            case PAWN:
                addPawnMoves(validMoves, board, myPosition);
                break;
            case ROOK:
                addRookMoves(validMoves, board, myPosition);
                break;
            case BISHOP:
                addBishopMoves(validMoves, board, myPosition);
                break;
            case QUEEN:
                addQueenMoves(validMoves, board, myPosition);
                break;
            case KNIGHT:
                addKnightMoves(validMoves, board, myPosition);
                break;
            case KING:
                addKingMoves(validMoves, board, myPosition);
                break;
        }
        return validMoves;
    }

    private void addPawnMoves(Collection<ChessMove> moves, ChessBoard board, ChessPosition pos) {
        int direction = (pieceColor == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startRow = (pieceColor == ChessGame.TeamColor.WHITE) ? 2 : 7;

        // one movement (basic)
        ChessPosition oneStep = new ChessPosition(pos.getRow() + direction, pos.getColumn());
        if (board.getPiece(oneStep) == null) {
            moves.add(new ChessMove(pos, oneStep, null));

            // move two squares if it's initial
            if (pos.getRow() == startRow) {
                ChessPosition twoStep = new ChessPosition(pos.getRow() + 2 * direction, pos.getColumn());
                if (board.getPiece(twoStep) == null) {
                    moves.add(new ChessMove(pos, twoStep, null));
                }
            }
        }

        // diagonal movement
        for (int colOffset : new int[]{-1, 1}) {
            ChessPosition attackPos = new ChessPosition(pos.getRow() + direction, pos.getColumn() + colOffset);
            ChessPiece targetPiece = board.getPiece(attackPos);
            if (targetPiece != null && targetPiece.getTeamColor() != pieceColor) {
                moves.add(new ChessMove(pos, attackPos, null));
            }
        }
    }

    private void addRookMoves(Collection<ChessMove> moves, ChessBoard board, ChessPosition pos) {
        int[] directions = {1, -1};

        // 가로, 세로 방향 확인
        for (int dir : directions) {
            for (int i = 1; i <= 8; i++) {
                ChessPosition newPos = new ChessPosition(pos.getRow() + i * dir, pos.getColumn());
                if (!isValidPosition(newPos)) break;

                ChessPiece piece = board.getPiece(newPos);
                if (piece == null) {
                    moves.add(new ChessMove(pos, newPos, null));
                } else {
                    if (piece.getTeamColor() != pieceColor) {
                        moves.add(new ChessMove(pos, newPos, null));
                    }
                    break;  // 같은 팀 말이 있으면 이동 불가
                }
            }
        }
    }

    private void addBishopMoves(Collection<ChessMove> moves, ChessBoard board, ChessPosition pos) {
        int[] directions = {1, -1};

        for (int rowDir : directions) {
            for (int colDir : directions) {
                for (int i = 1; i <= 8; i++) {
                    int newRow = pos.getRow() + i * rowDir;
                    int newCol = pos.getColumn() + i * colDir;

                    // 추가된 조건: 체스 보드 범위 내에 있는지 확인
                    if (newRow < 1 || newRow > 8 || newCol < 1 || newCol > 8) {
                        break;
                    }

                    ChessPosition newPos = new ChessPosition(newRow, newCol);
                    ChessPiece piece = board.getPiece(newPos);

                    if (piece == null) {
                        moves.add(new ChessMove(pos, newPos, null));
                    } else {
                        if (piece.getTeamColor() != this.getTeamColor()) {
                            moves.add(new ChessMove(pos, newPos, null));
                        }
                        break;  // 다른 말이 있으면 이동 종료
                    }
                }
            }
        }
    }


    private void addQueenMoves(Collection<ChessMove> moves, ChessBoard board, ChessPosition pos) {
        // Reuse bishop moves (diagonal)
        addBishopMoves(moves, board, pos);

        // Reuse rook moves (straight lines)
        addRookMoves(moves, board, pos);
    }

    private void addKnightMoves(Collection<ChessMove> moves, ChessBoard board, ChessPosition pos) {
        int[][] movesArray = {
                {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };

        for (int[] move : movesArray) {
            ChessPosition newPos = new ChessPosition(pos.getRow() + move[0], pos.getColumn() + move[1]);
            if (isValidPosition(newPos)) {
                ChessPiece piece = board.getPiece(newPos);
                if (piece == null || piece.getTeamColor() != pieceColor) {
                    moves.add(new ChessMove(pos, newPos, null));
                }
            }
        }
    }

    private void addKingMoves(Collection<ChessMove> moves, ChessBoard board, ChessPosition pos) {
        int[] rowMoves = {-1, 0, 1};
        int[] colMoves = {-1, 0, 1};

        for (int rowChange : rowMoves) {
            for (int colChange : colMoves) {
                if (rowChange == 0 && colChange == 0) continue; // Skip current position

                int newRow = pos.getRow() + rowChange;
                int newCol = pos.getColumn() + colChange;

                // 체스 보드의 유효 범위 체크 (1~8)
                if (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                    ChessPosition newPos = new ChessPosition(newRow, newCol);
                    ChessPiece piece = board.getPiece(newPos);

                    if (piece == null || piece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(pos, newPos, null));
                    }
                }
            }
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    // Override hashCode() for correct hashing behavior
    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    // Override toString() for debugging purposes
    @Override
    public String toString() {
        return pieceColor + " " + type;
    }


    private boolean isValidPosition(ChessPosition pos) {
        return pos.getRow() >= 1 && pos.getRow() <= 8 && pos.getColumn() >= 1 && pos.getColumn() <= 8;
    }
}
