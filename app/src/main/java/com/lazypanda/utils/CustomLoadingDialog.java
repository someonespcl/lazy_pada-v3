package com.lazypanda.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Color;
import com.lazypanda.R;

public class CustomLoadingDialog {
    
    private static Dialog dialog;
    
    public static void showLoadingDialog(Context context) {
        if (dialog == null) {
            dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.layout_custom_loading_dialog);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
        }

        if (!dialog.isShowing()) {
            dialog.show();
        }
    }
    
    public static void hideLoadingDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}