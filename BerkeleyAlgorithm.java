import java.util.*;

public class BerkeleyAlgorithm {

    // Server time (assumed to be coordinator)
    private static int coordinatorTime;

    // Client clocks
    private static int[] clientTimes;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Take number of clients
        System.out.print("Enter number of clients: ");
        int numClients = scanner.nextInt();
        clientTimes = new int[numClients];

        // Take coordinator time
        System.out.print("Enter coordinator (server) time: ");
        coordinatorTime = scanner.nextInt();

        // Take client times
        for (int i = 0; i < numClients; i++) {
            System.out.print("Enter time for client " + (i + 1) + ": ");
            clientTimes[i] = scanner.nextInt();
        }

        System.out.println("\n--- Before Synchronization ---");
        displayTimes();

        synchronizeClocks();

        System.out.println("\n--- After Synchronization ---");
        displayTimes();
    }

    private static void synchronizeClocks() {
        int sumDifferences = 0;
        int[] timeDifferences = new int[clientTimes.length];

        // Calculate difference of each client from coordinator
        for (int i = 0; i < clientTimes.length; i++) {
            timeDifferences[i] = clientTimes[i] - coordinatorTime;
            sumDifferences += timeDifferences[i];
        }

        // Also add coordinator's own time difference (0)
        int totalDevices = clientTimes.length + 1; // clients + coordinator

        // Calculate average time difference
        int avgDifference = sumDifferences / totalDevices;

        // Adjust coordinator time
        coordinatorTime += avgDifference;

        // Adjust each client's time
        for (int i = 0; i < clientTimes.length; i++) {
            clientTimes[i] = clientTimes[i] - timeDifferences[i] + avgDifference;
        }
    }

    private static void displayTimes() {
        System.out.println("Coordinator Time: " + coordinatorTime);
        for (int i = 0; i < clientTimes.length; i++) {
            System.out.println("Client " + (i + 1) + " Time: " + clientTimes[i]);
        }
    }
}
