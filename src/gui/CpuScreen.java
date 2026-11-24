/*
 * Shared class (Leader).
 * The screen for a CPU scheduling algorithm. FCFS and Priority use this same
 * screen; only the scheduler object passed in is different.
 * Left side = input. Right side = Gantt chart, results table and metrics.
 */
package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import cpu.CpuMetrics;
import cpu.CpuScheduler;
import cpu.GanttBlock;
import cpu.Process;

public class CpuScreen extends JFrame {

    // Sample data for "Load Sample Data". FCFS: {arrival, burst}. Priority: {arrival, burst, priority}.
    private static final int[][] FCFS_SAMPLE = {{0, 4}, {1, 3}, {2, 1}, {10, 2}, {10, 3}};
    private static final int[][] PRIORITY_SAMPLE = {{0, 7, 3}, {2, 4, 1}, {4, 1, 4}, {5, 4, 2}, {6, 2, 2}};

    private CpuScheduler scheduler;

    private JTextField countField = new JTextField("3", 4);
    private DefaultTableModel inputModel;
    private JTable inputTable;

    private GanttChart ganttChart = new GanttChart();
    private DefaultTableModel resultModel;
    private JTextArea metricsArea = new JTextArea(5, 40);

    public CpuScreen(CpuScheduler scheduler) {
        super(scheduler.getName());
        this.scheduler = scheduler;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // ---------- Input table ----------
        if (scheduler.usesPriority()) {
            inputModel = new DefaultTableModel(new String[]{"PID", "Arrival Time", "Burst Time", "Priority"}, 0);
        } else {
            inputModel = new DefaultTableModel(new String[]{"PID", "Arrival Time", "Burst Time"}, 0);
        }
        inputTable = new JTable(inputModel);
        setNumberOfRows(3);

        JButton setButton = new JButton("Set");
        setButton.addActionListener(e -> onSet());
        JPanel countRow = new JPanel();
        countRow.add(new JLabel("Number of processes (1-20):"));
        countRow.add(countField);
        countRow.add(setButton);

        JButton sampleButton = new JButton("Load Sample Data");
        JButton resetButton = new JButton("Reset / Clear");
        JButton computeButton = new JButton("Compute");
        sampleButton.addActionListener(e -> loadSample());
        resetButton.addActionListener(e -> reset());
        computeButton.addActionListener(e -> compute());
        JPanel buttons = new JPanel(new GridLayout(3, 1, 5, 5));
        buttons.add(sampleButton);
        buttons.add(resetButton);
        buttons.add(computeButton);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Input"));
        inputPanel.setPreferredSize(new Dimension(380, 0));
        inputPanel.add(countRow, BorderLayout.NORTH);
        inputPanel.add(new JScrollPane(inputTable), BorderLayout.CENTER);
        inputPanel.add(buttons, BorderLayout.SOUTH);

        // ---------- Output ----------
        String[] resultColumns = {"PID", "Arrival", "Burst", "Priority", "Completion", "Turnaround", "Waiting", "Response"};
        resultModel = new DefaultTableModel(resultColumns, 0);
        JTable resultTable = new JTable(resultModel);
        resultTable.setEnabled(false);   // read only
        metricsArea.setEditable(false);
        metricsArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        JScrollPane ganttScroll = new JScrollPane(ganttChart);
        ganttScroll.setPreferredSize(new Dimension(600, 145));
        ganttScroll.setBorder(BorderFactory.createTitledBorder("Gantt Chart"));
        JScrollPane resultScroll = new JScrollPane(resultTable);
        resultScroll.setBorder(BorderFactory.createTitledBorder("Results per Process"));
        JScrollPane metricsScroll = new JScrollPane(metricsArea);
        metricsScroll.setBorder(BorderFactory.createTitledBorder("Averages and Performance"));

        JPanel outputPanel = new JPanel(new BorderLayout(5, 5));
        outputPanel.add(ganttScroll, BorderLayout.NORTH);
        outputPanel.add(resultScroll, BorderLayout.CENTER);
        outputPanel.add(metricsScroll, BorderLayout.SOUTH);

        // ---------- Top bar ----------
        JLabel title = new JLabel(scheduler.getName());
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        JButton backButton = new JButton("Back to Menu");
        backButton.addActionListener(e -> dispose());
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.add(title, BorderLayout.WEST);
        topBar.add(backButton, BorderLayout.EAST);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        content.add(topBar, BorderLayout.NORTH);
        content.add(inputPanel, BorderLayout.WEST);
        content.add(outputPanel, BorderLayout.CENTER);
        setContentPane(content);

        setSize(1150, 680);
        setLocationRelativeTo(null);
    }

