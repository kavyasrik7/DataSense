package com.datasense;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Analyzer {
    private final PatternLearner learner;
    // pattern -> (count, example message)
    private final Map<String, PatternInfo> patterns = new HashMap<>();
    private int total = 0;

    public Analyzer(PatternLearner learner) {
        this.learner = learner;
    }

    public void ingest(LogEntry entry) {
        total++;
        String norm = learner.normalize(entry.message);
        PatternInfo info = patterns.computeIfAbsent(norm, k -> new PatternInfo());
        info.count++;
        if (info.example == null) info.example = entry.message;
        info.levelCounts.merge(entry.level == null ? "INFO" : entry.level, 1, Integer::sum);
    }

    public void printSummary() {
        System.out.println("Total log lines: " + total);
        System.out.println("Distinct patterns: " + patterns.size());

        List<Map.Entry<String, PatternInfo>> top = patterns.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().count, a.getValue().count))
                .limit(10)
                .collect(Collectors.toList());

        System.out.println("\nTop patterns:");
        int rank=1;
        for (Map.Entry<String, PatternInfo> e : top) {
            System.out.printf("%d) [%d occurrences] %s%n   example: %s%n", rank++, e.getValue().count, e.getKey(), e.getValue().example);
        }
    }

    public void exportCSV(String outPath) throws IOException {
        List<Map.Entry<String, PatternInfo>> sorted = patterns.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().count, a.getValue().count))
                .collect(Collectors.toList());

        try (BufferedWriter w = new BufferedWriter(new FileWriter(outPath))) {
            w.write("rank,pattern,count,example,levels\n");
            int i=1;
            for (Map.Entry<String, PatternInfo> e : sorted) {
                PatternInfo p = e.getValue();
                String levels = p.levelCounts.entrySet().stream()
                        .map(kv -> kv.getKey()+":"+kv.getValue())
                        .collect(Collectors.joining("|"));
                String pat = escapeCsv(e.getKey());
                String ex = escapeCsv(p.example);
                w.write(String.format("%d,%s,%d,%s,%s%n", i++, pat, p.count, ex, levels));
            }
        }
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        String out = s.replace("\"", "\"\"");
        if (out.contains(",") || out.contains("\n") || out.contains("\"")) {
            out = "\"" + out + "\"";
        }
        return out;
    }

    private static class PatternInfo {
        int count = 0;
        String example = null;
        Map<String,Integer> levelCounts = new HashMap<>();
    }
}
