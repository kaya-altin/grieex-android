package com.grieex.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.grieex.GrieeX;
import com.grieex.R;
import com.grieex.adapter.CustomMenuAdapter;
import com.grieex.adapter.TraktCommentsAdapter;
import com.grieex.core.TraktTv;
import com.grieex.core.listener.OnTraktTvEventListener;
import com.grieex.enums.TraktResult;
import com.grieex.helper.Connectivity;
import com.grieex.helper.Constants;
import com.grieex.helper.EndlessRecyclerOnScrollListener;
import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.NLog;
import com.grieex.helper.SampleRecycler;
import com.grieex.helper.TraktSettings;
import com.grieex.model.CustomMenuItem;
import com.grieex.tasks.RefreshAccessTokenTask;
import com.grieex.ui.dialogs.CustomProgressDialog;
import com.grieex.ui.dialogs.TraktCommentDialog;
import com.uwetrottmann.trakt5.entities.Comment;

import java.util.ArrayList;

public class TraktCommentsActivity extends BaseActivity {
    private static final String TAG = TraktCommentsActivity.class.getName();

    public enum ActivityTypes {List, ReplyList}

    private ProgressBar progressBar;

    private RecyclerView mRecyclerView;
    private TraktCommentsAdapter mAdapter;

    private Integer id;
    private ActivityTypes type;

    private TraktTv traktTv;

    private CustomProgressDialog progressDialog;

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
        setContentView(R.layout.activity_trakt_comments);
        try {
            Toolbar toolbar = findViewById(R.id.myToolbar);
            setSupportActionBar(toolbar);

            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null)
                actionBar.setDisplayHomeAsUpEnabled(true);


            id = this.getIntent().getIntExtra(Constants.ID, 0);
            type = (ActivityTypes) getIntent().getSerializableExtra(Constants.ActivityType);


            mRecyclerView = findViewById(R.id.list);
            mRecyclerView.setHasFixedSize(true);
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(new SampleRecycler());

