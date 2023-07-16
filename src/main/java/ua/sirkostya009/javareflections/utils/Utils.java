package ua.sirkostya009.javareflections.utils;

import lombok.experimental.UtilityClass;
import ua.sirkostya009.javareflections.annotation.Parser;

@UtilityClass
public class Utils {
    public String generateId(Parser parser) {
        var customerFirstCharCode = (int) parser.customer().name().charAt(0);
        var parserNameFirstCharCode = (int) parser.name().charAt(0);

        return customerFirstCharCode + "-" + parserNameFirstCharCode;
    }
}
