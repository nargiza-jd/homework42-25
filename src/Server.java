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
                pool.submit(() -> handle(clientSocket));
            }
        } catch (IOException e) {
            System.out.printf("Вероятнее всего порт %s занят.%n", port);
            e.printStackTrace();
        }
    }

    private void handle(Socket socket) {
        System.out.printf("Подключился клиент: %S%n", socket);
        try (
                socket;
                Scanner reader = getReader(socket);
                PrintWriter writer = getWriter(socket);
        ){
            sendResponse("Привет " + socket, writer);
            while (true) {
                String massage = reader.nextLine().strip();
                System.out.printf("Got massage: %s%n", massage);

                if (isQuitMsg(massage) || isEmptyMsg(massage)) {
                    break;
                }

                sendResponse(massage, writer);
            }
        } catch (NoSuchElementException e) {
            System.out.println("Client dropped connection");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.printf("Клиент отключен: %s%n", socket);
    }

    private boolean isEmptyMsg(String msg) {
        return  msg == null || msg.isBlank();
    }

    private boolean isQuitMsg(String msg) {
        return msg.equalsIgnoreCase("bye");
    }

    private Scanner getReader(Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        return new Scanner(inputStreamReader);
    }

    private PrintWriter getWriter(Socket socket) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        return new PrintWriter(outputStream);
    }

    private void sendResponse(String response, Writer writer) throws IOException {
        writer.write(response);
        writer.write(System.lineSeparator());
        writer.flush();
    }
}
