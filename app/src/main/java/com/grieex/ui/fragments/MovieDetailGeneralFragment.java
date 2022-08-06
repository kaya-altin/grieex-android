package com.grieex.ui.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.grieex.R;
import com.grieex.core.Imdb;
import com.grieex.core.Tmdb;
import com.grieex.core.listener.OnImdbEventListener;
import com.grieex.core.listener.OnTmdbEventListener;
import com.grieex.helper.Connectivity;
import com.grieex.helper.Constants;
import com.grieex.helper.Constants.ContentProviders;
import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.DbUtils;
import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.NLog;
import com.grieex.helper.Utils;
import com.grieex.model.tables.Movie;
import com.grieex.model.tables.Trailer;
import com.grieex.ui.FullScreenImageActivity;
import com.grieex.widget.ScaleImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class MovieDetailGeneralFragment extends Fragment {
    private final ImageLoader imageLoader;
    private final DisplayImageOptions options;
    private static final String TAG = MovieDetailGeneralFragment.class.getName();

    private static final String ARG_Movie = "Movie";

    private Movie mMovie;

    private ImageView ivPoster;
    private CheckBox chkSeen;
    private TextView tvOriginalName, tvOtherName, tvGenre, tvDirector, tvWriter, tvCountry, tvRunningTime, tvLanguage, tvSubtitle, tvDubbing, tvBudget, tvProductionCompany, tvEnglishPlot, tvOtherPlot, tvImdbUserRating, tvTmdbUserRating, tvReleaseDate;
    private LinearLayout llSubtitle, llDubbing;
    private LinearLayout llImdbRating, llTmdbRating, llEnglishPlot, llOtherPlot;
    private ScaleImageView ivYouTube;

    private Activity activity;
    private DatabaseHelper dbHelper;

    public static MovieDetailGeneralFragment newInstance(Movie movie) {
        MovieDetailGeneralFragment fragment = new MovieDetailGeneralFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_Movie, movie);
        fragment.setArguments(args);
        return fragment;
    }

    public MovieDetailGeneralFragment() {
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.transparent_back).cacheInMemory(true).cacheOnDisk(true).considerExifParams(true).build();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (getArguments() != null) {
                mMovie = (Movie) getArguments().getSerializable(ARG_Movie);
            }
            activity = getActivity();
            dbHelper = DatabaseHelper.getInstance(activity);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_movie_detail_general, container, false);
        try {
            chkSeen = v.findViewById(R.id.chkSeen);
            tvOriginalName = v.findViewById(R.id.tvOriginalName);
            tvOtherName = v.findViewById(R.id.tvOtherName);
            tvGenre = v.findViewById(R.id.tvGenre);
            tvDirector = v.findViewById(R.id.tvDirector);
            tvWriter = v.findViewById(R.id.tvWriter);
            tvCountry = v.findViewById(R.id.tvCountry);
            tvRunningTime = v.findViewById(R.id.tvRunningTime);
            tvLanguage = v.findViewById(R.id.tvLanguage);
            tvDubbing = v.findViewById(R.id.tvDubbing);
            tvSubtitle = v.findViewById(R.id.tvSubtitle);
            tvBudget = v.findViewById(R.id.tvBudget);
            tvProductionCompany = v.findViewById(R.id.tvProductionCompany);
            tvEnglishPlot = v.findViewById(R.id.tvEnglishPlot);
            tvOtherPlot = v.findViewById(R.id.tvOtherPlot);
            tvReleaseDate = v.findViewById(R.id.tvReleaseDate);
            tvImdbUserRating = v.findViewById(R.id.tvImdbUserRating);
            tvTmdbUserRating = v.findViewById(R.id.tvTmdbUserRating);
            llImdbRating = v.findViewById(R.id.llImdbRating);
            llTmdbRating = v.findViewById(R.id.llTmdbRating);
            llEnglishPlot = v.findViewById(R.id.llEnglishPlot);
            llOtherPlot = v.findViewById(R.id.llOtherPlot);
            ivYouTube = v.findViewById(R.id.ivYouTube);
            ivYouTube.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    ImageView view = (ImageView) v;
                    Drawable drawable = view.getDrawable();
                    if (drawable == null)
                        return false;

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            //overlay is black with transparency of 0x77 (119)
                            drawable.setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                            view.invalidate();
                            break;
                        }
                        case MotionEvent.ACTION_UP:
                            //clear the overlay
                            drawable.clearColorFilter();
                            view.invalidate();

                            if (mMovie.getTrailers().size() > 0)
                                watchYoutubeVideo(mMovie.getTrailers().get(0).getUrl());
                            break;
                        case MotionEvent.ACTION_CANCEL: {
                            //clear the overlay
                            drawable.clearColorFilter();
                            view.invalidate();
                            break;
                        }
                    }

                    return true;
                }
            });

            tvOriginalName.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager) activity
                                .getSystemService(Context.CLIPBOARD_SERVICE);
                        android.content.ClipData clipData = android.content.ClipData
                                .newPlainText("label", tvOriginalName.getText());
                        clipboardManager.setPrimaryClip(clipData);
                    } catch (Exception e) {

                    }
                }
            });

            llImdbRating.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // if (mMovie.getContentProvider() == ContentProviders.Imdb.value) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.imdb.com/title/" + mMovie.getImdbNumber()));
                    startActivity(browserIntent);
                    //} else if (mMovie.getContentProvider() == ContentProviders.TMDb.value) {
                    //     Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.themoviedb.org/search?query=" + mMovie.getImdbNumber()));
                    //     startActivity(browserIntent);
                    //}

                }
            });

            llTmdbRating.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

