/*
 * Shared class (group).
 * Computes the averages, CPU utilization and throughput.
 * Both CPU algorithms use these same methods, so the results are always computed the same way.
 */
package cpu;

import java.util.List;

public class CpuMetrics {

    public static double averageTurnaroundTime(List<Process> processes) {
        double sum = 0;
        for (Process p : processes) {
            sum += p.getTurnaroundTime();
        }
        return sum / processes.size();
    }

    public static double averageWaitingTime(List<Process> processes) {
        double sum = 0;
        for (Process p : processes) {
            sum += p.getWaitingTime();
        }
        return sum / processes.size();
    }

    public static double averageResponseTime(List<Process> processes) {
        double sum = 0;
        for (Process p : processes) {
            sum += p.getResponseTime();
        }
        return sum / processes.size();
    }

    /** Total time = from 0 to the end of the last block. */
    public static int totalTime(List<GanttBlock> chart) {
        return chart.get(chart.size() - 1).end;
    }

    /** Busy time = time when the CPU was running a process (not idle). */
    public static int busyTime(List<GanttBlock> chart) {
        int busy = 0;
        for (GanttBlock b : chart) {
            if (!b.name.equals("Idle")) {
                busy += b.end - b.start;
            }
        }
        return busy;
    }

    /** Utilization = busy time / total time x 100. */
    public static double cpuUtilization(List<GanttBlock> chart) {
        return busyTime(chart) * 100.0 / totalTime(chart);
    }

    /** Throughput = number of processes / total time. */
    public static double throughput(List<Process> processes, List<GanttBlock> chart) {
        return (double) processes.size() / totalTime(chart);
    }
}
