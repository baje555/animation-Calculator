package com.animationcalc.logic;

/**
 * Converts between a frame count (a pure duration) and seconds.
 *
 * All values are durations – they are never offset by a start-frame setting.
 *   total frames = seconds * fps + extraFrames
 *   seconds      = total / fps  (integer division)
 *   extraFrames  = total % fps
 *   decimal      = total / fps  (floating point, 4 d.p.)
 *
 * No UI code in this class – it is fully unit-testable.
 */
public class TimeFrameConverter {

    // ── Seconds → Frames ──────────────────────────────────────────────────────

    /**
     * Result of a seconds-to-frames conversion.
     *
     * If {@code valid} is false, {@code errorMessage} explains why and the
     * numeric fields are meaningless.
     */
    public static class SecondsToFramesResult {
        public final boolean valid;
        public final int     totalFrames;
        public final String  errorMessage;

        private SecondsToFramesResult(int totalFrames) {
            this.valid        = true;
            this.totalFrames  = totalFrames;
            this.errorMessage = null;
        }

        private SecondsToFramesResult(String error) {
            this.valid        = false;
            this.totalFrames  = 0;
            this.errorMessage = error;
        }
    }

    /**
     * Convert seconds + extra frames to a total frame count.
     *
     * @param seconds     whole seconds (>= 0)
     * @param extraFrames sub-second frames; must be < fps (not carried automatically –
     *                    an error is returned instead so the user knows to fix the input)
     * @param fps         frames per second (>= 1)
     * @return result with totalFrames = seconds * fps + extraFrames
     */
    public static SecondsToFramesResult secondsToFrames(int seconds, int extraFrames, int fps) {
        if (fps < 1)
            return new SecondsToFramesResult("FPS must be at least 1.");
        if (seconds < 0)
            return new SecondsToFramesResult("Seconds cannot be negative.");
        if (extraFrames < 0)
            return new SecondsToFramesResult("Extra frames cannot be negative.");
        if (extraFrames >= fps)
            return new SecondsToFramesResult(
                "Extra frames (" + extraFrames + ") must be less than FPS (" + fps + ").");

        return new SecondsToFramesResult(seconds * fps + extraFrames);
    }

    // ── Frames → Seconds ──────────────────────────────────────────────────────

    /**
     * Result of a frames-to-seconds conversion.
     */
    public static class FramesToSecondsResult {
        public final boolean valid;
        public final int     wholeSeconds;
        public final int     extraFrames;
        /** Total frames / fps, rounded to 4 decimal places. */
        public final double  decimalSeconds;
        public final String  errorMessage;

        private FramesToSecondsResult(int wholeSeconds, int extraFrames, double decimalSeconds) {
            this.valid          = true;
            this.wholeSeconds   = wholeSeconds;
            this.extraFrames    = extraFrames;
            this.decimalSeconds = decimalSeconds;
            this.errorMessage   = null;
        }

        private FramesToSecondsResult(String error) {
            this.valid          = false;
            this.wholeSeconds   = 0;
            this.extraFrames    = 0;
            this.decimalSeconds = 0;
            this.errorMessage   = error;
        }
    }

    /**
     * Convert a total frame count to seconds and decimal seconds.
     *
     * @param totalFrames total number of frames (>= 0)
     * @param fps         frames per second (>= 1)
     * @return result with wholeSeconds, extraFrames, and decimalSeconds
     */
    public static FramesToSecondsResult framesToSeconds(int totalFrames, int fps) {
        if (fps < 1)
            return new FramesToSecondsResult("FPS must be at least 1.");
        if (totalFrames < 0)
            return new FramesToSecondsResult("Total frames cannot be negative.");

        int    wholeSeconds   = totalFrames / fps;
        int    extraFrames    = totalFrames % fps;
        double decimalSeconds = (double) totalFrames / fps;

        return new FramesToSecondsResult(wholeSeconds, extraFrames, decimalSeconds);
    }
}
