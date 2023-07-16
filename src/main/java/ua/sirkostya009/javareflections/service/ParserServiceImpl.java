package ua.sirkostya009.javareflections.service;

import lombok.Cleanup;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ua.sirkostya009.javareflections.annotation.NameContains;
import ua.sirkostya009.javareflections.annotation.Parse;
import ua.sirkostya009.javareflections.annotation.Parser;
import ua.sirkostya009.javareflections.model.Customer;
import ua.sirkostya009.javareflections.utils.Utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.util.*;

import static java.util.stream.Collectors.*;

@Service
public class ParserServiceImpl implements ParserService {

    private final Map<Customer, Map<String, List<Method>>> methods;
    private final Map<Customer, Map<String, Object>> parsers;

    public ParserServiceImpl(ApplicationContext context) {
        var beans = context.getBeansWithAnnotation(Parser.class).values();

        parsers = beans.stream()
                .map(bean -> Map.entry(Utils.generateId(bean.getClass().getAnnotation(Parser.class)), bean))
                .collect(groupingBy(
                        entry -> entry.getValue().getClass().getAnnotation(Parser.class).customer(),
                        toMap(Map.Entry::getKey, Map.Entry::getValue)
                ));

        // grouping by id
        methods = beans.stream()
                .map(bean -> Map.entry(
                        bean.getClass().getAnnotation(Parser.class),
                        Arrays.stream(bean.getClass().getMethods())
                                .filter(method -> method.isAnnotationPresent(Parse.class))
                                .sorted(Comparator.comparing(method -> method.getAnnotation(Parse.class).pass()))
                ))
                .collect(groupingBy(
                        entry -> entry.getKey().customer(), // grouping by customer
                        groupingBy(
                                entry -> Utils.generateId(entry.getKey()), // grouping by id
                                flatMapping(Map.Entry::getValue, toList()) // collecting Parse methods to a list
                        )
                ));
    }

    @Override
    public byte[] parse(Customer customer, String id, List<MultipartFile> files) throws IOException {
        if (!methods.containsKey(customer)) {
            throw new RuntimeException("Customer " + customer + " not present");
        }

        if (!methods.get(customer).containsKey(id)) {
            throw new RuntimeException("Parse methods for " + customer + ", " + id + " are absent");
        }

        var parseMethods = methods.get(customer).get(id);
        var parser = parsers.get(customer).get(id);

        var temp = Files.createTempFile(UUID.randomUUID().toString(), ".csv");
        @Cleanup var writer = new CSVWriter(temp.toFile());

        var parameters = parseMethods.stream()
                .map(method -> Arrays.stream(method.getParameters())
                        .map(parameter -> injectedParameter(parameter, customer, id, files, writer))
                        .toArray())
                .toList();

        for (var i = 0; i < parseMethods.size(); i++) {
            try {
                parseMethods.get(i).invoke(parser, parameters.get(i));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to invoke parsing method", e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Failed to parse. Message: " + e.getMessage(), e);
            }
        }

        return Files.readAllBytes(temp);
    }

    @Override
    public List<Parser> getForCustomer(Customer customer) {
        return parsers.get(customer).values().stream()
                .map(object -> object.getClass().getAnnotation(Parser.class))
                .toList();
    }

    private Object injectedParameter(Parameter parameter,
                                     Customer customer,
                                     String id,
                                     List<MultipartFile> files,
                                     CSVWriter writer) {
        var nameContains = parameter.getAnnotation(NameContains.class);

        if (nameContains != null) {
            var value = nameContains.value();

            return files.stream()
                    .filter(file -> file.getOriginalFilename() != null && file.getOriginalFilename().contains(value))
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("No file where name contains " + value + " found"));
        }

        var type = parameter.getType();
        var label = parameter.getName();

        if (type == Writer.class
                || type == OutputStreamWriter.class
                || type == FileWriter.class
                || type == CSVWriter.class) {
            return writer;
        }

        if (type.isAssignableFrom(files.getClass())) {
            return files;
        }

        if (type == String.class && "id".equalsIgnoreCase(label)) {
            return id;
        }

        if (type == Customer.class) {
            return customer;
        }

        return null;
    }

}
