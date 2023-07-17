package ua.sirkostya009.javareflections.service;

import org.springframework.web.multipart.MultipartFile;
import ua.sirkostya009.javareflections.model.Customer;

import java.util.Collection;
import java.util.List;

public interface ParserService {
    byte[] parse(Customer customer, String name, List<MultipartFile> files) throws Exception;

    Collection<String> getForCustomer(Customer customer);
}
