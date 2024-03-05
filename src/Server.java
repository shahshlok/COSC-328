import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class Server {
    public static void main(String[] args) {
        int port = 12000;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("FTP Server Started on Port " + port);

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected.");
                    new ClientHandler(clientSocket).start();
                } catch (IOException e) {
                    System.out.println("Server exception: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}

class ClientHandler extends Thread {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

            String commandLine;
            while ((commandLine = input.readLine()) != null) {
                System.out.println("Command: " + commandLine);
                String[] tokens = commandLine.split(" ");
                String command = tokens[0].toUpperCase();

                switch (command) {
                    case "GET":
                        if (tokens.length > 1) {
                            sendFile(tokens[1], output);
                        } else {
                            output.println("ERROR: Filename is missing.");
                        }
                        break;
                    case "PUT":
                        if (tokens.length > 1) {
                            receiveFile(tokens[1], input);
                        } else {
                            output.println("ERROR: Filename is missing.");
                        }
                        break;
                    case "QUIT":
                        output.println("QUIT");
                        System.out.println("Connection closed.");
                        socket.close();
                        return;
                    default:
                        output.println("ERROR: Unknown command.");
                        break;
                }
            }
            socket.close();
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void sendFile(String fileName, PrintWriter output) {
        File file = new File("/Users/Shlok/IdeaProjects/COSC 328/server/serverFile" + fileName);
        if (file.exists()) {
            try (InputStream fileInputStream = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int bytesRead;

                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    output.write(Arrays.toString(buffer), 0, bytesRead);
                }

                // Send an EOF marker
                output.write("EOF\n");
                output.flush();
            } catch (IOException e) {
                output.println("ERROR: Error reading file.");
            }
        } else {
            output.println("ERROR: File not found.");
        }
    }



    private void receiveFile(String fileName, BufferedReader input) throws IOException {
        File file = new File("/Users/Shlok/IdeaProjects/COSC 328/server/serverFile" + fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        PrintWriter fileWriter = new PrintWriter(new FileWriter(file));
        String line;
        while (!(line = input.readLine()).equals("EOF")) {
            fileWriter.println(line);
        }
        fileWriter.close();
    }
}
