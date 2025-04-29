import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SlaveClient {

    // Thread to send current time to server
    static class TimeSender extends Thread {
        private final Socket socket;
        private final PrintWriter out;

        public TimeSender(Socket socket) throws IOException {
            this.socket = socket;
            this.out = new PrintWriter(socket.getOutputStream(), true);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
                    out.println(currentTime);
                    System.out.println("Recent time sent successfully");
                    Thread.sleep(5000);  // Sleep for 5 seconds
                }
            } catch (InterruptedException e) {
                System.out.println("TimeSender thread interrupted.");
            }
        }
    }

    // Thread to receive synchronized time from server
    static class TimeReceiver extends Thread {
        private final Socket socket;
        private final BufferedReader in;

        public TimeReceiver(Socket socket) throws IOException {
            this.socket = socket;
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        @Override
        public void run() {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println("Synchronized time at the client is: " + line);
                }
            } catch (IOException e) {
                System.out.println("Error reading from server.");
            }
        }
    }

    public static void initiateSlaveClient(int port) {
        try {
            Socket socket = new Socket("127.0.0.1", port);
            System.out.println("Connected to server");

            System.out.println("Starting to send time to server");
            new TimeSender(socket).start();

            System.out.println("Starting to receive synchronized time from server");
            new TimeReceiver(socket).start();

        } catch (IOException e) {
            System.out.println("Failed to connect to the server on port " + port);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        initiateSlaveClient(8080);
    }
}
