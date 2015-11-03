package com.anthonycaliendo.googleimagesearch.ui.search;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.anthonycaliendo.googleimagesearch.ImageQuery;
import com.anthonycaliendo.googleimagesearch.ImageResult;
import com.anthonycaliendo.googleimagesearch.R;
import com.anthonycaliendo.googleimagesearch.service.ImageSearchClient;
import com.anthonycaliendo.googleimagesearch.ui.view.ViewActivity;
import com.etsy.android.grid.StaggeredGridView;
import com.loopj.android.http.AsyncHttpClient;

import java.util.ArrayList;
import java.util.List;

import static com.anthonycaliendo.googleimagesearch.Instrumentation.debug;

/**
 * Activity to search for images.
 */
public class SearchActivity extends AppCompatActivity {

    /**
     * The maximum images we can search for.
     */
    private static final int MAX_IMAGE_COUNT = 64;

    private ImageSearchClient  imageSearchClient;
    private ImageQuery         searchQuery;
    private ImageResultAdapter imageResultAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setSupportActionBar((Toolbar) findViewById(R.id.image_search_toolbar));

        imageResultAdapter = new ImageResultAdapter(this, new ArrayList<ImageResult>());
        searchQuery        = new ImageQuery();
        imageSearchClient  = new ImageSearchClient(new AsyncHttpClient());

