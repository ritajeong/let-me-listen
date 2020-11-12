package com.example.letmelisten;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

public class OutputSettingsFragment extends PreferenceFragmentCompat {
    public OutputSettingsFragment(){

    }
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.output_preferences, rootKey);
    }
}