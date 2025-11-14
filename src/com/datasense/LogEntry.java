package com.datasense;

import java.time.LocalDateTime;

public class LogEntry {
    public final LocalDateTime timestamp;
    public final String level;
    public final String message;

    public LogEntry(LocalDateTime timestamp, String level, String message) {
        this.timestamp = timestamp;
        this.level = level;
        this.message = message;
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + level + " " + message;
    }
}
