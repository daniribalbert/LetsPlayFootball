package com.daniribalbert.letsplayfootball.ui.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import com.daniribalbert.letsplayfootball.R;

/**
 * App/User Settings Fragment.
 */
public class SettingsFragment extends PreferenceFragment {

    public static final String TAG = SettingsFragment.class.getSimpleName();

    public static SettingsFragment newInstance() {
        
        Bundle args = new Bundle();
        
        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        Bundle args = getArguments();
        if (args != null) {
            // Do something!
        }
    }


}
