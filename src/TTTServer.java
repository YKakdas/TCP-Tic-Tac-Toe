import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class TTTServer {

    private final int serverPort = 4000;
    private char serverSymbol;
    private char clientSymbol;
    ;
    private DataInputStream input;
    private DataOutputStream output;
    private Scanner sc;
    private ServerSocket server;
    private Socket socket;

    public static void main(String[] args) throws IOException {
        TTTUtil.printTutorialBoard();
        new TTTServer();
    }

    /**
     * <ul>
     *     <li> Starts TCP Server Socket for given port number </li>
     *     <li> Sends information that client has connected to the server and prompts user to pick a symbol </li>
     *     <li> Receives the symbol that client picked. Asks user to change if it is duplicate </li>
     *     <li> Randomly chooses which player to start first </li>
     * </ul>
     */
    public TTTServer() throws IOException {
        server = new ServerSocket(serverPort);

        while (true) {
            socket = server.accept();

            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            sc = new Scanner(System.in);

            output.write("Server started the game. Please choose your symbol".getBytes());
            output.flush();

            System.out.println("The other player has joined to the game. Please choose your symbol");
            serverSymbol = sc.next().charAt(0);

            System.out.println("Waiting for other player to choose their symbol");

            clientSymbol = input.readChar();

            while (serverSymbol == clientSymbol) {
                System.out.println("The symbol you entered is already chosen by the client. Please pick another symbol");
                serverSymbol = sc.next().charAt(0);
            }

            output.writeChar(serverSymbol);
            output.flush();

            System.out.println("You: " + serverSymbol + " , Client: " + clientSymbol);

            determineFirstPlayer();
        }
    }

    /**
     * Randomly choose a player to play first. Inform the client side about who is going to start first
     */
    private void determineFirstPlayer() throws IOException {
        double rand = Math.random();

        if (rand > 0.5) {
            System.out.println("Server is going to start first...");
            output.write("Server is going to start first...".getBytes());
            output.writeBoolean(false);
            output.flush();
            serverIsPlaying();
        } else {
            System.out.println("Client is going to start first...");
            output.write("Client is going to start first...".getBytes());
            output.writeBoolean(true);
            output.flush();
            clientIsPlaying();
        }
    }

    /**
     * Server's turn. Call play function for server. Check if game is over each time a player makes a move.
     * After server player plays his turn, call waiting function to wait client side to play their move and
     * receive updated game board.
     */
    private void serverIsPlaying() throws IOException {
        String gameBoard = TTTUtil.play(serverSymbol, sc, output);
        checkIfGameIsOver(gameBoard);
        clientIsPlaying();
    }

    /**
     * Wait until the client plays, receive the updated game board, check if game is over and call play function for
     * server's turn.
     */
    private void clientIsPlaying() throws IOException {
        String gameBoard = TTTUtil.waitOtherPlayer(input);
        checkIfGameIsOver(gameBoard);
        serverIsPlaying();
    }

    /**
     * If game is over, show results, close sockets and streams and exit the application.
     */
    private void checkIfGameIsOver(String gameBoard) throws IOException {
        TTTUtil.checkGameBoard(gameBoard, serverSymbol, clientSymbol);
        if (TTTUtil.isGameOver()) {
            TTTUtil.showGameResults();
            closeStreams();
            System.exit(0);
        }
    }

    /**
     * Close sockets and streams
     */
    private void closeStreams() throws IOException {
        input.close();
        output.close();
        sc.close();
        server.close();
        socket.close();
    }
}
