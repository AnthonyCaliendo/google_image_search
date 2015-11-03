package com.anthonycaliendo.googleimagesearch;

/**
 * Represents the search filters for an image query.
 */
public class ImageQuery {

    public enum Color {
        black, blue, brown, gray, green, orange, pink, purple, red, teal, white, yellow;
    }

    public enum Size {
        small, medium, large, extraLarge;
    }

    public enum Type {
        face, photo, clipart, lineart;
    }

    /**
     * The query text to search on.
     */
    public String query;

    /**
     * The pagination offset.
     */
    public int offset;

    /**
     * The site to filter on.
     */
    public String site;

    /**
     * The image color to filter on.
     */
    public Color color;

    /**
     * The image size to filter on.
     */
    public Size size;

    /**
     * The image type to filter on.
     */
    public Type type;

    public ImageQuery() {
        offset = 0;
    }

    public boolean hasSite() {
        return site != null && !site.isEmpty();
    }

    public boolean hasColor() {
        return color != null;
    }

    public boolean hasSize() {
        return size != null;
    }

    public boolean hasType() {
        return type != null;
    }
}
