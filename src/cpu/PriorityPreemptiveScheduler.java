/*
 * Algorithm : Priority Scheduling – Preemptive
 * Owner     : Emman
 *
 * At every time unit, the CPU runs the ready process with the highest
 * priority (lower number = higher priority). A running process is
 * preempted only if a ready process has a STRICTLY higher priority.
 * Ties go to the earlier arrival, then the lower PID. No aging.
 */
package cpu;

import java.util.ArrayList;
import java.util.List;

public class PriorityPreemptiveScheduler implements CpuScheduler {

    public String getName() {
        return "Priority Scheduling (Preemptive)";
    }

    public boolean usesPriority() {
        return true;
    }

    public List<GanttBlock> schedule(List<Process> processes) {
        List<GanttBlock> chart = new ArrayList<GanttBlock>();
        int time = 0;
        int finishedCount = 0;
        Process running = null;   // the process that used the CPU in the last time unit

        while (finishedCount < processes.size()) {

            // 1. Find the ready process with the highest priority.
            Process best = null;
            for (Process p : processes) {
                boolean isReady = p.arrivalTime <= time && p.remainingTime > 0;
                if (isReady && (best == null || isHigherPriority(p, best))) {
                    best = p;
                }
            }

            // 2. No process is ready: the CPU is idle for this time unit.
            if (best == null) {
                GanttBlock.add(chart, "Idle", time, time + 1);
                time++;
                continue;
            }

            // 3. Preempt only if the new process has a STRICTLY higher priority.
            //    Equal priority: the running process keeps the CPU.
            if (running != null && best.priority >= running.priority) {
                best = running;
            }

            // 4. Run the chosen process for one time unit.
            if (best.startTime == -1) {
                best.startTime = time;          // first time on the CPU (for response time)
            }
            GanttBlock.add(chart, best.pid, time, time + 1);
            best.remainingTime--;
            time++;

            // 5. Check if it finished.
            if (best.remainingTime == 0) {
                best.completionTime = time;
                finishedCount++;
                running = null;
            } else {
                running = best;
            }
        }
        return chart;
    }

    /**
     * True if process a should run before process b:
     * lower priority number first; on a tie, earlier arrival first.
     * (If both are equal, b is kept, and b is the earlier row = lower PID.)
     */
    private boolean isHigherPriority(Process a, Process b) {
        if (a.priority != b.priority) {
            return a.priority < b.priority;
        }
        return a.arrivalTime < b.arrivalTime;
    }
}
