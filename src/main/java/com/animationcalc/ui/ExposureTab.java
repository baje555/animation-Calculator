package com.animationcalc.ui;

import com.animationcalc.logic.ExposureCalculator;
import com.animationcalc.logic.ExposureCalculator.Drawing;
import com.animationcalc.logic.ExposureCalculator.Result;
import com.animationcalc.logic.ExposureCalculator.Segment;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;

/**
 * Tab 1 – Exposure Calculator.
 *
 * The user builds a list of segments (each with a frame count and a step),
 * presses Calculate, and sees a table of Drawing #, Frame, Held For.
 */
public class ExposureTab {

    private final IntSupplier getFps;

    // Each row in the segment editor: [framesField] frames on [stepField]s  [Remove]
    private final List<SegmentRow> segmentRows = new ArrayList<>();
    private final VBox             segmentBox  = new VBox(6);

    // Output
    private final TableView<Drawing> table       = new TableView<>();
    private final Label              summaryLabel = new Label();
    private final Label              errorLabel   = new Label();

    private final VBox root = new VBox(14);

    public ExposureTab(IntSupplier getFps) {
        this.getFps = getFps;
        root.setPadding(new Insets(16));

        // ── Segment list ──────────────────────────────────────────────────────
        addSegmentRow();   // start with one row already present

        Button addRowBtn = new Button("+ Add Row");
        addRowBtn.setOnAction(e -> addSegmentRow());

        // ── Calculate button ──────────────────────────────────────────────────
        Button calcBtn = new Button("Calculate");
        calcBtn.setDefaultButton(true);
        calcBtn.setOnAction(e -> calculate());

        // ── Results table ─────────────────────────────────────────────────────
        TableColumn<Drawing, Number> numCol = new TableColumn<>("Drawing #");
        numCol.setCellValueFactory(cd ->
            new javafx.beans.property.SimpleIntegerProperty(cd.getValue().drawingNumber));
        numCol.setPrefWidth(90);

        TableColumn<Drawing, Number> frameCol = new TableColumn<>("Frame");
        frameCol.setCellValueFactory(cd ->
            new javafx.beans.property.SimpleIntegerProperty(cd.getValue().frame));
        frameCol.setPrefWidth(80);

        TableColumn<Drawing, Number> heldCol = new TableColumn<>("Held For");
        heldCol.setCellValueFactory(cd ->
            new javafx.beans.property.SimpleIntegerProperty(cd.getValue().heldFor));
        heldCol.setPrefWidth(80);

        table.getColumns().addAll(numCol, frameCol, heldCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(300);
        table.setPlaceholder(new Label("Press Calculate to see results."));

        // ── Error label ───────────────────────────────────────────────────────
        errorLabel.setStyle("-fx-text-fill: #cc0000;");
        errorLabel.setWrapText(true);

        // ── Summary label ─────────────────────────────────────────────────────
        summaryLabel.setStyle("-fx-font-weight: bold;");

        root.getChildren().addAll(
            new Label("Segments  (frames  |  step = on Xs):"),
            segmentBox,
            addRowBtn,
            calcBtn,
            errorLabel,
            summaryLabel,
            table
        );
    }

    public Node getRoot() { return root; }

    // ── Segment row management ────────────────────────────────────────────────

    private void addSegmentRow() {
        SegmentRow row = new SegmentRow();
        segmentRows.add(row);
        segmentBox.getChildren().add(row.node);
    }

    private void removeSegmentRow(SegmentRow row) {
        segmentRows.remove(row);
        segmentBox.getChildren().remove(row.node);
    }

    // ── Calculation ───────────────────────────────────────────────────────────

    private void calculate() {
        errorLabel.setText("");
        summaryLabel.setText("");
        table.getItems().clear();

        // Validate FPS
        int fps = getFps.getAsInt();
        if (fps < 1) {
            errorLabel.setText("FPS must be at least 1.");
            return;
        }

        // Parse segments
        List<Segment> segments = new ArrayList<>();
        for (int i = 0; i < segmentRows.size(); i++) {
            SegmentRow sr = segmentRows.get(i);
            String framesText = sr.framesField.getText().trim();
            String stepText   = sr.stepField.getText().trim();

            int frameCount;
            int step;
            try {
                frameCount = Integer.parseInt(framesText);
            } catch (NumberFormatException e) {
                errorLabel.setText("Row " + (i + 1) + ": \"Frames\" must be a whole number.");
                return;
            }
            try {
                step = Integer.parseInt(stepText);
            } catch (NumberFormatException e) {
                errorLabel.setText("Row " + (i + 1) + ": \"Step\" must be a whole number.");
                return;
            }
            if (frameCount < 1) {
                errorLabel.setText("Row " + (i + 1) + ": Frames must be at least 1.");
                return;
            }
            if (step < 1) {
                errorLabel.setText("Row " + (i + 1) + ": Step must be at least 1.");
                return;
            }
            segments.add(new Segment(frameCount, step));
        }

        // Calculate
        ExposureCalculator calc   = new ExposureCalculator();
        Result             result = calc.calculate(segments);

        table.getItems().setAll(result.drawings);
        summaryLabel.setText(
            result.drawings.size() + " drawings over " + result.totalFrames + " frames");
    }

    // ── Inner: one segment row ────────────────────────────────────────────────

    private class SegmentRow {
        final TextField framesField = new TextField("12");
        final TextField stepField   = new TextField("2");
        final Node      node;

        SegmentRow() {
            framesField.setPrefWidth(60);
            stepField.setPrefWidth(50);

            Button removeBtn = new Button("✕");
            removeBtn.setOnAction(e -> {
                if (segmentRows.size() > 1) removeSegmentRow(this);
            });

            HBox row = new HBox(8,
                framesField, new Label("frames  on  step"),
                stepField, new Label("s"),
                removeBtn
            );
            row.setAlignment(Pos.CENTER_LEFT);
            node = row;
        }
    }
}
