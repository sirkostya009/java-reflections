package ua.sirkostya009.javareflections.parser;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import ua.sirkostya009.javareflections.annotation.NameContains;
import ua.sirkostya009.javareflections.annotation.Parse;
import ua.sirkostya009.javareflections.annotation.Parser;
import ua.sirkostya009.javareflections.model.Customer;
import ua.sirkostya009.javareflections.service.CSVWriter;
import ua.sirkostya009.javareflections.service.EmptyService;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Parser(customer = Customer.TEST, name = "Sample Parser")
public class SampleParser {
    private final EmptyService emptyService;

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
    public void pass2(@NameContains("2") MultipartFile file, Writer writer, String id) throws IOException {
        writer.write(id);
        log.info("pass 2, file: {}", file.getOriginalFilename());
    }

    @Parse(pass = 3)
    public void finalParse(CSVWriter writer) throws IOException {
        log.info("Finished parsing");
        writer.write(new String[]{"asd"});
    }
}
