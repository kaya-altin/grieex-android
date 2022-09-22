package com.grieex.ui.fragments;

import android.content.Context;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.grieex.R;
import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.NLog;
import com.grieex.helper.Utils;
import com.grieex.model.tables.Movie;
import com.grieex.ui.dialogs.AutoTextDialog;
import com.grieex.ui.dialogs.RatingDialog;
import com.grieex.ui.dialogs.TextDialog;
import com.grieex.ui.dialogs.TextDialog.OnCustomEventListener;


public class MovieDetailOtherFragment extends Fragment {
    private static final String TAG = MovieDetailOtherFragment.class.getName();
    private static final String ARG_Movie = "Movie";
    private TextView tvArchivesNumber, tvUserColumn1, tvUserColumn2, tvUserColumn3, tvUserColumn4, tvUserColumn5, tvUserColumn6, tvRlsType, tvRlsGroup, tvPersonalRating;


    // TODO: Rename and change types of parameters
    private Movie mMovie;
    private Context mContext;


    public MovieDetailOtherFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static MovieDetailOtherFragment newInstance(Movie Movie) {
        MovieDetailOtherFragment fragment = new MovieDetailOtherFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_Movie, Movie);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMovie = (Movie) getArguments().getSerializable(ARG_Movie);
        }

        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_movie_detail_other, container, false);
        try {
            tvArchivesNumber = v.findViewById(R.id.tvArchivesNumber);
            tvUserColumn1 = v.findViewById(R.id.tvUserColumn1);
            tvUserColumn2 = v.findViewById(R.id.tvUserColumn2);
            tvUserColumn3 = v.findViewById(R.id.tvUserColumn3);
            tvUserColumn4 = v.findViewById(R.id.tvUserColumn4);
            tvUserColumn5 = v.findViewById(R.id.tvUserColumn5);
            tvUserColumn6 = v.findViewById(R.id.tvUserColumn6);
            tvRlsType = v.findViewById(R.id.tvRlsType);
            tvRlsGroup = v.findViewById(R.id.tvRlsGroup);
            tvPersonalRating = v.findViewById(R.id.tvPersonalRating);


            tvArchivesNumber.setText(mMovie.getArchivesNumber());
            tvUserColumn1.setText(mMovie.getUserColumn1());
            tvUserColumn2.setText(mMovie.getUserColumn2());
            tvUserColumn3.setText(mMovie.getUserColumn3());
            tvUserColumn4.setText(mMovie.getUserColumn4());
            tvUserColumn5.setText(mMovie.getUserColumn5());
            tvUserColumn6.setText(mMovie.getUserColumn6());
            tvRlsType.setText(mMovie.getRlsType());
            tvRlsGroup.setText(mMovie.getRlsGroup());
            tvPersonalRating.setText(mMovie.getPersonalRating());

            tvArchivesNumber.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextDialog d = new TextDialog(mContext);
                    d.setInputType(InputType.TYPE_CLASS_NUMBER);
                    d.setTitle(getString(R.string.archives_number));
                    d.setText(mMovie.getArchivesNumber());
                    d.setCustomEventListener(new OnCustomEventListener() {
                        @Override
                        public void onOkClicked(String str) {
                            try {
                                tvArchivesNumber.setText(str);
                                mMovie.setArchivesNumber(str);
                                DatabaseHelper dbHelper = DatabaseHelper.getInstance(mContext);
                                dbHelper.ExecuteQuery("Update Movies Set ArchivesNumber=" + DatabaseUtils.sqlEscapeString(str) + " Where _id=" + mMovie.getID());
                            } catch (Exception e) {
                                NLog.e(TAG, e);
                            }
                        }

                        @Override
                        public void onDialogClosed() {
                            // TODO Auto-generated method stub

                        }
                    });
                    d.showDialog();
                }
            });

            tvUserColumn1.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextDialog d = new TextDialog(mContext);
                    d.setTitle(getString(R.string.user_column1));
                    d.setText(mMovie.getUserColumn1());
                    d.setCustomEventListener(new OnCustomEventListener() {
                        @Override
                        public void onOkClicked(String str) {
                            try {
                                tvUserColumn1.setText(str);
                                mMovie.setUserColumn1(str);
                                DatabaseHelper dbHelper = DatabaseHelper.getInstance(mContext);
                                dbHelper.ExecuteQuery("Update Movies Set UserColumn1=" + DatabaseUtils.sqlEscapeString(str) + " Where _id=" + mMovie.getID());
                            } catch (Exception e) {
                                NLog.e(TAG, e);
                            }
                        }

                        @Override
                        public void onDialogClosed() {
                            // TODO Auto-generated method stub

                        }
                    });
                    d.showDialog();
                }
            });

            tvUserColumn2.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextDialog d = new TextDialog(mContext);
                    d.setTitle(getString(R.string.user_column2));
                    d.setText(mMovie.getUserColumn2());
                    d.setCustomEventListener(new OnCustomEventListener() {
                        @Override
                        public void onOkClicked(String str) {
                            try {
                                tvUserColumn2.setText(str);
                                mMovie.setUserColumn2(str);
                                DatabaseHelper dbHelper = DatabaseHelper.getInstance(mContext);
                                dbHelper.ExecuteQuery("Update Movies Set UserColumn2=" + DatabaseUtils.sqlEscapeString(str) + " Where _id=" + mMovie.getID());
                            } catch (Exception e) {
                                NLog.e(TAG, e);
                            }
                        }

                        @Override
                        public void onDialogClosed() {
                            // TODO Auto-generated method stub

                        }
                    });
                    d.showDialog();
                }
            });

            tvUserColumn3.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextDialog d = new TextDialog(mContext);
                    d.setTitle(getString(R.string.user_column3));
                    d.setText(mMovie.getUserColumn3());
                    d.setCustomEventListener(new OnCustomEventListener() {
                        @Override
                        public void onOkClicked(String str) {
                            try {
                                tvUserColumn3.setText(str);
                                mMovie.setUserColumn3(str);
                                DatabaseHelper dbHelper = DatabaseHelper.getInstance(mContext);
                                dbHelper.ExecuteQuery("Update Movies Set UserColumn3=" + DatabaseUtils.sqlEscapeString(str) + " Where _id=" + mMovie.getID());
                            } catch (Exception e) {
                                NLog.e(TAG, e);
                            }
                        }

                        @Override
                        public void onDialogClosed() {
                            // TODO Auto-generated method stub

                        }
                    });
                    d.showDialog();
                }
            });

            tvUserColumn4.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextDialog d = new TextDialog(mContext);
                    d.setTitle(getString(R.string.user_column4));
                    d.setText(mMovie.getUserColumn4());
                    d.setCustomEventListener(new OnCustomEventListener() {
                        @Override
                        public void onOkClicked(String str) {
                            try {
                                tvUserColumn4.setText(str);
                                mMovie.setUserColumn4(str);
                                DatabaseHelper dbHelper = DatabaseHelper.getInstance(mContext);
                                dbHelper.ExecuteQuery("Update Movies Set UserColumn4=" + DatabaseUtils.sqlEscapeString(str) + " Where _id=" + mMovie.getID());
                            } catch (Exception e) {
                                NLog.e(TAG, e);
                            }
                        }

                        @Override
                        public void onDialogClosed() {
                            // TODO Auto-generated method stub

                        }
                    });
                    d.showDialog();
                }
            });


            tvUserColumn5.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextDialog d = new TextDialog(mContext);
                    d.setTitle(getString(R.string.user_column5));
                    d.setText(mMovie.getUserColumn5());
                    d.setCustomEventListener(new OnCustomEventListener() {
                        @Override
                        public void onOkClicked(String str) {
                            try {
                                tvUserColumn5.setText(str);
                                mMovie.setUserColumn5(str);
                                DatabaseHelper dbHelper = DatabaseHelper.getInstance(mContext);
                                dbHelper.ExecuteQuery("Update Movies Set UserColumn5=" + DatabaseUtils.sqlEscapeString(str) + " Where _id=" + mMovie.getID());
                            } catch (Exception e) {
                                NLog.e(TAG, e);
                            }
                        }

                        @Override
                        public void onDialogClosed() {
                            // TODO Auto-generated method stub

                        }
                    });
                    d.showDialog();
                }
            });


            tvUserColumn6.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextDialog d = new TextDialog(mContext);
                    d.setTitle(getString(R.string.user_column6));
                    d.setText(mMovie.getUserColumn6());
                    d.setCustomEventListener(new OnCustomEventListener() {
                        @Override
                        public void onOkClicked(String str) {
                            try {
                                tvUserColumn6.setText(str);
                                mMovie.setUserColumn6(str);
                                DatabaseHelper dbHelper = DatabaseHelper.getInstance(mContext);
                                dbHelper.ExecuteQuery("Update Movies Set UserColumn6=" + DatabaseUtils.sqlEscapeString(str) + " Where _id=" + mMovie.getID());
                            } catch (Exception e) {
                                NLog.e(TAG, e);
                            }
                        }

                        @Override
                        public void onDialogClosed() {
                            // TODO Auto-generated method stub

                        }
                    });
                    d.showDialog();
                }
            });


            tvRlsType.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    AutoTextDialog d = new AutoTextDialog(mContext);
                    d.setColumn("RlsType");
                    d.setTitle(mContext.getText(R.string.rls_type));
                    d.setCustomEventListener(new AutoTextDialog.OnCustomEventListener() {
                        @Override
                        public void onOkClicked(String str) {
                            try {
                                tvRlsType.setText(str);
                                mMovie.setRlsType(str);
                                DatabaseHelper dbHelper = DatabaseHelper.getInstance(mContext);
                                dbHelper.ExecuteQuery("Update Movies Set RlsType=" + DatabaseUtils.sqlEscapeString(str) + " Where _id=" + mMovie.getID());
                            } catch (Exception e) {
                                NLog.e(TAG, e);
                            }
                        }

                        @Override
                        public void onDialogClosed() {

                        }
                    });
                    d.showDialog();
                }
            });


            tvRlsGroup.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    AutoTextDialog d = new AutoTextDialog(mContext);
                    d.setColumn("RlsGroup");
                    d.setTitle(mContext.getText(R.string.rls_group));
                    d.setCustomEventListener(new AutoTextDialog.OnCustomEventListener() {
                        @Override
                        public void onOkClicked(String str) {
                            try {
                                tvRlsGroup.setText(str);
                                mMovie.setRlsGroup(str);
                                DatabaseHelper dbHelper = DatabaseHelper.getInstance(mContext);
                                dbHelper.ExecuteQuery("Update Movies Set RlsGroup=" + DatabaseUtils.sqlEscapeString(str) + " Where _id=" + mMovie.getID());
                            } catch (Exception e) {
                                NLog.e(TAG, e);
                            }
                        }

                        @Override
                        public void onDialogClosed() {

                        }
                    });
                    d.showDialog();
                }
            });

            tvPersonalRating.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    RatingDialog rd = new RatingDialog(mContext);
                    rd.setRating(Utils.parseFloat(mMovie.getPersonalRating()));
                    rd.setCustomEventListener(new RatingDialog.OnCustomEventListener() {
                        @Override
                        public void onOkClicked(String str) {
                            try {
                                tvPersonalRating.setText(str);
                                mMovie.setPersonalRating(str);
                                DatabaseHelper dbHelper = DatabaseHelper.getInstance(mContext);
                                dbHelper.ExecuteQuery("Update Movies Set PersonalRating=" + DatabaseUtils.sqlEscapeString(str) + " Where _id=" + mMovie.getID());
                            } catch (Exception e) {
                                NLog.e(TAG, e);
                            }
                        }

                        @Override
                        public void onDialogClosed() {

                        }
                    });
                    rd.showDialog();
                }
            });

        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

}
