package com.huawei.agentlitedemo.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuItem;

import com.huawei.agentlitedemo.R;
import com.huawei.agentlitedemo.bean.ConfigName;
import com.huawei.agentlitedemo.widget.BaseAty;

/**
 * Created by wWX285441 on 7/7/17.
 */

public class AgentLiteSettings extends BaseAty {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.agentlite_settings);
        getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new ContentFragment()).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_go_to_bind) {
            startActivity(new Intent(this, AgentLiteBind.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class ContentFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        SharedPreferences mPreferences;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            initViews();
        }

        private void initViews() {
            mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

            PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getActivity());
            PreferenceCategory category = new PreferenceCategory(getActivity());
            category.setTitle("Basic");
            screen.addPreference(category);

            for (ConfigName configName : ConfigName.values()) {
                EditTextPreference p = new EditTextPreference(getActivity());
                p.setKey(configName.name());
                p.setTitle(configName.name());
                p.setSummary(mPreferences.getString(configName.name(), ""));
                p.setDialogTitle(configName.name());
                category.addPreference(p);
            }

            setPreferenceScreen(screen);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference preference = findPreference(key);
            preference.setSummary(sharedPreferences.getString(key, ""));
        }
    }
}
