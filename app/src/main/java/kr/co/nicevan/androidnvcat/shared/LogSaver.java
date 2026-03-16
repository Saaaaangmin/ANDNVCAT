// OSM20250929 : 로그 저장 클래스 (getExternalFilesDir("NICELOG") 버전) (OSM20251121 : MERGE 완료)
package kr.co.nicevan.androidnvcat.shared;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static kr.co.nicevan.androidnvcat.shared.SharedManager.bRelease;

public final class LogSaver {
    private static final String TAG = "LogSaver";

    private static final AtomicLong lastSaveAt = new AtomicLong(0L);

    private static final AtomicLong lastLogLineTime = new AtomicLong(0L);   //OSM20251215 : 마지막으로 저장한 logcat 로그의 타임스탬프(ms)


    private static final ExecutorService ioExec =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "LogSaver-IO");
                t.setPriority(Thread.NORM_PRIORITY - 1);
                return t;
            });

    private LogSaver() {}

    public static void saveLogAsyncDebounced(Context ctx, long intervalMs) {
        final long now = System.currentTimeMillis();
        final long last = lastSaveAt.get();
        if (now - last < intervalMs) return;
        if (!lastSaveAt.compareAndSet(last, now)) return;
        saveLogAsync(ctx);
    }

    public static void saveLogAsync(Context ctx) {
        final Context app = ctx.getApplicationContext();
        ioExec.execute(() -> { // ← EXEC 말고 ioExec 사용!
            try {
                saveLogFile(app);
            } catch (Throwable t) {
                Log.e("debugjy", "[NVCAT] saveLogAsync error", t);
            }
        });
    }

    public static void saveLogFile(Context context) {
        final String TAG = "debugjy";

        //디렉토리 확보 (앱 전용 외부)
        File dir = new File(Environment.getExternalStorageDirectory(), "NICELOG");

        if (!dir.exists() && !dir.mkdirs()) {
            Log.e(TAG, "[NVCAT] Failed to mkdirs: " + dir.getAbsolutePath());
            return;
        }

        //파일명
        String day = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        File file = new File(dir, "logcat_" + day + ".txt");

        //logcat 읽기
        int pid = android.os.Process.myPid();
        ArrayList<String> cmd = new ArrayList<>();
        cmd.add("logcat");
        cmd.add("-d");                      // 덤프 후 종료
        cmd.add("--pid=" + pid);            // 자기 프로세스 로그만 확실히
        if (bRelease) {                     // 릴리즈면 태그 필터링
            cmd.add("debugjy:V");
            cmd.add("*:S");
        }

        //OSM20251215 : 직전까지 저장했던 로그 라인의 시각(ms)
        long lastTimeMs = lastLogLineTime.get();
        long newestTimeMs = 0L;

        //OSM20251215 : 새로 추가할 로그만 담을 버퍼
        StringBuilder newLogBuf = new StringBuilder();

        //OSM20251215 : logcat 타임스탬프 파서 (예: "12-15 13:00:32.348")
        java.text.SimpleDateFormat logcatSdf =
                new java.text.SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US);
        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);

        Process proc = null;
        BufferedReader br = null;
        try {
            proc = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;

            // lastTimeMs == 0 이면 첫 호출 → 전체 저장
            boolean writeMode = (lastTimeMs == 0L);

            while ((line = br.readLine()) != null) {
                //OSM20251215 : 로그 라인의 앞부분에서 타임스탬프 파싱 시도
                if (line.length() >= 18 && Character.isDigit(line.charAt(0))) {
                    String tsStr = line.substring(0, 18); // "MM-dd HH:mm:ss.SSS"
                    try {
                        Date d = logcatSdf.parse(tsStr);
                        if (d != null) {
                            java.util.Calendar c = java.util.Calendar.getInstance();
                            c.setTime(d);
                            c.set(java.util.Calendar.YEAR, currentYear);
                            long tsMs = c.getTimeInMillis();

                            //아직 writeMode가 아니고, 이 줄의 시각이 lastTime 이후라면
                            if (!writeMode && tsMs > lastTimeMs) {
                                writeMode = true;
                            }

                            //이번 덤프 중 가장 최신 시각 기록
                            if (tsMs > newestTimeMs) {
                                newestTimeMs = tsMs;
                            }
                        }
                    } catch (Exception ignore) {
                        // 타임스탬프 파싱 실패 시 그냥 무시
                    }
                }

                //writeMode가 true인 이후의 줄들만 버퍼에 쌓기
                if (writeMode) {
                    newLogBuf.append(line).append('\n');
                }
            }

            // 프로세스 종료 대기
            proc.waitFor();
        } catch (Exception e) {
            Log.e(TAG, "[NVCAT] Logcat read error", e);
        } finally {
            try { if (br != null) br.close(); } catch (IOException ignore) {}
            if (proc != null) proc.destroy();
        }

        //OSM20251215 : 이번에 본 로그 중 최신 시각을 저장 → 다음 호출 때 기준점으로 사용
        if (newestTimeMs > 0L) {
            lastLogLineTime.set(newestTimeMs);
        }

        //OSM20251215 : 새로 추가할 로그가 하나도 없으면 파일 쓰기 생략
        if (newLogBuf.length() == 0) {
            Log.d(TAG, "[NVCAT] 로그 갱신 생략");
            return;
        }

        //파일 append
        String eventStamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
//        String payload = "\n\n=== EVENT @ " + eventStamp + " (pid " + pid + ") ===\n"
//                + newLogBuf
//                + "\n";

        try (FileOutputStream fos = new FileOutputStream(file, true);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
            osw.write(newLogBuf.toString());        //OSM20260205 : 로그 append 시, 신규 이벤트 라인에 대해 구분하지 않고 그대로 로그값 출력
            osw.flush();
        } catch (IOException e) {
            Log.e(TAG, "[NVCAT] 로그 생성 에러 : " + e.getMessage());
        }

        //30일 정리
        final long THIRTY_DAYS = 30L * 24 * 60 * 60 * 1000;
        long now = System.currentTimeMillis();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile() && now - f.lastModified() > THIRTY_DAYS) {
                    //noinspection ResultOfMethodCallIgnored
                    f.delete();
                }
            }
        }
    }
}
