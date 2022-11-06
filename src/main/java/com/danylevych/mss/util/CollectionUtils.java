package com.danylevych.mss.util;

import static java.util.stream.Collectors.joining;

import java.util.Collection;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;

public final class CollectionUtils {

    private CollectionUtils() {

    }

    public static ObservableValue<String> joinAndWrap(Collection<?> c) {
        return new ReadOnlyStringWrapper(joinCollection(c));
    }

    private static String joinCollection(Collection<?> collection) {
        return collection.stream().map(Object::toString).collect(joining(", "));
    }

}
