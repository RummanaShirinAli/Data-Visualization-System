import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.PieDataset;
import org.junit.jupiter.api.Test;


class ChartGeneratorTest {

    private final ChartGenerator generator = new ChartGenerator();

    @Test
    void createsBarChartWithAllMeasurementsAndRecordTitle() {
        StudentRecord record = new StudentRecord("42", "Alice", Map.of("Math", 91.0, "Science", 88.0));

        JFreeChart chart = generator.createBarChart(record);
        CategoryPlot plot = chart.getCategoryPlot();
        CategoryDataset dataset = plot.getDataset();

        assertEquals("Measurements for Alice (ID: 42)", chart.getTitle().getText());
        assertEquals(1, dataset.getRowCount());
        assertEquals(2, dataset.getColumnCount());
        assertEquals("Alice", dataset.getRowKey(0));
        assertEquals(91.0, dataset.getValue("Alice", "Math"));
        assertEquals(88.0, dataset.getValue("Alice", "Science"));
        assertEquals(true, plot.getRangeAxis().isAutoRange());
    }

    @Test
    void createsPieChartForPositiveNonNegativeMeasurements() {
        StudentRecord record = new StudentRecord("42", "Alice", Map.of("Math", 91.0, "Science", 0.0));

        JFreeChart chart = generator.createPieChart(record);
        PiePlot<?> plot = (PiePlot<?>) chart.getPlot();
        @SuppressWarnings("unchecked")
        PieDataset<String> dataset = (PieDataset<String>) plot.getDataset();

        assertEquals("Measurements for Alice (ID: 42)", chart.getTitle().getText());
        assertEquals(91.0, dataset.getValue("Math"));
        assertEquals(0.0, dataset.getValue("Science"));
    }

    @Test
    void rejectsNegativeMeasurements() {
        StudentRecord record = new StudentRecord("42", "Alice", Map.of("Math", -1.0, "Science", 2.0));

        assertThrows(IllegalArgumentException.class, () -> generator.createPieChart(record));
    }

    @Test
    void rejectsZeroOrNonFinitePieTotals() {
        StudentRecord zeroTotal = new StudentRecord("42", "Alice", Map.of("Math", 0.0));
        StudentRecord infiniteTotal = new StudentRecord("42", "Alice",
                Map.of("Math", Double.MAX_VALUE, "Science", Double.MAX_VALUE));
        StudentRecord notANumber = new StudentRecord("42", "Alice", Map.of("Math", Double.NaN));

        assertThrows(IllegalArgumentException.class, () -> generator.createPieChart(zeroTotal));
        assertThrows(IllegalArgumentException.class, () -> generator.createPieChart(infiniteTotal));
        assertThrows(IllegalArgumentException.class, () -> generator.createPieChart(notANumber));
    }
}
