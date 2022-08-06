package com.grieex.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.grieex.R;
import com.grieex.helper.Constants;
import com.grieex.helper.NLog;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class FullScreenImageActivity extends Activity {
    private static final String TAG = FullScreenImageActivity.class.getName();
    private ImageLoader imageLoader;
    private DisplayImageOptions options;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.actvity_full_screen_image);
        try {
            imageLoader = ImageLoader.getInstance();
            options = new DisplayImageOptions.Builder().cacheInMemory(false).cacheOnDisk(false).considerExifParams(true).build();

            final String ImageLink = this.getIntent().getStringExtra(Constants.ImageLink);

            final ProgressBar progressBar1 = findViewById(R.id.progressBar1);

            if (TextUtils.isEmpty(ImageLink)) {
                progressBar1.setVisibility(View.GONE);
                return;
            }
            final ImageView ivFullImage = findViewById(R.id.ivFullImage);


            imageLoader.displayImage(ImageLink, ivFullImage, options, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    progressBar1.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    progressBar1.setVisibility(View.GONE);

                    String ImageLinkBig = ImageLink;
                    if (ImageLink.contains("._")) {
                        int nEndPos = ImageLink.indexOf(".jpg", 0);
                        ImageLinkBig = ImageLink.substring(0, nEndPos + 4);
                        ImageLinkBig = ImageLinkBig.substring(0, ImageLink.lastIndexOf("._")) + ".jpg";
                    }

                    if (ImageLinkBig.contains("w185")) {
                        ImageLinkBig = ImageLink.replace("w185", "w780");
                    } else if (ImageLinkBig.contains("w342")) {
                        ImageLinkBig = ImageLink.replace("w342", "w780");
                    } else if (ImageLinkBig.contains("w500")) {
                        ImageLinkBig = ImageLink.replace("w500", "w780");
                    }
//					if (ImageLinkBig.contains("w780")){
//						ImageLinkBig = ImageLink.replace("w780","w500");
//					}


                    imageLoader.displayImage(ImageLinkBig, ivFullImage, options, new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            progressBar1.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            progressBar1.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            progressBar1.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {
                            progressBar1.setVisibility(View.GONE);
                        }
                    });
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }
            });

        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

}
