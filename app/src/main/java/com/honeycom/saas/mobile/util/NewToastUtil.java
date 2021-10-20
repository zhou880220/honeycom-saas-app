package com.honeycom.saas.mobile.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.honeycom.saas.mobile.R;


/**
 * @Creator: Gu FanFan.
 * @Date: 2019-06-23.
 * @Description: .
 */
public class NewToastUtil {

    private static Toast toast;
    private static Handler handler = new Handler(Looper.getMainLooper());

    public static void showShortToast(Context context, String text) {
        showShortToastSafe(context, text);
    }

    public static void showShortToast(Context context, int resId) {
        showShortToast(context, context.getString(resId));
    }

    public static void showFormatShortToast(Context context, String format, Object... args) {
        showShortToast(context, String.format(format, args));
    }

    public static void showFormatShortToast(Context context, int formatResId, Object... args) {
        showShortToast(context, context.getString(formatResId, args));
    }

    private static void showShortToastSafe(final Context context, final String text) {
        if (handler != null) {
            handler.post((Runnable) (new Runnable() {
                public final void run() {
                    showToast(context, text, 300);
                }
            }));
        }
    }

    public static void showLongToast(Context context, String text) {
        showLongToastSafe(context, text);
    }

    public static void showLongToast(Context context, int resId) {
        showLongToast(context, context.getString(resId));
    }

    public static void showFormatLongToast(Context context, String format, Object... args) {
        showLongToast(context, String.format(format, args));
    }

    public final void showFormatLongToast(Context context, int formatResId, Object... args) {
        showLongToast(context, context.getString(formatResId, args));
    }

    private static void showLongToastSafe(final Context context, final String text) {
        if (handler != null) {
            handler.post((Runnable) (new Runnable() {
                public final void run() {
                    showToast(context, (CharSequence) text, 800);
                }
            }));
        }
    }

    private static void showToast(Context context, CharSequence text, int duration) {
        cancelToast();
        if (toast == null) {
            toast = new Toast(context);
            toast.setGravity(Gravity.CENTER, 0, 0);
        }
        View toastView = View.inflate(context, R.layout.toast_layout, null);
        TextView toastText = toastView.findViewById(R.id.toast_tv_text);
        toastText.setText(text);
        toast.setView(toastView);
        toast.setDuration(duration);
        toast.show();
    }

    private static void cancelToast() {
        if (toast != null) {
            toast.cancel();
        }

        toast = null;
    }
}
