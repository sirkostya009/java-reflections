package ua.sirkostya009.javareflections.service;

import org.springframework.web.multipart.MultipartFile;
import ua.sirkostya009.javareflections.model.Customer;
import ua.sirkostya009.javareflections.model.ParserDto;

import java.util.List;

public interface ParserService {
    byte[] parse(Customer customer, String id, List<MultipartFile> files) throws Exception;

    List<ParserDto> getForCustomer(Customer customer);
}
