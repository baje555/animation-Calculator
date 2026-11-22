package com.animationcalc;

import com.animationcalc.ui.ConverterTab;
import com.animationcalc.ui.ExposureTab;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Entry point for the Animation Exposure Calculator.
 *
 * Builds a small top bar with the shared FPS field, then two tabs:
 *   Tab 1 – Exposure Calculator
 *   Tab 2 – Time ↔ Frames Converter
 */
public class App extends Application {

    // Shared FPS spinner – both tabs read from this.
    private final Spinner<Integer> fpsSpinner = new Spinner<>(1, 999, 24);

    @Override
    public void start(Stage stage) {
        fpsSpinner.setEditable(true);
        fpsSpinner.setPrefWidth(80);

        // ── FPS bar ───────────────────────────────────────────────────────────
        HBox fpsBar = new HBox(8, new Label("FPS:"), fpsSpinner);
        fpsBar.setAlignment(Pos.CENTER_LEFT);
        fpsBar.setPadding(new Insets(10, 14, 10, 14));
        fpsBar.setStyle("-fx-border-color: transparent transparent #cccccc transparent;"
                      + "-fx-border-width: 0 0 1 0;");

        // ── Tabs ──────────────────────────────────────────────────────────────
        ExposureTab exposureTab  = new ExposureTab(this::getFps);
        ConverterTab converterTab = new ConverterTab(this::getFps);

        Tab tab1 = new Tab("Exposure",         exposureTab.getRoot());
        Tab tab2 = new Tab("Time ↔ Frames",    converterTab.getRoot());
        tab1.setClosable(false);
        tab2.setClosable(false);

        TabPane tabPane = new TabPane(tab1, tab2);

        // ── Root ──────────────────────────────────────────────────────────────
        VBox root = new VBox(fpsBar, tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Scene scene = new Scene(root, 520, 620);
        stage.setTitle("Animation Exposure Calculator");
        stage.setScene(scene);
        stage.setMinWidth(420);
        stage.setMinHeight(400);
        stage.show();
    }

    /** Returns the current FPS value. Called by both tabs. */
    private int getFps() {
        Integer v = fpsSpinner.getValue();
        return (v != null && v >= 1) ? v : 24;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
