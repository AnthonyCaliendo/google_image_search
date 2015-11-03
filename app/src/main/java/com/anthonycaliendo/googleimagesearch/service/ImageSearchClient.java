package com.anthonycaliendo.googleimagesearch.service;

import android.support.annotation.NonNull;

import com.anthonycaliendo.googleimagesearch.ImageQuery;
import com.anthonycaliendo.googleimagesearch.ImageResult;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

import static com.anthonycaliendo.googleimagesearch.Instrumentation.debug;

/**
 * Client for interacting with the image search API.
 */
public class ImageSearchClient {

    /**
     * Async callback to handle responses.
     */
    public interface ResponseHandler {
        /**
         * Callback method invoked on a successful response.
         *
         * @param images
         *      the list of imagesbeing returned
         */
        void onSuccess(List<ImageResult> images);

        /**
         * Callback method invoked when unable to retrieve a response for any reason.
         */
        void onFail();
    }

    /**
     * The url for the image search endpoint.
     */
    private static final String IMAGE_SEARCH_URL = "https://ajax.googleapis.com/ajax/services/search/images";

    /**
     * The param name for the query to search for.
     */
    private static final String QUERY_STRING_PARAM_NAME = "q";

    /**
     * The query param which specifies version.
     */
    private static final String VERSION_PARAM_NAME = "v";

    /**
     * The value for the version query param.
     */
    private static final String VERSION_PARAM_VALUE = "1.0";

    /**
     * The query param for the site filter.
     */
    private static final String SITE_FILTER_PARAM_NAME = "as_sitesearch";

    /**
     * The param name for predominant image color to search for.
     */
    private static final String COLOR_FILTER_PARAM_NAME = "imgcolor";

    /**
     * The param name for the image size to search for.
     */
    private static final String SIZE_FILTER_PARAM_NAME = "imgsz";

    private static final Map<ImageQuery.Size, String> SIZE_PARAM_VALUE_MAPPING = new HashMap<>();
    static {
        SIZE_PARAM_VALUE_MAPPING.put(ImageQuery.Size.small, "icon");
        SIZE_PARAM_VALUE_MAPPING.put(ImageQuery.Size.medium, "medium");
        SIZE_PARAM_VALUE_MAPPING.put(ImageQuery.Size.large, "xxlarge");
        SIZE_PARAM_VALUE_MAPPING.put(ImageQuery.Size.extraLarge, "huge");
    }

    /**
     * The param name for the type of image to search for.
     */
    private static final String TYPE_FITER_PARAM_NAME = "imgtype";

    /**
     * The param name for the number of results to return.
     */
    private static final String RESULT_SIZE_PARAM_NAME = "rsz";

    /**
     * The number of results to return.
     */
    private static final String RESULT_SIZE_PARAM_VALUE = "8";

    /**
     * The param name for the offset to use.
     */
    private static final String OFFSET_PARAM_NAME = "start";

    /**
     * Used to make HTTP calls.
     */
    private final AsyncHttpClient asyncHttpClient;

    /**
     * Used to parse the JSON responses.
     */
    private final ResponseParser responseParser;


    /**
     * Instantiates an Instagram client which can make calls to instagram.
     * Requests are made asynchronously, and will invoke callbacks when they complete.
     *  @param asyncHttpClient
     *      the {@link AsyncHttpClient} to use to make requests
     *
     */
    public ImageSearchClient(final AsyncHttpClient asyncHttpClient) {
        this(asyncHttpClient, new ResponseParser());
    }

    /**
     * Instantiates an Instagram client which can make calls to instagram.
     * Requests are made asynchronously, and will invoke callbacks when they complete.
     *  @param asyncHttpClient
     *      the {@link AsyncHttpClient} to use to make requests
     *  @param responseParser
     *      the {@link ResponseParser} to use to parse responses
     */
    public ImageSearchClient(final AsyncHttpClient asyncHttpClient, final ResponseParser responseParser) {
        this.asyncHttpClient = asyncHttpClient;
        this.responseParser  = responseParser;
    }

    /**
     * Make a an image search request, parse the response, and return the results.
     *
     * @param imageQuery
     *      the filters to apply to the search
     * @param responseHandler
     *      the callbacks to handle the response
     */
    public void searchImages(final ImageQuery imageQuery, final ResponseHandler responseHandler) {
        final RequestParams requestParams = buildRequestParams(imageQuery);

        try {
            asyncHttpClient.get(IMAGE_SEARCH_URL, requestParams, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(final int statusCode, final Header[] headers, final JSONObject response) {
                    final int responseStatus = response.optInt("responseStatus");
                    if (responseStatus != 200) {
                        responseHandler.onFail();
                        return;
                    }

                    debug(this, "method=searchImages handler=onSuccess statusCode=" + statusCode + " response=" + response.toString());
                    try {
                        responseHandler.onSuccess(responseParser.parseImageSearchResponse(response));
                    } catch (final RuntimeException e) {
                        debug(this, "method=searchImages handler=onSuccess", e);
                        responseHandler.onFail();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    debug(this, "method=searchImages handler=onFailure statusCode=" + statusCode);
                    responseHandler.onFail();
                }
            });
        } catch (final Exception e){
            debug(this, "method=searchImages", e);
            responseHandler.onFail();
        }
    }

    @NonNull
    private RequestParams buildRequestParams(final ImageQuery imageQuery) {
        final RequestParams requestParams = new RequestParams();

        requestParams.put(VERSION_PARAM_NAME, VERSION_PARAM_VALUE);
        requestParams.put(RESULT_SIZE_PARAM_NAME, RESULT_SIZE_PARAM_VALUE);

        if (imageQuery == null) {
            return requestParams;
        }

        requestParams.put(QUERY_STRING_PARAM_NAME, imageQuery.query);
        requestParams.put(OFFSET_PARAM_NAME, imageQuery.offset);

        if (imageQuery.hasSite()) {
            requestParams.put(SITE_FILTER_PARAM_NAME, imageQuery.site);
        }

        if (imageQuery.hasColor()) {
            requestParams.put(COLOR_FILTER_PARAM_NAME, imageQuery.color.toString());
        }

        if (imageQuery.hasSize()) {
            requestParams.put(SIZE_FILTER_PARAM_NAME, SIZE_PARAM_VALUE_MAPPING.get(imageQuery.size));
        }

        if (imageQuery.hasType()) {
            requestParams.put(TYPE_FITER_PARAM_NAME, imageQuery.type.toString());
        }

        debug(this, "method=buildRequestParams requestParams=" + requestParams.toString());

        return requestParams;
    }
}
