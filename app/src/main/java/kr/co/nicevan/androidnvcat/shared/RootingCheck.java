package kr.co.nicevan.androidnvcat.shared;

import static kr.co.nicevan.androidnvcat.shared.SharedManager.bLogUse;

import java.io.File;

public class RootingCheck {
    public static boolean checkSuperUser() {
        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 루팅체크시작");
        return (checkRootedFiles() == true || checkSuperUserCommand() == true || checkTags() == true) ? true : false;
//        return (checkRootedFiles() == true || checkSuperUserCommand() == true ) ? true : false;
    }

    private static boolean checkRootedFiles() {
        final String[] files = {
                "/sbin/su",
                "/system/su",
                "/system/bin/su",
                "/system/sbin/su",
                "/system/xbin/su",
                "/system/xbin/mu",
                "/system/bin/.ext/.su",
                "/system/usr/su-backup",
                "/data/data/com.noshufou.android.su",
                "/system/app/superuser.apk",
                "/system/app/su.apk",
                "/system/bin/.ext",
                "/system/xbin/.ext",
                "/data/data/com.noshufou.android.su"};

        for (int i = 0; i < files.length; i++) {
            File file = new File(files[i]);
            if (null != file && file.exists()) {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 루팅파일이름 : " + file.getAbsolutePath() + " : " + file.getName());
                return true;
            }
        }
        return false;
    }

    /*
    루팅이 된 기기는 일반적으로 Build.TAGS 값이 제조사 키값이 아닌 test 키 값을 가지고 있습니다.
    */
    private static boolean checkTags() {
        String buildTags = android.os.Build.TAGS;
        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] buildTags : " + buildTags);
        return buildTags != null && buildTags.contains("test-keys");
    }

    private static boolean checkSuperUserCommand() {
        try {
            Runtime.getRuntime().exec("su");
            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] device has super user");
            return true;
        } catch (Error e) {

        } catch (Exception e) {

        }
        return false;
    }
}
