package com.anthonycaliendo.googleimagesearch;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents an image result.
 */
public class ImageResult implements Parcelable {

    public static final String PARCELABLE_KEY = ImageResult.class.getCanonicalName();

    /**
     * The preview/thumbnail image url.
     */
    public String previewImageUrl;

    /**
     * The full image url.
     */
    public String imageUrl;

    /**
     * The title for the image;
     */
    public String title;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.previewImageUrl);
        dest.writeString(this.imageUrl);
        dest.writeString(this.title);
    }

    public ImageResult() {
    }

    private ImageResult(Parcel in) {
        this.previewImageUrl = in.readString();
        this.imageUrl = in.readString();
        this.title = in.readString();
    }

    public static final Creator<ImageResult> CREATOR = new Creator<ImageResult>() {
        public ImageResult createFromParcel(Parcel source) {
            return new ImageResult(source);
        }

        public ImageResult[] newArray(int size) {
            return new ImageResult[size];
        }
    };
}
