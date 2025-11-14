package com.datasense;

import java.util.regex.Pattern;

// Normalizes messages into a pattern-like string.
// Strategy: replace numbers, UUID-like tokens, hex, and file paths with placeholders.
public class PatternLearner {
    private static final Pattern NUM = Pattern.compile("\\b\\d+\\b");
    private static final Pattern HEX = Pattern.compile("\\b0x[a-fA-F0-9]+\\b");
    private static final Pattern UUID = Pattern.compile("\\b[0-9a-fA-F]{8}-[0-9a-fA-F\\-]{27,36}\\b");
    private static final Pattern PATH = Pattern.compile("(/[\\w\\-\\.]+)+|[A-Za-z]:\\\\[\\\\\\w\\-\\.]+");

    public String normalize(String message) {
        if (message == null) return "";
        String s = message;
        s = UUID.matcher(s).replaceAll("<ID>");
        s = HEX.matcher(s).replaceAll("<HEX>");
        s = NUM.matcher(s).replaceAll("<NUM>");
        s = PATH.matcher(s).replaceAll("<PATH>");
        // collapse repeated spaces
        s = s.replaceAll("\\s+", " ").trim();
        return s;
    }
}
