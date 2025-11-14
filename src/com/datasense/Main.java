package com.datasense;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        String input = "logs/sample.log";
        if (args.length >= 1) input = args[0];

        Path logPath = Paths.get(input);
        LogParser parser = new LogParser();
        PatternLearner learner = new PatternLearner();
        Analyzer analyzer = new Analyzer(learner);

        System.out.println("Reading: " + logPath.toAbsolutePath());
        try {
            parser.parseFile(logPath).forEach(analyzer::ingest);
            analyzer.printSummary();
            analyzer.exportCSV("output/datasense_summary.csv");
            System.out.println("Exported CSV -> output/datasense_summary.csv");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
