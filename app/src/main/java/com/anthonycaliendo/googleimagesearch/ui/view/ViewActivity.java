package com.anthonycaliendo.googleimagesearch.ui.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.anthonycaliendo.googleimagesearch.ImageResult;
import com.anthonycaliendo.googleimagesearch.R;
import com.ortiz.touch.TouchImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.anthonycaliendo.googleimagesearch.Instrumentation.debug;

/**
 * Activity to view images.
 */
public class ViewActivity extends AppCompatActivity {

    TouchImageView imageView;
    ImageResult    imageResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        imageResult = getIntent().getParcelableExtra(ImageResult.PARCELABLE_KEY);

        setSupportActionBar((Toolbar) findViewById(R.id.image_view_toolbar));
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(imageResult.title);
        actionBar.setDisplayHomeAsUpEnabled(true);

        imageView = (TouchImageView) findViewById(R.id.image_view_image);

        Picasso.with(this)
                .load(Uri.parse(imageResult.imageUrl))
                .placeholder(R.drawable.loading)
                .error(R.drawable.ic_broken_image_black_48dp)
                .into(imageView);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.image_search_filters) {
            shareImage();
        } else {
            finish();
        }

        return true;
    }

    /**
     * Prompts the user with a Share Image dialog.
     */
    private void shareImage() {
        final Uri bmpUri = getLocalBitmapUri();

        if (bmpUri != null) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
            shareIntent.setType("image/*");

            startActivity(
                    Intent.createChooser(
                            shareIntent,
                            getString(R.string.image_view_share_image_title)
                    )
            );
        } else {
            Toast.makeText(
                    this,
                    getString(R.string.image_view_share_image_error),
                    Toast.LENGTH_LONG
            ).show();
        }

    }

    /**
     * Converts the image in the imageview into a Uri.
     * @return
     *      the Uri for the image
     */
    public Uri getLocalBitmapUri() {
        final Drawable drawable = imageView.getDrawable();
        Bitmap bitmap           = null;

        if (drawable instanceof BitmapDrawable){
            bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }

        final Uri parsedRemoteImageUrl = Uri.parse(imageResult.imageUrl);
        final String imageFileName     = parsedRemoteImageUrl.getLastPathSegment();
        final File file                =  new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    imageFileName + ".png"
        );
        file.getParentFile().mkdirs();

        FileOutputStream outputStream  = null;
        try {
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
        } catch (final IOException e) {
            debug(this, "method=getLocalBitmapUri", e);
            throw new RuntimeException(e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    debug(this, "method=getLocalBitmapUri action=closingStream", e);
                }
            }

        }

        return Uri.fromFile(file);
    }

}
