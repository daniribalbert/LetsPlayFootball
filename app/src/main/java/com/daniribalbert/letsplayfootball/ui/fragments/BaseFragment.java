package com.daniribalbert.letsplayfootball.ui.fragments;

import android.app.Fragment;
import android.view.View;
import android.widget.ProgressBar;

import com.daniribalbert.letsplayfootball.ui.activities.BaseActivity;
import com.daniribalbert.letsplayfootball.utils.LogUtils;

/**
 * That BaseFragment that starts every other Fragment.
 */
public abstract class BaseFragment extends Fragment {

    protected ProgressBar mProgressBar;

    public BaseActivity getBaseActivity(){
        return (BaseActivity) getActivity();
    }

    public void setProgress(ProgressBar progressBar){ mProgressBar = progressBar; }

    public void showProgress(boolean show){
        if (mProgressBar != null) {
            mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);

        }
    }
}
