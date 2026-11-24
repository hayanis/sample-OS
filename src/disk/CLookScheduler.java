/*
 * Algorithm : C-LOOK (Circular LOOK)
 * Owner     : James
 *
 * Like C-SCAN, but the head only goes as far as the LAST REQUEST in its
 * direction (not to the end of the disk). Then it jumps straight to the
 * farthest request on the other side and keeps moving in the SAME direction.
 * The jump is counted in head movement.
 */
package disk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CLookScheduler implements DiskScheduler {

    public String getName() {
        return "C-LOOK (Circular LOOK)";
    }

    public List<Integer> schedule(int[] requests, int head, int diskSize, boolean towardHigher) {
        int[] sorted = requests.clone();
        Arrays.sort(sorted);                       // smallest to largest

        List<Integer> path = new ArrayList<Integer>();
        path.add(head);

        if (towardHigher) {
            // 1. Move up: serve every request at or above the head.
            for (int r : sorted) {
                if (r >= head) path.add(r);
            }
            // 2. Jump to the lowest request below the head, then move up again.
            //    (The first request added here is the jump.)
            for (int r : sorted) {
                if (r < head) path.add(r);
            }
        } else {
            // 1. Move down: serve every request at or below the head (largest first).
            for (int i = sorted.length - 1; i >= 0; i--) {
                if (sorted[i] <= head) path.add(sorted[i]);
            }
            // 2. Jump to the highest request above the head, then move down again.
            for (int i = sorted.length - 1; i >= 0; i--) {
                if (sorted[i] > head) path.add(sorted[i]);
            }
        }
        return path;
    }
}
