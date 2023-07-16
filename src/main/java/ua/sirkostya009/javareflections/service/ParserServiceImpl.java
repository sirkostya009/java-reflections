package ua.sirkostya009.javareflections.service;

import lombok.Cleanup;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ua.sirkostya009.javareflections.annotation.NameContains;
import ua.sirkostya009.javareflections.annotation.Parse;
import ua.sirkostya009.javareflections.annotation.Parser;
import ua.sirkostya009.javareflections.model.Customer;
import ua.sirkostya009.javareflections.parser.SampleParser;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@Service
public class ParserServiceImpl implements ParserService {

    private final Map<Customer, Map<Integer, Map<Parser, List<Method>>>> parsers;

    private final Map.Entry<SampleParser, List<Method>> sampleParserMethods;

    public ParserServiceImpl(ApplicationContext context, SampleParser parser) {
        parsers = Arrays.stream(context.getBeanNamesForAnnotation(Parser.class))
                .map(name -> {
                    var bean = context.getBean(name).getClass();
                    return Map.entry(
                            bean.getAnnotation(Parser.class),
                            Arrays.stream(bean.getMethods())
                                    .filter(method -> method.isAnnotationPresent(Parse.class))
                                    .sorted(Comparator.comparing(method -> method.getAnnotation(Parse.class).pass()))
                                    .toList()
                    );
                })
                .collect(groupingBy(
                        entry -> entry.getKey().customer(),
                        groupingBy(
                                entry -> Objects.hash(entry.getKey().name(), entry.getKey().customer()),
                                toMap(Map.Entry::getKey, Map.Entry::getValue)
                        )
                ));

        this.sampleParserMethods = Map.entry(parser, Arrays.stream(parser.getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(Parse.class))
                .toList());
    }

    @Override
    public byte[] parse(List<MultipartFile> files) throws IOException {
        var temp = Files.createTempFile(UUID.randomUUID().toString(), ".csv").toFile();
        @Cleanup var writer = new CSVWriter(temp);

        var parameters = sampleParserMethods.getValue().stream()
                .map(method -> Arrays.stream(method.getParameters())
                        .map(parameter -> injectedParameter(parameter, files, writer))
                        .toArray())
                .toList();

        IntStream.range(0, parameters.size())
                .forEach(i -> {
                    try {
                        sampleParserMethods.getValue().get(i).invoke(sampleParserMethods.getKey(), parameters.get(i));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });

        return null;
    }

    private Object injectedParameter(Parameter parameter, List<MultipartFile> files, CSVWriter writer) {
        var nameContains = parameter.getAnnotation(NameContains.class);

        if (nameContains != null) {
            var value = nameContains.value();

            return files.stream()
                    .filter(file -> file.getOriginalFilename() != null && file.getOriginalFilename().contains(value))
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("No file where name contains " + value + " found"));
        }

        var type = parameter.getType();

        if (type.equals(Writer.class)
                || type.equals(OutputStreamWriter.class)
                || type.equals(FileWriter.class)
                || type.equals(CSVWriter.class))
            return writer;

        if (type.equals(List.class)) {
            return files;
        }

        return null;
    }

}
