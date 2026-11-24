/*
 * Algorithm : C-SCAN (Circular SCAN)
 * Owner     : Miel
 *
 * The head serves requests in one direction only. When no requests are
 * left in that direction, it goes to the END of the disk, jumps to the
 * other end, and keeps moving in the SAME direction.
 * The travel to the end and the jump are both counted in head movement.
 */
package disk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CScanScheduler implements DiskScheduler {

    public String getName() {
        return "C-SCAN (Circular SCAN)";
    }

    public List<Integer> schedule(int[] requests, int head, int diskSize, boolean towardHigher) {
        int[] sorted = requests.clone();
        Arrays.sort(sorted);                       // smallest to largest
        int lastCylinder = diskSize - 1;

        List<Integer> path = new ArrayList<Integer>();
        path.add(head);

        if (towardHigher) {
            // 1. Move up: serve every request at or above the head.
            for (int r : sorted) {
                if (r >= head) path.add(r);
            }
            // 2. Requests left below the head? Go to the end, jump to 0, move up again.
            if (sorted[0] < head) {
                goToEnd(path, lastCylinder);
                path.add(0);                       // the return jump
                for (int r : sorted) {
                    if (r < head) path.add(r);
                }
            }
        } else {
            // 1. Move down: serve every request at or below the head (largest first).
            for (int i = sorted.length - 1; i >= 0; i--) {
                if (sorted[i] <= head) path.add(sorted[i]);
            }
            // 2. Requests left above the head? Go to 0, jump to the last cylinder, move down again.
            if (sorted[sorted.length - 1] > head) {
                goToEnd(path, 0);
                path.add(lastCylinder);            // the return jump
                for (int i = sorted.length - 1; i >= 0; i--) {
                    if (sorted[i] > head) path.add(sorted[i]);
                }
            }
        }
        return path;
    }

    /** Moves the head to the end of the disk, unless it is already there. */
    private void goToEnd(List<Integer> path, int endCylinder) {
        int current = path.get(path.size() - 1);
        if (current != endCylinder) {
            path.add(endCylinder);
        }
    }
}
