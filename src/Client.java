import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    public Client(String address, int port) throws IOException {
        socket = new Socket(address, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public void sendCommand(String command) throws IOException {
        writer.write(command);
        writer.newLine();
        writer.flush();
    }

    public void getFile(String fileName) throws IOException {
        sendCommand("GET " + fileName);
        File file = new File("client/" + fileName);
        try (OutputStream fileOutputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = socket.getInputStream().read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    public void putFile(String fileName) throws IOException {
        File file = new File("client/" + fileName);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            return;
        }

        sendCommand("PUT " + fileName);
        try (InputStream fileInputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                socket.getOutputStream().write(buffer, 0, bytesRead);
            }

            // Send an EOF marker
            socket.getOutputStream().write("EOF\n".getBytes());
            socket.getOutputStream().flush();
        }
    }

    public void quit() throws IOException {
        sendCommand("QUIT");
    }

    public void close() throws IOException {
        reader.close();
        writer.close();
        socket.close();
    }

    public static void main(String[] args) {
        try {
            Scanner sc = new Scanner(System.in);
            System.out.println("Enter port number: ");
            int port = sc.nextInt();
            sc.close();

            Client client = new Client(SERVER_ADDRESS, port);
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            String input;
            boolean continueLoop = true;

            while (continueLoop) {
                System.out.print("Enter command: ");
                input = consoleReader.readLine();

                String[] commandParts = input.split("\\s+", 2);
                String command = commandParts[0].toUpperCase();
                String fileName = commandParts.length > 1 ? commandParts[1] : "";

                switch (command) {
                    case "GET":
                        client.getFile(fileName);
                        break;
                    case "PUT":
                        client.putFile(fileName);
                        break;
                    case "QUIT":
                        client.quit();
                        continueLoop = false;
                        break;
                    default:
                        System.out.println("Unknown command.");
                        break;
                }
            }

            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
