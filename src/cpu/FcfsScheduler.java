/*
 * Algorithm : First-Come, First-Served (FCFS) – non-preemptive
 * Owner     : Hyannis
 *
 * The process that arrives first runs first, and it keeps the CPU
 * until it finishes. If no process has arrived yet, the CPU is idle.
 * Ties in arrival time go to the lower PID (the earlier row).
 */
package cpu;

import java.util.ArrayList;
import java.util.List;

public class FcfsScheduler implements CpuScheduler {

    public String getName() {
        return "First-Come, First-Served (FCFS)";
    }

    public boolean usesPriority() {
        return false;
    }

    public List<GanttBlock> schedule(List<Process> processes) {
        List<GanttBlock> chart = new ArrayList<GanttBlock>();
        boolean[] finished = new boolean[processes.size()];
        int time = 0;

        for (int count = 0; count < processes.size(); count++) {

            // 1. Find the unfinished process that arrived first.
            //    We use "<" (not "<="), so on a tie the earlier row (lower PID) is kept.
            int next = -1;
            for (int i = 0; i < processes.size(); i++) {
                if (!finished[i]) {
                    if (next == -1 || processes.get(i).arrivalTime < processes.get(next).arrivalTime) {
                        next = i;
                    }
                }
            }
            Process p = processes.get(next);

            // 2. If it has not arrived yet, the CPU is idle until it arrives.
            if (time < p.arrivalTime) {
                GanttBlock.add(chart, "Idle", time, p.arrivalTime);
                time = p.arrivalTime;
            }

            // 3. Run the process from start to finish (no preemption).
            p.startTime = time;
            time = time + p.burstTime;
            p.completionTime = time;
            GanttBlock.add(chart, p.pid, p.startTime, p.completionTime);

            finished[next] = true;
        }
        return chart;
    }
}
