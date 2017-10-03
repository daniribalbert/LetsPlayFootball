package com.daniribalbert.letsplayfootball.ui.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.database.MatchDbUtils;
import com.daniribalbert.letsplayfootball.data.database.listeners.BaseUploadListener;
import com.daniribalbert.letsplayfootball.data.database.listeners.BaseValueEventListener;
import com.daniribalbert.letsplayfootball.data.model.Match;
import com.daniribalbert.letsplayfootball.utils.ActivityUtils;
import com.daniribalbert.letsplayfootball.utils.FileUtils;
import com.daniribalbert.letsplayfootball.utils.GlideUtils;
import com.daniribalbert.letsplayfootball.utils.ToastUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

/**
 * Dialog fragment used to add/edit the next match.
 */
public class DialogFragmentEditMatch extends BaseDialogFragment implements View.OnClickListener,
                                                                       DatePickerDialog.OnDateSetListener,
                                                                       TimePickerDialog.OnTimeSetListener {

    public static final String TAG = DialogFragmentEditMatch.class.getSimpleName();

    public static final String ARGS_MATCH_ID = "ARGS_MATCH_ID";
    public static final String ARGS_LEAGUE_ID = "ARGS_LEAGUE_ID";

    @BindView(R.id.edit_match_pic)
    ImageView mMatchImage;

    @BindView(R.id.edit_match_time_day)
    TextView mMatchDay;

    @BindView(R.id.edit_match_time_hour)
    TextView mMatchHour;

    @BindView(R.id.edit_match_time_layout)
    View mTimeLayout;

    @BindView(R.id.bt_save_match)
    View mSaveMatch;

    private EditMatchListener mListener;

    private Uri mImageUri;

    @BindView(R.id.dialog_progress)
    ProgressBar mProgressBar;

    private Match mMatch;
    private Calendar mCalendar = Calendar.getInstance();

    private String mMatchId;
    private String mLeagueId;

    public static DialogFragmentEditMatch newInstance(String leagueId) {
        Bundle bundle = new Bundle();
        bundle.putString(ARGS_LEAGUE_ID, leagueId);

        DialogFragmentEditMatch dFrag = new DialogFragmentEditMatch();
        dFrag.setRetainInstance(true);
        dFrag.setArguments(bundle);
        return dFrag;
    }

    public static DialogFragmentEditMatch newInstance(String leagueId, String matchId) {
        Bundle bundle = new Bundle();
        bundle.putString(ARGS_LEAGUE_ID, leagueId);
        bundle.putString(ARGS_MATCH_ID, matchId);

        DialogFragmentEditMatch dFrag = new DialogFragmentEditMatch();
        dFrag.setArguments(bundle);
        dFrag.setRetainInstance(true);
        return dFrag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadArgs();
    }

    private void loadArgs() {
        Bundle args = getArguments();
        if (args != null) {
            mMatchId = args.getString(ARGS_MATCH_ID);
            mLeagueId = args.getString(ARGS_LEAGUE_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_match, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSaveMatch.setOnClickListener(this);
        mMatchImage.setOnClickListener(this);
        mTimeLayout.setOnClickListener(this);
        if (savedInstanceState == null) {
            if (TextUtils.isEmpty(mMatchId)) {
                mMatch = new Match(mLeagueId);
                updatedTimeText();
            } else {
                loadMatchData(mLeagueId, mMatchId);
            }
        } else {
            if (mImageUri != null) {
                GlideUtils.loadCircularImage(mImageUri, mMatchImage);
            } else if (mMatch != null && mMatch.hasImage()) {
                GlideUtils.loadCircularImage(mMatch.image, mMatchImage);
            }
        }
    }

    private void loadMatchData(String leagueId, String matchId) {
        MatchDbUtils.getMatch(leagueId, matchId, new BaseValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mMatch = dataSnapshot.getValue(Match.class);

                if (mMatch != null) {
                    updatedTimeText();
                    if (mMatch.hasImage()) {
                        GlideUtils.loadCircularImage(mMatch.image, mMatchImage);
                    }
                }
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow()
                       .setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                                  WindowManager.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_save_match:
                if (mImageUri == null) {
                    mListener.onMatchSaved(mMatch);
                    if (getDialog() != null) {
                        dismiss();
                    }
                } else {
                    uploadImage();
                }
                break;
            case R.id.edit_match_pic:
                promptSelectImage();
                break;
            case R.id.edit_match_time_layout:
                showDatePickerDialog();
        }
    }

    private void showTimePickerDialog() {
        long matchTime = mMatch.time;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(matchTime);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        boolean isSystem24hMode = DateFormat.is24HourFormat(getActivity());

        TimePickerDialog timePickerDialog;
        timePickerDialog = new TimePickerDialog(getActivity(), this, hour, minute, isSystem24hMode);
        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();
    }

    private void showDatePickerDialog() {
        long matchTime = mMatch.time;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(matchTime);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month,
                                                                 day);
        datePickerDialog.setTitle("Select Day");
        datePickerDialog.show();
    }

    private void uploadImage() {
        showProgress(true);
        FileUtils.uploadImage(mImageUri, new BaseUploadListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                super.onFailure(e);
                showProgress(false);
            }

            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                mMatch.image = downloadUrl.toString();
                mListener.onMatchSaved(mMatch);
                if (getDialog() != null) {
                    dismiss();
                }
                showProgress(false);
                ToastUtils.show(R.string.toast_generic_saved, Toast.LENGTH_SHORT);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ARGS_IMAGE_SELECT) {
                mImageUri = ActivityUtils.extractImageUri(data, getActivity());
                GlideUtils.loadCircularImage(mImageUri, mMatchImage);
            }
        }
    }

    public void setListener(EditMatchListener listener) {
        mListener = listener;
    }

    private void showProgress(boolean show) {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        // handles https://code.google.com/p/android/issues/detail?id=17423
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.DAY_OF_MONTH, day);
        showTimePickerDialog();
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
        mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mCalendar.set(Calendar.MINUTE, minute);
        long nextMatchTime = mCalendar.getTimeInMillis();
        long currentTimeMillis = System.currentTimeMillis();
        if (nextMatchTime < currentTimeMillis) {
            ToastUtils.show(R.string.toast_error_match_time, Toast.LENGTH_SHORT);
            return;
        }
        mMatch.time = nextMatchTime;
        updatedTimeText();
    }

    private void updatedTimeText() {
        mMatchDay.setText(mMatch.getDate());
        mMatchHour.setText(mMatch.getTime());
    }


    public interface EditMatchListener {
        void onMatchSaved(Match match);
    }
}
