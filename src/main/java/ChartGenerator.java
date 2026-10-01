import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

public class ChartGenerator {

    private static final Color CHART_BACKGROUND = new Color(248, 249, 252);
    private static final Color GRIDLINE_COLOR = new Color(220, 224, 230);
    private static final Color BAR_COLOR = new Color(59, 130, 246);

    public JFreeChart createBarChart(StudentRecord record) {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        record.measurements().forEach((label, value) ->
                dataset.addValue(value, record.name(), label)
        );

        JFreeChart chart = ChartFactory.createBarChart(
                "Measurements for " + record.name() + " (ID: " + record.id() + ")",
                "Measurement",
                "Value",
                dataset
        );

        CategoryPlot plot = chart.getCategoryPlot();

        // Background and gridlines
        plot.setBackgroundPaint(CHART_BACKGROUND);
        plot.setRangeGridlinePaint(GRIDLINE_COLOR);
        plot.setRangeGridlineStroke(new BasicStroke(1f));
        plot.setOutlineVisible(false);

        // Chart spacing
        plot.setInsets(new RectangleInsets(10, 10, 10, 10));

        // Domain axis
        plot.getDomainAxis().setTickLabelFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );

        // Range axis
        plot.getRangeAxis().setTickLabelFont(
                new Font("SansSerif", Font.PLAIN, 11)
        );

        // Bar renderer
        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(
                new StandardCategoryItemLabelGenerator()
        );

        renderer.setDefaultItemLabelFont(
                new Font("SansSerif", Font.BOLD, 11)
        );

        renderer.setSeriesPaint(0, BAR_COLOR);

        // Slightly narrower bars for cleaner spacing
        renderer.setMaximumBarWidth(0.15);

        // Chart title
        chart.getTitle().setFont(
                new Font("SansSerif", Font.BOLD, 18)
        );

        chart.getTitle().setPadding(8, 0, 12, 0);

        return chart;
    }

    public JFreeChart createPieChart(StudentRecord record) {

        if (!canCreatePieChart(record)) {
            throw new IllegalArgumentException(
                    "Pie chart requires non-negative finite measurements with a positive finite total."
            );
        }

        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

        record.measurements().forEach(dataset::setValue);

        JFreeChart chart = ChartFactory.createPieChart(
                "Measurements for " + record.name() + " (ID: " + record.id() + ")",
                dataset,
                true,
                true,
                false
        );

        PiePlot<?> plot = (PiePlot<?>) chart.getPlot();

        // Background
        plot.setBackgroundPaint(CHART_BACKGROUND);
        plot.setOutlineVisible(false);

        // Remove 3D-style shadow
        plot.setShadowPaint(null);

        // Labels: Subject + percentage
        plot.setLabelGenerator(
                new StandardPieSectionLabelGenerator(
                        "{0}: {2}",
                        java.text.NumberFormat.getNumberInstance(),
                        java.text.NumberFormat.getPercentInstance()
                )
        );

        plot.setLabelFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );

        plot.setLabelBackgroundPaint(Color.WHITE);
        plot.setLabelOutlinePaint(GRIDLINE_COLOR);
        plot.setLabelShadowPaint(null);

        // Cleaner label placement
        plot.setLabelGap(0.03);

        // Title
        chart.getTitle().setFont(
                new Font("SansSerif", Font.BOLD, 18)
        );

        chart.getTitle().setPadding(8, 0, 12, 0);

        // Legend font
        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(
                    new Font("SansSerif", Font.PLAIN, 11)
            );
        }

        return chart;
    }

    private boolean canCreatePieChart(StudentRecord record) {

        double total = record.measurements()
                .values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        return total > 0
                && Double.isFinite(total)
                && record.measurements()
                        .values()
                        .stream()
                        .allMatch(value ->
                                value >= 0 && Double.isFinite(value)
                        );
    }
}