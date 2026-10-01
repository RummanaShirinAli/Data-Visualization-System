import java.io.BufferedReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CsvReader {

    public List<StudentRecord> read(Path file) throws IOException {
        List<CsvRow> rows = readRows(file);
        if (rows.isEmpty()) {
            throw new IOException("The selected CSV file is empty.");
        }

        List<String> headers = new ArrayList<>(rows.get(0).fields());
        headers.set(0, headers.get(0).replace("\uFEFF", ""));
        if (headers.size() < 3) {
            throw new IOException("The CSV must contain an ID column, a name column, and at least one numeric data column.");
        }

        Set<String> uniqueHeaders = new LinkedHashSet<>();
        for (int i = 0; i < headers.size(); i++) {
            headers.set(i, headers.get(i).trim());
            if (headers.get(i).isEmpty()) {
                throw new IOException("CSV column " + (i + 1) + " has no header.");
            }
            if (!uniqueHeaders.add(headers.get(i))) {
                throw new IOException("Duplicate CSV column name: " + headers.get(i));
            }
        }

        List<StudentRecord> records = new ArrayList<>();
        Set<String> recordIds = new HashSet<>();
        for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
            CsvRow row = rows.get(rowIndex);
            List<String> values = row.fields();
            if (values.stream().allMatch(String::isBlank)) {
                continue;
            }
            if (values.size() != headers.size()) {
                throw new IOException("CSV row " + row.lineNumber() + " has " + values.size()
                        + " columns; expected " + headers.size() + ".");
            }

            String id = values.get(0).trim();
            String name = values.get(1).trim();
            if (id.isEmpty() || name.isEmpty()) {
                throw new IOException("CSV row " + row.lineNumber() + " must include both an ID and a name.");
            }
            if (!recordIds.add(id)) {
                throw new IOException("Duplicate ID '" + id + "' on CSV row " + row.lineNumber() + ".");
            }

            LinkedHashMap<String, Double> measurements = new LinkedHashMap<>();
            for (int column = 2; column < headers.size(); column++) {
                String value = values.get(column).trim();
                if (value.isEmpty()) {
                    throw new IOException("Missing value for '" + headers.get(column) + "' on CSV row "
                            + row.lineNumber() + ".");
                }
                try {
                    double number = Double.parseDouble(value);
                    if (!Double.isFinite(number)) {
                        throw new NumberFormatException("Value must be finite.");
                    }
                    measurements.put(headers.get(column), number);
                } catch (NumberFormatException exception) {
                    throw new IOException("Invalid number '" + value + "' for '" + headers.get(column)
                            + "' on CSV row " + row.lineNumber() + ".", exception);
                }
            }
            records.add(new StudentRecord(id, name, measurements));
        }

        if (records.isEmpty()) {
            throw new IOException("The CSV contains headers but no data rows.");
        }
        return List.copyOf(records);
    }

    private List<CsvRow> readRows(Path file) throws IOException {
        List<CsvRow> rows = new ArrayList<>();
        try (PushbackReader reader = new PushbackReader(
                new BufferedReader(Files.newBufferedReader(file, StandardCharsets.UTF_8)), 1)) {
            List<String> fields = new ArrayList<>();
            StringBuilder field = new StringBuilder();
            boolean inQuotes = false;
            boolean closedQuote = false;
            int lineNumber = 1;
            int rowStartLine = 1;
            int character;

            while ((character = reader.read()) != -1) {
                char current = (char) character;
                if (inQuotes) {
                    if (current == '"') {
                        int next = reader.read();
                        if (next == '"') {
                            field.append('"');
                        } else {
                            inQuotes = false;
                            closedQuote = true;
                            if (next != -1) {
                                reader.unread(next);
                            }
                        }
                    } else if (current == '\r' || current == '\n') {
                        appendLineBreak(reader, current, field);
                        lineNumber++;
                    } else {
                        field.append(current);
                    }
                    continue;
                }

                if (current == '"') {
                    if (field.length() != 0 || closedQuote) {
                        throw new IOException("Unexpected quote on CSV row " + lineNumber + ".");
                    }
                    inQuotes = true;
                } else if (current == ',') {
                    fields.add(field.toString());
                    field.setLength(0);
                    closedQuote = false;
                } else if (current == '\r' || current == '\n') {
                    appendLineBreak(reader, current, null);
                    fields.add(field.toString());
                    rows.add(new CsvRow(List.copyOf(fields), rowStartLine));
                    fields.clear();
                    field.setLength(0);
                    closedQuote = false;
                    lineNumber++;
                    rowStartLine = lineNumber;
                } else if (closedQuote) {
                    if (!Character.isWhitespace(current)) {
                        throw new IOException("Unexpected text after a quoted value on CSV row " + lineNumber + ".");
                    }
                } else {
                    field.append(current);
                }
            }

            if (inQuotes) {
                throw new IOException("Unclosed quoted value starting on CSV row " + rowStartLine + ".");
            }
            if (!fields.isEmpty() || field.length() > 0 || closedQuote) {
                fields.add(field.toString());
                rows.add(new CsvRow(List.copyOf(fields), rowStartLine));
            }
        }
        return rows;
    }

    private void appendLineBreak(PushbackReader reader, char current, StringBuilder field) throws IOException {
        if (current == '\r') {
            int next = reader.read();
            if (next != '\n' && next != -1) {
                reader.unread(next);
            }
        }
        if (field != null) {
            field.append('\n');
        }
    }

    private record CsvRow(List<String> fields, int lineNumber) {
    }
}
