package ua.sirkostya009.javareflections.parser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import ua.sirkostya009.javareflections.annotation.NameContains;
import ua.sirkostya009.javareflections.annotation.Parse;
import ua.sirkostya009.javareflections.annotation.Parser;
import ua.sirkostya009.javareflections.model.Customer;

import java.io.Writer;
import java.util.List;

@Slf4j
@Parser(customer = Customer.TEST, name = "Sample Parser")
public class SampleParser {
    @Parse
    public void pass1(@NameContains("1") MultipartFile file, List<MultipartFile> files) {
        log.info(file.getOriginalFilename());
        log.info(files.toString());
    }

    @Parse(pass = 2)
    public void pass2(@NameContains("2") MultipartFile file, Writer writer) {
        log.info(file.getOriginalFilename());
    }

    @Parse(pass = 3)
    public void finalParse() {
        log.info("Finished parsing");
    }
}
