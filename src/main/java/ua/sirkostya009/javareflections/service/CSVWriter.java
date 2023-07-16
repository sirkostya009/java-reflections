package ua.sirkostya009.javareflections.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CSVWriter extends FileWriter {
    public CSVWriter(File file) throws IOException {
        super(file);
    }

    public void write(Collection<String> row) throws IOException {
        write(collect(row.stream()));
    }

    public void write(String[] row) throws IOException {
        write(collect(Arrays.stream(row)));
    }

    private String collect(Stream<String> stream) {
        return stream.collect(Collectors.joining(",")) + "\n";
    }
}
