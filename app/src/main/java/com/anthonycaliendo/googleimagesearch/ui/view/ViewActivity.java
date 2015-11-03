package com.anthonycaliendo.googleimagesearch.ui.view;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.anthonycaliendo.googleimagesearch.ImageResult;
import com.anthonycaliendo.googleimagesearch.R;
import com.ortiz.touch.TouchImageView;
import com.squareup.picasso.Picasso;

/**
 * Activity to view images.
 */
public class ViewActivity extends AppCompatActivity {

    TouchImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        final ImageResult imageResult = getIntent().getParcelableExtra(ImageResult.PARCELABLE_KEY);

        setSupportActionBar((Toolbar) findViewById(R.id.image_view_toolbar));
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(imageResult.title);
        actionBar.setDisplayHomeAsUpEnabled(true);

        imageView = (TouchImageView) findViewById(R.id.image_view_image);

        Picasso.with(this)
                .load(Uri.parse(imageResult.imageUrl))
                .placeholder(R.drawable.ic_image_black_48dp)
                .error(R.drawable.ic_broken_image_black_48dp)
                .into(imageView);

    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        finish();
        return true;
    }
}
