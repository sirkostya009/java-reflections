package ua.sirkostya009.javareflections;

import org.apache.commons.csv.CSVFormat;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean("default")
    public CSVFormat defaultFormat() {
        return CSVFormat.DEFAULT;
    }

}
