import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ExecutorService pool = Executors.newCachedThreadPool();
    private final int port;

    private Server(int port) {
        this.port = port;
    }

    public static Server bindToPort(int port) {
        return new Server(port);
    }

    public void run() {
        try(ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server connection accepted");

            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                pool.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.out.printf("Вероятнее всего порт %s занят.%n", port);
            e.printStackTrace();
        }
    }
}