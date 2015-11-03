package com.anthonycaliendo.googleimagesearch.service;

import com.anthonycaliendo.googleimagesearch.ImageResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.anthonycaliendo.googleimagesearch.Instrumentation.debug;

/**
 * Parses responses for clients.
 */
class ResponseParser {

    /**
     * Parses the json response into a list of {@link ImageResult} objects.  Unprocessable results are
     * ignored.
     *
     * @param responseObject
     *      the response to parse
     * @return
     *      the parsed list of response objects.
     */
    List<ImageResult> parseImageSearchResponse(final JSONObject responseObject) {
        final ArrayList<ImageResult> images = new ArrayList<>();

        final JSONObject responseData = responseObject.optJSONObject("responseData");
        if (responseData == null) {
            return images;
        }

        final JSONArray results = responseData.optJSONArray("results");
        if (results == null) {
            return images;
        }

        for (int imageIndex = 0; imageIndex < results.length(); imageIndex++) {
            final ImageResult image = parseImageResult(results.optJSONObject(imageIndex));

            if (image != null) {
                images.add(image);
            }
        }

        debug(this, "method=parseImageSearchResponse inputSize=" + results.length() + " ouputSize=" + images.size());

        return images;
    }

    /**
     * Parses the image result from json and into the domain.
     *
     * @param jsonObject
     *      the json object to parse.  may be null
     * @return
     *      the parsed object.  will return {@code null} if the jsonObject is null or cannot be parsed
     */
    private ImageResult parseImageResult(final JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }

        final ImageResult image  = new ImageResult();
        image.previewImageUrl = jsonObject.optString("tbUrl");
        image.title              = jsonObject.optString("titleNoFormatting");
        image.imageUrl           = jsonObject.optString("unescapedUrl");

        return image;
    }
}
