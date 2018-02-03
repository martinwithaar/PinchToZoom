package com.bogdwellers.pinchtozoom.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Martin on 14-10-2016.
 */

public class InfoDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.info);

        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View view = layoutInflater.inflate(R.layout.dialog_about, null);
        TextView textView = view.findViewById(R.id.text);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        Activity activity = getActivity();
        try {
            PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            TextView versionView = view.findViewById(R.id.version);
            versionView.setText(packageInfo.versionName);
        } catch(PackageManager.NameNotFoundException e) {
            // Do nothing
        }
        builder.setView(view);

        return builder.create();
    }
}
