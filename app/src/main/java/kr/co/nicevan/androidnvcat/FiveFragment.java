package kr.co.nicevan.androidnvcat;


import static kr.co.nicevan.androidnvcat.shared.SharedArray.PopupOpenWithClose;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.LogDebug;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.bLogUse;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.bRelease;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kr.co.nicevan.androidnvcat.shared.DealItem;
import kr.co.nicevan.androidnvcat.shared.DealListAdapter;
import kr.co.nicevan.androidnvcat.shared.SharedArray;
import kr.co.nicevan.androidnvcat.shared.SharedManager;


/**
 * A simple {@link Fragment} subclass.
 */
public class FiveFragment extends Fragment {

    private ListView mlistview = null;
    Button btnchkdealresult, btnSendLog;

    public FiveFragment() {
        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 승인내역조회 탭입니다.");
    }

    public static void saveLogFile() {    //OSM20241230 : 로그 저장 함수 별도 생성
        File file;
        StringBuilder log = null;

        //SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] LOG파일 생성합니다.");

        try {
            Process process;
            if (bRelease)
                process = Runtime.getRuntime().exec("logcat -d debugjy:V *:S"); // 'debugjy' 필터만 포함
            else
                process = Runtime.getRuntime().exec("logcat -d");

            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            log = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line);
                log.append("\n");
            }
        } catch (IOException e) {
            Log.e("debugjy", "[NVCAT] Logcat : " + e.getMessage());
        }


        // Convert log to string
        final String logString = log.toString();

        // OSM20241023 : 현재 날짜를 'yyyyMMdd_HHmmss' 형식으로 가져오기
        String timeStamp = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        // OSM20241023 : 파일명에 날짜 형식 추가
        String fileName = "logcat_" + timeStamp + ".txt";


        // Create text file in NICELOG folder
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/NICELOG");
        dir.mkdirs();

        // Clean up old log files (30 days)
        final long THIRTY_DAYS_IN_MILLIS = 30L * 24 * 60 * 60 * 1000; // 30일의 밀리초 계산
        long currentTime = System.currentTimeMillis();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File logFile : files) {
                if (logFile.isFile() && (currentTime - logFile.lastModified() > THIRTY_DAYS_IN_MILLIS)) {
                    logFile.delete(); // 30일 지난 파일 삭제
                }
            }
        }

        file = new File(dir, fileName);

        try {
            // Write logcat in text file
            FileOutputStream fOut = new FileOutputStream(file); // OSM20241025 : Log파일 append 방식 미사용
            OutputStreamWriter osw = new OutputStreamWriter(fOut);

            // Write the string to the file
            osw.write(logString);
            osw.flush();
            osw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//    public static void saveLogFile() {    // OSM20241230 : 로그 저장 함수 별도 생성
//        File file;
//        StringBuilder log = null;
//
//        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] LOG파일 생성합니다.");
//
//        try {
//            Process process;
//            if (bRelease) {
//                process = Runtime.getRuntime().exec("logcat -d debugjy:V *:S");
//            } else {
//                process = Runtime.getRuntime().exec("logcat -d");
//            }
//            BufferedReader bufferedReader = new BufferedReader(
//                    new InputStreamReader(process.getInputStream()));
//
//            log = new StringBuilder();
//            String line;
//
//            // OSM20241023 : 현재 날짜를 'yyyyMMdd' 형식으로 가져오기
//            String timeStamp = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
//
//            // OSM20241023 : 로그 파일명의 날짜(MM-dd) 추출
//            String fileDate = new SimpleDateFormat("MM-dd", Locale.getDefault()).format(new Date());
//
//            while ((line = bufferedReader.readLine()) != null) {
//                // "debugjy" 필터링 및 로그의 MM-dd 날짜 확인
//                if (line.contains("debugjy")) {
//                    // 로그의 날짜 형식: MM-dd
//                    String datePattern = "\\d{2}-\\d{2}"; // 로그의 MM-dd 날짜 패턴
//                    String logDate = extractPattern(line, datePattern);
//
//                    if (logDate != null && logDate.equals(fileDate)) {
//                        log.append(line);
//                        log.append("\n");
//                    }
//                }
//            }
//        } catch (IOException e) {
//            Log.e("debugjy", "[NVCAT] Logcat : " + e.getMessage());
//        }
//
//        // Convert log to string
//        final String logString = log != null ? log.toString() : "";
//
//        // OSM20241023 : 파일명에 날짜 형식 추가
//        String fileName = "ANDNVCAT_" + new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()) + ".txt";
//
//        // 외부 저장소의 NICELOG 폴더 경로 (외부 SDCard가 아닌 앱 외부 저장소)
//        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "NICELOG");
//        if (!dir.exists()) {
//            dir.mkdirs(); // 디렉토리 생성
//        }
//
//        // Clean up old log files (30 days)
//        final long THIRTY_DAYS_IN_MILLIS = 30L * 24 * 60 * 60 * 1000; // 30일의 밀리초 계산
//        long currentTime = System.currentTimeMillis();
//        File[] files = dir.listFiles();
//        if (files != null) {
//            for (File logFile : files) {
//                if (logFile.isFile() && (currentTime - logFile.lastModified() > THIRTY_DAYS_IN_MILLIS)) {
//                    logFile.delete(); // 30일 지난 파일 삭제
//                }
//            }
//        }
//
//        file = new File(dir, fileName);
//
//        try {
//            // Write logcat in text file
//            FileOutputStream fOut = new FileOutputStream(file); // OSM20241025 : Log파일 append 방식 미사용
//            OutputStreamWriter osw = new OutputStreamWriter(fOut);
//
//            // Write the string to the file
//            osw.write(logString);
//            osw.flush();
//            osw.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * 패턴에 해당하는 문자열을 추출하는 유틸리티 메서드
     */
    private static String extractPattern(String text, String pattern) {
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(text);
        return matcher.find() ? matcher.group() : null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_five, container, false);

        btnSendLog = (Button) view.findViewById(R.id.btnsendlog);
        btnSendLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file;
                StringBuilder log = null;

                //SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] LOG파일 생성합니다.");

                try {
                    Process process;
                    if (bRelease)
                        process = Runtime.getRuntime().exec("logcat -d debugjy:V *:S");
                    else
                        process = Runtime.getRuntime().exec("logcat -d");
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(process.getInputStream()));

                    log = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        log.append(line);
                        log.append("\n");
                    }
                } catch (IOException e) {
                    Log.e("debugjy", "[NVCAT] Logcat : " + e.getMessage());
                }

                // Convert log to string
                final String logString = log.toString();

                // OSM20241023 : 현재 날짜를 'yyyyMMdd_HHmmss' 형식으로 가져오기
                String timeStamp = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

                // OSM20241023 : 파일명에 날짜 형식 추가
                String fileName = "logcat_" + timeStamp + ".txt";

                // Create text file in NICELOG folder
                File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/NICELOG");
                dir.mkdirs();

                // Clean up old log files (30 days)
                final long THIRTY_DAYS_IN_MILLIS = 30L * 24 * 60 * 60 * 1000; // 30일의 밀리초 계산
                long currentTime = System.currentTimeMillis();
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File logFile : files) {
                        if (logFile.isFile() && (currentTime - logFile.lastModified() > THIRTY_DAYS_IN_MILLIS)) {
                            logFile.delete(); // 30일 지난 파일 삭제
                        }
                    }
                }

                file = new File(dir, fileName);

                try {
                    // Write logcat in text file
                    FileOutputStream fOut = new FileOutputStream(file); // OSM20241025 : Log파일 append 방식 미사용
                    OutputStreamWriter osw = new OutputStreamWriter(fOut);

                    // Write the string to the file
                    osw.write(logString);
                    osw.flush();
                    osw.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Intnet to send log file as an email attachment
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
//        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"0chul0chul@naver.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "안드로이드 NVCAT 로그 전달");
                intent.putExtra(Intent.EXTRA_TEXT, "첨부파일 참고하세요.");
//        File root = Environment.getExternalStorageDirectory();
                if (!file.exists() || !file.canRead()) {
                    Toast.makeText(getContext(), "첨부파일 첨부에러", Toast.LENGTH_SHORT).show();
                    LogDebug(bLogUse, "debugjy", "[NVCAT] 첨부실패!");

                    //return;
                    PopupOpenWithClose(getContext(), "NVCAT 앱 저장공간 권한 필요");
                }
                else {
//        Uri uri = Uri.parse("file://" + file);
                    Uri uri = FileProvider.getUriForFile(getContext(), "kr.co.nicevan.androidnvcat.fileprovider", file); //20200318 : Android 7.0이상에서 FileUri 노출 금지에 대한 처리
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(Intent.createChooser(intent, "Email Log"));
                }
            }
        });

        btnchkdealresult = (Button) view.findViewById(R.id.btnchkdealresult);
        btnchkdealresult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 승인내역조회 버튼 클릭되었습니다.");

                SQLiteDatabase db = SharedArray.dbHelper.getReadableDatabase();
                Cursor cursor = db.rawQuery("SELECT * FROM CHKDEALTABLE", null);

                ArrayList<DealItem> arrDealitem = new ArrayList<>();
                while (cursor.moveToNext()) {
                    DealItem mdealitem = new DealItem();
                    mdealitem.str_id = cursor.getString(0);
                    mdealitem.str_date = cursor.getString(1);
                    if (cursor.getString(2).equals("21") == true)
                        mdealitem.str_dealtp = "현금";
                    else if (cursor.getString(2).equals("10") == true || cursor.getString(2).equals("30") == true)
                        mdealitem.str_dealtp = "신용";
                    else if (cursor.getString(2).equals("UP") == true)
                        mdealitem.str_dealtp = "은련";
                    else
                        mdealitem.str_dealtp = "";

                    if (cursor.getString(3).equals("0210") == true)
                        mdealitem.str_dealgb = "승인";
                    else if (cursor.getString(3).equals("0430") == true)
                        mdealitem.str_dealgb = "취소";
                    else
                        mdealitem.str_dealgb = "";

                    mdealitem.str_cardno = cursor.getString(4);
                    mdealitem.str_money = cursor.getString(5) + "원";
                    mdealitem.str_tax = cursor.getString(6);
                    mdealitem.str_bongsa = cursor.getString(7);
                    mdealitem.str_halbu = cursor.getString(8);
                    mdealitem.str_apprno = cursor.getString(9);
                    mdealitem.str_apprdate = cursor.getString(10);
                    mdealitem.str_tid = cursor.getString(11);
                    mdealitem.str_bgnm = cursor.getString(12);
                    mdealitem.str_minm = cursor.getString(13);
                    mdealitem.str_storeno = cursor.getString(14);
                    mdealitem.str_recvmsg = cursor.getString(15);
                    mdealitem.str_recvcode = cursor.getString(16);
                    mdealitem.str_bal = cursor.getString(17);
                    mdealitem.str_wcc = cursor.getString(18);
                    mdealitem.str_cardgb = cursor.getString(19);
                    mdealitem.str_msgno = cursor.getString(20);
                    mdealitem.str_dealno = cursor.getString(21);
                    mdealitem.str_micode = cursor.getString(22);
                    mdealitem.str_bgcode = cursor.getString(23);


                    arrDealitem.add(mdealitem);

                    // ListView, Adapter 생성 및 연결
                    mlistview = (ListView) view.findViewById(R.id.listViewdeal);
                    DealListAdapter mlistadapter = new DealListAdapter(arrDealitem);
                    mlistview.setAdapter(mlistadapter);
                    mlistview.setSelection(mlistadapter.getCount() - 1); //가장하단조회
                    mlistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(getContext(), DetailDealResult.class);
                            intent.putExtra("str_id", arrDealitem.get(position).str_id);
                            intent.putExtra("str_date", arrDealitem.get(position).str_date);
                            intent.putExtra("str_cardno", arrDealitem.get(position).str_cardno);
                            intent.putExtra("str_money", arrDealitem.get(position).str_money);
                            intent.putExtra("str_apprno", arrDealitem.get(position).str_apprno);
                            startActivity(intent);
                        }
                    });
                }
            }
        });

        return view;
    }
}