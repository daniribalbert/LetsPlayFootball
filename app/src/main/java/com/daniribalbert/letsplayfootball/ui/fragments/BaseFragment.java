package com.daniribalbert.letsplayfootball.ui.fragments;

import android.app.Fragment;
import android.view.View;
import android.widget.ProgressBar;

import com.daniribalbert.letsplayfootball.ui.activities.BaseActivity;

/**
 * That BaseFragment that starts every other Fragment.
 */
public abstract class BaseFragment extends Fragment {

    private ProgressBar mProgressBar;

    public BaseActivity getBaseActivity(){
        return (BaseActivity) getActivity();
    }

    public void setProgress(ProgressBar progressBar){ mProgressBar = progressBar; }

    protected void showProgress(boolean show){
        if (mProgressBar != null) {
            mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);

        }
    }
}
