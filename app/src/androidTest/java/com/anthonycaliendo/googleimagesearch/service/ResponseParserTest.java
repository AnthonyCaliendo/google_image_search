package com.anthonycaliendo.googleimagesearch.service;

import android.test.AndroidTestCase;

import com.anthonycaliendo.googleimagesearch.ImageResult;

import org.json.JSONObject;

import java.util.List;

public class ResponseParserTest extends AndroidTestCase {

    ResponseParser parser;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        parser = new ResponseParser();
    }

    public void testParseImageSearchResponse_ParsesResponseIntoList() throws Exception {
        final List<ImageResult> images = parser.parseImageSearchResponse(new JSONObject("{\n" +
                "  \"responseData\": {\n" +
                "    \"results\": [\n" +
                "      {\n" +
                "        \"originalContextUrl\": \"https://www.petfinder.com/pet-adoption/dog-adoption/\",\n" +
                "        \"width\": \"2048\",\n" +
                "        \"GsearchResultClass\": \"GimageSearch\",\n" +
                "        \"content\": \"<b>Dog</b> Adoption\",\n" +
                "        \"tbUrl\": \"http://example.com/result1-preview.jpg\",\n" +
                "        \"visibleUrl\": \"www.petfinder.com\",\n" +
                "        \"height\": \"1536\",\n" +
                "        \"contentNoFormatting\": \"result1-content\",\n" +
                "        \"tbWidth\": \"100\",\n" +
                "        \"imageId\": \"ANd9GcSqGZcxLYGBgK_IbVRU0UF5t0q7EJmQ-d89zcElAQiSlEXnWjb0TG6hLlwK\",\n" +
                "        \"tbHeight\": \"101\",\n" +
                "        \"titleNoFormatting\": \"result1-title\",\n" +
                "        \"url\": \"https://www.petfinder.com/wp-content/uploads/2012/11/dog-how-to-select-your-new-best-friend-thinkstock99062463.jpg\",\n" +
                "        \"title\": \"<b>Dog</b> Adoption - Petfinder\",\n" +
                "        \"unescapedUrl\": \"https://example.com/result1.jpg\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"originalContextUrl\": \"http://weknowyourdreams.com/dog.html\",\n" +
                "        \"width\": \"2121\",\n" +
                "        \"GsearchResultClass\": \"GimageSearch\",\n" +
                "        \"content\": \"Dreams Interpretation - <b>Dog</b>\",\n" +
                "        \"tbUrl\": \"http://example.com/result2-preview.jpg\",\n" +
                "        \"visibleUrl\": \"weknowyourdreams.com\",\n" +
                "        \"height\": \"2317\",\n" +
                "        \"contentNoFormatting\": \"result2-content\",\n" +
                "        \"tbWidth\": \"200\",\n" +
                "        \"imageId\": \"ANd9GcT8RE2cQLz6yHvZl9FeE3jq2eXh50Mk-0jSTuO4zYtp9D0VN8_m6ayO8kj6vA\",\n" +
                "        \"tbHeight\": \"202\",\n" +
                "        \"titleNoFormatting\": \"result2-title\",\n" +
                "        \"url\": \"http://weknowyourdreams.com/images/dog/dog-07.jpg\",\n" +
                "        \"title\": \"Interpretation of a dream in which you saw «<b>Dog</b>»\",\n" +
                "        \"unescapedUrl\": \"https://example.com/result2.jpg\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}"));

        assertEquals("should have empty images list", 2, images.size());

        final ImageResult firstImage = images.get(0);
        assertEquals("should set the preview url", "http://example.com/result1-preview.jpg", firstImage.previewImageUrl);
        assertEquals("should set the title", "result1-title", firstImage.title);
        assertEquals("should set the image url", "http://example.com/result1.jpg", firstImage.imageUrl);

        final ImageResult secondImage = images.get(1);
        assertEquals("should set the preview url", "http://example.com/result2-preview.jpg", secondImage.previewImageUrl);
        assertEquals("should set the title", "result2-title", secondImage.title);
        assertEquals("should set the image url", "http://example.com/result2.jpg", secondImage.imageUrl);
    }

    public void testParseImageSearchResponse_ResultMissingDataData_SkipsResult() throws Exception {

    }

    public void testParseImageSearchResponse_HandlesMissingResponseData() throws Exception {
        final List<ImageResult> images = parser.parseImageSearchResponse(new JSONObject("{}"));

        assertEquals("should have empty images list", 0, images.size());
    }

    public void testParseImageSearchResponse_HandlesMissingResponseDataResults() throws Exception {
        final List<ImageResult> images = parser.parseImageSearchResponse(new JSONObject("{\"responseDate\":{}}"));

        assertEquals("should have empty images list", 0, images.size());
    }
}
