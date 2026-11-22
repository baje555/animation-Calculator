module com.animationcalc {
    requires javafx.controls;

    opens com.animationcalc    to javafx.graphics;
    opens com.animationcalc.ui to javafx.graphics;

    exports com.animationcalc;
    exports com.animationcalc.ui;
    exports com.animationcalc.logic;
}
