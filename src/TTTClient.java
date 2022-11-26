import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class TTTClient {

    private InetAddress serverAddress = InetAddress.getByName("127.0.0.1");
    private int serverPort = 4000;
    private char clientSymbol;
    private char serverSymbol;
    private DataInputStream input;
    private DataOutputStream output;
    private Scanner sc;
    private Socket socket;


    public static void main(String[] args) throws IOException {
        TTTUtil.printTutorialBoard();
        new TTTClient();
    }

    /**
     * <ul>
     *     <li> Starts TCP Client Socket for given address and port number </li>
     *     <li> Waits server to confirm the connection. </li>
     *     <li> Asks user to enter the symbol and sends it to the server </li>
     *     <li> Receives the symbol that server side picked </li>
     *     <li> Waits server to send information about who is going to start first </li>
     * </ul>
     */
    public TTTClient() throws IOException {
        socket = new Socket(serverAddress, serverPort);

        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());

        sc = new Scanner(System.in);

        System.out.println(TTTUtil.readLine(input));
        clientSymbol = sc.next().charAt(0);

        System.out.println("Waiting for server to choose their symbol...");

        output.writeChar(clientSymbol);
        output.flush();

        serverSymbol = input.readChar();

        System.out.println("You: " + clientSymbol + " , Server: " + serverSymbol);
        System.out.println(TTTUtil.readLine(input));
        if (input.readBoolean()) {
            clientIsPlaying();
        } else {
            serverIsPlaying();
        }

    }

    /**
     * Wait until the server plays, receive the updated game board, check if game is over and call play function for
     * client's turn.
     */
    private void serverIsPlaying() throws IOException {
        String gameBoard = TTTUtil.waitOtherPlayer(input);
        checkIfGameIsOver(gameBoard);
        clientIsPlaying();
    }

    /**
     * Client's turn. Call play function for client. Check if game is over each time a player makes a move.
     * After client plays his turn, call waiting function to wait server side to play their move and
     * receive updated game board.
     */
    private void clientIsPlaying() throws IOException {
        String gameBoard = TTTUtil.play(clientSymbol, sc, output);
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
     * If game is over, show results, close sockets and streams and exit the application.
     */
    private void closeStreams() throws IOException {
        input.close();
        output.close();
        sc.close();
        socket.close();
    }
}
