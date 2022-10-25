module mss {
    requires transitive javafx.graphics;
    requires transitive javafx.fxml;
    requires javafx.controls;

    opens com.danylevych.mss;
    opens com.danylevych.mss.view;
    opens com.danylevych.mss.controller;

    exports com.danylevych.mss;
    exports com.danylevych.mss.model;
    exports com.danylevych.mss.model.event;
    exports com.danylevych.mss.model.sheduler;
    exports com.danylevych.mss.controller;
}
