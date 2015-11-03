package com.anthonycaliendo.googleimagesearch.service;

import com.anthonycaliendo.googleimagesearch.ImageResult;

import java.util.List;

public class RecordingResponseHandler implements ImageSearchClient.ResponseHandler {
    public List<ImageResult> images;
    public boolean isFailed;

    public RecordingResponseHandler() {
        isFailed = false;
    }

    @Override
    public void onSuccess(final List<ImageResult> images) {
        this.images = images;
    }

    @Override
    public void onFail() {
        this.isFailed = true;
    }
}
