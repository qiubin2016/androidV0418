package com.huawei.agentlitedemo.widget;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by wWX285441 on 7/7/17.
 */

public class BaseAty extends AppCompatActivity {
    protected <T extends View> T fv(int id) {
        //noinspection unchecked
        return (T) findViewById(id);
    }
}
