package net.mycitizen.mcn;

import android.app.Application;

import org.acra.*;
import org.acra.annotation.*;

//@ReportsCrashes(formKey = "dGhJMThjSzE4RUJXUU9WNFhSZEdSOVE6MQ")
@ReportsCrashes(
        formKey = "", // This is required for backward compatibility but not used
        formUri = "http://mcn-acra.mycitizen.net/report/report.php",
        //httpMethod = org.acra.sender.HttpSender.Method.PUT,
        reportType = org.acra.sender.HttpSender.Type.JSON,
        formUriBasicAuthLogin = "mcn_crash_report",
        formUriBasicAuthPassword = "HFqWeTvV7hh63r2k",
        customReportContent = {
                ReportField.APP_VERSION_CODE,
                ReportField.APP_VERSION_NAME,
                ReportField.ANDROID_VERSION,
                ReportField.PACKAGE_NAME,
                ReportField.REPORT_ID,
                ReportField.BUILD,
                ReportField.PHONE_MODEL,
                ReportField.STACK_TRACE,
                ReportField.CUSTOM_DATA
        },
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.toast_crash
)

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
        ACRA.getErrorReporter().putCustomData("version", Config.version);
    }
}
