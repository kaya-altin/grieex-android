package com.grieex.ui;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.grieex.R;
import com.grieex.helper.Constants;
import com.grieex.helper.NLog;


public class FullScreenImageActivity extends Activity {
    private static final String TAG = FullScreenImageActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.actvity_full_screen_image);
        try {
            final String imageLink = this.getIntent().getStringExtra(Constants.ImageLink);

            final ProgressBar progressBar1 = findViewById(R.id.progressBar1);

            if (TextUtils.isEmpty(imageLink)) {
                progressBar1.setVisibility(View.GONE);
                return;
            }

            final ImageView ivFullImage = findViewById(R.id.ivFullImage);

            progressBar1.setVisibility(View.VISIBLE);
//            Glide.with(this)
//                    .load(imageLink)
//                    .listener(new RequestListener<Drawable>() {
//                        @Override
//                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                            progressBar1.setVisibility(View.GONE);
//                            return false;
//                        }
//
//                        @Override
//                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
////                            progressBar1.setVisibility(View.GONE);
//                            return false;
//                        }
//                    })
//                    .into(ivFullImage);


            String imageLinkBig = imageLink;
            if (imageLink.contains("._")) {
                int nEndPos = imageLink.indexOf(".jpg");
                imageLinkBig = imageLink.substring(0, nEndPos + 4);
                imageLinkBig = imageLinkBig.substring(0, imageLink.lastIndexOf("._")) + ".jpg";
            }

            if (imageLinkBig.contains("w185")) {
                imageLinkBig = imageLink.replace("w185", "w780");
            } else if (imageLinkBig.contains("w342")) {
                imageLinkBig = imageLink.replace("w342", "w780");
            } else if (imageLinkBig.contains("w500")) {
                imageLinkBig = imageLink.replace("w500", "w780");
            }

            Glide.with(this)
                    .load(imageLinkBig)
                    .thumbnail(
                            Glide.with(this)
                                    .load(imageLink))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            progressBar1.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            progressBar1.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(ivFullImage);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

}
