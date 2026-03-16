package kr.co.nicevan.androidnvcat.shared;

import static kr.co.nicevan.androidnvcat.shared.SharedManager.LogDebug;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.bLogUse;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AppCheck {

    public static final String ROOT_DIR = "/data/data/kr.co.nicevan.androidnvcat/";
    public static final String DATABASE_NAME = "CHKDATABASE.db";

    //'CHKDATABASE.db' 를 READONLY로 여는 헬퍼 (경로/플래그만 지정) --> DBHelper 객체로 Open했을 때, CHKDATABASE.db 가 열려 발생하는 혼동 방지
    private static SQLiteDatabase openAssetDbReadOnly(Context ctx) {
        String path = ROOT_DIR + "databases/" + DATABASE_NAME;
        return SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
    }

    public static boolean CheckAppValid(Context context) {
        String appPath = null;
        String hash = null;
        int numBytes;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            appPath = packageInfo.applicationInfo.sourceDir; //해당앱의 apk의 path를 구해온다

            SharedManager.LogDebug(bLogUse, "debugjy", "appPath : " + appPath);
            FileInputStream is = new FileInputStream(appPath);

            MessageDigest md = MessageDigest.getInstance("MD5"); //md5로 해쉬를 생성
            md.reset();
            byte[] bytes = new byte[2048];
            while ((numBytes = is.read(bytes)) != -1)
                md.update(bytes, 0, numBytes);

            byte[] digest = md.digest();

            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < digest.length; i++)
                sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));

            hash = Base64.encodeToString(sb.toString().getBytes(), Base64.NO_WRAP);

            SharedManager.LogDebug(bLogUse, "debugjy", "hash : " + hash);


        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        initialize(context); //파일 카피

        //DBHelper 사용 제거, 읽기전용으로 직접 오픈
        SQLiteDatabase db = openAssetDbReadOnly(context);

        Cursor cursor = db.rawQuery("SELECT * FROM APPCHECK", null); //CHKVALID테이블 조회

        String temp = "";
        while (cursor.moveToNext()) {
            temp = cursor.getString(1);
        }
        LogDebug(bLogUse, "debugjy", "temp : " + temp);

        if (temp == null) {
            return false;
        }

        if (temp.equals(hash))
            return true;
        else
            return false;
    }

    public static void initialize(Context ctx) {
        File folder = new File(ROOT_DIR + "databases");
        folder.mkdirs();
        File outfile = new File(ROOT_DIR + "databases/" + DATABASE_NAME);
        if (outfile.length() <= 0) {
            AssetManager assetManager = ctx.getResources().getAssets();
            try {
                InputStream is = assetManager.open(DATABASE_NAME, AssetManager.ACCESS_BUFFER);
                long filesize = is.available();
                byte[] tempdata = new byte[(int) filesize];
                is.read(tempdata);
                is.close();

                outfile.createNewFile();
                FileOutputStream fo = new FileOutputStream(outfile);
                fo.write(tempdata);
                fo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static boolean CheckAppSign(Context context) {
        PackageManager pm = context.getPackageManager();
        String packageName = context.getPackageName();
        String cert = null;

        try {
            PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            Signature certSignature = packageInfo.signatures[0];
            MessageDigest msgDigest = MessageDigest.getInstance("SHA1");
            msgDigest.update(certSignature.toByteArray());
            cert = Base64.encodeToString(msgDigest.digest(), Base64.NO_WRAP);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        initialize(context); //파일 카피

        //DBHelper 사용 제거, 읽기전용으로 직접 오픈
        SQLiteDatabase db = openAssetDbReadOnly(context);

        String temp = "";
        Cursor cursor = db.rawQuery("SELECT * FROM N", null); //CHKVALID테이블 조회

        while (cursor.moveToNext()) {
            temp = temp + cursor.getString(0);
        }
        cursor = db.rawQuery("SELECT * FROM I", null); //CHKVALID테이블 조회
        while (cursor.moveToNext()) {
            temp = temp + cursor.getString(0);
        }
        cursor = db.rawQuery("SELECT * FROM C", null); //CHKVALID테이블 조회
        while (cursor.moveToNext()) {
            temp = temp + cursor.getString(0);
        }
        cursor = db.rawQuery("SELECT * FROM E", null); //CHKVALID테이블 조회
        while (cursor.moveToNext()) {
            temp = temp + cursor.getString(0); //테스트시
        }

        new File(ROOT_DIR + "databases/" + DATABASE_NAME).delete(); //db파일 조작 못하게 삭제
        new File(ROOT_DIR + "databases/" + DATABASE_NAME + "-journal").delete(); //db파일 조작 못하게 삭제

        if (temp == null) {
            return false;
        }

        if (temp.equals(cert))
            return true;
        else
            return false;
    }
}

