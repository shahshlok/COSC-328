import java.io.*;

public class PortFlusher {
    private static final int PORT = 12000;

    public static void main(String[] args) {
        try {
            // Get the process ID of the process using the port
            Process lsofProcess = new ProcessBuilder("lsof", "-t", "-i", ":" + PORT).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(lsofProcess.getInputStream()));
            String pid = reader.readLine();

            if (pid != null) {
                // Kill the process
                Process killProcess = new ProcessBuilder("kill", "-9", pid).start();
                killProcess.waitFor();
                System.out.println("Process running on port " + PORT + " has been terminated.");
            } else {
                System.out.println("No process is running on port " + PORT + ".");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
