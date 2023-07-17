package ua.sirkostya009.javareflections;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ua.sirkostya009.javareflections.model.Customer;
import ua.sirkostya009.javareflections.model.ParserDto;
import ua.sirkostya009.javareflections.service.ParserService;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@SpringBootTest
class AutomatedParserTests {

    @Autowired
    private ParserService parserService;

    @Test
    void testAllParsers() {
        var parsers = getParsers();

        parsers.forEach((customer, ids) -> ids.forEach(id -> {
            try {
                parserService.parse(customer, id, getFiles(customer, id));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));
    }

    private Map<Customer, List<String>> getParsers() {
        return Arrays.stream(Customer.values())
                .collect(toMap(Function.identity(),
                               customer -> parserService.getForCustomer(customer).stream().map(ParserDto::id).toList()));
    }

    private List<MultipartFile> getFiles(Customer customer, String id) throws URISyntaxException {
        var directory = AutomatedParserTests.class.getResource("/" + customer + "/" + id + "/");
        assert directory != null;
        var files = Path.of(directory.toURI()).toFile().listFiles();

        assert files != null;
        return Arrays.stream(files).map(this::toMultipart).toList();
    }

    @SneakyThrows
    private MultipartFile toMultipart(File file) {
        return new MockMultipartFile(file.getName(), Files.newInputStream(file.toPath()));
    }

}
