/*
 * Test log (Testing Lead).
 * Each case was solved by hand first. The program's answer must match exactly.
 * Run: java -cp build/classes;build/test AlgorithmTests      (Windows)
 */
import java.util.ArrayList;
import java.util.List;

import cpu.CpuMetrics;
import cpu.CpuScheduler;
import cpu.FcfsScheduler;
import cpu.GanttBlock;
import cpu.PriorityPreemptiveScheduler;
import cpu.Process;
import disk.CLookScheduler;
import disk.CScanScheduler;
import disk.DiskMetrics;
import disk.DiskScheduler;

public class AlgorithmTests {

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        CpuScheduler fcfs = new FcfsScheduler();
        CpuScheduler prio = new PriorityPreemptiveScheduler();
        DiskScheduler cscan = new CScanScheduler();
        DiskScheduler clook = new CLookScheduler();

        // ----- FCFS: data is {arrival, burst}; expected Gantt chart and waiting times -----
        testCpu("FCFS sample (idle + tie)", fcfs, new int[][]{{0,4},{1,3},{2,1},{10,2},{10,3}},
                "P1[0-4] P2[4-7] P3[7-8] Idle[8-10] P4[10-12] P5[12-15]", new int[]{0,3,5,0,2});
        testCpu("FCFS idle at start", fcfs, new int[][]{{3,2}},
                "Idle[0-3] P1[3-5]", new int[]{0});
        testCpu("FCFS all arrive at 0", fcfs, new int[][]{{0,5},{0,3},{0,8}},
                "P1[0-5] P2[5-8] P3[8-16]", new int[]{0,5,8});
        testCpu("FCFS out-of-order input", fcfs, new int[][]{{6,2},{0,3},{2,4}},
                "P2[0-3] P3[3-7] P1[7-9]", new int[]{1,0,1});

        // ----- Priority (preemptive): data is {arrival, burst, priority} -----
        testCpu("PRIO sample", prio, new int[][]{{0,7,3},{2,4,1},{4,1,4},{5,4,2},{6,2,2}},
                "P1[0-2] P2[2-6] P4[6-10] P5[10-12] P1[12-17] P3[17-18]", new int[]{10,0,13,1,4});
        testCpu("PRIO equal priority does NOT preempt", prio, new int[][]{{0,5,2},{1,3,2}},
                "P1[0-5] P2[5-8]", new int[]{0,4});
        testCpu("PRIO idle gap + preemption", prio, new int[][]{{2,3,3},{3,2,1},{10,1,5}},
                "Idle[0-2] P1[2-3] P2[3-5] P1[5-7] Idle[7-10] P3[10-11]", new int[]{2,0,0});
        testCpu("PRIO tie same priority & arrival -> lower PID", prio, new int[][]{{0,2,1},{0,2,1}},
                "P1[0-2] P2[2-4]", new int[]{0,2});
        testCpu("PRIO single process", prio, new int[][]{{0,4,1}},
                "P1[0-4]", new int[]{0});

        // ----- C-SCAN: expected head path and total head movement -----
        int[] q = {98, 183, 37, 122, 14, 124, 65, 67};
        testDisk("C-SCAN textbook, toward higher", cscan, q, 53, 200, true,
                "53 65 67 98 122 124 183 199 0 14 37", 382);
        testDisk("C-SCAN textbook, toward lower", cscan, q, 53, 200, false,
                "53 37 14 0 199 183 124 122 98 67 65", 386);
        testDisk("C-SCAN duplicates + request at head", cscan, new int[]{50, 20, 50, 80}, 50, 100, true,
                "50 50 50 80 99 0 20", 168);
        testDisk("C-SCAN head at boundary 199", cscan, new int[]{10, 150}, 199, 200, true,
                "199 0 10 150", 349);
        testDisk("C-SCAN all requests in direction", cscan, new int[]{60, 90}, 50, 200, true,
                "50 60 90", 40);

        // ----- C-LOOK -----
        testDisk("C-LOOK textbook, toward higher", clook, q, 53, 200, true,
                "53 65 67 98 122 124 183 14 37", 322);
        testDisk("C-LOOK textbook, toward lower", clook, q, 53, 200, false,
                "53 37 14 183 124 122 98 67 65", 326);
        testDisk("C-LOOK duplicates", clook, new int[]{40, 40, 10, 70}, 30, 200, true,
                "30 40 40 70 10", 100);
        testDisk("C-LOOK all requests behind head", clook, new int[]{5, 25}, 30, 200, true,
                "30 5 25", 45);
        testDisk("C-LOOK head at 0, toward lower", clook, new int[]{0, 100, 30}, 0, 200, false,
                "0 0 100 30", 170);

        System.out.println();
        System.out.println("PASSED: " + passed + "   FAILED: " + failed);
    }

    /** Runs a CPU algorithm and compares the Gantt chart and every waiting time. */
    static void testCpu(String name, CpuScheduler scheduler, int[][] data, String expectedGantt, int[] expectedWaiting) {
        List<Process> processes = new ArrayList<Process>();
        for (int i = 0; i < data.length; i++) {
            int priority = (data[i].length > 2) ? data[i][2] : 0;
            processes.add(new Process("P" + (i + 1), data[i][0], data[i][1], priority));
        }
        List<GanttBlock> chart = scheduler.schedule(processes);

        String gantt = "";
        for (GanttBlock b : chart) gantt += b + " ";
        boolean ok = gantt.trim().equals(expectedGantt);
        for (int i = 0; i < processes.size(); i++) {
            if (processes.get(i).getWaitingTime() != expectedWaiting[i]) ok = false;
        }
        report(name, ok, "gantt=" + gantt.trim() + String.format("  avgTAT=%.2f avgWT=%.2f avgRT=%.2f util=%.2f%%",
                CpuMetrics.averageTurnaroundTime(processes), CpuMetrics.averageWaitingTime(processes),
                CpuMetrics.averageResponseTime(processes), CpuMetrics.cpuUtilization(chart)));
    }

    /** Runs a disk algorithm and compares the head path and total head movement. */
    static void testDisk(String name, DiskScheduler scheduler, int[] requests, int head, int size,
                         boolean towardHigher, String expectedPath, int expectedTotal) {
        List<Integer> path = scheduler.schedule(requests, head, size, towardHigher);
        String pathText = "";
        for (int c : path) pathText += c + " ";
        int total = DiskMetrics.totalHeadMovement(path);
        boolean ok = pathText.trim().equals(expectedPath) && total == expectedTotal;
        report(name, ok, "path=" + pathText.trim() + "  total=" + total
                + String.format("  avg=%.2f", DiskMetrics.averageSeekDistance(path, requests.length)));
    }

    static void report(String name, boolean ok, String detail) {
        if (ok) passed++; else failed++;
        System.out.println((ok ? "[PASS] " : "[FAIL] ") + name);
        System.out.println("       " + detail);
    }
}
