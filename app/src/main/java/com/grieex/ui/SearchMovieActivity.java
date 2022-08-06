package com.grieex.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.grieex.GrieeX;
import com.grieex.R;
import com.grieex.adapter.SearchMovieAdapter;
import com.grieex.core.Beyazperde;
import com.grieex.core.ImportQueues;
import com.grieex.core.SearchResult;
import com.grieex.core.Sinemalar;
import com.grieex.core.Tmdb;
import com.grieex.core.listener.OnBeyazperdeEventListener;
import com.grieex.core.listener.OnSinemalarEventListener;
import com.grieex.core.listener.OnTmdbEventListener;
import com.grieex.helper.BroadcastNotifier;
import com.grieex.helper.Connectivity;
import com.grieex.helper.Constants;
import com.grieex.helper.Constants.ContentProviders;
import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.DateUtils;
import com.grieex.helper.DbUtils;
import com.grieex.helper.EndlessRecyclerOnScrollListener;
import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.NLog;
import com.grieex.helper.SampleRecycler;
import com.grieex.helper.Utils;
import com.grieex.model.tables.Movie;
import com.grieex.service.ServiceManager;

import java.util.ArrayList;

public class SearchMovieActivity extends BaseActivity {
    private static final String TAG = SearchMovieActivity.class.getName();
    private boolean isProInstalled = false;
    private Movie mMovie;
    private ContentProviders resultType = ContentProviders.Imdb;
    private BroadcastNotifier mBroadcaster;

    private ProgressBar progressBarSearch;
    private EditText etSearch;

    private int iPageID = -1;

    private RecyclerView mRecyclerView;
    private SearchMovieAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private boolean AddedMessageDisplayed = false;

    private String locale = "en";
    private Tmdb tmdb;


