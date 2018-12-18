package tw.idv.fy.chrometab;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.AnimRes;
import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.ActivityOptionsCompat;
import androidx.appcompat.app.AlertDialog;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

import static androidx.browser.customtabs.CustomTabsIntent.EXTRA_EXIT_ANIMATION_BUNDLE;

@SuppressWarnings({"unused", "WeakerAccess", "SpellCheckingInspection"})
public class ChromeTabHelper {

    public static final int NO_TITLE = CustomTabsIntent.NO_TITLE;
    public static final int SHOW_PAGE_TITLE = CustomTabsIntent.SHOW_PAGE_TITLE;
    public static final String EXTRA_TITLE_VISIBILITY_STATE = CustomTabsIntent.EXTRA_TITLE_VISIBILITY_STATE;
    public static final String EXTRA_ENABLE_URLBAR_HIDING   = CustomTabsIntent.EXTRA_ENABLE_URLBAR_HIDING; // 預設: true(隱藏)
    public static final String EXTRA_TOOLBAR_COLOR          = CustomTabsIntent.EXTRA_TOOLBAR_COLOR;

    private static final String Extra_Key_OptionIntent  = "mOptionIntent";
    private static final String Extra_Key_InAnimBundle  = "mStartAnimationBundle";
    private static final String Extra_Key_OutAnimBundle = "mExitAnimationBundle";
    private static final @AnimRes int Dummy_AnimRes = 0;

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
    private static Bundle mStartAnimationBundle;
    private static Bundle mExitAnimationBundle;

    private static boolean CheckSupport(Context ctx) {
        if (sPackageNameToUse == null) {
            sPackageNameToUse = CustomTabsClient.getPackageName(ctx, All_Possible_Chrome_Package);
        }
        return sPackageNameToUse != null;
    }

    private static void LoadUri(Context ctx, Intent mIntent) {
        try {
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                    .setStartAnimations(ctx, Dummy_AnimRes, Dummy_AnimRes) // must be, 否則無法覆蓋
                    .setShowTitle(true)
                    .build();
            assert customTabsIntent.startAnimationBundle != null;
            customTabsIntent.startAnimationBundle.putAll(mIntent.getBundleExtra(Extra_Key_InAnimBundle));
            customTabsIntent.intent.putExtra(EXTRA_EXIT_ANIMATION_BUNDLE, mIntent.getBundleExtra(Extra_Key_OutAnimBundle));
            customTabsIntent.intent.putExtras((Intent) mIntent.getParcelableExtra(Extra_Key_OptionIntent));
            customTabsIntent.intent.setPackage(sPackageNameToUse);
            customTabsIntent.launchUrl(ctx, mIntent.getData());
        } catch (Throwable e) {
            Toast.makeText(ctx, R.string.something_wrong, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        if (ctx instanceof Activity) ((Activity) ctx).finish();
    }

    public static Bundle DefaultEnterAnimationBundle(@NonNull Context context) {
        if (mStartAnimationBundle == null) {
            mStartAnimationBundle = ActivityOptionsCompat
                    .makeCustomAnimation(context, android.R.anim.fade_in, android.R.anim.fade_out)
                    .toBundle();
        }
        return mStartAnimationBundle;
    }

    public static Bundle DefaultExitAnimationBundle(@NonNull Context context) {
        if (mExitAnimationBundle == null) {
            mExitAnimationBundle = ActivityOptionsCompat
                    .makeCustomAnimation(context, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .toBundle();
        }
        return mExitAnimationBundle;
    }

    public static void Open(@NonNull Context context, @NonNull Uri uri) {
        Open(context, uri, DefaultEnterAnimationBundle(context), DefaultExitAnimationBundle(context));
    }

    public static void Open(@NonNull Context context, @NonNull Uri uri,
                            @NonNull Bundle mStartAnimationBundle,
                            @NonNull Bundle mExitAnimationBundle) {
        Open(context, uri, mStartAnimationBundle, mExitAnimationBundle, new Intent());
    }

    public static void Open(@NonNull Context context, @NonNull Uri uri,
                            @NonNull Bundle mStartAnimationBundle,
                            @NonNull Bundle mExitAnimationBundle,
                            @NonNull Intent mOptionIntent) {
        context.startActivity(
                new Intent(context, Activity.class)
                        .setData(uri)
                        .putExtra(Extra_Key_InAnimBundle , mStartAnimationBundle)
                        .putExtra(Extra_Key_OutAnimBundle, mExitAnimationBundle)
                        .putExtra(Extra_Key_OptionIntent , mOptionIntent)
        );
    }

    public static class Activity extends android.app.Activity {

        @Override
        protected void onResume() {
            super.onResume();
            final Context ctx = this;
            if (CheckSupport(ctx)) {
                LoadUri(ctx, getIntent());
                return;
            }
            new AlertDialog.Builder(ctx, R.style.Theme_AppCompat_Dialog_Alert)
                    .setPositiveButton(android.R.string.ok, (d, w) ->
                            startActivity(new Intent(Intent.ACTION_VIEW, Chrome_MarketUri))
                    )
                    .setNegativeButton(android.R.string.no, (d, w) ->
                            LoadUri(ctx, getIntent())
                    )
                    .setOnCancelListener(d -> finish())
                    .setTitle(R.string.update_title)
                    .show();
        }
    }
}
