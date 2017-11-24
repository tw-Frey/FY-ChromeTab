package tw.idv.fy.chrometab;

import android.content.Context;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;

public class ChromeTabHelper {
    public static boolean LoadUri(Context context, Uri uri) {
        try {
            new CustomTabsIntent.Builder().build().launchUrl(context, uri);
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
