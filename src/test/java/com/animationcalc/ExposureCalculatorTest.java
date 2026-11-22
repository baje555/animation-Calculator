package com.animationcalc;

import com.animationcalc.logic.ExposureCalculator;
import com.animationcalc.logic.ExposureCalculator.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExposureCalculatorTest {

    private final ExposureCalculator calc = new ExposureCalculator();

    // ── Spec examples ─────────────────────────────────────────────────────────

    // 24 frames on 2s → frames 1,3,5,...,23 (12 drawings, each held 2)
    @Test
    void example_24framesOn2s() {
        Result r = calc.calculate(List.of(new Segment(24, 2)));

        assertEquals(12, r.drawings.size());
        assertEquals(24, r.totalFrames);

        for (int i = 0; i < 12; i++) {
            assertEquals(i + 1,       r.drawings.get(i).drawingNumber);
            assertEquals(1 + i * 2,   r.drawings.get(i).frame);
            assertEquals(2,           r.drawings.get(i).heldFor);
        }
    }

    // 12 frames on 2s then 12 frames on 3s → 1,3,5,7,9,11,13,16,19,22 (10 drawings)
    @Test
    void example_mixedSegments() {
        Result r = calc.calculate(List.of(
            new Segment(12, 2),
            new Segment(12, 3)
        ));

        assertEquals(10, r.drawings.size());
        assertEquals(24, r.totalFrames);

        int[] frames = {1, 3, 5, 7, 9, 11, 13, 16, 19, 22};
        for (int i = 0; i < frames.length; i++) {
            assertEquals(frames[i], r.drawings.get(i).frame,
                "Drawing " + (i + 1) + " frame mismatch");
        }
    }

    // ── Hold lengths in mixed example ─────────────────────────────────────────

    @Test
    void example_mixedSegments_holdLengths() {
        Result r = calc.calculate(List.of(
            new Segment(12, 2),
            new Segment(12, 3)
        ));

        // Segment 1: 6 drawings, each held 2
        for (int i = 0; i < 6; i++)
            assertEquals(2, r.drawings.get(i).heldFor, "seg1 drawing " + (i+1) + " hold");

        // Segment 2: 4 drawings, each held 3
        for (int i = 6; i < 10; i++)
            assertEquals(3, r.drawings.get(i).heldFor, "seg2 drawing " + (i+1) + " hold");
    }

    // ── Uneven division: last drawing gets shorter hold ────────────────────────

    // 10 frames on 3s → drawings at 1,4,7,10; holds 3,3,3,1
    @Test
    void unevenDivision_10framesOn3s() {
        Result r = calc.calculate(List.of(new Segment(10, 3)));

        assertEquals(4, r.drawings.size());
        assertEquals(10, r.totalFrames);

        int[] frames = {1, 4, 7, 10};
        int[] holds  = {3, 3, 3,  1};
        for (int i = 0; i < 4; i++) {
            assertEquals(frames[i], r.drawings.get(i).frame,   "frame " + i);
            assertEquals(holds[i],  r.drawings.get(i).heldFor, "hold "  + i);
        }
    }

    // ── Step larger than total frames: one drawing, held for all frames ────────

    @Test
    void stepLargerThanFrameCount() {
        Result r = calc.calculate(List.of(new Segment(3, 10)));

        assertEquals(1, r.drawings.size());
        assertEquals(1, r.drawings.get(0).frame);
        assertEquals(3, r.drawings.get(0).heldFor,
            "Single drawing should be held for all 3 frames");
        assertEquals(3, r.totalFrames);
    }

    // ── Single frame ─────────────────────────────────────────────────────────

    @Test
    void singleFrame() {
        Result r = calc.calculate(List.of(new Segment(1, 1)));

        assertEquals(1, r.drawings.size());
        assertEquals(1, r.drawings.get(0).frame);
        assertEquals(1, r.drawings.get(0).heldFor);
        assertEquals(1, r.totalFrames);
    }

    // ── On 1s: every frame is a new drawing ───────────────────────────────────

    @Test
    void on1s_everyFrameNewDrawing() {
        Result r = calc.calculate(List.of(new Segment(5, 1)));

        assertEquals(5, r.drawings.size());
        for (int i = 0; i < 5; i++) {
            assertEquals(i + 1, r.drawings.get(i).frame);
            assertEquals(1,     r.drawings.get(i).heldFor);
        }
    }

    // ── Drawing numbers are sequential across segments ────────────────────────

    @Test
    void drawingNumbers_sequential() {
        Result r = calc.calculate(List.of(
            new Segment(4, 2),
            new Segment(6, 3)
        ));
        for (int i = 0; i < r.drawings.size(); i++)
            assertEquals(i + 1, r.drawings.get(i).drawingNumber);
    }

    // ── Total frames is the sum of all segment frame counts ───────────────────

    @Test
    void totalFrames_sumOfSegments() {
        Result r = calc.calculate(List.of(
            new Segment(10, 2),
            new Segment(15, 3),
            new Segment(5, 1)
        ));
        assertEquals(30, r.totalFrames);
    }

    // ── Validation ───────────────────────────────────────────────────────────

    @Test
    void emptySegments_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> calc.calculate(List.of()));
    }

    @Test
    void zeroFrameCount_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> new Segment(0, 2));
    }

    @Test
    void zeroStep_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> new Segment(12, 0));
    }

    @Test
    void negativeFrameCount_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> new Segment(-1, 2));
    }
}