            if (type == ActivityTypes.List) {
                mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(mLayoutManager) {
                    @Override
                    public void onLoadMore(int page) {
                        if (!Connectivity.isConnected(TraktCommentsActivity.this)) {
                            Toast.makeText(TraktCommentsActivity.this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (mAdapter != null && mAdapter.getItemCount() == 0)
                            return;

                        getComments(page);
                    }
                });
            }

            progressBar = findViewById(R.id.progress);

            getComments(1);

        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    private void getComments(final int page) {
        try {
            if (!Connectivity.isConnected(TraktCommentsActivity.this)) {
                Toast.makeText(TraktCommentsActivity.this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            if (traktTv == null) {
                traktTv = new TraktTv();
                traktTv.setTraktEventListener(new OnTraktTvEventListener() {
                    @Override
                    public void onCompleted(Object m) {
                        if (m != null) {
                            ArrayList<Comment> comments = (ArrayList<Comment>) m;
                            if (mAdapter == null) {
                                mAdapter = new TraktCommentsAdapter(TraktCommentsActivity.this, comments);
                                mAdapter.setOnItemClickListener(itemClicked);
                                mAdapter.setOnItemLikeClickListener(itemLikeClicked);
                                mAdapter.setOnItemLongClickListener(itemLongClicked);
                                mRecyclerView.setAdapter(mAdapter);

                                if (type == ActivityTypes.ReplyList) {
                                    mAdapter.setFirstItemDisable(true);
                                }
                            } else {
                                mAdapter.addAllEnd(comments);
                            }
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }

            if (type == ActivityTypes.List)
                traktTv.getComments(id, page, 20);
            else
                traktTv.getCommentReplies(id);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }


    private final TraktCommentsAdapter.OnItemLikeClickListener itemLikeClicked = new TraktCommentsAdapter.OnItemLikeClickListener() {
        @Override
        public void onItemLikeClick(View itemView, final int position) {
            try {
                final Comment comment = mAdapter.getItem(position);
                if (comment != null) {
                    TraktTv traktTv = new TraktTv();
                    traktTv.setTraktEventListener(new OnTraktTvEventListener() {
                        @Override
                        public void onCompleted(Object m) {
                            if (m != null) {
                                int result = (int) m;
                                switch (result) {
                                    case TraktResult.SUCCESS:
                                        //minmih
//                                    if (!comment.mylike)
//                                        comment.likes = comment.likes + 1;
//                                    else
//                                        comment.likes = comment.likes - 1;
//
//                                    comment.mylike = !comment.mylike;
                                        mAdapter.notifyItemChanged(position);
                                        break;
                                    case TraktResult.AUTH_ERROR:
                                        TraktSettings.logOut(TraktCommentsActivity.this);
                                        Intent login = new Intent(TraktCommentsActivity.this, TraktAuthActivity.class);
                                        startActivity(login);
                                        break;
                                    case TraktResult.ERROR:
                                        Toast.makeText(TraktCommentsActivity.this, R.string.crash_toast_text, Toast.LENGTH_LONG).show();
                                        break;
                                }
                            }
                        }
                    });
                    traktTv.likeComment(TraktSettings.getTraktAccessToken(getBaseContext()), comment);
                }
            } catch (Exception e) {
                NLog.e(TAG, e);
            }
        }
    };

    private final TraktCommentsAdapter.OnItemClickListener itemClicked = new TraktCommentsAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View itemView, int position) {
            try {
                Comment c = mAdapter.getItem(position);
                if (!c.id.equals(id)) {
                    Intent it = new Intent(TraktCommentsActivity.this, TraktCommentsActivity.class);
                    it.putExtra(Constants.ID, c.id);
                    it.putExtra(Constants.ActivityType, ActivityTypes.ReplyList);
                    startActivity(it);
                }
            } catch (Exception e) {
                NLog.e(TAG, e);
            }
        }
    };

    private final TraktCommentsAdapter.OnItemLongClickListener itemLongClicked = new TraktCommentsAdapter.OnItemLongClickListener() {
        @Override
        public void onItemLongClick(View itemView, int position) {
            try {
                final Comment comment = mAdapter.getItem(position);

                if (comment.user.username.equals(TraktSettings.getTraktUserName(TraktCommentsActivity.this))) {
                    final ArrayList<CustomMenuItem> items = new ArrayList<>();
                    items.add(new CustomMenuItem(1, -1, getString(R.string.delete)));

                    CustomMenuAdapter a = new CustomMenuAdapter(TraktCommentsActivity.this, items);

                    AlertDialog.Builder builder = new AlertDialog.Builder(TraktCommentsActivity.this);
                    builder.setCancelable(true);
                    builder.setAdapter(a, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            try {
                                CustomMenuItem menuItem = items.get(item);

                                if (menuItem.getId() == 1) {
                                    TraktTv traktTv = new TraktTv();
                                    traktTv.setTraktEventListener(new OnTraktTvEventListener() {
                                        @Override
                                        public void onCompleted(Object m) {
                                            int result = (int) m;
                                            if (result == TraktResult.SUCCESS) {
                                                mAdapter.remove(comment);
                                            }
                                        }
                                    });
                                    traktTv.deleteComment(TraktSettings.getTraktAccessToken(getBaseContext()), comment.id);
                                }
                            } catch (Exception e) {
                                NLog.e(TAG, e);
                            }
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.setCanceledOnTouchOutside(true);
                    alert.show();
                }
            } catch (Exception e) {
                NLog.e(TAG, e);
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.series_comments_actionbar_menu, menu);

            final MenuItem menuAddItem = menu.findItem(R.id.action_add);
            Drawable newIcon = menuAddItem.getIcon();
            newIcon.mutate().setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN);
            menuAddItem.setIcon(newIcon);
            menuAddItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    menuAddItemClick();
                    return false;
                }
            });

        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void menuAddItemClick() {
        if (!TextUtils.isEmpty(TraktSettings.getTraktAccessToken(TraktCommentsActivity.this))) {
            if (TraktSettings.isTimeToRefreshAccessToken(TraktCommentsActivity.this)) {
                showProgress();
                RefreshAccessTokenTask task = new RefreshAccessTokenTask(TraktCommentsActivity.this);
                task.setCustomEventListener(new RefreshAccessTokenTask.OnCustomEventListener() {
                    @Override
                    public void onCompleted(Integer result) {
                        switch (result) {
                            case TraktResult.SUCCESS:
                                hideProgress();
                                menuAddItemClick();
                                break;
                            case TraktResult.AUTH_ERROR:
                                TraktSettings.logOut(TraktCommentsActivity.this);
                                Intent login = new Intent(TraktCommentsActivity.this, TraktAuthActivity.class);
                                startActivity(login);
                                break;
                            case TraktResult.ERROR:
                                Toast.makeText(TraktCommentsActivity.this, R.string.crash_toast_text, Toast.LENGTH_LONG).show();
                                break;
                        }
                    }
                });
                task.execute();
                return;
            }

            TraktCommentDialog dialog = new TraktCommentDialog(TraktCommentsActivity.this);
            dialog.setCustomEventListener(new TraktCommentDialog.OnCustomEventListener() {
                @Override
                public void onOkClicked(String comment, boolean isSpoiler) {
                    TraktTv traktTv = new TraktTv();
                    traktTv.setTraktEventListener(new OnTraktTvEventListener() {
                        @Override
                        public void onCompleted(Object m) {
                            if (m != null) {
                                if (m instanceof Comment) {
                                    if (type == ActivityTypes.List) {
                                        mAdapter.add(0, (Comment) m);
                                        mRecyclerView.scrollToPosition(0);
                                    }else
                                        mAdapter.addEnd((Comment) m);

                                    Toast.makeText(TraktCommentsActivity.this, R.string.comment_published, Toast.LENGTH_LONG).show();
                                } else {
                                    int result = (int) m;

                                    if (result == TraktResult.AUTH_ERROR) {
                                        TraktSettings.logOut(TraktCommentsActivity.this);
                                        finish();
                                        Intent login = new Intent(TraktCommentsActivity.this, TraktAuthActivity.class);
                                        startActivity(login);
                                    } else if (result == TraktResult.ERROR) {
                                        Toast.makeText(TraktCommentsActivity.this, R.string.crash_toast_text, Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        }
                    });

                    if (type == ActivityTypes.List)
                        traktTv.addComment(TraktSettings.getTraktAccessToken(getBaseContext()), id, comment, isSpoiler);
                    else
                        traktTv.replyComment(TraktSettings.getTraktAccessToken(getBaseContext()), id, comment, isSpoiler);
                }

                @Override
                public void onDialogClosed() {

                }
            });
            dialog.showDialog();
        } else {
            Intent login = new Intent(TraktCommentsActivity.this, TraktAuthActivity.class);
            startActivity(login);
        }
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

    private void showProgress() {
        progressDialog = new CustomProgressDialog(TraktCommentsActivity.this);
        progressDialog.setText(getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        progressDialog.showDialog();
    }

    private void hideProgress() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }

}