    @Override
    protected void onStart() {
        super.onStart();
        if (GrieeXSettings.RELEASE_MODE) {
            GrieeX.getInstance().trackScreenView(this.getClass().getName());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_movie);
        try {
            Toolbar toolbar = findViewById(R.id.myToolbar);
            setSupportActionBar(toolbar);

            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null)
                actionBar.setDisplayHomeAsUpEnabled(true);

            isProInstalled = Utils.isProInstalled(this);
            locale = GrieeXSettings.getLocale(this);

            tmdb = new Tmdb();
            tmdb.setTmdbEventListener(new OnTmdbEventListener() {
                @Override
                public void onCompleted(Object m) {
                    ArrayList<SearchResult> results = (ArrayList<SearchResult>) m;
                    if (results != null) {
                        if (mAdapter == null) {
                            mAdapter = new SearchMovieAdapter(results);
                            mAdapter.setResultType(resultType);
                            mAdapter.setOnAddClickListener(addClicked);
                            mAdapter.setOnItemClickListener(itemClicked);
                            mRecyclerView.setAdapter(mAdapter);
                        } else {
                            mAdapter.addAllEnd(results);
                        }
                    }
                    progressBarSearch.setVisibility(View.GONE);
                }

                @Override
                public void onNotCompleted(Throwable error, String content) {
                    progressBarSearch.setVisibility(View.GONE);
                }

            });

            mBroadcaster = new BroadcastNotifier(this);

            mRecyclerView = findViewById(R.id.list);
            mRecyclerView.setHasFixedSize(true);
            mLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.recylerviewColumnCountSearch));
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(new SampleRecycler());
            mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener((GridLayoutManager) mLayoutManager) {
                @Override
                public void onLoadMore(int page) {
                    if (!Connectivity.isConnected(SearchMovieActivity.this)) {
                        Toast.makeText(SearchMovieActivity.this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (mAdapter != null && mAdapter.getItemCount() == 0)
                        return;

                    getPopular(page + 1);
                }
            });

            progressBarSearch = findViewById(R.id.progressBarSearch);
            etSearch = findViewById(R.id.etSearch);
            etSearch.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        Search();
                        return true;
                    }
                    return false;
                }
            });

            ImageButton btnSearch = findViewById(R.id.btnSearch);
            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Search();
                }
            });

            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                resultType = (ContentProviders) bundle.getSerializable(Constants.EXTENDED_DATA_OBJECT);
                mMovie = (Movie) bundle.getSerializable(Constants.EXTENDED_DATA_OBJECT2);
                iPageID = bundle.getInt(Constants.EXTENDED_DATA_OBJECT3, -1);

                if (mMovie != null) {
                    etSearch.setText(mMovie.getOriginalName());
                    Search();
                } else {
                    getPopular(0);
                }
            } else {
                getPopular(0);
            }

        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    private void getPopular(int page) {
        try {
            progressBarSearch.setVisibility(View.VISIBLE);
            tmdb.getPopularListSearchResult(locale, page);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    private void showMovie(Movie m) {
        try {
            Intent it = new Intent(this, MovieDetailActivity.class);
            it.putExtra(Constants.Movie, m);
            startActivity(it);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    private void Search() {
        try {
            if (!Connectivity.isConnected(getApplicationContext())) {
                Toast.makeText(SearchMovieActivity.this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(etSearch.getText()))
                return;

            if (mAdapter != null) {
                mAdapter.clear();
                mAdapter.notifyDataSetChanged();
            }
            mRecyclerView.clearOnScrollListeners();

            Utils.hideKeyboard(this);

            progressBarSearch.setVisibility(View.VISIBLE);

            switch (resultType) {
                case TMDb: {
                    Tmdb bp = new Tmdb();
                    bp.setTmdbEventListener(new OnTmdbEventListener() {
                        @Override
                        public void onCompleted(Object m) {
                            ArrayList<SearchResult> results = (ArrayList<SearchResult>) m;
                            setAdaper(results);
                        }

                        @Override
                        public void onNotCompleted(Throwable error, String content) {
                            if (error != null)
                                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                            progressBarSearch.setVisibility(View.GONE);
                        }

                    });
                    bp.Search(etSearch.getText().toString());

                    break;
                }
                case Beyazperde: {
                    Beyazperde bp = new Beyazperde();
                    bp.setBeyazperdeEventListener(new OnBeyazperdeEventListener() {
                        @Override
                        public void onCompleted(Object m) {
                            ArrayList<SearchResult> results = (ArrayList<SearchResult>) m;
                            setAdaper(results);
                        }

                        @Override
                        public void onNotCompleted(Throwable error, String content) {
                            if (error != null)
                                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                            progressBarSearch.setVisibility(View.GONE);
                        }

                    });
                    bp.Search(etSearch.getText().toString());
                    break;
                }
                case Sinemalar: {
                    Sinemalar bp = new Sinemalar();
                    bp.setCustomEventListener(new OnSinemalarEventListener() {
                        @Override
                        public void onCompleted(Object m) {
                            ArrayList<SearchResult> results = (ArrayList<SearchResult>) m;
                            setAdaper(results);
                        }

                        @Override
                        public void onNotCompleted(Throwable error, String content) {
                            if (error != null)
                                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                            progressBarSearch.setVisibility(View.GONE);
                        }

                    });
                    bp.Search(etSearch.getText().toString());
                    break;
                }
                case TMDbTv:
//                TvDb bp = new TvDb();
//                bp.setCustomEventListener(new TvDb.OnTvDbEventListener() {
//                    @Override
//                    public void onCompleted(Object m) {
//                        results = (ArrayList<SearchResult>) m;
//                        if (results != null) {
//                            adapter = new SearchAdapter(SearchMovieActivity.this, (ArrayList<SearchResult>) m);
//                            adapter.setResultType(resultType);
//                            adapter.setCustomEventListener(addClicked);
//                            lv.setAdapter(adapter);
//                        }
//                        progressBarSearch.setVisibility(View.GONE);
//                    }
//
//                    @Override
//                    public void onNotCompleted(Throwable error, String content) {
//                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
//                        progressBarSearch.setVisibility(View.GONE);
//                    }
//
//                });
//                bp.Search(etSearch.getText().toString());

//                TmdbTv bp = new TmdbTv();
//                bp.setCustomEventListener(new TmdbTv.OnTmdbTvEventListener() {
//                    @Override
//                    public void onCompleted(Object m) {
//                        results = (ArrayList<SearchResult>) m;
//                        if (results != null) {
//                            adapter = new SearchAdapter(SearchMovieActivity.this, (ArrayList<SearchResult>) m);
//                            adapter.setResultType(resultType);
//                            adapter.setCustomEventListener(addClicked);
//                            lv.setAdapter(adapter);
//                        }
//                        progressBarSearch.setVisibility(View.GONE);
//                    }
//
//                    @Override
//                    public void onNotCompleted(Throwable error, String content) {
//                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
//                        progressBarSearch.setVisibility(View.GONE);
//                    }
//
//                });
//                bp.Search(etSearch.getText().toString());

                    break;
            }
        } catch (Exception e) {
            progressBarSearch.setVisibility(View.GONE);
            NLog.e(TAG, e);
        }
    }

    private void setAdaper(ArrayList<SearchResult> results) {
        if (results != null) {
            mAdapter = new SearchMovieAdapter(results);
            mAdapter.setResultType(resultType);
            mAdapter.setOnAddClickListener(addClicked);
            mAdapter.setOnItemClickListener(itemClicked);
            mRecyclerView.setAdapter(mAdapter);
        }
        progressBarSearch.setVisibility(View.GONE);
    }

    private final SearchMovieAdapter.OnItemClickListener itemClicked = new SearchMovieAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View itemView, int position) {
            try {
                if (!Connectivity.isConnected(SearchMovieActivity.this)) {
                    Toast.makeText(SearchMovieActivity.this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                    return;
                }

                SearchResult sr = mAdapter.getItem(position);

                if (resultType == ContentProviders.Imdb | resultType == ContentProviders.TMDb) {
                    Movie m = new Movie();
                    //m.setID(Utils.parseInt(sr.getKey()));
                    m.setTmdbNumber(sr.getKey());
                    showMovie(m);
                } else if (resultType == ContentProviders.Beyazperde) {
                    ImportQueues.AddQueue(SearchMovieActivity.this, mMovie.getID(), sr, ContentProviders.Beyazperde);
                    ServiceManager.startImportDataService(getApplicationContext());
                    finish();
                } else if (resultType == ContentProviders.Sinemalar) {
                    ImportQueues.AddQueue(SearchMovieActivity.this, mMovie.getID(), sr, ContentProviders.Sinemalar);
                    ServiceManager.startImportDataService(getApplicationContext());
                    finish();
                }
            } catch (Exception e) {
                NLog.e(TAG, e);
            }
        }
    };


    private final SearchMovieAdapter.OnAddClickListener addClicked = new SearchMovieAdapter.OnAddClickListener() {
        @Override
        public void onAddClick(final View itemView, final int position) {
            try {
                if (!Connectivity.isConnected(SearchMovieActivity.this)) {
                    Toast.makeText(SearchMovieActivity.this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isProInstalled) {
                    if (DbUtils.getMoviesCount(SearchMovieActivity.this) >= GrieeXSettings.FreeRecordLimitMovie) {
                        Toast.makeText(SearchMovieActivity.this, getResources().getString(R.string.alert7), Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                if (DbUtils.isMovieExistWithTmdbNumber(SearchMovieActivity.this, mAdapter.getItem(position).getKey())) {
                    new AlertDialog.Builder(SearchMovieActivity.this)
                            .setTitle(R.string.app_name)
                            .setMessage(getResources().getString(R.string.exist_movie_message))
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    AddMovie(position, itemView);
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();

                    return;
                }

                AddMovie(position, itemView);

            } catch (Exception e) {
                NLog.e(TAG, e);
            }
        }

    };

    private void AddMovie(int position, View itemView) {
        SearchResult sr = mAdapter.getItem(position);
        sr.setIsExisting(true);
        if (resultType == ContentProviders.Imdb | resultType == ContentProviders.TMDb) {
            Movie movie = new Movie();
            movie.setContentProvider(resultType.value);
            movie.setOriginalName(sr.getTitle());
            movie.setSeen("0");
            movie.setInsertDate(DateUtils.DateTimeNowString());
            movie.setUpdateDate(DateUtils.DateTimeNowString());
            movie.setArchivesNumber(DbUtils.getArchiveNumber(SearchMovieActivity.this));
            DatabaseHelper db = DatabaseHelper.getInstance(getApplicationContext());
            long _id = db.addMovie(movie);
            movie.setID((int) _id);

            if (iPageID != -1) {
                com.grieex.model.tables.ListsMovie listMovies = new com.grieex.model.tables.ListsMovie();
                listMovies.setListID(String.valueOf(iPageID));
                listMovies.setMovieID(String.valueOf(_id));
                db.addListsMovies(listMovies);
            }

            if (resultType == ContentProviders.Imdb) {
                ImportQueues.AddQueue(SearchMovieActivity.this, _id, sr, ContentProviders.Imdb);
            } else if (resultType == ContentProviders.TMDb) {
                ImportQueues.AddQueue(SearchMovieActivity.this, _id, sr, ContentProviders.TMDb);
            }
            mBroadcaster.broadcastIntentWithObject(Constants.STATE_INSERT_MOVIE, movie);

            if (!AddedMessageDisplayed) {
                AddedMessageDisplayed = true;
                Toast.makeText(SearchMovieActivity.this, getString(R.string.movie_added), Toast.LENGTH_SHORT).show();
            }
        }

        ServiceManager.startImportDataService(getApplicationContext());
        itemView.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {

                case (Constants.REFRESH): {
                    if (resultCode == Activity.RESULT_OK) {
                        if (data != null) {
                            String MovieName = data.getStringExtra(Constants.EXTENDED_DATA_OBJECT);

                            if (!TextUtils.isEmpty(MovieName)) {
                                etSearch.setText(MovieName);
                                Search();
                            }
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }
}
