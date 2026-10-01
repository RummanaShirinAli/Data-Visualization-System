import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;

public class DataVisualizationApp {

    private final JFrame frame = new JFrame("Data Visualization System");
    private final JPanel chartContainer = new JPanel(new BorderLayout(8, 8));
    private final JLabel statusLabel = new JLabel("Open a CSV file to begin.");
    private final JComboBox<StudentRecord> recordSelector = new JComboBox<>();
    private final JComboBox<String> chartSelector =
            new JComboBox<>(new String[]{"Bar chart", "Pie chart"});
    private final JButton exportButton = new JButton("Export chart");
    private final JButton openButton = new JButton("Open CSV");

    private final ChartGenerator chartGenerator = new ChartGenerator();
    private JFreeChart currentChart;

    public DataVisualizationApp() {
        configureFrame();
        configureControls();
        buildLayout();
    }

    private void configureFrame() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(8, 8));
        frame.setMinimumSize(new java.awt.Dimension(760, 500));
        frame.setSize(1000, 650);
        frame.setLocationRelativeTo(null);
    }

    private void configureControls() {
        openButton.setToolTipText("Choose a CSV dataset to visualize");
        openButton.addActionListener(event -> selectCsvFile());

        recordSelector.setEnabled(false);
        recordSelector.setToolTipText("Choose a record to visualize");
        recordSelector.addActionListener(event -> refreshChart());

        chartSelector.setEnabled(false);
        chartSelector.setToolTipText("Choose the visualization type");
        chartSelector.addActionListener(event -> refreshChart());

        exportButton.setEnabled(false);
        exportButton.setToolTipText("Save the current chart as a PNG image");
        exportButton.addActionListener(event -> exportChart());

        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 6, 8));
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
    }

    private void buildLayout() {
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        controls.setBorder(BorderFactory.createEmptyBorder(4, 8, 0, 8));
        controls.add(openButton);
        controls.add(new JLabel("Record:"));
        controls.add(recordSelector);
        controls.add(new JLabel("Chart:"));
        controls.add(chartSelector);
        controls.add(exportButton);

        chartContainer.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        chartContainer.add(createWelcomeLabel(), BorderLayout.CENTER);

        frame.add(controls, BorderLayout.NORTH);
        frame.add(chartContainer, BorderLayout.CENTER);
        frame.add(statusLabel, BorderLayout.SOUTH);
    }

    private JLabel createWelcomeLabel() {
        JLabel label = new JLabel(
                "<html><div style='text-align:center;'>"
                        + "<h2>Data Visualization System</h2>"
                        + "Open a CSV dataset to visualize its records using bar or pie charts."
                        + "</div></html>",
                SwingConstants.CENTER);
        return label;
    }

    public void show() {
        frame.setVisible(true);
    }

    private void selectCsvFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select a CSV dataset");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));

        if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            Path selectedFile = chooser.getSelectedFile().toPath();
            List<StudentRecord> records = new CsvReader().read(selectedFile);

            recordSelector.removeAllItems();
            records.forEach(recordSelector::addItem);

            recordSelector.setEnabled(true);
            chartSelector.setEnabled(true);
            currentChart = null;
            exportButton.setEnabled(false);

            statusLabel.setText(
                    "Loaded " + records.size() + " record"
                            + (records.size() == 1 ? "" : "s")
                            + " from " + selectedFile.getFileName());

            refreshChart();
        } catch (IOException | SecurityException exception) {
            currentChart = null;
            exportButton.setEnabled(false);
            statusLabel.setText("Unable to load the selected dataset.");
            JOptionPane.showMessageDialog(
                    frame,
                    exception.getMessage(),
                    "Unable to read CSV",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshChart() {
        StudentRecord selectedRecord =
                (StudentRecord) recordSelector.getSelectedItem();

        if (selectedRecord == null || !chartSelector.isEnabled()) {
            return;
        }

        try {
            boolean pieChart = "Pie chart".equals(chartSelector.getSelectedItem());

            currentChart = pieChart
                    ? chartGenerator.createPieChart(selectedRecord)
                    : chartGenerator.createBarChart(selectedRecord);

            ChartPanel chartPanel = new ChartPanel(currentChart);
            chartPanel.setMouseWheelEnabled(true);
            chartPanel.setDisplayToolTips(true);
            chartPanel.setPreferredSize(new java.awt.Dimension(900, 520));

            chartContainer.removeAll();
            chartContainer.add(chartPanel, BorderLayout.CENTER);

            exportButton.setEnabled(true);
            statusLabel.setText(
                    "Showing " + selectedRecord.name()
                            + " (ID: " + selectedRecord.id() + ") — "
                            + selectedRecord.measurements().size()
                            + " measurements");

        } catch (IllegalArgumentException exception) {
            currentChart = null;
            chartContainer.removeAll();
            chartContainer.add(
                    new JLabel(exception.getMessage(), SwingConstants.CENTER),
                    BorderLayout.CENTER);
            exportButton.setEnabled(false);
            statusLabel.setText("The selected data cannot be displayed as a pie chart.");
        }

        chartContainer.revalidate();
        chartContainer.repaint();
    }

    private void exportChart() {
        if (currentChart == null) {
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export chart as PNG");
        chooser.setSelectedFile(Path.of("chart.png").toFile());
        chooser.setFileFilter(new FileNameExtensionFilter("PNG image (*.png)", "png"));

        if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path output = chooser.getSelectedFile().toPath();
        if (!output.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".png")) {
            output = output.resolveSibling(output.getFileName() + ".png");
        }

        if (output.toFile().exists()
                && JOptionPane.showConfirmDialog(
                        frame,
                        "Replace the existing file?",
                        "Confirm overwrite",
                        JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            ChartUtils.saveChartAsPNG(output.toFile(), currentChart, 1200, 800);
            statusLabel.setText("Chart exported successfully to " + output.getFileName());
            JOptionPane.showMessageDialog(
                    frame,
                    "Chart exported to:\n" + output.toAbsolutePath(),
                    "Export complete",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException | SecurityException exception) {
            JOptionPane.showMessageDialog(
                    frame,
                    exception.getMessage(),
                    "Unable to export chart",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void launch() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Keep the default Swing look and feel if the system look and feel is unavailable.
        }
        new DataVisualizationApp().show();
    }
}
