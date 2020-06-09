package com.testacuant.p20.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import java.util.ArrayList;
import java.util.List;

public class PermissionManager {

    private PermissionManager() {
        throw new IllegalStateException("Permission Manager");
    }

    /**
     * Checks for runtime permission.
     * @param activity Calling activity.
     * @param askForThem {@code True} if missing permission should be requested, else {@code false}.
     * @param permissions List of permissions.
     * @return {@code True} if permissions are present, else {@code false}.
     */
    @TargetApi(23)
    public static boolean checkPermissions(final Activity activity,
                                           final boolean askForThem,
                                           final String... permissions) {

        // Old SDK version does not have dynamic permissions.
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }

        final List<String> permissionsToCheck = new ArrayList<>();

        for (final String permission : permissions) {
           if (ContextCompat.checkSelfPermission(activity, permission) != PermissionChecker.PERMISSION_GRANTED) {
                permissionsToCheck.add(permission);
            }
        }
        if (!permissionsToCheck.isEmpty() && askForThem) {
            ActivityCompat
                    .requestPermissions(activity, permissionsToCheck.toArray(new String[0]), 0);
        }

        return permissionsToCheck.isEmpty();
    }
}
