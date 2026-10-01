import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvReaderTest {

    @TempDir
    Path temporaryDirectory;

    private final CsvReader reader = new CsvReader();

    @Test
    void readsTrimmedHeadersQuotedCommasAndMultilineFields() throws IOException {
        Path file = writeCsv("\uFEFF ID, Name, Score\r\n"
                + "1,\"Alice, A.\",91\r\n"
                + "2,\"Bob\nBuilder\",82\r\n");

        List<StudentRecord> records = reader.read(file);

        assertEquals(2, records.size());
        assertEquals("Alice, A.", records.get(0).name());
        assertEquals(91.0, records.get(0).measurements().get("Score"));
        assertEquals("Bob\nBuilder", records.get(1).name());
        assertEquals(List.of("Score"), List.copyOf(records.get(0).measurements().keySet()));
    }

    @Test
    void skipsBlankRowsAndReturnsAnImmutableList() throws IOException {
        Path file = writeCsv("id,name,score\n1,Alice,91\n,,\n");

        List<StudentRecord> records = reader.read(file);

        assertEquals(1, records.size());
        assertThrows(UnsupportedOperationException.class,
                () -> records.add(new StudentRecord("2", "Bob", java.util.Map.of("score", 82.0))));
    }

    @Test
    void rejectsEmptyFileAndHeadersWithoutMeasurementColumns() throws IOException {
        IOException emptyFailure = assertThrows(IOException.class, () -> reader.read(writeCsv("")));
        assertTrue(emptyFailure.getMessage().contains("empty"));

        IOException headerFailure = assertThrows(IOException.class,
                () -> reader.read(writeCsv("id,name\n")));
        assertTrue(headerFailure.getMessage().contains("at least one numeric data column"));
    }

    @Test
    void rejectsDuplicateOrBlankHeaders() throws IOException {
        assertTrue(assertThrows(IOException.class,
                () -> reader.read(writeCsv("id,name,score,score\n1,Alice,1,2\n")))
                .getMessage().contains("Duplicate CSV column"));
        assertTrue(assertThrows(IOException.class,
                () -> reader.read(writeCsv("id,name,\n1,Alice,1\n")))
                .getMessage().contains("has no header"));
    }

    @Test
    void rejectsRowsWithWrongColumnCountOrMissingIdentity() throws IOException {
        assertTrue(assertThrows(IOException.class,
                () -> reader.read(writeCsv("id,name,score\n1,Alice\n")))
                .getMessage().contains("expected 3"));
        assertTrue(assertThrows(IOException.class,
                () -> reader.read(writeCsv("id,name,score\n1,,90\n")))
                .getMessage().contains("both an ID and a name"));
    }

    @Test
    void rejectsDuplicateIdsAndMissingOrInvalidMeasurements() throws IOException {
        assertTrue(assertThrows(IOException.class,
                () -> reader.read(writeCsv("id,name,score\n1,Alice,90\n1,Bob,80\n")))
                .getMessage().contains("Duplicate ID"));
        assertTrue(assertThrows(IOException.class,
                () -> reader.read(writeCsv("id,name,score\n1,Alice,\n")))
                .getMessage().contains("Missing value"));
        assertTrue(assertThrows(IOException.class,
                () -> reader.read(writeCsv("id,name,score\n1,Alice,NaN\n")))
                .getMessage().contains("Invalid number"));
    }

    @Test
    void rejectsUnclosedAndMalformedQuotedFields() throws IOException {
        assertTrue(assertThrows(IOException.class,
                () -> reader.read(writeCsv("id,name,score\n1,\"Alice,90\n")))
                .getMessage().contains("Unclosed quoted value"));
        assertTrue(assertThrows(IOException.class,
                () -> reader.read(writeCsv("id,name,score\n1,\"Alice\"unexpected,90\n")))
                .getMessage().contains("Unexpected text after a quoted value"));
    }

    @Test
    void rejectsHeaderOnlyFiles() throws IOException {
        assertTrue(assertThrows(IOException.class,
                () -> reader.read(writeCsv("id,name,score\n")))
                .getMessage().contains("no data rows"));
    }

    private Path writeCsv(String content) throws IOException {
        Path file = temporaryDirectory.resolve("dataset.csv");
        return Files.writeString(file, content);
    }
}