//                    if (mMovie.getContentProvider() == ContentProviders.Imdb.value) {
//                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.imdb.com/title/" + mMovie.getImdbNumber()));
//                        startActivity(browserIntent);
//                    } else if (mMovie.getContentProvider() == ContentProviders.TMDb.value) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.themoviedb.org/movie/" + mMovie.getTmdbNumber()));
                    startActivity(browserIntent);
                    //  }

                }
            });

            llSubtitle = v.findViewById(R.id.llSubtitle);
            llDubbing = v.findViewById(R.id.llDubbing);

            ivPoster = v.findViewById(R.id.ivPoster);
            ivPoster.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    ImageView view = (ImageView) v;
                    Drawable drawable = view.getDrawable();
                    if (drawable == null)
                        return false;

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            //overlay is black with transparency of 0x77 (119)
                            drawable.setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                            view.invalidate();
                            break;
                        }
                        case MotionEvent.ACTION_UP:
                            //clear the overlay
                            drawable.clearColorFilter();
                            view.invalidate();

                            Intent it = new Intent(activity, FullScreenImageActivity.class);
                            it.putExtra(Constants.ImageLink, mMovie.getPoster());
                            startActivity(it);
                            break;
                        case MotionEvent.ACTION_CANCEL: {
                            //clear the overlay
                            drawable.clearColorFilter();
                            view.invalidate();
                            break;
                        }
                    }

                    return true;
                }
            });

            chkSeen.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isChecked = chkSeen.isChecked();
                    mMovie.setSeen(Utils.parseBooleanInt(isChecked));

                    DbUtils.SetMovieSeen(activity, mMovie.getID(), isChecked);
                    if (isChecked) {
                        Toast.makeText(activity, R.string.marked_as_seen, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(activity, R.string.marked_as_not_seen, Toast.LENGTH_SHORT).show();
                    }
                }
            });


            Load();
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        // Inflate the layout for this fragment
        return v;
    }


    private void Load() {
        try {
            //DatabaseHelper db = DatabaseHelper.getInstance(activity);

            if (mMovie.getSeen().equals("1"))
                chkSeen.setChecked(true);

            if (mMovie.getID() <= 0)
                chkSeen.setVisibility(View.GONE);


//            if (mMovie.getContentProvider() == ContentProviders.Imdb.value) {
//                ivRating.setImageResource(R.drawable.imdb);
//            } else if (mMovie.getContentProvider() == ContentProviders.TMDb.value) {
//                ivRating.setImageResource(R.drawable.tmdb);
//            }

            if (!TextUtils.isEmpty(mMovie.getOriginalName())) {
                tvOriginalName.setText(mMovie.getOriginalName());
                activity.setTitle(mMovie.getOriginalName() + " " + mMovie.getYear());
            }

            if (!TextUtils.isEmpty(mMovie.getYear()))
                activity.setTitle(mMovie.getOriginalName() + " (" + mMovie.getYear() + ")");
            else
                activity.setTitle(mMovie.getOriginalName());

            if (!TextUtils.isEmpty(mMovie.getOtherName())) {
                tvOtherName.setVisibility(View.VISIBLE);
                tvOtherName.setText(mMovie.getOtherName());
            } else {
                tvOtherName.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(mMovie.getGenre())) {
                if (GrieeXSettings.getLocale(activity).equals("tr")) {
                    tvGenre.setText(GenreReplace(mMovie.getGenre()));
                } else {
                    tvGenre.setText(mMovie.getGenre());
                }

            }

            if (!TextUtils.isEmpty(mMovie.getDirector())) {
                tvDirector.setText(mMovie.getDirector());
            }

            if (!TextUtils.isEmpty(mMovie.getWriter())) {
                tvWriter.setText(mMovie.getWriter());
            }

            if (!TextUtils.isEmpty(mMovie.getCountry())) {
                tvCountry.setText(mMovie.getCountry());
            }

            if (!TextUtils.isEmpty(mMovie.getRunningTime())) {
                tvRunningTime.setText(mMovie.getRunningTime());
            }

            if (!TextUtils.isEmpty(mMovie.getLanguage())) {
                tvLanguage.setText(mMovie.getLanguage());
            }

            if (!TextUtils.isEmpty(mMovie.getSubtitle())) {
                llSubtitle.setVisibility(View.VISIBLE);
                tvSubtitle.setText(mMovie.getSubtitle());
            }

            if (!TextUtils.isEmpty(mMovie.getDubbing())) {
                llDubbing.setVisibility(View.VISIBLE);
                tvDubbing.setText(mMovie.getDubbing());
            }

            if (!TextUtils.isEmpty(mMovie.getBudget())) {
                tvBudget.setText(mMovie.getBudget());
            }

            if (!TextUtils.isEmpty(mMovie.getProductionCompany())) {
                tvProductionCompany.setText(mMovie.getProductionCompany());
            }

            if (!TextUtils.isEmpty(mMovie.getEnglishPlot())) {
                llEnglishPlot.setVisibility(View.VISIBLE);
                tvEnglishPlot.setText(mMovie.getEnglishPlot());
            } else {
                llEnglishPlot.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(mMovie.getOtherPlot())) {
                llOtherPlot.setVisibility(View.VISIBLE);
                tvOtherPlot.setText(mMovie.getOtherPlot());
            } else {
                llOtherPlot.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(mMovie.getReleaseDate())) {
                tvReleaseDate.setText(mMovie.getReleaseDate());
            }

            setImdRating(false);
            setTmdbRating(false);


            if (TextUtils.isEmpty(mMovie.getImdbUserRating()) & TextUtils.isEmpty(mMovie.getTmdbUserRating())) {
                if (!TextUtils.isEmpty(mMovie.getUserRating())) {
                    String strVotes = new DecimalFormat("###,###").format(Utils.parseInt(mMovie.getVotes()));

                    if (mMovie.getContentProvider() == ContentProviders.Imdb.value) {
                        tvImdbUserRating.setText(mMovie.getUserRating() + " / " + strVotes.replace(",", "."));
                        llImdbRating.setVisibility(View.VISIBLE);
                    } else if (mMovie.getContentProvider() == ContentProviders.TMDb.value) {
                        tvTmdbUserRating.setText(mMovie.getUserRating() + " / " + strVotes.replace(",", "."));
                        llTmdbRating.setVisibility(View.VISIBLE);
                    }
                }
            }


            // String ImageLink = "file:///" + GrieeXSettings.getImagePath() +
            // "Posters" + File.separator + mMovie.getID() + ".jpg";
            imageLoader.displayImage(mMovie.getPoster(), ivPoster, options);

            getImdbRating();
            getTmdbRating();

            if (mMovie.getTrailers().size() == 0)
                getTrailers();
            else
                ivYouTube.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    // public OnClickListener OnListener_Image = new OnClickListener() {
    // public void onClick(View v) {
    //
    // Intent it = new Intent(getActivity(), FullScreenImageActivity.class);
    // it.putExtra("MovieID", String.valueOf(mMovieID));
    // startActivity(it);
    // }
    // };

    private boolean ImdbRatingIsVisible = false;

    private void setImdRating(boolean withAnimation) {
        if (!TextUtils.isEmpty(mMovie.getImdbUserRating()) & !TextUtils.isEmpty(mMovie.getImdbVotes())) {
            if (mMovie.getImdbVotes() != null && mMovie.getImdbVotes().equals("0")) {
                tvImdbUserRating.setText(mMovie.getImdbUserRating());
            } else {
                String strImdbVotes = new DecimalFormat("###,###").format(Utils.parseInt(mMovie.getImdbVotes()));
                tvImdbUserRating.setText(mMovie.getImdbUserRating() + " / " + strImdbVotes.replace(",", "."));
            }

            if (withAnimation) {
                if (!ImdbRatingIsVisible) {
                    ImdbRatingIsVisible = true;
                    llImdbRating.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.fade_in));
                    llImdbRating.setVisibility(View.VISIBLE);
                }
            }else{
                ImdbRatingIsVisible = true;
                llImdbRating.setVisibility(View.VISIBLE);
            }
        } else {
            llImdbRating.setVisibility(View.GONE);
        }
    }

    private boolean TmdbRatingIsVisible = false;

    private void setTmdbRating(boolean withAnimation) {
        if (!TextUtils.isEmpty(mMovie.getTmdbUserRating()) & !TextUtils.isEmpty(mMovie.getTmdbVotes())) {
            if (mMovie.getTmdbVotes().equals("0")) {
                tvTmdbUserRating.setText(mMovie.getTmdbUserRating());
            } else {
                String strTmdbVotes = new DecimalFormat("###,###").format(Utils.parseInt(mMovie.getTmdbVotes()));
                tvTmdbUserRating.setText(mMovie.getTmdbUserRating() + " / " + strTmdbVotes.replace(",", "."));
            }

            if (withAnimation) {
                if (!TmdbRatingIsVisible) {
                    TmdbRatingIsVisible = true;
                    llTmdbRating.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.fade_in));
                    llTmdbRating.setVisibility(View.VISIBLE);
                }
            }else{
                TmdbRatingIsVisible = true;
                llTmdbRating.setVisibility(View.VISIBLE);
            }
        } else {
            llTmdbRating.setVisibility(View.GONE);
        }
    }

    private void getImdbRating() {
        if (!Connectivity.isConnected(activity) || TextUtils.isEmpty(mMovie.getImdbNumber()))
            return;

        Imdb imdb = new Imdb();
        imdb.setImdbEventListener(new OnImdbEventListener() {
            @Override
            public void onNotCompleted(Throwable error, String content) {

            }

            @Override
            public void onCompleted(Object m) {
                try {
                    Movie p = (Movie) m;
                    mMovie.setImdbUserRating(p.getUserRating());
                    mMovie.setImdbVotes(p.getVotes());

                    if (mMovie.getID() > 0)
                        dbHelper.updateMovie(mMovie);

                    setImdRating(true);
                } catch (Exception e) {
                    //NLog.e(TAG, e);
                }
            }
        });
        imdb.ParseRatingAsync(mMovie.getImdbNumber());
    }

    private void getTmdbRating() {
        if (!Connectivity.isConnected(activity) || (TextUtils.isEmpty(mMovie.getTmdbNumber()) && TextUtils.isEmpty(mMovie.getImdbNumber())))
            return;


        Tmdb tmdb = new Tmdb();
        tmdb.setTmdbEventListener(new OnTmdbEventListener() {
            @Override
            public void onNotCompleted(Throwable error, String content) {

            }

            @Override
            public void onCompleted(Object m) {
                try {
                    Movie p = (Movie) m;
                    mMovie.setTmdbNumber(p.getTmdbNumber());
                    mMovie.setTmdbUserRating(p.getUserRating());
                    mMovie.setTmdbVotes(p.getVotes());

                    if (mMovie.getID() > 0)
                        dbHelper.updateMovie(mMovie);

                    setTmdbRating(true);
                } catch (Exception e) {
                    // NLog.e(TAG, e);
                }

            }
        });

        if (!TextUtils.isEmpty(mMovie.getTmdbNumber()))
            tmdb.ParseRatingWithTmdbNumberAsync(mMovie.getTmdbNumber());
        else
            tmdb.ParseRatingWithImdbNumberAsync(mMovie.getImdbNumber());
    }


    private void getTrailers() {
        if (!Connectivity.isConnected(activity))
            return;

        Tmdb tmdb = new Tmdb();
        tmdb.setTmdbEventListener(new OnTmdbEventListener() {
            @Override
            public void onCompleted(Object m) {
                try {
                    ArrayList<Trailer> trailers = (ArrayList<Trailer>) m;
                    if (trailers != null && trailers.size() > 0) {
                        mMovie.setTrailers(trailers);

                        DatabaseHelper db = DatabaseHelper.getInstance(activity);
                        db.fillTrailers(trailers, mMovie.getID(), Constants.CollectionType.Movie);
                        ivYouTube.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    NLog.e(TAG, e);
                }
            }

            @Override
            public void onNotCompleted(Throwable error, String content) {

            }
        });
        tmdb.getTrailers(mMovie.getImdbNumber());

    }

    private void watchYoutubeVideo(String id) {
        try {
//            Intent lightboxIntent = new Intent(getActivity(), CustomLightboxActivity.class);
//            lightboxIntent.putExtra(CustomLightboxActivity.KEY_VIDEO_ID, id);
//            startActivity(lightboxIntent);

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
            activity.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v=" + id));
            activity.startActivity(intent);
        }
    }

    private String GenreReplace(String genre) {
        genre = genre.replace("Family", "Ailesel");
        genre = genre.replace("Action", "Aksiyon");
        genre = genre.replace("Animation", "Animasyon");
        genre = genre.replace("Sci-Fi", "Bilimkurgu");
        genre = genre.replace("Biography", "Biyografik");
        genre = genre.replace("Documentary", "Dökümantasyon");
        genre = genre.replace("Drama", "Drama");
        genre = genre.replace("Fantasy", "Fantastik");
        genre = genre.replace("Thriller", "Gerilim");
        genre = genre.replace("Mystery", "Gizem");
        genre = genre.replace("Short", "Kısa Film");
        genre = genre.replace("Comedy", "Komedi");
        genre = genre.replace("Concert", "Konser");
        genre = genre.replace("Horror", "Korku");
        genre = genre.replace("Adventure", "Macera");
        genre = genre.replace("Music", "Müzik");
        genre = genre.replace("Musical", "Müzikal");
        genre = genre.replace("Crime", "Polisiye");
        genre = genre.replace("Romance", "Romantik");
        genre = genre.replace("War", "Savaş");
        genre = genre.replace("Magic", "Sihir");
        genre = genre.replace("Sport", "Spor");
        genre = genre.replace("History", "Tarihi");
        genre = genre.replace("Western", "Vahşi Batı");
        genre = genre.replace("Science Fiction", "Bilim-Kurgu");
        genre = genre.replace("Foreign", "Yabancı");

        return genre;
    }


}
