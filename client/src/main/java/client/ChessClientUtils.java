package client;

import chess.ChessGame;
import chess.ChessPosition;

import static chess.ChessPiece.PieceType.*;
import static ui.EscapeSequences.*;

public class ChessClientUtils {

    public static void drawBoard(ChessGame game, String perspective) {
        boolean isWhite = perspective.equalsIgnoreCase("WHITE");

        int[] rows = isWhite ? new int[]{8,7,6,5,4,3,2,1} : new int[]{1,2,3,4,5,6,7,8};
        char[] cols = isWhite ? new char[]{'a','b','c','d','e','f','g','h'} : new char[]{'h','g','f','e','d','c','b','a'};

        // Top labels
        System.out.print("   ");
        for (char col : cols) System.out.print(" " + col + " ");
        System.out.println();

        for (int row : rows) {
            System.out.print(" " + row + " ");
            for (int i = 0; i < cols.length; i++) {
                int actualCol = isWhite ? i + 1 : 8 - i;  // 실제 컬럼 계산
                ChessPosition pos = new ChessPosition(row, actualCol);
                var piece = game.getBoard().getPiece(pos);

                boolean isLight = (row + actualCol) % 2 == 0;  // ⚠️ 여기 i → actualCol로 변경
                String bg = isLight ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY;
                String pieceStr = "   ";

                if (piece != null) {
                    String letter = switch (piece.getPieceType()) {
                        case KING -> "K";
                        case QUEEN -> "Q";
                        case ROOK -> "R";
                        case BISHOP -> "B";
                        case KNIGHT -> "N";
                        case PAWN -> "P";
                    };
                    String color = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? SET_TEXT_COLOR_RED : SET_TEXT_COLOR_BLUE;
                    pieceStr = color + " " + letter + " " + RESET_TEXT_COLOR;
                }

                System.out.print(bg + pieceStr + RESET_BG_COLOR);
            }
            System.out.println(" " + row);
        }


        // Bottom labels
        System.out.print("   ");
        for (char col : cols) System.out.print(" " + col + " ");
        System.out.println();
    }


    private static String[][] getInitialBoard() {
        return new String[][]{
                {"r", "n", "b", "q", "k", "b", "n", "r"},
                {"p", "p", "p", "p", "p", "p", "p", "p"},
                {" ", " ", " ", " ", " ", " ", " ", " "},
                {" ", " ", " ", " ", " ", " ", " ", " "},
                {" ", " ", " ", " ", " ", " ", " ", " "},
                {" ", " ", " ", " ", " ", " ", " ", " "},
                {"P", "P", "P", "P", "P", "P", "P", "P"},
                {"R", "N", "B", "Q", "K", "B", "N", "R"}
        };
    }

    private static String colorizePiece(String piece) {
        return switch (piece) {
            case "K" -> SET_TEXT_COLOR_RED + " K ";
            case "Q" -> SET_TEXT_COLOR_RED + " Q ";
            case "R" -> SET_TEXT_COLOR_RED + " R ";
            case "B" -> SET_TEXT_COLOR_RED + " B ";
            case "N" -> SET_TEXT_COLOR_RED + " N ";
            case "P" -> SET_TEXT_COLOR_RED + " P ";
            case "k" -> SET_TEXT_COLOR_BLUE + " K ";
            case "q" -> SET_TEXT_COLOR_BLUE + " Q ";
            case "r" -> SET_TEXT_COLOR_BLUE + " R ";
            case "b" -> SET_TEXT_COLOR_BLUE + " B ";
            case "n" -> SET_TEXT_COLOR_BLUE + " N ";
            case "p" -> SET_TEXT_COLOR_BLUE + " P ";
            default -> "   ";
        };
    }

}
