package kr.co.nicevan.androidnvcat.shared;

import static kr.co.nicevan.androidnvcat.shared.SharedManager.bLogUse;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";

    //OSM20250902 : 새 DB 파일명,버전 — 버전은 반드시 기존보다 큼
    public static final String DB_NAME = "ANDROIDNVCAT2.db";
    public static final int DB_VERSION = 2;

    private final Context appCtx;

    //새 권장 생성자
    public DBHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
        this.appCtx = context.getApplicationContext();
    }

    //OSM20250902 : 예전 호출부와의 호환용 생성자(이름/버전 인자를 무시하고 새 값 사용 ---> 기존 호출부 수정을 최소화하고자 함)
    public DBHelper(Context context, String /*ignored*/ name,
                    SQLiteDatabase.CursorFactory factory, int /*ignored*/ version) {
        super(context.getApplicationContext(), DB_NAME, factory, DB_VERSION);
        this.appCtx = context.getApplicationContext();
    }

    @Override public void onCreate(SQLiteDatabase db) {
        // 새 스키마
        db.execSQL("CREATE TABLE IF NOT EXISTS CHKVALIDTABLE (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "date TEXT, result TEXT, reason TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS CHKDEALTABLE (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "date TEXT, dealtp TEXT, dealgb TEXT, cardno TEXT, money TEXT, tax TEXT," +
                "bongsa TEXT, halbu TEXT, apprno TEXT, apprdate TEXT, catid TEXT," +
                "bgname TEXT, miname TEXT, bizno TEXT, recvmsg TEXT, recvcd TEXT," +
                "bal TEXT, apprtp TEXT, cardcb TEXT, msgno TEXT, dealnum TEXT," +
                "msgtxt TEXT, micode TEXT, bgcode TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS CHKAPPSIGN (src TEXT, date TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS CHKAPPVALID (src TEXT, date TEXT)");

        //OSM20250902 : 최초 생성 시에만 ‘구 DB’ → ‘신 DB’ 이관을 시도
        migrateFromOldIfPresent(db);
    }

    //OSM20250902 : 필요 시 버전별 마이그레이션 로직 추가
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // CHKDEALTABLE에 micode/bgcode가 없으면 추가
        if (!columnExists(db, "CHKDEALTABLE", "micode")) {
            db.execSQL("ALTER TABLE CHKDEALTABLE ADD COLUMN micode TEXT");
        }
        if (!columnExists(db, "CHKDEALTABLE", "bgcode")) {
            db.execSQL("ALTER TABLE CHKDEALTABLE ADD COLUMN bgcode TEXT");
        }
    }

    //OSM20250902 : 다운그레이드 크래시 방지 (플레이스토어 롤백 등)
    @Override public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] onDowngrade " + oldVersion + " -> " + newVersion);

        onUpgrade(db, oldVersion, newVersion);
    }


    private boolean columnExists(SQLiteDatabase db, String table, String column) {
        Cursor c = db.rawQuery("PRAGMA table_info(" + table + ")", null);
        try {
            while (c.moveToNext()) {
                if (column.equalsIgnoreCase(c.getString(1))) return true; // 1 = name
            }
        } finally {
            if (c != null) c.close();
        }
        return false;
    }


    //OSM20250902 : 최초 생성 시, 새 DB가 비어 있고 옛 DB 파일이 존재하면 데이터 이
    private void migrateFromOldIfPresent(SQLiteDatabase newDb) {
        try {
            // 새 DB가 이미 데이터 있으면 스킵
            long rows = DatabaseUtils.queryNumEntries(newDb, "CHKDEALTABLE");
            if (rows > 0) return;

            // 구 DB 파일 확인
            String oldPath = appCtx.getDatabasePath("ANDROIDNVCAT.db").getPath();
            File oldFile = new File(oldPath);
            if (!oldFile.exists()) {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] Old DB not found. Skip migration.");
                return;
            }

            // ATTACH
            String esc = oldPath.replace("'", "''");
            newDb.execSQL("ATTACH DATABASE '" + esc + "' AS oldDb;");

            newDb.beginTransaction();
            try {
                // CHKVALIDTABLE
                newDb.execSQL(
                        "INSERT INTO CHKVALIDTABLE(date, result, reason) " +
                                "SELECT date, result, reason FROM oldDb.CHKVALIDTABLE;"
                );

                // CHKDEALTABLE (구 DB에 micode, bgcode 없으므로 공백으로 채움)
                newDb.execSQL(
                        "INSERT INTO CHKDEALTABLE(" +
                                "date,dealtp,dealgb,cardno,money,tax,bongsa,halbu,apprno,apprdate," +
                                "catid,bgname,miname,bizno,recvmsg,recvcd,bal,apprtp,cardcb,msgno," +
                                "dealnum,msgtxt,micode,bgcode) " +
                                "SELECT " +
                                "date,dealtp,dealgb,cardno,money,tax,bongsa,halbu,apprno,apprdate," +
                                "catid,bgname,miname,bizno,recvmsg,recvcd,bal,apprtp,cardcb,msgno," +
                                "dealnum,msgtxt,'' AS micode,'' AS bgcode " +   // 공백 처리
                                "FROM oldDb.CHKDEALTABLE;"
                );

                // CHKAPPSIGN
                newDb.execSQL(
                        "INSERT INTO CHKAPPSIGN(src, date) " +
                                "SELECT src, date FROM oldDb.CHKAPPSIGN;"
                );

                // CHKAPPVALID
                newDb.execSQL(
                        "INSERT INTO CHKAPPVALID(src, date) " +
                                "SELECT src, date FROM oldDb.CHKAPPVALID;"
                );

                newDb.setTransactionSuccessful();
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] Migration completed.");
            } finally {
                newDb.endTransaction();
                try { newDb.execSQL("DETACH DATABASE oldDb;"); } catch (Exception ignore) {}
            }
        } catch (Exception e) {
            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] Migration failed: " + e.getMessage());
        }
    }

    public void insert(String date, String result, String reason) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("date", date);
        cv.put("result", result);
        cv.put("reason", reason);
        db.insert("CHKVALIDTABLE", null, cv);
    }

    public void insertChkapp(String sign) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("src", "src");
        cv.put("date", sign);
        db.insert("CHKAPPSIGN", null, cv);
    }

    public void insertDeal(
            String date, String dealtp, String dealgb, String cardno, String money, String tax,
            String bongsa, String halbu, String apprno, String apprdate, String catid,
            String bgname, String miname, String bizno, String recvmsg, String recvcd,
            String bal, String apprtp, String cardcb, String msgno, String dealnum,
            String msgtxt, String micode, String bgcode
    ) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("date", date);
        cv.put("dealtp", dealtp);
        cv.put("dealgb", dealgb);
        cv.put("cardno", cardno);
        cv.put("money", money);
        cv.put("tax", tax);
        cv.put("bongsa", bongsa);
        cv.put("halbu", halbu);
        cv.put("apprno", apprno);
        cv.put("apprdate", apprdate);
        cv.put("catid", catid);
        cv.put("bgname", bgname);
        cv.put("miname", miname);
        cv.put("bizno", bizno);
        cv.put("recvmsg", recvmsg);
        cv.put("recvcd", recvcd);
        cv.put("bal", bal);
        cv.put("apprtp", apprtp);
        cv.put("cardcb", cardcb);
        cv.put("msgno", msgno);
        cv.put("dealnum", dealnum);
        cv.put("msgtxt", msgtxt);
        cv.put("micode", micode);
        cv.put("bgcode", bgcode);
        db.insert("CHKDEALTABLE", null, cv);
    }
}


