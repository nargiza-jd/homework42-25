import java.io.*;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ClientHandler implements Runnable {
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.printf("Подключился клиент: %s%n", socket);

        try (
                socket;
                Scanner reader = getReader(socket);
                PrintWriter writer = getWriter(socket)
        ) {
            sendResponse("Привет " + socket, writer);

            while (true) {
                String message = reader.nextLine().strip();
                System.out.printf("Получено сообщение: %s%n", message);

                if (isQuitMsg(message) || isEmptyMsg(message)) {
                    break;
                }

                sendResponse(message, writer);
            }

        } catch (NoSuchElementException e) {
            System.out.println("Клиент отключился неожиданно");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.printf("Клиент отключён: %s%n", socket);
    }

    private boolean isEmptyMsg(String msg) {
        return msg == null || msg.isBlank();
    }

    private boolean isQuitMsg(String msg) {
        return msg.equalsIgnoreCase("bye");
    }

    private Scanner getReader(Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();
        InputStreamReader isr = new InputStreamReader(inputStream);
        return new Scanner(isr);
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