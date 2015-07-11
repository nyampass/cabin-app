package com.nyampass.cabin;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static String regexTransform(Pattern pattern, String input,
                                        Function<Matcher, String> function) {
        Matcher m = pattern.matcher(input);
        StringBuffer str = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(str, function.apply(m));
        }
        m.appendTail(str);
        return str.toString();
    }
}
