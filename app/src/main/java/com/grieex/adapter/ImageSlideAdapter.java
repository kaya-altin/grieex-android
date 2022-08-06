package com.grieex.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;

import com.grieex.R;
import com.grieex.helper.NLog;
import com.grieex.model.tables.Backdrop;
import com.grieex.widget.AspectRatioImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

public class ImageSlideAdapter extends PagerAdapter {
    private static final String TAG = ImageSlideAdapter.class.getName();
    private final ImageLoader imageLoader;
    private final DisplayImageOptions options;
    private final Activity activity;
    private final ArrayList<Backdrop> images;
    private boolean firstImageLoaded = false;

    private OnImageLoadingListener listener;

    public interface OnImageLoadingListener {
        void onOnImageLoaded();
    }

    private void setOnImageLoadingListener(OnImageLoadingListener listener) {
        this.listener = listener;
    }

    private ImageSlideAdapter(Activity activity, ArrayList<Backdrop> images) {
        this.activity = activity;
        this.images = images;
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).considerExifParams(true).build();
    }

    public ImageSlideAdapter(Activity activity, ArrayList<Backdrop> images, OnImageLoadingListener listener) {
        this.activity = activity;
        this.images = images;
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).considerExifParams(true).build();
        setOnImageLoadingListener(listener);
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
            // mImageView.setOnClickListener(new OnClickListener() {
            //
            // @Override
            // public void onClick(View v) {
            // Bundle arguments = new Bundle();
            // Fragment fragment = null;
            // Log.d("position adapter", "" + position);
            // String product = (String) products.get(position);
            // // arguments.putParcelable("singleProduct", product);
            //
            // // Start a new fragment
            // fragment = new ProductDetailFragment();
            // fragment.setArguments(arguments);
            //
            // FragmentTransaction transaction =
            // activity.getSupportFragmentManager().beginTransaction();
            // transaction.replace(R.id.content_frame, fragment,
            // ProductDetailFragment.ARG_ITEM_ID);
            // transaction.addToBackStack(ProductDetailFragment.ARG_ITEM_ID);
            // transaction.commit();
            // }
            // });
            imageLoader.displayImage(images.get(position).getUrl(), mImageView, options, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {

                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    if (!firstImageLoaded && listener != null) {
                        firstImageLoaded = true;
                        listener.onOnImageLoaded();

//                        new Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                listener.onOnImageLoaded();
//                            }
//                        }, 1000);
                    }
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }
            });
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