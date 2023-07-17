package ua.sirkostya009.javareflections.parser;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.web.multipart.MultipartFile;
import ua.sirkostya009.javareflections.annotation.NameContains;
import ua.sirkostya009.javareflections.annotation.Parse;
import ua.sirkostya009.javareflections.annotation.Parser;
import ua.sirkostya009.javareflections.model.Customer;
import ua.sirkostya009.javareflections.service.EmptyService;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Parser(
        customer = Customer.TEST,
        name = "Sample Parser",
        sourceFormatBeanQualifier = "sampleSourceFormat",
        resultFormatBeanQualifier = "sampleResultFormat"
)
public class SampleParser {

    private final EmptyService emptyService;

    @Bean
    public CSVFormat sampleResultFormat() {
        return CSVFormat.MONGODB_CSV;
    }

    @Bean
    public CSVFormat sampleSourceFormat() {
        return CSVFormat.DEFAULT.withSkipHeaderRecord().withFirstRecordAsHeader().withIgnoreEmptyLines().withTrim();
    }

    @PostConstruct
    public void init() {
        log.info("@PostConstruct called; emptyService: {}", emptyService);
    }

    @Parse
    public void pass1(@NameContains("1") MultipartFile file, List<MultipartFile> files) {
        log.info("pass 1, file: {}", file.getOriginalFilename());
        log.info(files.toString());
    }

    @Parse(pass = 2)
    public void pass2(@NameContains("2") CSVParser parser,
                      CSVPrinter printer,
                      String id,
                      @Qualifier("sampleSourceFormat") CSVFormat sourceFormat) throws IOException {
        parser.stream()
                .map(CSVRecord::values)
                .forEach(values -> {
                    try {
                        printer.printRecord((Object[]) values);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        log.info("pass 2, id: {}", id);
        log.info("pass 2, headers: {}", parser.getHeaderNames());
        log.info("pass 2, source format: {}", sourceFormat);
        parser.close(); // IMPORTANT! Close CSVParser after you're done working with it
    }

    @Parse(pass = 3)
    public void finalParse(CSVParser[] parsers, Writer writer) throws IOException {
        log.info("Finished parsing");
        writer.write("you got the cuh");
        log.info("Parsers: {}", Arrays.stream(parsers).flatMap(CSVParser::stream).toList());
        for (var parser : parsers)
            parser.close();
    }

}
