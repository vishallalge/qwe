import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class server {

    // Data structure to hold client data
    static class ClientData {
        public Date clockTime;
        public long timeDifferenceMillis;  // Difference in milliseconds
        public Socket socket;

        public ClientData(Date clockTime, long timeDifferenceMillis, Socket socket) {
            this.clockTime = clockTime;
            this.timeDifferenceMillis = timeDifferenceMillis;
            this.socket = socket;
        }
    }

    static ConcurrentHashMap<String, ClientData> clientDataMap = new ConcurrentHashMap<>();
    static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    // Thread to receive time from each client
    static class ClientHandler extends Thread {
        private final Socket socket;
        private final String clientId;

        public ClientHandler(Socket socket, String clientId) {
            this.socket = socket;
            this.clientId = clientId;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (true) {
                    String timeStr = in.readLine();
                    if (timeStr == null) break;

                    Date clientTime = formatter.parse(timeStr);
                    Date serverTime = new Date();
                    long diffMillis = serverTime.getTime() - clientTime.getTime();

                    clientDataMap.put(clientId, new ClientData(clientTime, diffMillis, socket));
                    System.out.println("Client data updated: " + clientId);

                    Thread.sleep(5000);  // Pause before next read
                }
            } catch (IOException | ParseException | InterruptedException e) {
                System.out.println("Client " + clientId + " disconnected or error occurred.");
                clientDataMap.remove(clientId);
            }
        }
    }

    // Function to calculate average clock difference
    public static long getAverageClockDiff() {
        if (clientDataMap.isEmpty()) return 0;
        long totalDiff = 0;
        for (ClientData data : clientDataMap.values()) {
            totalDiff += data.timeDifferenceMillis;
        }
        return totalDiff / clientDataMap.size();
    }

    // Thread to synchronize all clients
    static class ClockSynchronizer extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    System.out.println("\nNew synchronization cycle started.");
                    System.out.println("Number of clients to synchronize: " + clientDataMap.size());

                    if (!clientDataMap.isEmpty()) {
                        long avgDiff = getAverageClockDiff();
                        Date syncedTime = new Date(System.currentTimeMillis() + avgDiff);
                        String timeStr = formatter.format(syncedTime);

                        for (Map.Entry<String, ClientData> entry : clientDataMap.entrySet()) {
                            try {
                                PrintWriter out = new PrintWriter(entry.getValue().socket.getOutputStream(), true);
                                out.println(timeStr);
                            } catch (IOException e) {
                                System.out.println("Error sending to " + entry.getKey());
                            }
                        }
                    } else {
                        System.out.println("No clients connected. Skipping synchronization.");
                    }

                    Thread.sleep(5000);  // 5-second cycle
                } catch (InterruptedException e) {
                    System.out.println("Synchronization interrupted.");
                }
            }
        }
    }

    public static void initiateClockServer(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Clock server started on port " + port);

            // Start synchronization thread
            new ClockSynchronizer().start();

            // Accept clients continuously
            while (true) {
                Socket clientSocket = serverSocket.accept();
                String clientId = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
                System.out.println("Client connected: " + clientId);

                new ClientHandler(clientSocket, clientId).start();
            }

        } catch (IOException e) {
            System.out.println("Error starting server.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        initiateClockServer(8080);
    }
}
