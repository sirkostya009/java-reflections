package ua.sirkostya009.javareflections.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ua.sirkostya009.javareflections.annotation.NameContains;
import ua.sirkostya009.javareflections.annotation.Parse;
import ua.sirkostya009.javareflections.annotation.Parser;
import ua.sirkostya009.javareflections.model.Customer;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Files;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;

@Service
public class ParserServiceImpl implements ParserService {

    private final Map<Customer, Map<String, Map.Entry<Object, List<Method>>>> map;
    private final Map<String, CSVFormat> csvFormats;

    public ParserServiceImpl(ApplicationContext context) {
        var beans = context.getBeansWithAnnotation(Parser.class).values();

        map = beans.stream()
                .map(bean -> Map.entry(
                        bean,
                        Arrays.stream(bean.getClass().getMethods())
                                .filter(method -> method.isAnnotationPresent(Parse.class))
                                .sorted(Comparator.comparing(method -> method.getAnnotation(Parse.class).pass()))
                                .toList()
                ))
                .collect(groupingBy(entry -> entry.getKey().getClass().getAnnotation(Parser.class).customer(),
                                    groupingBy(entry -> entry.getKey().getClass().getAnnotation(Parser.class).name(),
                                               reducing(Map.entry("", List.of()),
                                                        ($, actual) -> actual))));

        csvFormats = context.getBeansOfType(CSVFormat.class);
    }

    @Override
    public byte[] parse(Customer customer, String name, List<MultipartFile> files) throws Exception {
        if (!map.containsKey(customer)) {
            throw new RuntimeException("Customer " + customer + " not present");
        }

        if (!map.get(customer).containsKey(name)) {
            throw new RuntimeException("Parse methods for " + customer + ", " + name + " are absent");
        }

        var entry = map.get(customer).get(name);
        var object = entry.getKey();
        var parser = object.getClass().getAnnotation(Parser.class);
        var parseMethods = entry.getValue();

        var temp = Files.createTempFile(UUID.randomUUID().toString(), ".csv");
        var writer = new FileWriter(temp.toFile());
        var printer = new CSVPrinter(writer, csvFormats.get(parser.resultFormatBeanQualifier()));

        var parameters = parseMethods.stream()
                .map(method -> Arrays.stream(method.getParameters())
                        .map(parameter -> injectParameter(parameter, parser, name, files, writer, printer))
                        .toArray())
                .toList();

        for (var i = 0; i < parseMethods.size(); i++) {
            parseMethods.get(i).invoke(object, parameters.get(i));
        }

        printer.close(true);
        return Files.readAllBytes(temp);
    }

    @Override
    public Collection<String> getForCustomer(Customer customer) {
        if (!map.containsKey(customer)) {
            throw new RuntimeException("Customer " + customer + " not found");
        }

        return map.get(customer).keySet();
    }

    private Object injectParameter(Parameter parameter,
                                   Parser parser,
                                   String name,
                                   List<MultipartFile> files,
                                   FileWriter writer,
                                   CSVPrinter printer) {
        var nameContains = parameter.getAnnotation(NameContains.class);

        if (nameContains != null) {
            var value = nameContains.value();
            var found = files.stream()
                    .filter(file -> file.getName().contains(value)
                                || (file.getOriginalFilename() != null && file.getOriginalFilename().contains(value)))
                    .findAny()
                    .orElse(null);

            var type = parameter.getType();

            if (type == MultipartFile.class) {
                return found;
            }

            if (type == CSVParser.class) {
                return toParser(found, parser.sourceFormatBeanQualifier());
            }
        }

        var type = parameter.getType();
        var label = parameter.getName();

        if (type.isAssignableFrom(writer.getClass())) {
            return writer;
        }

        if (type.isAssignableFrom(printer.getClass())) {
            return printer;
        }

        if (Iterable.class.isAssignableFrom(type)) {
            var generic = ((ParameterizedType) parameter.getParameterizedType()).getActualTypeArguments()[0];

            if (generic.getTypeName().equals(CSVParser.class.getTypeName())) {
                return files.stream()
                        .map(file -> toParser(file, parser.sourceFormatBeanQualifier()))
                        .toList();
            }

            if (generic.getTypeName().equals(MultipartFile.class.getTypeName())) {
                return files;
            }
        }

        if (type == MultipartFile[].class) {
            return files.toArray(MultipartFile[]::new);
        }

        if (type == CSVParser[].class) {
            return files.stream()
                    .map(file -> toParser(file, parser.sourceFormatBeanQualifier()))
                    .toArray(CSVParser[]::new);
        }

        if (type == String.class) {
            if ("id".equalsIgnoreCase(label))
                return name;

            if ("name".equalsIgnoreCase(label))
                return parser.name();
        }

        if (type == Customer.class) {
            return parser.customer();
        }

        if (type == CSVFormat.class) {
            var qualifier = parameter.getAnnotation(Qualifier.class);

            return csvFormats.get(qualifier != null ? qualifier.value() : label);
        }

        return null;
    }

    private CSVParser toParser(MultipartFile file, String sourceFormat) {
        try {
            return file != null
                    ? CSVParser.parse(new InputStreamReader(file.getInputStream()), csvFormats.get(sourceFormat))
                    : null;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create parser for file");
        }
    }

}
