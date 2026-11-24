/*
 * Shared class (group).
 * Holds one process: its input values and the times computed by the algorithm.
 */
package cpu;

public class Process {

    // Input values
    public String pid;
    public int arrivalTime;
    public int burstTime;
    public int priority;        // lower number = higher priority

    // Filled in by the scheduling algorithm
    public int startTime = -1;  // first time the process gets the CPU (-1 = not yet)
    public int completionTime;
    public int remainingTime;   // burst time still left (used by the preemptive algorithm)

    public Process(String pid, int arrivalTime, int burstTime, int priority) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        this.remainingTime = burstTime;
    }

    // Formulas from the course conventions
    public int getTurnaroundTime() {
        return completionTime - arrivalTime;
    }

    public int getWaitingTime() {
        return getTurnaroundTime() - burstTime;
    }

    public int getResponseTime() {
        return startTime - arrivalTime;
    }
}
