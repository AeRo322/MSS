package com.danylevych.mss.controller.action;

import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

public class ActionContainer<R> {

    private final SortedMap<Object, Action<R>> actions = new TreeMap<>();

    public R perform(Object action) {
        return actions.get(action).perform();
    }

    public void add(Object key, Action<R> value) {
        Objects.requireNonNull(value);
        Objects.requireNonNull(key);
        actions.put(key, value);
    }

}
