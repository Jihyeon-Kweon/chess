package client;

import java.util.Scanner;

public class ChessClient {
    public static void main(String[] args) {
        new ChessClient().run();
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print(">>> ");  // 사용자 입력 프롬프트

            String input = scanner.nextLine().trim();  // 사용자 입력 받기

            if (input.equalsIgnoreCase("quit")) {
                System.out.println("Exit the program.");
                break;
            } else if (input.equalsIgnoreCase("help")) {
                printHelp();
            } else {
                System.out.println("Unknown command");
            }
        }

        scanner.close();
    }

    private void printHelp() {
        System.out.println("Available commands:");
        System.out.println("help - print a list of available commands");
        System.out.println("quit - exit the program");
    }
}
