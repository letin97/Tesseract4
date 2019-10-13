package com.example.letrongtin.tesseract4.utils;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

public class PermissionUtils {

    private static final String TAG = PermissionUtils.class.getSimpleName();

    public static final int PERMISSION_REQUEST_CODE = 0;
    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
    private static final String STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final double MIN_OPENGL_VERSION = 3.1;

    public static boolean checkPermission(Activity activity) {
        int result = ContextCompat.checkSelfPermission(activity, CAMERA_PERMISSION);
        int result1 = ContextCompat.checkSelfPermission(activity, STORAGE_PERMISSION);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{CAMERA_PERMISSION, STORAGE_PERMISSION}, PERMISSION_REQUEST_CODE);
    }

    public static void shouldShowRequestPermissionRationale(Activity activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, CAMERA_PERMISSION)
        || ActivityCompat.shouldShowRequestPermissionRationale(activity, STORAGE_PERMISSION)) {
            showSettingsDialog(activity);
        }
    }

    private static void showSettingsDialog(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings(activity);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                activity.finish();
            }
        });
        builder.show();
    }

    private static void openSettings(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        activity.startActivity(intent);
    }

    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        boolean result = true;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            result = false;
        }
        String openGlVersionString = ((ActivityManager) activity.getSystemService(
                Context.ACTIVITY_SERVICE)).getDeviceConfigurationInfo().getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.1 later");
            result = false;
        }

        if (!result) {
            showAlertDeny(activity);
        }
        return true;
    }

    private static void showAlertDeny(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Error");
        builder.setMessage("Sorry!! Your device does not support this application.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

}
