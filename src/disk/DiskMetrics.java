/*
 * Shared class (group).
 * Computes head movement from a head path. Both disk algorithms use these same methods.
 */
package disk;

import java.util.List;

public class DiskMetrics {

    /** Total head movement = sum of |next position - current position| for every step. */
    public static int totalHeadMovement(List<Integer> path) {
        int total = 0;
        for (int i = 1; i < path.size(); i++) {
            total += Math.abs(path.get(i) - path.get(i - 1));
        }
        return total;
    }

    /** Average seek distance = total head movement / number of requests. */
    public static double averageSeekDistance(List<Integer> path, int numberOfRequests) {
        return (double) totalHeadMovement(path) / numberOfRequests;
    }
}
