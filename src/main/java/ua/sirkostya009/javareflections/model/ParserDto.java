package ua.sirkostya009.javareflections.model;

import ua.sirkostya009.javareflections.annotation.Parser;
import ua.sirkostya009.javareflections.utils.Utils;

public record ParserDto(
        String id,
        String name
) {
    public static ParserDto of(Parser parser) {
        return new ParserDto(
                Utils.generateId(parser),
                parser.name()
        );
    }
}
