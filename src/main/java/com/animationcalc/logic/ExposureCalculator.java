package com.animationcalc.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Calculates exposure-sheet drawing positions from a list of segments.
 *
 * A segment is a pair (frameCount, step): the number of frames it covers and
 * how many frames apart consecutive drawings are.  step=2 means "on 2s".
 *
 * Frame numbering always starts at 1.
 *
 * No UI code in this class – it is fully unit-testable.
 */
public class ExposureCalculator {

    // ── Public data types ─────────────────────────────────────────────────────

    /** One row in the output table. */
    public static class Drawing {
        public final int drawingNumber;
        public final int frame;
        public final int heldFor;

        public Drawing(int drawingNumber, int frame, int heldFor) {
            this.drawingNumber = drawingNumber;
            this.frame         = frame;
            this.heldFor       = heldFor;
        }
    }

    /** One timing segment supplied by the user. */
    public static class Segment {
        public final int frameCount;
        public final int step;

        /**
         * @param frameCount number of frames this segment covers (>= 1)
         * @param step       frames between drawings (>= 1)
         * @throws IllegalArgumentException if either value is out of range
         */
        public Segment(int frameCount, int step) {
            if (frameCount < 1)
                throw new IllegalArgumentException("Frame count must be >= 1, got: " + frameCount);
            if (step < 1)
                throw new IllegalArgumentException("Step must be >= 1, got: " + step);
            this.frameCount = frameCount;
            this.step       = step;
        }
    }

    /** The full result returned by {@link #calculate}. */
    public static class Result {
        public final List<Drawing> drawings;
        public final int totalFrames;

        public Result(List<Drawing> drawings, int totalFrames) {
            this.drawings    = Collections.unmodifiableList(drawings);
            this.totalFrames = totalFrames;
        }
    }

    // ── Core calculation ──────────────────────────────────────────────────────

    /**
     * Calculate drawing positions for one or more segments.
     *
     * <p>Within each segment, drawings land at the segment's first frame,
     * then every {@code step} frames.  If the last drawing would overshoot
     * the end of the segment, it is instead held for the remaining frames
     * (the shorter hold).</p>
     *
     * <p>Frame numbering starts at 1 and is continuous across segments.</p>
     *
     * @param segments non-empty list of segments
     * @return the full result
     * @throws IllegalArgumentException if the list is null or empty
     */
    public Result calculate(List<Segment> segments) {
        if (segments == null || segments.isEmpty())
            throw new IllegalArgumentException("At least one segment is required.");

        List<Drawing> drawings = new ArrayList<>();
        int drawingNumber = 1;
        // absoluteFrame tracks the first frame of the current segment (1-based).
        int absoluteFrame = 1;

        for (Segment seg : segments) {
            int frameCount = seg.frameCount;
            int step       = seg.step;
            // segOffset is 0-based within this segment.
            int segOffset = 0;

            while (segOffset < frameCount) {
                int remaining = frameCount - segOffset;
                // Hold is a full step, unless we're at the last (short) drawing.
                int hold = Math.min(step, remaining);

                drawings.add(new Drawing(drawingNumber, absoluteFrame + segOffset, hold));
                drawingNumber++;
                segOffset += step;
            }

            absoluteFrame += frameCount;
        }

        int totalFrames = absoluteFrame - 1; // absoluteFrame is now one past the end
        return new Result(drawings, totalFrames);
    }
}
