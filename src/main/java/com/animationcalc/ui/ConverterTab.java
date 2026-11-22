package com.animationcalc.ui;

import com.animationcalc.logic.TimeFrameConverter;
import com.animationcalc.logic.TimeFrameConverter.FramesToSecondsResult;
import com.animationcalc.logic.TimeFrameConverter.SecondsToFramesResult;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.function.IntSupplier;

/**
 * Tab 2 – Time ↔ Frames converter.
 *
 * Two independent boxes, each with its own inputs, Calculate button, and
 * result label.  Nothing links them together.
 *
 * Box A: Seconds + extra frames  →  total frames
 * Box B: Total frames  →  seconds + extra frames + decimal seconds
 */
public class ConverterTab {

    private final IntSupplier getFps;
    private final VBox        root = new VBox(20);

    // ── Box A fields ──────────────────────────────────────────────────────────
    private final TextField aSecsField    = new TextField("0");
    private final TextField aExtrasField  = new TextField("0");
    private final Label     aResultLabel  = new Label();
    private final Label     aErrorLabel   = new Label();

    // ── Box B fields ──────────────────────────────────────────────────────────
    private final TextField bTotalField   = new TextField("0");
    private final Label     bResultLabel  = new Label();
    private final Label     bErrorLabel   = new Label();

    public ConverterTab(IntSupplier getFps) {
        this.getFps = getFps;
        root.setPadding(new Insets(16));

        root.getChildren().addAll(buildBoxA(), new Separator(), buildBoxB());

        for (Label l : new Label[]{aErrorLabel, bErrorLabel}) {
            l.setStyle("-fx-text-fill: #cc0000;");
            l.setWrapText(true);
        }
        for (Label l : new Label[]{aResultLabel, bResultLabel}) {
            l.setStyle("-fx-font-weight: bold;");
        }
    }

    public Node getRoot() { return root; }

    // ── Box A ─────────────────────────────────────────────────────────────────

    private TitledPane buildBoxA() {
        aSecsField.setPrefWidth(70);
        aExtrasField.setPrefWidth(70);

        Button calcBtn = new Button("Calculate");
        calcBtn.setOnAction(e -> calculateA());

        HBox inputRow = new HBox(8,
            new Label("Seconds:"), aSecsField,
            new Label("Extra frames:"), aExtrasField
        );
        inputRow.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(10, inputRow, calcBtn, aErrorLabel, aResultLabel);
        box.setPadding(new Insets(10));

        TitledPane tp = new TitledPane("Seconds → Frames", box);
        tp.setCollapsible(false);
        return tp;
    }

    private void calculateA() {
        aErrorLabel.setText("");
        aResultLabel.setText("");

        int fps = getFps.getAsInt();

        int seconds, extras;
        try {
            seconds = Integer.parseInt(aSecsField.getText().trim());
        } catch (NumberFormatException e) {
            aErrorLabel.setText("Seconds must be a whole number.");
            return;
        }
        try {
            extras = Integer.parseInt(aExtrasField.getText().trim());
        } catch (NumberFormatException e) {
            aErrorLabel.setText("Extra frames must be a whole number.");
            return;
        }

        SecondsToFramesResult result = TimeFrameConverter.secondsToFrames(seconds, extras, fps);
        if (!result.valid) {
            aErrorLabel.setText(result.errorMessage);
        } else {
            aResultLabel.setText("Total frames: " + result.totalFrames);
        }
    }

    // ── Box B ─────────────────────────────────────────────────────────────────

    private TitledPane buildBoxB() {
        bTotalField.setPrefWidth(90);

        Button calcBtn = new Button("Calculate");
        calcBtn.setOnAction(e -> calculateB());

        HBox inputRow = new HBox(8, new Label("Total frames:"), bTotalField);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(10, inputRow, calcBtn, bErrorLabel, bResultLabel);
        box.setPadding(new Insets(10));

        TitledPane tp = new TitledPane("Frames → Seconds", box);
        tp.setCollapsible(false);
        return tp;
    }

    private void calculateB() {
        bErrorLabel.setText("");
        bResultLabel.setText("");

        int fps = getFps.getAsInt();

        int total;
        try {
            total = Integer.parseInt(bTotalField.getText().trim());
        } catch (NumberFormatException e) {
            bErrorLabel.setText("Total frames must be a whole number.");
            return;
        }

        FramesToSecondsResult result = TimeFrameConverter.framesToSeconds(total, fps);
        if (!result.valid) {
            bErrorLabel.setText(result.errorMessage);
        } else {
            bResultLabel.setText(
                result.wholeSeconds + " sec + " + result.extraFrames + " frames"
                + "    (" + String.format("%.4f", result.decimalSeconds) + " s)");
        }
    }
}
