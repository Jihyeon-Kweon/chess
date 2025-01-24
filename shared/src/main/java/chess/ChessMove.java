package chess;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {

    private final ChessPosition startPosition;
    private final ChessPosition endPosition;
    private final ChessPiece.PieceType promotionPiece;

    /**
     * Constructs a chess move with start and end positions, and optional promotion.
     *
     * @param startPosition the starting position of the move
     * @param endPosition   the ending position of the move
     * @param promotionPiece the piece type for pawn promotion, or null if not applicable
     */
    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     ChessPiece.PieceType promotionPiece) {
        if (startPosition == null || endPosition == null) {
            throw new IllegalArgumentException("Start and end positions must not be null.");
        }
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.promotionPiece = promotionPiece;
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return startPosition;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return endPosition;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return promotionPiece;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ChessMove that = (ChessMove) obj;
        return startPosition.equals(that.startPosition) &&
                endPosition.equals(that.endPosition) &&
                ((promotionPiece == null && that.promotionPiece == null) ||
                        (promotionPiece != null && promotionPiece.equals(that.promotionPiece)));
    }

    @Override
    public int hashCode() {
        int result = startPosition.hashCode();
        result = 31 * result + endPosition.hashCode();
        result = 31 * result + (promotionPiece != null ? promotionPiece.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ChessMove{" +
                "startPosition=" + startPosition +
                ", endPosition=" + endPosition +
                ", promotionPiece=" + promotionPiece +
                '}';
    }
}
