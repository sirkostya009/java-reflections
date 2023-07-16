package ua.sirkostya009.javareflections.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ua.sirkostya009.javareflections.model.Customer;
import ua.sirkostya009.javareflections.model.ParserDto;
import ua.sirkostya009.javareflections.service.ParserService;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/parser")
public class ParserController {
    private final ParserService service;

    @GetMapping("/{customer}")
    public List<ParserDto> getForCustomer(@PathVariable("customer") Customer customer) {
        log.info("Getting parser for {}", customer);
        return service.getForCustomer(customer).stream().map(ParserDto::of).toList();
    }

    @PostMapping("/{customer}/{id}")
    public byte[] parse(@PathVariable Customer customer,
                        @PathVariable String id,
                        @RequestParam("files") List<MultipartFile> files) throws IOException {
        var start = System.currentTimeMillis();
        log.info("Parsing files {} using parser {}:{}", files.stream().map(MultipartFile::getOriginalFilename).toList(), customer, id);

        var parsed = service.parse(customer, id, files);

        var finish = System.currentTimeMillis();
        log.info("Finished parsing in {} ms", finish - start);

        return parsed;
    }
}
