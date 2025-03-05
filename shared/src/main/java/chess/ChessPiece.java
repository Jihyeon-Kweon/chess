package chess;

import java.util.Collection;
import java.util.HashSet;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType pieceType;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        if (pieceColor == null || type == null) {
            throw new IllegalArgumentException("Piece color and type cannot be null.");
        }
        this.pieceColor = pieceColor;
        this.pieceType = type;
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
        return pieceType;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> possibleMoves = new HashSet<>();

        switch (this.pieceType) {
            case PAWN:
                addPawnMoves(possibleMoves, board, myPosition);
                break;
            case ROOK:
                addLinearMoves(possibleMoves, board, myPosition, new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}});
                break;
            case BISHOP:
                addLinearMoves(possibleMoves, board, myPosition, new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}});
                break;
            case QUEEN:
                addLinearMoves(possibleMoves, board, myPosition, new int[][]{
                        {1, 0}, {-1, 0}, {0, 1}, {0, -1}, // Rook movement
                        {1, 1}, {1, -1}, {-1, 1}, {-1, -1}  // Bishop movement
                });
                break;
            case KNIGHT:
                addKnightMoves(possibleMoves, board, myPosition);
                break;
            case KING:
                addKingMoves(possibleMoves, board, myPosition);
                break;
        }

        return possibleMoves;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) return false;
        ChessPiece that = (ChessPiece) obj;
        return pieceColor == that.pieceColor && pieceType == that.pieceType;
    }

    @Override
    public int hashCode() {
        int result = pieceColor.hashCode();
        result = 31 * result + pieceType.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ChessPiece{" + "pieceColor=" + pieceColor + ", pieceType=" + pieceType + '}';
    }

    private void addPawnMoves(Collection<ChessMove> moves, ChessBoard board, ChessPosition pos) {
        int direction = (pieceColor == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int finalRow = (pieceColor == ChessGame.TeamColor.WHITE) ? 8 : 1;

        // 앞으로 한 칸 이동
        ChessPosition oneStep = new ChessPosition(pos.getRow() + direction, pos.getColumn());
        if (board.getPiece(oneStep) == null) {
            if (oneStep.getRow() == finalRow) {
                addPromotionMoves(moves, pos, oneStep);
            } else {
                moves.add(new ChessMove(pos, oneStep, null));
            }
        }

        // 첫 이동 시 두 칸 이동
        if ((pieceColor == ChessGame.TeamColor.WHITE && pos.getRow() == 2) ||
                (pieceColor == ChessGame.TeamColor.BLACK && pos.getRow() == 7)) {
            ChessPosition twoSteps = new ChessPosition(pos.getRow() + 2 * direction, pos.getColumn());
            if (board.getPiece(twoSteps) == null && board.getPiece(oneStep) == null) {
                moves.add(new ChessMove(pos, twoSteps, null));
            }
        }

        // 대각선 공격 (캡처 가능 여부 체크)
        for (int colOffset : new int[]{-1, 1}) {
            int newCol = pos.getColumn() + colOffset;
            if (newCol >= 1 && newCol <= 8) {
                ChessPosition attackPos = new ChessPosition(pos.getRow() + direction, newCol);
                ChessPiece target = board.getPiece(attackPos);
                if (target != null && target.getTeamColor() != pieceColor) {
                    if (attackPos.getRow() == finalRow) {
                        addPromotionMoves(moves, pos, attackPos);
                    } else {
                        moves.add(new ChessMove(pos, attackPos, null));
                    }
                }
            }
        }
    }

    private void addPromotionMoves(Collection<ChessMove> moves, ChessPosition start, ChessPosition end) {
        moves.add(new ChessMove(start, end, ChessPiece.PieceType.QUEEN));
        moves.add(new ChessMove(start, end, ChessPiece.PieceType.ROOK));
        moves.add(new ChessMove(start, end, ChessPiece.PieceType.BISHOP));
        moves.add(new ChessMove(start, end, ChessPiece.PieceType.KNIGHT));
    }

    private void addLinearMoves(Collection<ChessMove> moves, ChessBoard board, ChessPosition pos, int[][] directions) {
        for (int[] dir : directions) {
            int row = pos.getRow();
            int col = pos.getColumn();

            while (true) {
                row += dir[0];
                col += dir[1];
                if (row < 1 || row > 8 || col < 1 || col > 8) {
                    break;
                }

                ChessPosition newPos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(newPos);
                if (piece == null) {
                    moves.add(new ChessMove(pos, newPos, null));
                } else {
                    if (piece.getTeamColor() != pieceColor) {
                        moves.add(new ChessMove(pos, newPos, null));
                    }
                    break;
                }
            }
        }
    }

    private void addMovesByOffsets(Collection<ChessMove> moves, ChessBoard board, ChessPosition pos, int[][] offsets) {
        for (int[] offset : offsets) {
            int newRow = pos.getRow() + offset[0];
            int newCol = pos.getColumn() + offset[1];

            if (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                ChessPosition newPos = new ChessPosition(newRow, newCol);
                ChessPiece target = board.getPiece(newPos);

                if (target == null || target.getTeamColor() != pieceColor) {
                    moves.add(new ChessMove(pos, newPos, null));
                }
            }
        }
    }


    private void addKnightMoves(Collection<ChessMove> moves, ChessBoard board, ChessPosition pos) {
        int[][] knightMoves = {
                {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };
        addMovesByOffsets(moves, board, pos, knightMoves);
    }

    private void addKingMoves(Collection<ChessMove> moves, ChessBoard board, ChessPosition pos) {
        int[][] kingMoves = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };
        addMovesByOffsets(moves, board, pos, kingMoves);
    }


}
