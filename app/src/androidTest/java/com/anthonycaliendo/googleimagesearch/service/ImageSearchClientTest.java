package com.anthonycaliendo.googleimagesearch.service;

import android.test.AndroidTestCase;

import com.anthonycaliendo.googleimagesearch.ImageQuery;
import com.anthonycaliendo.googleimagesearch.ImageResult;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class ImageSearchClientTest extends AndroidTestCase {

    public void testSearchImages_200ResponseWithData_InvokesOnSuccessAndReturnsResults() {
        final RecordingResponseHandler responseHandler = searchImages("{\n" +
                "  \"responseData\" : {\n" +
                "    \"results\" : [{\n" +
                "            \"originalContextUrl\" : \"https://www.youtube.com/watch?v=KY4IzMcjX3Y\",\n" +
                "            \"width\" : \"1280\",\n" +
                "            \"GsearchResultClass\" : \"GimageSearch\",\n" +
                "            \"content\" : \"WORLD&#39;S FUNNIEST <b>DOG</b> FAILS\",\n" +
                "            \"tbUrl\" : \"http://example.com/first-result-preview.jpg\",\n" +
                "            \"visibleUrl\" : \"www.youtube.com\",\n" +
                "            \"height\" : \"720\",\n" +
                "            \"contentNoFormatting\" : \"WORLD&#39;S FUNNIEST DOG FAILS\",\n" +
                "            \"tbWidth\" : \"150\",\n" +
                "            \"imageId\" : \"ANd9GcQua1V7xxVaQFwNMQy0kccVUK_Rb4ZlCl1Q-UTYtxytB3GGG51o2fwt36t2\",\n" +
                "            \"tbHeight\" : \"84\",\n" +
                "            \"titleNoFormatting\" : \"WORLD&#39;S FUNNIEST DOG FAILS - YouTube\",\n" +
                "            \"url\" : \"https://i.ytimg.com/vi/KY4IzMcjX3Y/maxresdefault.jpg\",\n" +
                "            \"title\" : \"WORLD&#39;S FUNNIEST <b>DOG</b> FAILS - YouTube\",\n" +
                "            \"unescapedUrl\" : \"https://i.ytimg.com/vi/KY4IzMcjX3Y/maxresdefault.jpg\"\n" +
                "         }],\n" +
                "    \"cursor\" : {}\n" +
                "  },\n" +
                "  \"responseDetails\" : null,\n" +
                "  \"responseStatus\" : 200\n" +
                "}");

        assertFalse("should not have failed", responseHandler.isFailed);
        assertEquals("should return 1 result", 1, responseHandler.images.size());
        assertEquals("should return the correct preview url", "http://example.com/first-result-preview.jpg", responseHandler.images.get(0).previewImageUrl);
    }

    public void testSearchImages_Non200Response_InvokesOnFail() {
        final RecordingResponseHandler responseHandler = searchImages("{\n" +
                "  \"responseDetails\" : \"something-broke-at-google\",\n" +
                "  \"responseStatus\" : 500\n" +
                "}");

        assertTrue("should have failed", responseHandler.isFailed);
    }

    public void testSearchImages_200Response_NoResponseDataResults_InvokesOnFail() {
        final RecordingResponseHandler responseHandler = searchImages("{\n" +
                "  \"responseData\" : {\n" +
                "  },\n" +
                "  \"responseDetails\" : null\n" +
                "  \"responseStatus\" : 200\n" +
                "}");

        assertTrue("should have failed", responseHandler.isFailed);
    }

    public void testSearchImages_200Response_NoResponseData_InvokesOnFail() {
        final RecordingResponseHandler responseHandler = searchImages("{\n" +
                "  \"responseDetails\" : null\n" +
                "  \"responseStatus\" : 200\n" +
                "}\n");

        assertTrue("should have failed", responseHandler.isFailed);
    }

    public void testSearchImages_ExceptionThrownInHttpClient_InvokesOnFail() {
        final ImageSearchClient client = new ImageSearchClient(new AsyncHttpClient() {
            @Override
            public RequestHandle get(final String url, final RequestParams params, final ResponseHandlerInterface responseHandler) {
                throw new RuntimeException("http client exception");
            }
        });

        final RecordingResponseHandler responseHandler = new RecordingResponseHandler() {
            @Override
            public void onSuccess(final List<ImageResult> images) {
                fail("should not invoke the success callback");
            }
        };

        client.searchImages(null, responseHandler);

        assertTrue("should have invoked the onFail handler", responseHandler.isFailed);
    }

    public void testSearchImages_ExceptionThrownInResponseParser_InvokesOnFail() {
        final RecordingResponseHandler responseHandler =  searchImages("query", new ResponseParser() {
            @Override
            List<ImageResult> parseImageSearchResponse(final JSONObject responseObject) {
                throw new RuntimeException("response parser exception");
            }
        });

        assertTrue("should have invoked the onFail handler", responseHandler.isFailed);
    }

    public void testSearchImages_InvokesCorrectHttpRequest() {
        class RecordingAsyncHttpClient extends AsyncHttpClient {
            public String              invokedUrl;
            public Map<String, String> requestParams;

            @Override
            public RequestHandle get(final String url, final RequestParams requestParams, final ResponseHandlerInterface responseHandler) {
                this.invokedUrl    = url;

                // {@link RequestParams} is designed poorly, so we need to use reflection to grab what is inside it.
                final Field field;
                try {
                    field = requestParams.getClass().getDeclaredField("urlParams");
                    field.setAccessible(true);
                    this.requestParams = (Map<String, String>) field.get(requestParams);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        }

        final RecordingAsyncHttpClient asyncHttpClient = new RecordingAsyncHttpClient();
        final ImageSearchClient client                 = new ImageSearchClient(asyncHttpClient);
        final ImageQuery imageQuery                    = new ImageQuery();

        imageQuery.query  = "the image search";
        imageQuery.offset = 7;
        imageQuery.site   = "groupon.com";
        imageQuery.size   = ImageQuery.Size.medium;
        imageQuery.color  = ImageQuery.Color.blue;
        imageQuery.type   = ImageQuery.Type.face;

        client.searchImages(imageQuery, new RecordingResponseHandler());

        assertEquals("should pass correct url into remoter", "https://ajax.googleapis.com/ajax/services/search/images", asyncHttpClient.invokedUrl);
        assertEquals("should pass correct query param", "the image search", asyncHttpClient.requestParams.get("q"));
        assertEquals("should pass correct offset param", 7, asyncHttpClient.requestParams.get("start"));
        assertEquals("should pass correct version param", "1.0" , asyncHttpClient.requestParams.get("v"));
        assertEquals("should pass correct result size param", "8" , asyncHttpClient.requestParams.get("rsz"));
        assertEquals("should pass correct site search param", "groupon.com" , asyncHttpClient.requestParams.get("as_sitesearch"));
        assertEquals("should pass correct color param", "blue" , asyncHttpClient.requestParams.get("imgcolor"));
        assertEquals("should pass correct size param", "medium" , asyncHttpClient.requestParams.get("imgsz"));
        assertEquals("should pass correct type param", "face" , asyncHttpClient.requestParams.get("imgtype"));
    }


    /**
     * Creates an {@link ImageSearchClient} with a stub {@link AsyncHttpClient} which will immediately
     * callback with a successful response using the specified payload.
     *
     * @param jsonResponse
     *      the json payload to use as a response
     * @return
     *      the response handler for the request
     */
    private RecordingResponseHandler searchImages(final String jsonResponse) {
        return searchImages(jsonResponse, null);
    }

    /**
     * Creates an {@link ImageSearchClient} with a stub {@link AsyncHttpClient} which will immediately
     * callback with a successful response using the specified payload.
     *
     * @param jsonResponse
     *      the json payload to use as a response
     * @param responseParser
     *      the response parser to use to parse the response
     * @return
     *      the response handler for the request
     */
    private RecordingResponseHandler searchImages(final String jsonResponse, ResponseParser responseParser) {
        if (responseParser == null) {
            responseParser = new ResponseParser();
        }

        final AsyncHttpClient stubbedAsyncClient = new AsyncHttpClient() {
            @Override
            public RequestHandle get(final String url, final RequestParams requestParams, final ResponseHandlerInterface responseHandler) {
                final JsonHttpResponseHandler jsonResponseHandler = (JsonHttpResponseHandler) responseHandler;
                try {
                    jsonResponseHandler.onSuccess(200, null, new JSONObject(jsonResponse));
                } catch (final JSONException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        };

        final ImageSearchClient client = new ImageSearchClient(stubbedAsyncClient, responseParser);

        final RecordingResponseHandler responseHandler = new RecordingResponseHandler();
        client.searchImages(new ImageQuery(), responseHandler);

        return responseHandler;
    }
}
