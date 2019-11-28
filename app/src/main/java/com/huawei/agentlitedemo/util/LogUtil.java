package com.huawei.agentlitedemo.util;

import com.huawei.agentlitedemo.bean.ScrollableLogView;
import com.huawei.usp.UspLog;

import java.util.Locale;

/**
 * Created by wWX285441 on 7/7/17.
 */

public class LogUtil {
    public static void i(ScrollableLogView logView, String tag, String msg) {
        UspLog.i(tag, msg);
        if (logView != null) {
            logView.appendLog(String.format(Locale.US, "%s %s", tag, msg), true);
        }
    }
}
