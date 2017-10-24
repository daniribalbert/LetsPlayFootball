package com.daniribalbert.letsplayfootball.ui.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.data.cache.PlayersCache;
import com.daniribalbert.letsplayfootball.data.firebase.listeners.BaseUploadListener;
import com.daniribalbert.letsplayfootball.data.model.Match;
import com.daniribalbert.letsplayfootball.data.model.Player;
import com.daniribalbert.letsplayfootball.ui.constants.IntentConstants;
import com.daniribalbert.letsplayfootball.utils.FileUtils;
import com.daniribalbert.letsplayfootball.utils.GlideUtils;
import com.daniribalbert.letsplayfootball.utils.LogUtils;
import com.daniribalbert.letsplayfootball.utils.ToastUtils;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.HashMap;

import butterknife.BindView;

/**
 * Dialog fragment used to add/edit the next match.
 */
public class DialogFragmentEditMatch extends DialogFragmentViewMatch implements
                                                                     DatePickerDialog.OnDateSetListener,
                                                                     TimePickerDialog.OnTimeSetListener {

    public static final String TAG = DialogFragmentEditMatch.class.getSimpleName();

    @BindView(R.id.edit_match_time_layout)
    View mTimeLayout;
    @BindView(R.id.edit_match_check_in_start_layout)
    View mCheckInStartLayout;
    @BindView(R.id.edit_match_check_in_end_layout)
    View mCheckInEndLayout;

    private EditMatchListener mListener;
    private Calendar mCalendar = Calendar.getInstance();
    private int currentTimeViewId;

    public static DialogFragmentEditMatch newInstance(String leagueId) {
        Bundle bundle = new Bundle();
        bundle.putString(IntentConstants.ARGS_LEAGUE_ID, leagueId);

        DialogFragmentEditMatch dFrag = new DialogFragmentEditMatch();
        dFrag.setRetainInstance(true);
        dFrag.setArguments(bundle);
        return dFrag;
    }

    public static DialogFragmentEditMatch newInstance(String leagueId, String matchId,
                                                      String playerId) {
        Bundle bundle = new Bundle();
        bundle.putString(IntentConstants.ARGS_LEAGUE_ID, leagueId);
        bundle.putString(IntentConstants.ARGS_MATCH_ID, matchId);
        bundle.putString(IntentConstants.ARGS_PLAYER_ID, playerId);

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
        mCheckInStartLayout.setOnClickListener(this);
        mCheckInEndLayout.setOnClickListener(this);
        mMaxPlayersTextView.setOnClickListener(this);

        if (TextUtils.isEmpty(mMatchId)) {
            mCheckThemAllBox.setVisibility(View.VISIBLE);
            mCheckThemAllBox.setOnCheckedChangeListener(
                    new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton,
                                                     boolean checked) {
                            HashMap<String, Player> currentLeaguePlayers = PlayersCache
                                    .getCurrentLeaguePlayers();
                            if (checked) {
                                for (String playerId : currentLeaguePlayers.keySet()) {
                                    mMatch.players.put(playerId, System.currentTimeMillis());
                                }
                            } else {
                                mMatch.players.clear();
                            }
                        }
                    });
        } else {
            mCheckThemAllBox.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_save_match:
                if (mImageUri == null) {
                    mListener.onMatchSaved(mMatch);
                    tryAndCloseDialog();
                } else {
                    uploadImage();
                }
                break;
            case R.id.edit_match_pic:
                promptSelectImage();
                break;
            case R.id.edit_match_time_layout:
            case R.id.edit_match_check_in_start_layout:
            case R.id.edit_match_check_in_end_layout:
                currentTimeViewId = view.getId();
                showDatePickerDialog();
                break;
            case R.id.match_max_players:
                promptSelectMaxPlayers();
                break;

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

    private void promptSelectMaxPlayers() {
        final NumberPicker picker = new NumberPicker(getActivity());


        final String[] values = new String[(Match.MAX_PLAYERS - Match.MIN_PLAYERS) + 2];
        values[0] = getString(R.string.not_available_small);
        for (int i = 0; i <= Match.MAX_PLAYERS - Match.MIN_PLAYERS; i++) {
            values[i + 1] = String.valueOf(Match.MIN_PLAYERS + i);
        }
        picker.setMinValue(0);
        picker.setMaxValue(values.length - 1);
        picker.setDisplayedValues(values);
        picker.setWrapSelectorWheel(true);
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_choose_max_players)
                .setView(picker)
                .setPositiveButton(android.R.string.ok,
                                   new DialogInterface.OnClickListener() {
                                       public void onClick(DialogInterface dialog,
                                                           int whichButton) {
                                           int maxPlayers = picker.getValue() == 0
                                                            ? -1
                                                            : picker.getValue() + 1;
                                           LogUtils.w("VALUE= " + maxPlayers);
                                           mMatch.maxPlayers = maxPlayers;
                                           mMaxPlayersTextView.setText(mMatch.getMaxPlayersText());
                                       }
                                   })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void uploadImage() {
        showProgress(true);
        FileUtils.uploadImage(mImageUri, new BaseUploadListener(mProgressBar) {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                mMatch.image = downloadUrl.toString();
                mListener.onMatchSaved(mMatch);
                tryAndCloseDialog();

                showProgress(false);
                ToastUtils.show(R.string.toast_generic_saved, Toast.LENGTH_SHORT);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (handleImageSelectionActivityResult(requestCode, resultCode, data)) {
            GlideUtils.loadCircularImage(mImageUri, mMatchImage);
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

        switch (currentTimeViewId) {
            case R.id.edit_match_time_layout:
                if (nextMatchTime < currentTimeMillis) {
                    ToastUtils.show(R.string.toast_error_match_time, Toast.LENGTH_SHORT);
                    return;
                }
                mMatch.time = nextMatchTime;
                break;
            case R.id.edit_match_check_in_start_layout:
                mMatch.checkInStart = nextMatchTime;
                break;
            case R.id.edit_match_check_in_end_layout:
                mMatch.checkInEnds = nextMatchTime;
                break;

        }
        updatedTimeText();
    }


    public interface EditMatchListener {
        void onMatchSaved(Match match);
    }
}
