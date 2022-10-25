package com.danylevych.mss.util;

import static java.lang.Integer.parseInt;

import java.util.ArrayList;
import java.util.List;

public class StringUtils {

    private StringUtils() {

    }

    public static List<Integer> splitIntegers(String values) {
        List<Integer> result = new ArrayList<>();
        String[] splitted = values.split(",");
        for (int j = 0; j < splitted.length; j++) {
            result.add(parseInt(splitted[j]));
        }
        return result;
    }
}
