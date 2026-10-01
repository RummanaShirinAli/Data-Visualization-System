import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record StudentRecord(String id, String name, Map<String, Double> measurements) {

    public StudentRecord {
        id = Objects.requireNonNull(id, "id").trim();
        name = Objects.requireNonNull(name, "name").trim();
        measurements = Collections.unmodifiableMap(new LinkedHashMap<>(
                Objects.requireNonNull(measurements, "measurements")));
    }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}
