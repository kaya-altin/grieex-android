package com.grieex.adapter;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.grieex.R;
import com.grieex.helper.NLog;
import com.grieex.model.tables.Backdrop;
import com.grieex.widget.AspectRatioImageView;

import java.util.ArrayList;

public class ImageSlideAdapter extends PagerAdapter {
    private static final String TAG = ImageSlideAdapter.class.getName();
    private final Activity activity;
    private final ArrayList<Backdrop> images;
    private boolean firstImageLoaded = false;

    private OnImageLoadingListener listener;

    private ImageSlideAdapter(Activity activity, ArrayList<Backdrop> images) {
        this.activity = activity;
        this.images = images;
    }

    public ImageSlideAdapter(Activity activity, ArrayList<Backdrop> images, OnImageLoadingListener listener) {
        this.activity = activity;
        this.images = images;
        setOnImageLoadingListener(listener);
    }

    private void setOnImageLoadingListener(OnImageLoadingListener listener) {
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public View instantiateItem(ViewGroup container, final int position) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.vp_image, container, false);
        try {
            AspectRatioImageView mImageView = view.findViewById(R.id.image_display);

            Glide.with(activity)
                    .load(images.get(position).getUrl())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            if (!firstImageLoaded && listener != null) {
                                firstImageLoaded = true;
                                listener.onOnImageLoaded();
                            }
                            return false;
                        }
                    })
                    .into(mImageView);

            container.addView(view);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public interface OnImageLoadingListener {
        void onOnImageLoaded();
    }

//	private static class ImageDisplayListener extends SimpleImageLoadingListener {
//
//		static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());
//
//		@Override
//		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//			try {
//				if (loadedImage != null) {
//					ImageView imageView = (ImageView) view;
//					boolean firstDisplay = !displayedImages.contains(imageUri);
//					if (firstDisplay) {
//						FadeInBitmapDisplayer.animate(imageView, 500);
//						displayedImages.add(imageUri);
//					}
//				}
//			} catch (Exception e) {
//				NLog.e(TAG, e);
//			}
//		}
//	}
}