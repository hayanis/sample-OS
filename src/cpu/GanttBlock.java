/*
 * Shared class (group).
 * One block of the Gantt chart: a process (or "Idle") from a start time to an end time.
 */
package cpu;

import java.util.List;

public class GanttBlock {

    public String name;   // process ID, or "Idle"
    public int start;
    public int end;

    public GanttBlock(String name, int start, int end) {
        this.name = name;
        this.start = start;
        this.end = end;
    }

    /**
     * Adds a block to the chart. If the last block is the same process and ends
     * exactly where this one starts, the last block is simply made longer instead.
     * Example: P1[0-1] + P1[1-2] becomes P1[0-2].
     */
    public static void add(List<GanttBlock> chart, String name, int start, int end) {
        if (!chart.isEmpty()) {
            GanttBlock last = chart.get(chart.size() - 1);
            if (last.name.equals(name) && last.end == start) {
                last.end = end;
                return;
            }
        }
        chart.add(new GanttBlock(name, start, end));
    }

    @Override
    public String toString() {
        return name + "[" + start + "-" + end + "]";
    }
}