    // ===================== Buttons =====================

    private void onSet() {
        try {
            int n = InputCheck.readNumber(countField.getText(), "Number of processes", 1, 20);
            setNumberOfRows(n);
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    /** Changes the number of rows. Rows already typed in are kept; new rows get a PID. */
    private void setNumberOfRows(int n) {
        inputModel.setRowCount(n);
        for (int row = 0; row < n; row++) {
            if (inputModel.getValueAt(row, 0) == null) {
                inputModel.setValueAt("P" + (row + 1), row, 0);
            }
        }
        countField.setText(String.valueOf(n));
    }

    private void loadSample() {
        int[][] sample = scheduler.usesPriority() ? PRIORITY_SAMPLE : FCFS_SAMPLE;
        inputModel.setRowCount(0);
        for (int i = 0; i < sample.length; i++) {
            if (scheduler.usesPriority()) {
                inputModel.addRow(new Object[]{"P" + (i + 1), "" + sample[i][0], "" + sample[i][1], "" + sample[i][2]});
            } else {
                inputModel.addRow(new Object[]{"P" + (i + 1), "" + sample[i][0], "" + sample[i][1]});
            }
        }
        countField.setText(String.valueOf(sample.length));
        clearOutput();
    }

    private void reset() {
        inputModel.setRowCount(0);
        setNumberOfRows(3);
        clearOutput();
    }

    private void compute() {
        // Finish any cell that is still being typed in.
        if (inputTable.isEditing()) {
            inputTable.getCellEditor().stopCellEditing();
        }
        try {
            List<Process> processes = readProcesses();
            List<GanttBlock> chart = scheduler.schedule(processes);
            showResults(processes, chart);
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    // ===================== Reading the input =====================

    /** Reads every row of the table into a Process, checking each value. */
    private List<Process> readProcesses() {
        List<Process> processes = new ArrayList<Process>();
        List<String> usedPids = new ArrayList<String>();

        for (int row = 0; row < inputModel.getRowCount(); row++) {
            String pid = cell(row, 0);
            String rowName = "Row " + (row + 1) + ": ";
            if (pid.isEmpty()) {
                throw new IllegalArgumentException(rowName + "PID is required.");
            }
            if (usedPids.contains(pid)) {
                throw new IllegalArgumentException(rowName + "PID " + pid + " is used more than once.");
            }
            usedPids.add(pid);

            int arrival = InputCheck.readNumber(cell(row, 1), rowName + "Arrival Time", 0, 1000);
            int burst = InputCheck.readNumber(cell(row, 2), rowName + "Burst Time", 1, 1000);
            int priority = 0;
            if (scheduler.usesPriority()) {
                priority = InputCheck.readNumber(cell(row, 3), rowName + "Priority", 0, 999);
            }
            processes.add(new Process(pid, arrival, burst, priority));
        }
        return processes;
    }

    /** Returns the text in a table cell ("" if empty). */
    private String cell(int row, int column) {
        Object value = inputModel.getValueAt(row, column);
        return (value == null) ? "" : value.toString().trim();
    }

    // ===================== Showing the output =====================

    private void showResults(List<Process> processes, List<GanttBlock> chart) {
        ganttChart.setChart(chart);

        resultModel.setRowCount(0);
        for (Process p : processes) {
            String priority = scheduler.usesPriority() ? "" + p.priority : "-";
            resultModel.addRow(new Object[]{p.pid, p.arrivalTime, p.burstTime, priority, p.completionTime,
                    p.getTurnaroundTime(), p.getWaitingTime(), p.getResponseTime()});
        }

        int total = CpuMetrics.totalTime(chart);
        int busy = CpuMetrics.busyTime(chart);
        metricsArea.setText(
                String.format("Average Turnaround Time = %.2f%n", CpuMetrics.averageTurnaroundTime(processes))
              + String.format("Average Waiting Time    = %.2f%n", CpuMetrics.averageWaitingTime(processes))
              + String.format("Average Response Time   = %.2f%n", CpuMetrics.averageResponseTime(processes))
              + String.format("CPU Utilization         = %.2f%%   (busy %d / total %d)%n",
                        CpuMetrics.cpuUtilization(chart), busy, total)
              + String.format("Throughput              = %.4f processes per time unit   (%d / %d)",
                        CpuMetrics.throughput(processes, chart), processes.size(), total));
    }

    private void clearOutput() {
        ganttChart.setChart(new ArrayList<GanttBlock>());
        resultModel.setRowCount(0);
        metricsArea.setText("");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Invalid input", JOptionPane.ERROR_MESSAGE);
    }
}
