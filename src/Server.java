import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 12000;
    static final String SERVER_DIRECTORY = "server/";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("FTP Server Started on Port " + PORT);

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
    private BufferedReader input;
    private PrintWriter output;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
            String commandLine;
            while ((commandLine = input.readLine()) != null) {
                System.out.println("Command: " + commandLine);
                String[] tokens = commandLine.split(" ");
                String command = tokens[0].toUpperCase();

                switch (command) {
                    case "GET":
                        if (tokens.length > 1) {
                            sendFile(tokens[1]);
                        } else {
                            output.println("ERROR: Filename is missing.");
                        }
                        break;
                    case "PUT":
                        if (tokens.length > 1) {
                            receiveFile(tokens[1]);
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
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendFile(String fileName) {
        File file = new File(Server.SERVER_DIRECTORY + fileName);
        if (file.exists()) {
            try (InputStream fileInputStream = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int bytesRead;

                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    socket.getOutputStream().write(buffer, 0, bytesRead);
                }

                // Send an EOF marker
                socket.getOutputStream().write("EOF\n".getBytes());
                socket.getOutputStream().flush();
            } catch (IOException e) {
                output.println("ERROR: Error reading file.");
            }
        } else {
            output.println("ERROR: File not found.");
        }
    }

    private void receiveFile(String fileName) {
        File file = new File(Server.SERVER_DIRECTORY + fileName);
        try (OutputStream fileOutputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = socket.getInputStream().read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            output.println("ERROR: Error writing file.");
        }
    }
}
