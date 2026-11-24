/*
 * Shared class (Leader).
 * The screen for a disk scheduling algorithm. C-SCAN and C-LOOK use this same
 * screen; only the scheduler object passed in is different.
 * Left side = input. Right side = head path graph, steps table and summary.
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
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import disk.DiskMetrics;
import disk.DiskScheduler;

public class DiskScreen extends JFrame {

    private static final String SAMPLE_REQUESTS = "98, 183, 37, 122, 14, 124, 65, 67";
    private static final String TOWARD_HIGHER = "Toward higher (to N-1)";
    private static final String TOWARD_LOWER = "Toward lower (to 0)";

    private DiskScheduler scheduler;

    private JTextField sizeField = new JTextField("200", 8);
    private JTextField headField = new JTextField(8);
    private JComboBox<String> directionBox = new JComboBox<String>(new String[]{TOWARD_HIGHER, TOWARD_LOWER});
    private JTextArea requestsArea = new JTextArea(5, 20);

    private HeadPathGraph graph = new HeadPathGraph();
    private DefaultTableModel stepModel;
    private JTextArea summaryArea = new JTextArea(5, 40);

    public DiskScreen(DiskScheduler scheduler) {
        super(scheduler.getName());
        this.scheduler = scheduler;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // ---------- Input ----------
        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));
        form.add(new JLabel("Disk size N (cylinders):"));
        form.add(sizeField);
        form.add(new JLabel("Initial head position:"));
        form.add(headField);
        form.add(new JLabel("Initial direction:"));
        form.add(directionBox);

        requestsArea.setLineWrap(true);
        JPanel requestsPanel = new JPanel(new BorderLayout());
        requestsPanel.add(new JLabel("Request queue (separate with commas or spaces):"), BorderLayout.NORTH);
        requestsPanel.add(new JScrollPane(requestsArea), BorderLayout.CENTER);

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

        JPanel inputPanel = new JPanel(new BorderLayout(5, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Input"));
        inputPanel.setPreferredSize(new Dimension(380, 0));
        inputPanel.add(form, BorderLayout.NORTH);
        inputPanel.add(requestsPanel, BorderLayout.CENTER);
        inputPanel.add(buttons, BorderLayout.SOUTH);

        // ---------- Output ----------
        stepModel = new DefaultTableModel(new String[]{"Step", "From", "To", "Head Movement", "Note"}, 0);
        JTable stepTable = new JTable(stepModel);
        stepTable.setEnabled(false);   // read only
        summaryArea.setEditable(false);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        summaryArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        JScrollPane graphScroll = new JScrollPane(graph);
        graphScroll.setBorder(BorderFactory.createTitledBorder("Head Path (dashed line = return jump)"));
        JScrollPane summaryScroll = new JScrollPane(summaryArea);
        summaryScroll.setBorder(BorderFactory.createTitledBorder("Summary"));
        JScrollPane stepScroll = new JScrollPane(stepTable);
        stepScroll.setBorder(BorderFactory.createTitledBorder("Head Movement per Step"));
        stepScroll.setPreferredSize(new Dimension(600, 200));

        JPanel bottom = new JPanel(new BorderLayout(5, 5));
        bottom.add(summaryScroll, BorderLayout.NORTH);
        bottom.add(stepScroll, BorderLayout.CENTER);

        JPanel outputPanel = new JPanel(new BorderLayout(5, 5));
        outputPanel.add(graphScroll, BorderLayout.CENTER);
        outputPanel.add(bottom, BorderLayout.SOUTH);

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

        setSize(1150, 720);
        setLocationRelativeTo(null);
    }

    // ===================== Buttons =====================

    private void loadSample() {
        sizeField.setText("200");
        headField.setText("53");
        directionBox.setSelectedItem(TOWARD_HIGHER);
        requestsArea.setText(SAMPLE_REQUESTS);
        clearOutput();
    }

    private void reset() {
        sizeField.setText("200");
        headField.setText("");
        directionBox.setSelectedItem(TOWARD_HIGHER);
        requestsArea.setText("");
        clearOutput();
    }

    private void compute() {
        try {
            int diskSize = InputCheck.readNumber(sizeField.getText(), "Disk size N", 2, 10000);
            int head = InputCheck.readNumber(headField.getText(), "Initial head position", 0, diskSize - 1);
            int[] requests = readRequests(diskSize);
            boolean towardHigher = directionBox.getSelectedItem().equals(TOWARD_HIGHER);

            List<Integer> path = scheduler.schedule(requests, head, diskSize, towardHigher);
            showResults(path, requests.length, diskSize, towardHigher);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid input", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===================== Reading the input =====================

    /** Splits the request text into numbers and checks each one. */
    private int[] readRequests(int diskSize) {
        String text = requestsArea.getText().trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Enter at least one request.");
        }
        String[] parts = text.split("[,\\s]+");     // split on commas and/or spaces
        if (parts.length > 100) {
            throw new IllegalArgumentException("Too many requests (" + parts.length + "). The maximum is 100.");
        }
        int[] requests = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            requests[i] = InputCheck.readNumber(parts[i], "Request #" + (i + 1), 0, diskSize - 1);
        }
        return requests;
    }

    // ===================== Showing the output =====================

    private void showResults(List<Integer> path, int numberOfRequests, int diskSize, boolean towardHigher) {
        graph.setPath(path, diskSize, towardHigher);

        stepModel.setRowCount(0);
        for (int i = 1; i < path.size(); i++) {
            int from = path.get(i - 1);
            int to = path.get(i);
            boolean isJump = towardHigher ? to < from : to > from;   // moving backward = the return jump
            stepModel.addRow(new Object[]{i, from, to, Math.abs(to - from), isJump ? "return jump" : ""});
        }

        String sequence = "";
        for (int i = 0; i < path.size(); i++) {
            sequence += (i == 0 ? "" : " -> ") + path.get(i);
        }
        int total = DiskMetrics.totalHeadMovement(path);
        summaryArea.setText(
                "Seek sequence: " + sequence + "\n"
              + "Total head movement   = " + total + " cylinders\n"
              + String.format("Average seek distance = %d / %d = %.2f cylinders%n",
                        total, numberOfRequests, DiskMetrics.averageSeekDistance(path, numberOfRequests))
              + "Number of requests    = " + numberOfRequests);
    }

    private void clearOutput() {
        graph.setPath(new ArrayList<Integer>(), 200, true);
        stepModel.setRowCount(0);
        summaryArea.setText("");
    }
}
