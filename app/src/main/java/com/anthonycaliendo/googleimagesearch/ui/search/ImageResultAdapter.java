package com.anthonycaliendo.googleimagesearch.ui.search;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.anthonycaliendo.googleimagesearch.ImageResult;
import com.anthonycaliendo.googleimagesearch.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Extended {@link ArrayAdapter} which operates on {@link ImageResult} objects
 */
class ImageResultAdapter extends ArrayAdapter<ImageResult> {

    private static class ViewHolder {
        ImageView previewImage;
        TextView  title;
    }

    public ImageResultAdapter(final Context context, final List<ImageResult> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ImageResult item = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_image_result, parent, false);
            viewHolder.previewImage = (ImageView)convertView.findViewById(R.id.item_image_result_preview);
            viewHolder.title = (TextView)convertView.findViewById(R.id.item_image_result_title);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.title.setText(item.title);

        Picasso.with(getContext())
                .load(Uri.parse(item.previewImageUrl))
                .placeholder(R.drawable.loading)
                .error(R.drawable.ic_broken_image_black_24dp)
                .into(viewHolder.previewImage);

        return convertView;
    }
}
