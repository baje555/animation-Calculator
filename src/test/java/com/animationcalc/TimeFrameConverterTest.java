package com.animationcalc;

import com.animationcalc.logic.TimeFrameConverter;
import com.animationcalc.logic.TimeFrameConverter.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TimeFrameConverterTest {

    // ═══════════════════════════════════════════════════════════════════════
    // SECONDS → FRAMES
    // ═══════════════════════════════════════════════════════════════════════

    // Spec examples at 24 fps
    @Test
    void secondsToFrames_1sec1frame_24fps() {
        SecondsToFramesResult r = TimeFrameConverter.secondsToFrames(1, 1, 24);
        assertTrue(r.valid);
        assertEquals(25, r.totalFrames);
    }

    @Test
    void secondsToFrames_2sec5frames_24fps() {
        SecondsToFramesResult r = TimeFrameConverter.secondsToFrames(2, 5, 24);
        assertTrue(r.valid);
        assertEquals(53, r.totalFrames);
    }

    @Test
    void secondsToFrames_0sec12frames_24fps() {
        SecondsToFramesResult r = TimeFrameConverter.secondsToFrames(0, 12, 24);
        assertTrue(r.valid);
        assertEquals(12, r.totalFrames);
    }

    // 0 seconds, 0 extra frames → 0 total frames
    @Test
    void secondsToFrames_zeroSeconds_zeroFrames() {
        SecondsToFramesResult r = TimeFrameConverter.secondsToFrames(0, 0, 24);
        assertTrue(r.valid);
        assertEquals(0, r.totalFrames);
    }

    // At 25 fps
    @Test
    void secondsToFrames_25fps() {
        SecondsToFramesResult r = TimeFrameConverter.secondsToFrames(1, 0, 25);
        assertTrue(r.valid);
        assertEquals(25, r.totalFrames);
    }

    @Test
    void secondsToFrames_2sec5frames_25fps() {
        // 2*25 + 5 = 55
        SecondsToFramesResult r = TimeFrameConverter.secondsToFrames(2, 5, 25);
        assertTrue(r.valid);
        assertEquals(55, r.totalFrames);
    }

    // At 30 fps
    @Test
    void secondsToFrames_30fps() {
        SecondsToFramesResult r = TimeFrameConverter.secondsToFrames(1, 0, 30);
        assertTrue(r.valid);
        assertEquals(30, r.totalFrames);
    }

    @Test
    void secondsToFrames_2sec5frames_30fps() {
        // 2*30 + 5 = 65
        SecondsToFramesResult r = TimeFrameConverter.secondsToFrames(2, 5, 30);
        assertTrue(r.valid);
        assertEquals(65, r.totalFrames);
    }

    // Extra frames >= FPS → error, not carry
    @Test
    void secondsToFrames_extraFrames_equalToFps_error() {
        SecondsToFramesResult r = TimeFrameConverter.secondsToFrames(0, 24, 24);
        assertFalse(r.valid);
        assertNotNull(r.errorMessage);
        assertTrue(r.errorMessage.contains("less than FPS"),
            "Error should mention 'less than FPS', got: " + r.errorMessage);
    }

    @Test
    void secondsToFrames_extraFrames_greaterThanFps_error() {
        SecondsToFramesResult r = TimeFrameConverter.secondsToFrames(1, 30, 24);
        assertFalse(r.valid);
    }

    // Negative seconds → error
    @Test
    void secondsToFrames_negativeSeconds_error() {
        SecondsToFramesResult r = TimeFrameConverter.secondsToFrames(-1, 0, 24);
        assertFalse(r.valid);
    }

    // Negative extra frames → error
    @Test
    void secondsToFrames_negativeExtraFrames_error() {
        SecondsToFramesResult r = TimeFrameConverter.secondsToFrames(0, -1, 24);
        assertFalse(r.valid);
    }

    // Zero FPS → error
    @Test
    void secondsToFrames_zeroFps_error() {
        SecondsToFramesResult r = TimeFrameConverter.secondsToFrames(1, 0, 0);
        assertFalse(r.valid);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // FRAMES → SECONDS
    // ═══════════════════════════════════════════════════════════════════════

    // Spec examples at 24 fps
    @Test
    void framesToSeconds_26frames_24fps() {
        // 26 = 1 sec + 2 frames = 1.0833 s
        FramesToSecondsResult r = TimeFrameConverter.framesToSeconds(26, 24);
        assertTrue(r.valid);
        assertEquals(1, r.wholeSeconds);
        assertEquals(2, r.extraFrames);
        assertEquals(26.0 / 24.0, r.decimalSeconds, 1e-9);
    }

    @Test
    void framesToSeconds_48frames_24fps() {
        // 48 = 2 sec + 0 frames = 2.0000 s
        FramesToSecondsResult r = TimeFrameConverter.framesToSeconds(48, 24);
        assertTrue(r.valid);
        assertEquals(2, r.wholeSeconds);
        assertEquals(0, r.extraFrames);
        assertEquals(2.0, r.decimalSeconds, 1e-9);
    }

    @Test
    void framesToSeconds_53frames_24fps() {
        // 53 = 2 sec + 5 frames = 2.2083 s
        FramesToSecondsResult r = TimeFrameConverter.framesToSeconds(53, 24);
        assertTrue(r.valid);
        assertEquals(2, r.wholeSeconds);
        assertEquals(5, r.extraFrames);
        assertEquals(53.0 / 24.0, r.decimalSeconds, 1e-9);
    }

    // 1 frame at 24 fps
    @Test
    void framesToSeconds_1frame_24fps() {
        FramesToSecondsResult r = TimeFrameConverter.framesToSeconds(1, 24);
        assertTrue(r.valid);
        assertEquals(0, r.wholeSeconds);
        assertEquals(1, r.extraFrames);
        assertEquals(1.0 / 24.0, r.decimalSeconds, 1e-9);
    }

    // 0 frames
    @Test
    void framesToSeconds_0frames() {
        FramesToSecondsResult r = TimeFrameConverter.framesToSeconds(0, 24);
        assertTrue(r.valid);
        assertEquals(0, r.wholeSeconds);
        assertEquals(0, r.extraFrames);
        assertEquals(0.0, r.decimalSeconds, 1e-9);
    }

    // At 25 fps
    @Test
    void framesToSeconds_50frames_25fps() {
        // 50 / 25 = 2 sec + 0 frames = 2.0000 s
        FramesToSecondsResult r = TimeFrameConverter.framesToSeconds(50, 25);
        assertTrue(r.valid);
        assertEquals(2, r.wholeSeconds);
        assertEquals(0, r.extraFrames);
        assertEquals(2.0, r.decimalSeconds, 1e-9);
    }

    @Test
    void framesToSeconds_53frames_25fps() {
        // 53 / 25 = 2 sec + 3 frames
        FramesToSecondsResult r = TimeFrameConverter.framesToSeconds(53, 25);
        assertTrue(r.valid);
        assertEquals(2, r.wholeSeconds);
        assertEquals(3, r.extraFrames);
        assertEquals(53.0 / 25.0, r.decimalSeconds, 1e-9);
    }

    // At 30 fps
    @Test
    void framesToSeconds_60frames_30fps() {
        // 60 / 30 = 2 sec + 0 frames = 2.0000 s
        FramesToSecondsResult r = TimeFrameConverter.framesToSeconds(60, 30);
        assertTrue(r.valid);
        assertEquals(2, r.wholeSeconds);
        assertEquals(0, r.extraFrames);
        assertEquals(2.0, r.decimalSeconds, 1e-9);
    }

    @Test
    void framesToSeconds_53frames_30fps() {
        // 53 / 30 = 1 sec + 23 frames
        FramesToSecondsResult r = TimeFrameConverter.framesToSeconds(53, 30);
        assertTrue(r.valid);
        assertEquals(1, r.wholeSeconds);
        assertEquals(23, r.extraFrames);
        assertEquals(53.0 / 30.0, r.decimalSeconds, 1e-9);
    }

    // Round-trip: secondsToFrames then framesToSeconds
    @Test
    void roundTrip_24fps() {
        SecondsToFramesResult fwd = TimeFrameConverter.secondsToFrames(2, 5, 24);
        assertTrue(fwd.valid);
        FramesToSecondsResult back = TimeFrameConverter.framesToSeconds(fwd.totalFrames, 24);
        assertTrue(back.valid);
        assertEquals(2, back.wholeSeconds);
        assertEquals(5, back.extraFrames);
    }

    // Negative frames → error
    @Test
    void framesToSeconds_negativeFrames_error() {
        FramesToSecondsResult r = TimeFrameConverter.framesToSeconds(-1, 24);
        assertFalse(r.valid);
    }

    // Zero FPS → error
    @Test
    void framesToSeconds_zeroFps_error() {
        FramesToSecondsResult r = TimeFrameConverter.framesToSeconds(24, 0);
        assertFalse(r.valid);
    }

    // Decimal seconds formatting check (4 d.p. value correctness)
    @Test
    void decimalSeconds_4dp_correctValue() {
        FramesToSecondsResult r = TimeFrameConverter.framesToSeconds(53, 24);
        // 53/24 = 2.208333...
        // Rounded to 4 d.p. = 2.2083
        assertEquals("2.2083", String.format("%.4f", r.decimalSeconds));
    }
}
