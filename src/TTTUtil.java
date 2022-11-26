import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class TTTUtil {

    public static String gameBoard = "---------";
    private static boolean serverWon;
    private static boolean clientWon;
    private static boolean isTie;


    /**
     * Prints game board with indices to show the users how to enter a grid position
     */
    public static void printTutorialBoard() {
        System.out.println("For this game, please follow the schema given below for the positions\n");
        String initialBoard = "123---456---789---";
        printGameBoard(initialBoard);
    }

    /**
     * Each time a player makes a move, print the current game board in both client and server side.
     */
    public static void printGameBoard(String board) {
        for (int i = 0; i < board.length(); i++) {
            System.out.print(board.charAt(i) + "  ");
            if ((i + 1) % 3 == 0) {
                System.out.println();
            }
        }
        System.out.println();
    }

    /**
     *  Reads string from the socket
     */
    public static String readLine(DataInputStream input) throws IOException {
        byte[] receiveBuffer = new byte[1024];
        int numberOfReadChars = input.read(receiveBuffer);
        return new String(receiveBuffer, 0, numberOfReadChars);
    }

    /**
     * While 1 player is about to play, the other should be waiting. This function calls receive function and expects
     * the updated game board after the other player makes a move.
     */
    public static String waitOtherPlayer(DataInputStream input) throws IOException {
        System.out.println("Waiting for other player to make a move...");
        byte[] receiveBuffer = new byte[1024];
        int numberOfReadChars = input.read(receiveBuffer);
        gameBoard = new String(receiveBuffer, 0, numberOfReadChars);

        printGameBoard(gameBoard);
        return gameBoard;
    }

    /**
     * Prompts to a user to enter a grid position, checks if the value entered is eligible or not. Prompts until the user
     * enters a value from correct interval. Updates the game board regarding the player's move and sends updated game board
     * to the other player who has been waiting in the {@link #waitOtherPlayer(DataInputStream input)} function.
     */
    public static String play(char symbol, Scanner sc, DataOutputStream output) throws IOException {
        System.out.println("It's your turn. Please enter the position of the grid where you want to place your symbol(1-9)");

        int position = getPosition(sc);

        while (isPositionOccupied(gameBoard, position)) {
            System.out.println("The position you want to play has already been used. Please enter another position");
            position = getPosition(sc);
        }

        char[] chars = gameBoard.toCharArray();
        chars[position] = symbol;
        gameBoard = String.valueOf(chars);
        printGameBoard(gameBoard);

        output.write(gameBoard.getBytes());
        output.flush();

        return gameBoard;
    }

    /**
     * Error control for preventing unexpected crashes and ensuring a user has entered a reasonable input.
     */
    private static int getPosition(Scanner sc) {
        int position;
        while (true) {
            try {
                position = sc.nextInt() - 1;
                if (position < 0 || position > 8) {
                    System.out.println("Grid out of bound, please enter a number between 1 and 9...");
                    sc.nextLine();
                } else {
                    break;
                }

            } catch (Exception e) {
                System.out.println("Wrong input. Please reenter the position between 1-9");
                sc.nextLine();
            }
        }
        return position;
    }

    /**
     * Checks board after every player move to find if there is winner or game is over due to tie.
     */
    public static void checkGameBoard(String gameBoard, char serverSymbol, char clientSymbol) {
        int rowInfo = checkRows();
        int columnInfo = checkColumns();
        int diagonalInfo = checkDiagonal();

        char result = (char) -1;
        if (rowInfo != -1) {
            result = (char) rowInfo;
        }
        if (columnInfo != -1) {
            result = (char) columnInfo;
        }
        if (diagonalInfo != -1) {
            result = (char) diagonalInfo;
        }

        if (result == serverSymbol) {
            serverWon = true;
        } else if (result == clientSymbol) {
            clientWon = true;
        } else if (!gameBoard.contains("-")) {
            isTie = true;
        }

    }

    /**
     * Returns true when there is winner or is a tie. Used to finalize the game.
     */
    public static boolean isGameOver() {
        return serverWon || clientWon || isTie;
    }

    /**
     * Controls rows to see if there is any winner
     */
    private static int checkRows() {
        char[] chars = gameBoard.toCharArray();
        for (int i = 0; i < 3; i++) {
            if (chars[i * 3] == chars[i * 3 + 1] && chars[i * 3] == chars[i * 3 + 2]) {
                return chars[i * 3];
            }
        }
        return -1;
    }

    /**
     * Controls columns to see if there is any winner
     */
    private static int checkColumns() {
        char[] chars = gameBoard.toCharArray();
        for (int i = 0; i < 3; i++) {
            if (chars[i] == chars[i + 3] && chars[i] == chars[i + 6]) {
                return chars[i];
            }

        }
        return -1;
    }

    /**
     * Controls diagonals to see if there is any winner
     */
    private static int checkDiagonal() {
        char[] chars = gameBoard.toCharArray();

        if ((chars[0] == chars[4] && chars[0] == chars[8]) || (chars[2] == chars[4] && chars[2] == chars[6])) {
            return chars[4];
        }

        return -1;
    }

    /**
     * Announcing the game result
     */
    public static void showGameResults() {
        if (clientWon) {
            System.out.println("The Client Won the Game!");
        } else if (serverWon) {
            System.out.println("The Server Won the Game!");
        } else {
            System.out.println("It's a Tie!");
        }
    }

    /**
     * Checks if given grid position is empty or not
     */
    private static boolean isPositionOccupied(String gameBoard, int position) {
        return gameBoard.charAt(position) != '-';
    }
}
