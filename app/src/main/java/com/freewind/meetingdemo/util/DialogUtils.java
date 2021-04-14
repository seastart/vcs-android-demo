// ////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2015-2017 Hangzhou Freewind Technology Co., Ltd.
// All rights reserved.
// http://www.seastart.cn
//
// ///////////////////////////////////////////////////////////////////////////
package com.freewind.meetingdemo.util;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

import com.freewind.meetingdemo.R;

public class DialogUtils {
    private static DialogUtils instance;

    private DialogUtils() {
    }

    public static DialogUtils getInstance() {
        if (instance == null) {
            synchronized (DialogUtils.class) {
                if (instance == null) {
                    instance = new DialogUtils();
                }
            }
        }
        return instance;
    }

    /**
     * 普通对话框,带标题
     */
    public Dialog getConfirmDialog(Context context, String title, String content, String cancelStr, String confirmStr, CallBack callBack) {
        final Dialog dialog = new Dialog(context, R.style.MyCustomDialog);
        dialog.setContentView(R.layout.dialog_confirm_with_title);

        TextView titleTv = dialog.findViewById(R.id.dialog_title_tv);
        titleTv.setText(title);

        TextView contentTv = dialog.findViewById(R.id.dialog_content_tv);
        contentTv.setText(content);

        TextView cancelTv = dialog.findViewById(R.id.cancel_tv);
        cancelTv.setText(cancelStr);
        cancelTv.setOnClickListener(v -> {
            callBack.onCancel();
            dialog.dismiss();
        });

        TextView confirmTv = dialog.findViewById(R.id.confirm_tv);
        confirmTv.setText(confirmStr);
        confirmTv.setOnClickListener(v -> {
            callBack.onConfirm();
            dialog.dismiss();
        });

        return dialog;
    }

    public interface CallBack{
        void onConfirm();
        void onCancel();
    }
}
