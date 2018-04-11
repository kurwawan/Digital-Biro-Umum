package com.example.asus.d_biumsemarang1.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.asus.d_biumsemarang1.R;

/**
 * Created by ASUS on 11/6/2017.
 */

public final class DialogUtils {
    public static final String TAG = DialogUtils.class.getSimpleName();

    private DialogUtils() {

    }
    public static AlertDialog buildShowDialogProgress(Context context) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View customView = inflater.inflate(R.layout.dialog_progress, null);
        ProgressBar progress = customView.findViewById(R.id.progress);
        progress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(context, R.color.colorProgress), PorterDuff.Mode.SRC_ATOP);
        dialog.setView(customView);
        AlertDialog alertDialog = dialog.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        return alertDialog;
    }

    public static AlertDialog buildShowDialog(Context context, int title, int message, DialogInterface.OnClickListener listener) {
        String ya = context.getString(R.string.dialog_yes);
        String tidak = context.getString(R.string.dialog_no);
        AlertDialog.Builder dialog = new AlertDialog.Builder(context, R.style.AlertDialogCustom);
        dialog.setTitle(title).setMessage(message)
                .setPositiveButton(ya, listener)
                .setNegativeButton(tidak, listener);
        return dialog.create();
    }
    public static AlertDialog buildShowDialog(Context context, int title, String message, DialogInterface.OnClickListener listener) {
        String ya = context.getString(R.string.dialog_yes);
        String tidak = context.getString(R.string.dialog_no);
        AlertDialog.Builder dialog = new AlertDialog.Builder(context, R.style.AlertDialogCustom);
        dialog.setTitle(title).setMessage(message)
                .setPositiveButton(ya, listener)
                .setNegativeButton(tidak, listener);
        return dialog.create();
    }

    public static void dialogOtentifikasi(AlertDialog dialog){
        TextView text = dialog.getWindow().findViewById(R.id.text);
        Context context = dialog.getContext();
        text.setText(context.getString(R.string.authentication));
    }
    public static AlertDialog buildMessageDialog(Context context, int title, int message){
        String ok = context.getString(android.R.string.ok);
        AlertDialog.Builder dialog = new AlertDialog.Builder(context, R.style.AlertDialogCustom);
        dialog.setTitle(title).setMessage(message)
                .setPositiveButton(ok, null);
        return dialog.create();
    }
    public static AlertDialog buildMessageDialog(Context context, int title, String message){
        String ok = context.getString(android.R.string.ok);
        AlertDialog.Builder dialog = new AlertDialog.Builder(context, R.style.AlertDialogCustom);
        dialog.setTitle(title).setMessage(message)
                .setPositiveButton(ok, null);
        return dialog.create();
    }
    public static AlertDialog buildMessageDialog(Context context, String title, String message){
        String ok = context.getString(android.R.string.ok);
        AlertDialog.Builder dialog = new AlertDialog.Builder(context, R.style.AlertDialogCustom);
        dialog.setTitle(title).setMessage(message)
                .setPositiveButton(ok, null);
        return dialog.create();
    }



}