        final StaggeredGridView imageResultList = (StaggeredGridView) findViewById(R.id.image_search_results);
        imageResultList.setAdapter(imageResultAdapter);
        imageResultList.setOnScrollListener(new EndlessScrollListener(1) {
            @Override
            public boolean onLoadMore(final int page, final int totalItemsCount) {
                // Guard against this being invoked when it shouldn't be, as the current
                // EndlessScrollListener fires this method when it shouldn't.
                if (searchQuery.offset >= totalItemsCount || searchQuery.offset >= MAX_IMAGE_COUNT) {
                    return false;
                }

                debug(this, "handler=onScrollListener page=" + page + " totalItemsCount=" + totalItemsCount);
                searchQuery.offset = totalItemsCount;
                executeSearch(false);

                return false;
            }
        });
        imageResultList.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(final AdapterView<?> parent, View view, final int position, final long id) {
                        debug(this, "handler=onItemClickListener action=imageClicked position=" + position);

                        final ImageResult imageResult = imageResultAdapter.getItem(position);
                        final Intent intent           = new Intent(SearchActivity.this, ViewActivity.class);

                        intent.putExtra(ImageResult.PARCELABLE_KEY, imageResult);

                        startActivity(intent);
                    }
                }
        );

        toggleNetworkConnectivityMessage();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        final MenuItem searchItem   = menu.findItem(R.id.image_search_query);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                searchQuery.query = query;
                executeSearch(true);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.image_search_filters) {
            showEditFiltersDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Executes a search, and populates the results. If this is a fresh search, then existing results
     * are removed and pagination is reset.
     * @param freshSearch
     *      indicates if this search should be treated as a fresh search
     */
    private void executeSearch(final boolean freshSearch) {
        final View instructions = findViewById(R.id.image_search_instructions);
        instructions.setVisibility(View.INVISIBLE);

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.image_search_progress_bar);
        progressBar.setVisibility(ProgressBar.VISIBLE);
        progressBar.bringToFront();

        if (freshSearch) {
            searchQuery.offset = 0;
        }

        imageSearchClient.searchImages(searchQuery, new ImageSearchClient.ResponseHandler() {
            @Override
            public void onSuccess(final List<ImageResult> images) {
                if (freshSearch) {
                    imageResultAdapter.clear();
                }
                imageResultAdapter.addAll(images);
                progressBar.setVisibility(ProgressBar.INVISIBLE);
            }

            @Override
            public void onFail() {
                progressBar.setVisibility(ProgressBar.INVISIBLE);
                notifyNetworkIssue();
            }
        });
    }

    /**
     * Creates and shows the edit filters dialog.
     */
    private void showEditFiltersDialog() {
        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.image_search_edit_filters_title)
                .customView(R.layout.fragment_edit_filters, true)
                .positiveText(R.string.image_search_edit_filters_save)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(final MaterialDialog dialog, final DialogAction which) {
                        final View dialogView = dialog.getCustomView();

                        final TextView siteText = (TextView) dialogView.findViewById(R.id.image_search_edit_filters_site);
                        searchQuery.site = siteText.getText().toString();

                        final Spinner sizeSpinner = (Spinner) dialogView.findViewById(R.id.image_search_edit_filters_size);
                        searchQuery.size = (ImageQuery.Size) convertSelectionPositionToEnum(sizeSpinner.getSelectedItemPosition(), ImageQuery.Size.values());

                        final Spinner colorSpinner = (Spinner) dialogView.findViewById(R.id.image_search_edit_filters_color);
                        searchQuery.color = (ImageQuery.Color) convertSelectionPositionToEnum(colorSpinner.getSelectedItemPosition(), ImageQuery.Color.values());

                        final Spinner typeSpinner = (Spinner) dialogView.findViewById(R.id.image_search_edit_filters_type);
                        searchQuery.type = (ImageQuery.Type) convertSelectionPositionToEnum(typeSpinner.getSelectedItemPosition(), ImageQuery.Type.values());

                        dialog.dismiss();
                        executeSearch(true);
                    }
                })
                .negativeText(R.string.image_search_edit_filters_cancel)
                .autoDismiss(true)
                .show();

        final View dialogView = dialog.getCustomView();

        final Spinner sizeSpinner = (Spinner) dialogView.findViewById(R.id.image_search_edit_filters_size);
        final ArrayAdapter<CharSequence> sizeAdapter = ArrayAdapter.createFromResource(this, R.array.image_sizes, android.R.layout.simple_spinner_item);
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sizeSpinner.setAdapter(sizeAdapter);
        sizeSpinner.setSelection(determineSelectionPositionForFilter(searchQuery.size));

        final Spinner colorSpinner = (Spinner) dialogView.findViewById(R.id.image_search_edit_filters_color);
        final ArrayAdapter<CharSequence> colorAdapter = ArrayAdapter.createFromResource(this, R.array.image_colors, android.R.layout.simple_spinner_item);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSpinner.setAdapter(colorAdapter);
        colorSpinner.setSelection(determineSelectionPositionForFilter(searchQuery.color));

        final Spinner typeSpinner = (Spinner) dialogView.findViewById(R.id.image_search_edit_filters_type);
        final ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this, R.array.image_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);
        typeSpinner.setSelection(determineSelectionPositionForFilter(searchQuery.type));

        final TextView siteText = (TextView) dialogView.findViewById(R.id.image_search_edit_filters_site);
        siteText.setText(searchQuery.site);
    }

    /**
     * Returns the selection position for the specified value.  Nullsafe.
     * @param enumValue
     *      the enum value to determine the position for
     * @return
     *      the selection position
     */
    private int determineSelectionPositionForFilter(final Enum enumValue) {
        if (enumValue == null) {
            return 0;
        }

        return enumValue.ordinal() + 1;
    }

    /**
     * Returns the enum value for the passed selection position.
     * @param position
     *      the position of the selectorion
     * @param enumValues
     *      the enum values to pick from
     * @param <T>
     *      the enum type
     * @return
     *      the enum value
     */
    private <T extends Enum<T>> Enum<T> convertSelectionPositionToEnum(final int position, final T[] enumValues) {
        if (position <= 0 || position > enumValues.length) {
            return null;
        }

        return enumValues[position - 1];
    }
    /**
     * Checks if the network is available, and toggles display messaging based on the status.
     */
    private void toggleNetworkConnectivityMessage() {
        final NetworkInfo networkInfo = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            findViewById(R.id.image_search_network_error).setVisibility(View.INVISIBLE);
        } else {
            findViewById(R.id.image_search_network_error).setVisibility(View.VISIBLE);
            findViewById(R.id.image_search_instructions).setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Show an alert dialog indicating a network issue.
     */
    private void notifyNetworkIssue() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.image_search_network_error_dialog_title))
                .setMessage(getString(R.string.image_search_network_error_dialog_message))
                .setIcon(R.drawable.ic_signal_wifi_off_black_24dp)
                .setPositiveButton(getString(R.string.image_search_network_error_dialog_confirm), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }
}
