package com.danylevych.mss.model.event;

public interface EventListener {

    void handle(Event event, Object param);

}
