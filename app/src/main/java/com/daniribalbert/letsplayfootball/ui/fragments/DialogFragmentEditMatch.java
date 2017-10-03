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
public class DialogFragmentEditMatch extends DialogFragmentViewMatch implements
                                                                     DatePickerDialog.OnDateSetListener,
                                                                     TimePickerDialog.OnTimeSetListener {

    public static final String TAG = DialogFragmentEditMatch.class.getSimpleName();

    private EditMatchListener mListener;
    private Uri mImageUri;
    private Calendar mCalendar = Calendar.getInstance();

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
    protected void setupViewMode() {
        mSaveMatch.setText(R.string.save);
        mSaveMatch.setOnClickListener(this);
        mMatchImage.setOnClickListener(this);
        mTimeLayout.setOnClickListener(this);
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


    public interface EditMatchListener {
        void onMatchSaved(Match match);
    }
}
