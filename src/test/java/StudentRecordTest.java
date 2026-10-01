import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class StudentRecordTest {

    @Test
    void trimsIdAndNameAndFormatsRecord() {
        StudentRecord record = new StudentRecord(" 42 ", " Alice ", Map.of("Math", 91.0));

        assertEquals("42", record.id());
        assertEquals("Alice", record.name());
        assertEquals("42 - Alice", record.toString());
    }

    @Test
    void copiesMeasurementsAndExposesAnUnmodifiableMap() {
        Map<String, Double> source = new LinkedHashMap<>();
        source.put("Math", 91.0);
        StudentRecord record = new StudentRecord("42", "Alice", source);
        source.put("Science", 88.0);

        assertEquals(Map.of("Math", 91.0), record.measurements());
        assertThrows(UnsupportedOperationException.class,
                () -> record.measurements().put("Science", 88.0));
    }

    @Test
    void rejectsNullComponents() {
        assertThrows(NullPointerException.class,
                () -> new StudentRecord(null, "Alice", Map.of()));
        assertThrows(NullPointerException.class,
                () -> new StudentRecord("42", null, Map.of()));
        assertThrows(NullPointerException.class,
                () -> new StudentRecord("42", "Alice", null));
    }
}
