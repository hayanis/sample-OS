/*
 * Shared interface (group).
 * Every CPU scheduling algorithm implements these three methods,
 * so the same CPU screen can run any of them.
 */
package cpu;

import java.util.List;

public interface CpuScheduler {

    /** Name shown on the menu and the screen title. */
    String getName();

    /** True if the input table needs a Priority column. */
    boolean usesPriority();

    /**
     * Schedules the processes (given in PID order: P1, P2, ...).
     * Fills in each process's startTime and completionTime,
     * and returns the Gantt chart.
     */
    List<GanttBlock> schedule(List<Process> processes);
}
