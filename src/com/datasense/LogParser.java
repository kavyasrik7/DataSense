package com.datasense;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Simple parser: expects lines with a timestamp and level, but flexible enough for many logs.
public class LogParser {
    // Try a few common timestamp patterns; fallback if not found.
    private static final DateTimeFormatter[] FORMATS = new DateTimeFormatter[]{
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
        DateTimeFormatter.ISO_DATE_TIME
    };

    // Very permissive regex: [timestamp] LEVEL message OR timestamp LEVEL message
    private static final Pattern LINE = Pattern.compile(
        "^(?:\\[?([0-9\\-:/ T\\.]+)\\]?\\s+)?(INFO|WARN|ERROR|DEBUG|TRACE)?\\s*(.*)$",
        Pattern.CASE_INSENSITIVE
    );

    public List<LogEntry> parseFile(Path path) throws IOException {
        List<LogEntry> out = new ArrayList<>();
        List<String> lines = Files.readAllLines(path);
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            Matcher m = LINE.matcher(trimmed);
            if (m.matches()) {
                String ts = m.group(1);
                String level = m.group(2) != null ? m.group(2).toUpperCase() : "INFO";
                String msg = m.group(3) != null ? m.group(3) : trimmed;

                LocalDateTime ldt = tryParseTimestamp(ts);
                out.add(new LogEntry(ldt, level, msg));
            } else {
                // fallback: whole line as message
                out.add(new LogEntry(null, "INFO", trimmed));
            }
        }
        return out;
    }

    private LocalDateTime tryParseTimestamp(String s) {
        if (s == null) return null;
        for (DateTimeFormatter f : FORMATS) {
            try {
                return LocalDateTime.parse(s, f);
            } catch (Exception ignore) {}
        }
        return null;
    }
}
