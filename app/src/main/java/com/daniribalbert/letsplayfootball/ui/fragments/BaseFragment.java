package com.daniribalbert.letsplayfootball.ui.fragments;

import android.app.Fragment;

import com.daniribalbert.letsplayfootball.ui.activities.BaseActivity;

/**
 * That BaseFragment that starts every other Fragment.
 */
public abstract class BaseFragment extends Fragment {

    public BaseActivity getBaseActivity(){
        return (BaseActivity) getActivity();
    }
}
