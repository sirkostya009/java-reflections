package ua.sirkostya009.javareflections;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ua.sirkostya009.javareflections.model.Customer;
import ua.sirkostya009.javareflections.service.ParserService;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@SpringBootTest
class AutomatedParserTests {

    @Autowired
    private ParserService parserService;

    @Test
    void testAllParsers() {
        var parsers = getParsers();

        parsers.forEach((customer, names) -> names.forEach(name -> {
            try {
                var files = getFiles(customer, name);
                var expected = getResult(files);
                var actual = parserService.parse(customer, name, files);
                assert Arrays.equals(expected.getBytes(), actual);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));
    }

    private Map<Customer, Collection<String>> getParsers() {
        return Arrays.stream(Customer.values())
                .collect(toMap(Function.identity(),
                               parserService::getForCustomer));
    }

    private List<MultipartFile> getFiles(Customer customer, String name) throws URISyntaxException {
        var directory = AutomatedParserTests.class.getResource("/" + customer + "/" + name + "/");
        assert directory != null;

        var files = Path.of(directory.toURI()).toFile().listFiles();
        assert files != null;

        return new ArrayList<>(Arrays.stream(files).map(this::toMultipart).toList());
    }

    private MultipartFile getResult(List<MultipartFile> files) {
        var result = files.stream()
                .filter(file -> file.getName().toLowerCase().contains("result"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No result file found in " + files));

        files.remove(result);

        return result;
    }

    @SneakyThrows
    private MultipartFile toMultipart(File file) {
        return new MockMultipartFile(file.getName(), Files.newInputStream(file.toPath()));
    }

}
