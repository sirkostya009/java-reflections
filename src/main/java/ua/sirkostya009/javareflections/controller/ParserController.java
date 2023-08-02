package ua.sirkostya009.javareflections.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ua.sirkostya009.javareflections.model.Customer;
import ua.sirkostya009.javareflections.service.ParserService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/parser")
public class ParserController {

    private final ParserService service;

    @GetMapping("/{customer}")
    public Collection<String> getForCustomer(@PathVariable("customer") Customer customer) {
        log.info("Getting parser for {}", customer);
        return service.getForCustomer(customer);
    }

    @PostMapping("/{customer}/{name}")
    public byte[] parse(@PathVariable Customer customer,
                        @PathVariable String name,
                        @RequestParam("files") List<MultipartFile> files) throws Exception {
        log.info("Parsing files {} using parser {}:{}", files.stream().map(MultipartFile::getOriginalFilename).toList(), customer, name);
        var start = System.currentTimeMillis();

        var parsed = service.parse(customer, name, files);

        var finish = System.currentTimeMillis();
        log.info("Finished parsing in {} ms", finish - start);

        return parsed;
    }

    @ExceptionHandler(Exception.class)
    public ErrorResponse handle(Exception e) {
        log.error("Exception thrown: {}", e.getMessage(), e);
        return ErrorResponse.create(e, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

}
