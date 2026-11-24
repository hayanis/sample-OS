/*
 * Console runner for the Priority (Preemptive) module only (Emman).
 * Runs the algorithm without the GUI, for practice and checking hand solutions.
 * Run: java -cp build/classes;build/test RunPriority      (Windows)
 * Press Enter at the first question to use the sample data.
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import cpu.CpuMetrics;
import cpu.GanttBlock;
import cpu.PriorityPreemptiveScheduler;
import cpu.Process;

public class RunPriority {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<Process> processes = new ArrayList<Process>();

        System.out.print("Number of processes (Enter = sample data): ");
        String line = in.nextLine().trim();
        if (line.isEmpty()) {
            int[][] sample = {{0, 7, 3}, {2, 4, 1}, {4, 1, 4}, {5, 4, 2}, {6, 2, 2}};
            for (int i = 0; i < sample.length; i++) {
                processes.add(new Process("P" + (i + 1), sample[i][0], sample[i][1], sample[i][2]));
            }
        } else {
            int n = Integer.parseInt(line);
            for (int i = 0; i < n; i++) {
                System.out.print("P" + (i + 1) + " - arrival burst priority: ");
                processes.add(new Process("P" + (i + 1), in.nextInt(), in.nextInt(), in.nextInt()));
            }
        }

        List<GanttBlock> chart = new PriorityPreemptiveScheduler().schedule(processes);

        System.out.println();
        System.out.print("Gantt chart: ");
        for (GanttBlock b : chart) {
            System.out.print(b + " ");
        }
        System.out.println();
        System.out.println();
        System.out.println("PID   AT   BT   PR   CT  TAT   WT   RT");
        for (Process p : processes) {
            System.out.printf("%-4s %3d  %3d  %3d  %3d  %3d  %3d  %3d%n", p.pid, p.arrivalTime, p.burstTime,
                    p.priority, p.completionTime, p.getTurnaroundTime(), p.getWaitingTime(), p.getResponseTime());
        }
        System.out.println();
        System.out.printf("Average TAT = %.2f%n", CpuMetrics.averageTurnaroundTime(processes));
        System.out.printf("Average WT  = %.2f%n", CpuMetrics.averageWaitingTime(processes));
        System.out.printf("Average RT  = %.2f%n", CpuMetrics.averageResponseTime(processes));
        System.out.printf("CPU utilization = %.2f%%%n", CpuMetrics.cpuUtilization(chart));
        System.out.printf("Throughput = %.4f%n", CpuMetrics.throughput(processes, chart));
    }
}
