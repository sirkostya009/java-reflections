package ua.sirkostya009.javareflections.service;

import org.springframework.web.multipart.MultipartFile;
import ua.sirkostya009.javareflections.annotation.Parser;
import ua.sirkostya009.javareflections.model.Customer;

import java.io.IOException;
import java.util.List;

public interface ParserService {
    byte[] parse(Customer customer, String id, List<MultipartFile> files) throws IOException;

    List<Parser> getForCustomer(Customer customer);
}
