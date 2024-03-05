import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class Client {
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
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file))) {
            char[] buffer = new char[8192];
            int charsRead;

            while ((charsRead = reader.read(buffer)) != -1) {
                fileWriter.write(buffer, 0, charsRead);
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
        BufferedReader fileReader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = fileReader.readLine()) != null) {
            writer.write(line);
            writer.newLine();
        }
        writer.write("EOF");
        writer.newLine();
        writer.flush();
        fileReader.close();
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

            Client client = new Client("127.0.0.1", port);
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
