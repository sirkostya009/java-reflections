package ua.sirkostya009.javareflections.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ua.sirkostya009.javareflections.service.ParserService;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/parser")
public class ParserController {
    private final ParserService service;

    @PostMapping
    public byte[] parse(@RequestParam("files") List<MultipartFile> files) throws IOException {
        var start = System.currentTimeMillis();
        log.info("Parsing files {}", files.stream().map(MultipartFile::getOriginalFilename).toList());
        var parsed = service.parse(files);
        var finish = System.currentTimeMillis();
        log.info("Finished parsing in {} ms", finish - start);
        return parsed;
    }
}
