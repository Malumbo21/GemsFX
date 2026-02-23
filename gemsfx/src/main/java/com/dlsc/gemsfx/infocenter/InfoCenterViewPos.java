package com.dlsc.gemsfx.infocenter;

/**
 * Defines the position of the {@link InfoCenterView} within the {@link InfoCenterPane}.
 * The horizontal component (LEFT or RIGHT) determines which edge the info center slides
 * in from, and the vertical component (TOP, CENTER, or BOTTOM) determines where it is
 * placed along that edge.
 */
public enum InfoCenterViewPos {

    TOP_LEFT,
    TOP_RIGHT,

    CENTER_LEFT,
    CENTER_RIGHT,

    BOTTOM_LEFT,
    BOTTOM_RIGHT;

    /**
     * Returns {@code true} if this position is on the left side.
     *
     * @return {@code true} if the position is on the left side
     */
    public boolean isLeft() {
        return this == TOP_LEFT || this == CENTER_LEFT || this == BOTTOM_LEFT;
    }

    /**
     * Returns {@code true} if this position is on the right side.
     *
     * @return {@code true} if the position is on the right side
     */
    public boolean isRight() {
        return !isLeft();
    }
}