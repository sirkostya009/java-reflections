package ua.sirkostya009.javareflections.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ParserService {
    byte[] parse(List<MultipartFile> files) throws IOException;
}
