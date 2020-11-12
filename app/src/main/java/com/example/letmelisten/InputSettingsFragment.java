package com.example.letmelisten;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

public class InputSettingsFragment extends PreferenceFragmentCompat {

    public InputSettingsFragment(){

    }
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.input_preferences, rootKey);
    }
}