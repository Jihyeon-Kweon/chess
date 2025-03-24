package client;

import static ui.EscapeSequences.*;

public class ChessClientUtils {

    public static void drawBoard(String perspective) {
        String[][] board = getInitialBoard();

        boolean isWhite = perspective.equalsIgnoreCase("WHITE");

        int[] rows = isWhite ? new int[]{8,7,6,5,4,3,2,1} : new int[]{1,2,3,4,5,6,7,8};
        char[] cols = isWhite ? new char[]{'a','b','c','d','e','f','g','h'} : new char[]{'h','g','f','e','d','c','b','a'};

        // Top column labels
        System.out.print("   ");
        for (char col : cols) {
            System.out.print(" " + col + " ");
        }
        System.out.println();

        for (int row : rows) {
            System.out.print(" " + row + " ");
            for (int i = 0; i < cols.length; i++) {
                int actualCol = isWhite ? i : (cols.length - 1 - i);
                boolean isLight = (row + actualCol) % 2 == 0;
                String bgColor = isLight ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY;

                String piece = board[8 - row][actualCol];
                String coloredPiece = colorizePiece(piece);

                System.out.print(bgColor + coloredPiece + RESET_BG_COLOR + RESET_TEXT_COLOR);
            }
            System.out.println(" " + row);
        }

        // Bottom column labels
        System.out.print("   ");
        for (char col : cols) {
            System.out.print(" " + col + " ");
        }
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

    public static void observeGame(int gameID){
        System.out.println("Observing game " + gameID + "...");
        drawBoard("WHITE");
    }
}
