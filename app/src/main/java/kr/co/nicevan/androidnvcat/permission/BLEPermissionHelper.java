package kr.co.nicevan.androidnvcat.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class BLEPermissionHelper
{
    public static final int PERMISSION_CODE = 0x1004;

    private static final String[] REQ_PERMISSIONS = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    public static boolean hasPermission(Activity activity)
    {
        for (String permission : REQ_PERMISSIONS)
        {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }

        return true;
    }

    public static void requestPermission(Activity activity)
    {
        for (String permission : REQ_PERMISSIONS)
        {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED)
            {
                //ActivityCompat.requestPermissions(activity, new String[]{permission}, PERMISSION_CODE);
                ActivityCompat.requestPermissions(activity, REQ_PERMISSIONS, PERMISSION_CODE);
                return;
            }
        }
    }

    public static boolean shouldShowRequestPermissionRationale(Activity activity)
    {
        for (String permission : REQ_PERMISSIONS)
        {
            // shouldShowRequestPerssionRationale
            // true : 사용자가 전에 해당 요청을 거부
            // false : 거부했으며 다시묻지않음 또는 기기 정책에서 권한을 금지한 경우
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission))
                return true;
        }

        return false;
    }

    public static void launchPermissionSettings(Activity activity)
    {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        activity.startActivity(intent);
    }
}
