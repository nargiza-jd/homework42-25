import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ClientHandler implements Runnable {
    private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final Socket socket;
    private String userName;
    private PrintWriter writer;

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
            this.writer = writer;

            writer.write("Введите ваше имя: ");
            writer.flush();
            this.userName = reader.nextLine().strip();

            clients.add(this);
            broadcast(String.format("Пользователь %s подключился к чату %s%n", userName, this), this);

            while (true) {
                String msg = reader.nextLine().strip();


                if (msg.startsWith("/name ")) {
                    String newName = msg.substring(6).trim();

                    if (newName.isBlank() || newName.contains(" ")) {
                        sendMessage("Имя не должно быть пустым или содержать пробелы");
                        continue;
                    }

                    boolean nameTaken = clients.stream().anyMatch(c -> newName.equalsIgnoreCase(c.userName));
                    if (nameTaken) {
                        sendMessage("Имя уже занято другим пользователем");
                        continue;
                    }

                    String oldName = this.userName;
                    this.userName = newName;

                    sendMessage("Вы теперь известны как " + newName);
                    broadcast("Пользователь " + oldName + " теперь известен как " + newName, this);
                    continue;
                }

                if (msg.equals("/list")) {
                    String names = clients.stream()
                            .map(c -> c.userName)
                            .collect(Collectors.joining(", "));
                    sendMessage("Пользователи онлайн: " + names);
                    continue;
                }



                if (isQuitMsg(msg) || isEmptyMsg(msg)) {
                    break;
                }

                broadcast(String.format("%s: %s%s", userName, msg, this), this);
            }

        } catch (Exception e) {
            System.out.printf("Ошибка у клиента: %s", socket);
        } finally {
            clients.remove(this);
            broadcast(String.format("Пользователь %s покинул чат. %s", userName, this), this);
            System.out.printf("Клиент отключён: %s%n", socket);
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

    private void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    private void sendMessage(String message) {
        writer.write(message + "\n");
        writer.flush();
    }

}