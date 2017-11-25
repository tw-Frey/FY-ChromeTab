package tw.idv.fy.chrometab;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class ChromeTabHelper {

    private static final List<String> All_Possible_Chrome_Package;
    private static final String STABLE_PACKAGE;
    private static final String BETA_PACKAGE;
    private static final String DEV_PACKAGE;
    private static final String LOCAL_PACKAGE;
    private static final Uri Chrome_MarketUri;
    static {
        All_Possible_Chrome_Package = Arrays.asList(
                STABLE_PACKAGE = "com.android.chrome",
                BETA_PACKAGE   = "com.chrome.beta",
                DEV_PACKAGE    = "com.chrome.dev",
                LOCAL_PACKAGE  = "com.google.android.apps.chrome"
        );
        Chrome_MarketUri = Uri.parse("market://details?id=" + STABLE_PACKAGE);
    }
    private static String sPackageNameToUse;

    private static boolean CheckSupport(Context ctx) {
        if (sPackageNameToUse == null) {
            sPackageNameToUse = CustomTabsClient.getPackageName(ctx, All_Possible_Chrome_Package);
        }
        return sPackageNameToUse != null;
    }

    private static void LoadUri(Context ctx, Uri uri) {
        try {
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().build();
            customTabsIntent.intent.setPackage(sPackageNameToUse);
            customTabsIntent.launchUrl(ctx, uri);
        } catch (Throwable e) {
            Toast.makeText(ctx, "Url or Browser has error.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        if (ctx instanceof Activity) ((Activity) ctx).finish();
    }

    public static void Open(Context context, Uri uri) {
        context.startActivity(new Intent(context, Activity.class).setData(uri));
    }

    public static class Activity extends android.app.Activity {

        @Override
        protected void onResume() {
            super.onResume();
            final Uri uri = getIntent().getData();
            if (CheckSupport(this)) {
                LoadUri(this, uri);
                return;
            }
            new AlertDialog.Builder(this, R.style.Theme_AppCompat_Dialog_Alert)
                    .setPositiveButton(android.R.string.ok, (d, w) ->
                            startActivity(new Intent(Intent.ACTION_VIEW, Chrome_MarketUri))
                    )
                    .setNegativeButton(android.R.string.no, (d, w) ->
                            LoadUri(Activity.this, uri)
                    )
                    .setOnCancelListener(d -> finish())
                    .setTitle("需要最新 Chrome 嗎？")
                    .show();
        }
    }
}
