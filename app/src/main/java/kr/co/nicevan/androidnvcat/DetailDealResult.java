package kr.co.nicevan.androidnvcat;

import static com.felhr.utils.HexData.stringTobytes;
import static com.posbank.device.common.AscII.CH_ACK;
import static com.posbank.device.common.AscII.CH_NAK;
import static com.posbank.device.common.ReturnValue.RTN_COMM_OK;
import static com.posbank.device.common.ReturnValue.RTN_CONTINUE;
import static kr.co.nicevan.androidnvcat.MainActivity.CompareKsn;
import static kr.co.nicevan.androidnvcat.MainActivity.btnDisable;
import static kr.co.nicevan.androidnvcat.MainActivity.btnEnable;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.CardBrand;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.CardCvm;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.Paygb;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.PopupClose;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.PopupOpen;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.PopupOpenEOT;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.RECVBuf;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.bEncPin;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.cMediagb;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.calculate_interval;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.dbHelper;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.encdata;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.func_code;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.icdata;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.initSerial;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.isMultipad;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.isReaderCheck;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.isSign;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.isrun;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.istep;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.length_recv;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.mUart;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.recvBuff;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.sReaderApprtp;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.scr;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.sendBuff;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.slen;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.status;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.temp;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.tstart;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.usbService;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.writeBuffer;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.xor_sum;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.BarcodeToTrack2;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.IsBarcodeSign;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.LogDebug;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.bLogUse;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.bRelease;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.iresult;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.posbank.device.common.Utils;
import com.posbank.device.screader.kis.protocol.ScrProtocolCom;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import kr.co.nicevan.androidnvcat.shared.DealItem;
import kr.co.nicevan.androidnvcat.shared.DetailDealListAdapter;
import kr.co.nicevan.androidnvcat.shared.SharedManager;

import kr.co.nicevan.pos.PosClient;
import kr.co.nicevan.signenc.SignEnc;

import okpos.co.kr.payroid.libUart;

public class DetailDealResult extends Activity {

    private ListView mlistview = null;
    private handler_thread handlerThread;
    private String mTimeout = "", mCatid = "", mMoney = "", mHalbu = "", mBongsa = "", mTax = "", mHwnum = "", mServerip = "", mServerport = "", mApprno = "", mApprdate = "", mDealtp = "", mDealgb = "", mWCC = "", EncPin = "", mMsgtxt = ""; //20200129 : 포인트거래

    public static Button btnBack;
    Button btnReqCnl, btnLogcat, btnSenddb;
    SharedManager mSharedManager;
    private String mFiller;
    TextView tvCnlTitle; //LJY20221004 : tvCnlTitle 추가

    public void SendEmailLogcat() {
        File file;
        StringBuilder log = null;

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

        //convert log to string
        final String logString = log.toString();

        //OSM20241023 : 현재 날짜를 'yyyyMMdd_HHmmss' 형식으로 가져오기
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

        //OSM20241023 : 파일명에 날짜 형식 추가
        String fileName = "logcat_" + timeStamp + ".txt";

        //create text file in SDCard
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/andnvcat_logcat");
        dir.mkdirs();
        file = new File(dir, fileName);

        try {
            //to write logcat in text file
            FileOutputStream fOut = new FileOutputStream(file);   //OSM20241023 : Log파일 append 방식 미사용
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
            Toast.makeText(this, "첨부파일 첨부에러", Toast.LENGTH_SHORT).show();
            LogDebug(bLogUse, "debugjy", "[NVCAT] 첨부실패!");
            return;
        }
//        Uri uri = Uri.parse("file://" + file);
        Uri uri = FileProvider.getUriForFile(DetailDealResult.this, "kr.co.nicevan.androidnvcat.fileprovider", file); //20200318 : Android 7.0이상에서 FileUri 노출 금지에 대한 처리
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, "Email Log"));
    }

    public void SendEmailDB() {
        File backupDB = null;
        try {
            if (Environment.getExternalStorageDirectory().canWrite()) {
                File currentDB = new File(Environment.getDataDirectory(), "//data//" + getPackageName() + "//databases//" + "ANDROIDNVCAT.db" + "");
                backupDB = new File(Environment.getExternalStorageDirectory(), "ANDROIDNVCAT.db");

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "안드로이드 NVCAT DB 전달");
        intent.putExtra(Intent.EXTRA_TEXT, "첨부파일 참고하세요.");
//        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(backupDB));
        intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(DetailDealResult.this, "kr.co.nicevan.androidnvcat.fileprovider", backupDB)); //20200318 : Android 7.0이상에서 FileUri 노출 금지에 대한 처리
        startActivity(Intent.createChooser(intent, "Email DB"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_deal_result);
        mSharedManager = SharedManager.getInstance(getApplicationContext());

        initViews();

        btnSenddb = (Button) findViewById(R.id.btnsenddb);
        if (bRelease) {
            btnSenddb.setVisibility(View.GONE);
        } else {
            btnSenddb.setVisibility(View.VISIBLE);
            btnSenddb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogDebug(bLogUse, "debugjy", "[NVCAT] DB전송 버튼 클릭되었습니다.");
                    SendEmailDB();
                }
            });
        }

        btnLogcat = (Button) findViewById(R.id.btnlogcat);
        btnLogcat.setVisibility(View.VISIBLE);
        btnLogcat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogDebug(bLogUse, "debugjy", "[NVCAT] 로그전송 버튼 클릭되었습니다.");
                SendEmailLogcat();
            }
        });

        btnBack = (Button) findViewById(R.id.btnback);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 뒤로가기 버튼 클릭되었습니다.");

                if (!bRelease) {
                    if (new File(Environment.getExternalStorageDirectory(), "ANDROIDNVCAT.db").exists())
                        new File(Environment.getExternalStorageDirectory(), "ANDROIDNVCAT.db").delete();
                }

                btnEnable();
                finish();
            }
        });

        btnReqCnl = (Button) findViewById(R.id.btndealcnl);
        btnReqCnl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 결제 취소 버튼 클릭되었습니다.");
                int i = 0; //LJY20220816 : 선언부 추가

                //LJY20220816 : 비번 입력 후 진행
                final EditText etPassword = new EditText(DetailDealResult.this);

                AlertDialog.Builder dlg = new AlertDialog.Builder(DetailDealResult.this);
                dlg.setTitle("비밀번호를 입력해주세요.");
                dlg.setView(etPassword);
                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!etPassword.getText().toString().equals("nicenice"))
                        {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 비밀번호가 잘못 되었습니다.");
                            Toast.makeText(DetailDealResult.this, "비밀번호가 잘못 되었습니다.", Toast.LENGTH_LONG).show();
                            btnEnable();
                            btnBack.setEnabled(true);
                            return;
                        }
                        else
                        {
                            if (SharedManager.isStatus == false) {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 연결 상태 체크해주시길 바랍니다.");
                                Toast.makeText(DetailDealResult.this, "리더기 연결 상태 체크해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
                                btnEnable();
                                btnBack.setEnabled(true);
                                return;
                            }

                            mTimeout = mSharedManager.getPreferences().getString("Timeout", "30");
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기타임아웃 : " + mTimeout);

                            mHwnum = mSharedManager.getPreferences().getString("HWNUM", "################"); //LJY20220905 : 디폴드값 설정
                            if (mHwnum.length() != 16) {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] HW식별번호가 잘못 되었습니다.");
                                Toast.makeText(DetailDealResult.this, "HW식별번호가 잘못 되었습니다.", Toast.LENGTH_SHORT).show();
                                btnEnable();
                                btnBack.setEnabled(true);
                                return;
                            }
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] HW식별번호 : " + mHwnum);

                            mServerip = mSharedManager.getPreferences().getString("Serverip", "");
                            if (mServerip.length() == 0 || mServerip.length() > 16) {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 서버 IP가 잘못 되었습니다.");
                                Toast.makeText(DetailDealResult.this, "서버 IP가 잘못 되었습니다.", Toast.LENGTH_SHORT).show();
                                btnEnable();
                                btnBack.setEnabled(true);
                                return;
                            }
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 서버IP : " + mServerip);

                            mServerport = mSharedManager.getPreferences().getString("Serverport", "");
                            if (mServerport.length() == 0 || mServerport.length() > 6) {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 서버 PORT가 잘못 되었습니다.");
                                Toast.makeText(DetailDealResult.this, "서버 PORT가 잘못 되었습니다.", Toast.LENGTH_SHORT).show();
                                btnEnable();
                                btnBack.setEnabled(true);
                                return;
                            }
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 서버PORT : " + mServerport);

                            if (mDealtp.equals("21") && mDealgb.equals("0210") && mWCC.equals("@") == true) {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 현금 KEYIN 취소");
                                Intent intent = new Intent(DetailDealResult.this, KeyPadNumber.class);
                                startActivityForResult(intent, 3);
                            } else if (((mDealtp.equals("10") || mDealtp.equals("UP") || mDealtp.equals("21")) && (mDealgb.equals("0210") == true) || mDealgb.equals("0310") || mDealgb.equals("0330"))) //20200206 : 멤버쉽거래 //20200129 : 포인트거래//현금카드취소, 신용취소, 은련취소
                            {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 카드 취소");

                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 현금영수증 카드 리딩해주세요.");

                                String sReaderBinVer = mSharedManager.getPreferences().getString("READERBINVER", "  "); //LJY20250904 : 8BIN/통합결제 적용
                                if(sReaderBinVer.equals("02")) { //통합결제
                                    func_code = 0x9C;
                                    sReaderApprtp = "4";
                                } else if(sReaderBinVer.equals("01")) { //8BIN
                                    func_code = 0x9C;
                                    sReaderApprtp = "0";
                                } else { //6BIN
                                    func_code = 0x6C;
                                    sReaderApprtp = "0";
                                }

                                if (mSharedManager.getPreferences().getInt("Readertype", 0) == 3) //POSBANK
                                {
                                    isrun = true;

                                    Arrays.fill(RECVBuf, (char) 0x00);
                                    Arrays.fill(encdata, (char) 0x00);
                                    Arrays.fill(icdata, (char) 0x00);

                                    initSerial();
//                                    func_code = 0x6C;     //LJY20250904 : 8BIN/통합결제 적용
                                    btnDisable();
                                    btnBack.setEnabled(false);

                                    scr = new ScrProtocolCom(DetailDealResult.this, "COM" + (mSharedManager.getPreferences().getInt("Portnum", 0) + 1), mSharedManager.getPreferences().getString("BaudrateStr", "115200"));

                                    // Serial Port Check
                                    int readState = scr.checkSerialPortOpened();
                                    if (readState != RTN_COMM_OK) {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 연결 상태 체크해주시길 바랍니다.");
                                        Toast.makeText(DetailDealResult.this, "리더기 연결 상태 체크해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
                                        btnEnable();
                                        return;
                                    }

                                    scr.clearTxBuffer();

                                    writeBuffer = new char[44];
                                    writeBuffer[0] = 0x02; //Header ID
                                    writeBuffer[1] = func_code; //Command ID
                                    writeBuffer[2] = 0x00;
                                    writeBuffer[3] = 0x39; //Length(2)
                                    String sendstr = mTimeout; //Card 대기시간(2)
                                    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
                                    String strDate = sdfDate.format(new Date());
                                    sendstr = sendstr + strDate; //거래일시(14)
                                    sendstr = sendstr + mMoney; //거래금액(12)
                                    sendstr = sendstr + mCatid; //TID(10)
                                    sendstr = sendstr + sReaderApprtp; //거래종류(1)    //LJY20250904 : 8BIN/통합결제 적용
                                    System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 39);
                                    writeBuffer[43] = xor_sum(writeBuffer, 43);

                                    temp = new byte[44];
                                    for (i = 0; i < 44; i++) {
                                        temp[i] = (byte) writeBuffer[i];
                                    }
                                    scr.sendMsg(temp, temp.length);
                                    if (mDealtp.equals("10")) {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 신용취소 카드리딩해주세요.");
                                        PopupOpenEOT(DetailDealResult.this, "신용취소 카드리딩해주세요.");
                                    } else if (mDealtp.equals("21") == true) {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 현금영수증취소 카드리딩해주세요.");
                                        PopupOpenEOT(DetailDealResult.this, "현금영수증취소 카드리딩해주세요.");
                                    } else {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 카드리딩해주세요.");
                                        PopupOpenEOT(DetailDealResult.this, "카드리딩해주세요.");
                                    }

                                    scr.clearRxBuffer();
                                    handlerThread = new handler_thread(handler);
                                    handlerThread.start();
                                } else if (mSharedManager.getPreferences().getInt("Readertype", 0) == 2) //OKPOS
                                {
                                    mUart = new libUart();
                                    isrun = true;

                                    Arrays.fill(RECVBuf, (char) 0x00);
                                    Arrays.fill(encdata, (char) 0x00);
                                    Arrays.fill(icdata, (char) 0x00);

                                    initSerial();
//                                    func_code = 0x6C;     //LJY20250904 : 8BIN/통합결제 적용
                                    btnDisable();
                                    btnBack.setEnabled(false);

                                    writeBuffer = new char[44];
                                    writeBuffer[0] = 0x02; //Header ID
                                    writeBuffer[1] = func_code; //Command ID
                                    writeBuffer[2] = 0x00;
                                    writeBuffer[3] = 0x39; //Length(2)
                                    String sendstr = mTimeout; //Card 대기시간(2)
                                    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
                                    String strDate = sdfDate.format(new Date());
                                    sendstr = sendstr + strDate; //거래일시(14)
                                    sendstr = sendstr + mMoney; //거래금액(12)
                                    sendstr = sendstr + mCatid; //TID(10)
                                    sendstr = sendstr + sReaderApprtp; //거래종류(1)    //LJY20250904 : 8BIN/통합결제 적용
                                    System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 39);
                                    writeBuffer[43] = xor_sum(writeBuffer, 43);

                                    temp = new byte[44];
                                    for (i = 0; i < 44; i++) {
                                        temp[i] = (byte) writeBuffer[i];
                                    }
                                    if (mDealtp.equals("10")) {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 신용취소 카드리딩해주세요.");
                                        PopupOpenEOT(DetailDealResult.this, "신용취소 카드리딩해주세요.");
                                    } else if (mDealtp.equals("21") == true) {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 현금영수증취소 카드리딩해주세요.");
                                        PopupOpenEOT(DetailDealResult.this, "현금영수증취소 카드리딩해주세요.");
                                    } else {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 카드리딩해주세요.");
                                        PopupOpenEOT(DetailDealResult.this, "카드리딩해주세요.");
                                    }

                                    //LJY20201217 : 리더기 포트번호/통신속도 가변
                                    mUart.Init(mSharedManager.getPreferences().getInt("Portnum", 0));
                                    if (mUart.IsOpen(mSharedManager.getPreferences().getInt("Portnum", 0)) == false) {
                                        mUart.Open(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")), 8, 0, 1, true);
                                        mUart.SetBaudrate(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")));
                                    }
                                    mUart.QueueClear(mSharedManager.getPreferences().getInt("Portnum", 0));
                                    mUart.DataSend(mSharedManager.getPreferences().getInt("Portnum", 0), temp, temp.length);

                                    handlerThread = new handler_thread(handler);
                                    handlerThread.start();
                                } else {
                                    if (!SharedManager.isBizdown) {
                                        //LJY20200812 : 가맹점다운로드 예외처리
//                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 가맹점다운로드 해주시길 바랍니다.");
//                            Toast.makeText(DetailDealResult.this, "가맹점다운로드 해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
//                            btnEnable();
//                            return;
                                    }
                                    if (SharedManager.isStatus == false) {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 연결 상태 체크해주시길 바랍니다.");
                                        Toast.makeText(DetailDealResult.this, "리더기 연결 상태 체크해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
                                        btnEnable();
                                        return;
                                    }

                                    if (usbService != null) { // if UsbService was correctly binded, Send data
                                        isrun = true;

                                        handlerThread = new handler_thread(handler);
                                        handlerThread.start();

                                        Arrays.fill(RECVBuf, (char) 0x00);
                                        Arrays.fill(encdata, (char) 0x00);
                                        Arrays.fill(icdata, (char) 0x00);

                                        initSerial();
//                                    func_code = 0x6C;     //LJY20250904 : 8BIN/통합결제 적용
                                        btnDisable();
                                        btnBack.setEnabled(false);

                                        writeBuffer = new char[44];
                                        writeBuffer[0] = 0x02; //Header ID
                                        writeBuffer[1] = func_code; //Command ID
                                        writeBuffer[2] = 0x00;
                                        writeBuffer[3] = 0x39; //Length(2)
                                        String sendstr = mTimeout; //Card 대기시간(2)
                                        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
                                        String strDate = sdfDate.format(new Date());
                                        sendstr = sendstr + strDate; //거래일시(14)
                                        sendstr = sendstr + mMoney; //거래금액(12)
                                        sendstr = sendstr + mCatid; //TID(10)
                                        sendstr = sendstr + sReaderApprtp; //거래종류(1)    //LJY20250904 : 8BIN/통합결제 적용
                                        System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 39);
                                        writeBuffer[43] = xor_sum(writeBuffer, 43);

                                        temp = new byte[44];
                                        for (i = 0; i < 44; i++) {
                                            temp[i] = (byte) writeBuffer[i];
                                        }
                                        if (mDealtp.equals("10")) {
                                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 신용취소 카드리딩해주세요.");
                                            PopupOpenEOT(DetailDealResult.this, "신용취소 카드리딩해주세요.");
                                        } else if (mDealtp.equals("21") == true) {
                                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 현금영수증취소 카드리딩해주세요.");
                                            PopupOpenEOT(DetailDealResult.this, "현금영수증취소 카드리딩해주세요.");
                                        } else {
                                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 카드리딩해주세요.");
                                            PopupOpenEOT(DetailDealResult.this, "카드리딩해주세요.");
                                        }
                                        usbService.write(temp);
                                    } else {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                                        Toast.makeText(DetailDealResult.this, "USB 서비스 불가능합니다.", Toast.LENGTH_LONG).show();
                                        btnEnable();
                                        btnBack.setEnabled(true);
                                        return;
                                    }
                                }
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 취소불가! 취소거래입니다.");
                                Toast.makeText(DetailDealResult.this, "취소불가! 취소거래입니다.", Toast.LENGTH_LONG).show();
                                btnEnable();
                                btnBack.setEnabled(true);
                                return;
                            }
                        }
                    }
                });
                dlg.show();
            }
        });

        tvCnlTitle = (TextView)findViewById(R.id.tv_cnl_title); //LJY20221004 : tvCnlTitle 추가
        mServerip = mSharedManager.getPreferences().getString("Serverip", ""); //LJY20221004 : tvCnlTitle 컬러 변경
        if(mServerip.length() != 0 && mServerip.equals("211.33.136.19")) {
            tvCnlTitle.setText("거래상세내역 (테스트)");
            tvCnlTitle.setTextColor(Color.RED);
            btnReqCnl.setText("거래취소 (테스트)");
            btnReqCnl.setTextColor(Color.RED);
        } else {
            tvCnlTitle.setText("거래상세내역 (운영)");
            tvCnlTitle.setTextColor(Color.YELLOW);
            btnReqCnl.setText("거래취소 (운영)");
            btnReqCnl.setTextColor(Color.YELLOW);
        }
    }

    @Override
    //OSM20241209 : NVCAT 승인내역조회 탭 터치 안되는 이슈 수정
    //    public boolean dispatchTouchEvent(MotionEvent ev) {
    //        // 터치 이벤트를 무시하고 다음 이벤트로 전달하지 않음
    //        return true;
    //    }
    //

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) { //서명패드 결과
            if (resultCode == RESULT_OK) { //SignPad의 RESULT_OK
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 서명패드 OK 클릭");
                PopupClose();

                if (func_code == 0x6E || func_code == 0x9E) { //LJY20250904 : 8BIN/통합결제 적용
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 서명 결제 취소");

                    String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    PopupOpen(DetailDealResult.this, "FALLBACK VAN 취소 중입니다.");

                    PosClient posClient = new PosClient();
                    temp = new byte[4096];
                    //LJY20250904 : 서명 일부분 짤리는 부분 수정 (전문 길이 1505 > 1521)
                    if (mDealtp.equals("UP") == true) //은련
                    {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 은련취소");
                        sendBuff = ("1521CUP" + mCatid + strDate + "042030H1          " + mCatid + "F" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                    } else {
                        if (mDealgb.equals("0310")) //20200129 : 포인트거래
                        {
                            if(mDealtp.equals("10")) mDealtp = "30";

                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 포인트취소");
                            sendBuff = ("1521" + mMsgtxt + mCatid + strDate + "0420" + mDealtp + "H1          " + mCatid + "F" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                        } else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 신용취소");
                            sendBuff = ("1521HPS" + mCatid + strDate + "042030H1          " + mCatid + "F" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                        }
                    }
                    System.arraycopy(sendBuff, 0, temp, 0, sendBuff.length);
                    System.arraycopy(data.getByteArrayExtra("SIGN"), 2, temp, sendBuff.length, data.getByteArrayExtra("SIGN").length - 2);
                    if (bRelease)
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(temp)).substring(0, 56) + "*******************************************************************************************************************************" + (new String(temp)).substring(183, temp.length - 183));
                    else
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(temp));
                    if (mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
                        System.arraycopy(String.format("%04d", sendBuff.length).getBytes(), 0, temp, 0, 4);
                        recvBuff = posClient.service_line(mServerip, Integer.parseInt(mServerport), temp);
                    } else if(mSharedManager.getPreferences().getInt("Enctype", 0) == 0) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SEED 암복호화"); //OSM20250113
                        recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), temp);
                    }
                    else {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113
                        iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), temp, recvBuff);   //OSM20250113 : DES암복호화 함수 추가
                    }
                    InsertRecv(recvBuff);
                    PopupClose();
                } else {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 서명 결제 취소");

                    String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    PopupOpen(DetailDealResult.this, "IC VAN 취소 중입니다.");

                    PosClient posClient = new PosClient();
                    temp = new byte[4096];
                    //LJY20250904 : 서명 일부분 짤리는 부분 수정
                    //LJY20250904 : 통합결제 바코드 리딩 시 로직 추가 (매체구분 "B" && 결제구분 "B")
                    if (mDealtp.equals("UP")) //은련
                    {
                        if (Paygb[0] == 'B' && cMediagb[0] == 'B') {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 은련취소");
                            sendBuff = ("1521PRO" + mCatid + strDate + "042030H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                        }
                        else if(Paygb[0] == 'R' && CardBrand[0] == 'K') { //LJY20200713 : 동반위 JUST TOUCH
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF 은련취소(동반위)");
                            sendBuff = ("1778CUP" + mCatid + strDate + "042030H1          " + mCatid + "K" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                        }
                        else
                        if (Paygb[0] == 'I' || (Paygb[0] == 'R' && Integer.parseInt(new String(icdata, 0, 4)) > 0)) { //LJY20230818
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 은련취소");
                            sendBuff = ("1778CUP" + mCatid + strDate + "042030H1          " + mCatid + "I" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                        } else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS 은련취소");
                            sendBuff = ("1521CUP" + mCatid + strDate + "042030H1          " + mCatid + "A" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                        }
                    } else {
                        if (Paygb[0] == 'B' && cMediagb[0] == 'B') {
                            if (mDealgb.equals("0310")) //20200129 : 포인트거래
                            {
                                if (mDealtp.equals("10")) mDealtp = "30";

                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 포인트취소");
                                sendBuff = ("1521PRO" + mCatid + strDate + "0420" + mDealtp + "H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 신용취소");
                                sendBuff = ("1521PRO" + mCatid + strDate + "042030H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                            }
                        }
                        else if(Paygb[0] == 'R' && CardBrand[0] == 'K') { //LJY20200713 : 동반위 JUST TOUCH
                            if (mDealgb.equals("0310"))
                            {
                                if (mDealtp.equals("10")) mDealtp = "30";

                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF 포인트취소(동반위)");
                                sendBuff = ("1778" + mMsgtxt + mCatid + strDate + "0420" + mDealtp + "H1          " + mCatid + "K" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF 신용취소(동반위)");
                                sendBuff = ("1778HPS" + mCatid + strDate + "042030H1          " + mCatid + "K" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                            }
                        }
                        else
                        if (Paygb[0] == 'I' || (Paygb[0] == 'R' && Integer.parseInt(new String(icdata, 0, 4)) > 0)) { //LJY20230818
                            if (mDealgb.equals("0310")) //20200129 : 포인트거래
                            {
                                if (mDealtp.equals("10")) mDealtp = "30";

                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 포인트취소");
                                sendBuff = ("1778" + mMsgtxt + mCatid + strDate + "0420" + mDealtp + "H1          " + mCatid + "I" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 신용취소");
                                sendBuff = ("1778HPS" + mCatid + strDate + "042030H1          " + mCatid + "I" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                            }
                        }else {
                            if (mDealgb.equals("0310")) //20200129 : 포인트거래
                            {
                                if (mDealtp.equals("10")) mDealtp = "30";

                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS 포인트취소");
                                sendBuff = ("1521" + mMsgtxt + mCatid + strDate + "0420" + mDealtp + "H1          " + mCatid + "A" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS 신용취소");
                                sendBuff = ("1521HPS" + mCatid + strDate + "042030H1          " + mCatid + "A" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                            }
                        }
                    }
                    System.arraycopy(sendBuff, 0, temp, 0, sendBuff.length);
                    System.arraycopy(data.getByteArrayExtra("SIGN"), 2, temp, sendBuff.length, data.getByteArrayExtra("SIGN").length - 2);
                    if (Paygb[0] == 'I' || (Paygb[0] == 'R' && CardBrand[0] == 'K') || (Paygb[0] == 'R' && Integer.parseInt(new String(icdata, 0, 4)) > 0)) //LJY20230818 //LJY20200713 : 동반위 JUST TOUCH
                        System.arraycopy(new String(icdata).getBytes(), 0, temp, sendBuff.length + data.getByteArrayExtra("SIGN").length - 2, new String(icdata).length());
                    if (bRelease)
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(temp)).substring(0, 56) + "*******************************************************************************************************************************" + (new String(temp)).substring(183, temp.length - 183));
                    else
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(temp));
                    if (mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
                        System.arraycopy(String.format("%04d", sendBuff.length).getBytes(), 0, temp, 0, 4);
                        recvBuff = posClient.service_line(mServerip, Integer.parseInt(mServerport), temp);
                    } else if(mSharedManager.getPreferences().getInt("Enctype", 0) == 0) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SEED 암복호화"); //OSM20250113
                        recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), temp);
                    }
                    else {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113
                        iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), temp, recvBuff);   //OSM20250113 : DES암복호화 함수 추가
                    }
                    InsertRecv(recvBuff);
                    PopupClose();
                }
            } else {//SignPad의 RESULT_CANCEL
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 서명패드 취소 클릭");
                PopupClose();

                if (mSharedManager.getPreferences().getBoolean("Nosign", false)) {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 서명패드 오류시 결제");

                    if (func_code == 0x6E || func_code == 0x9E) { //LJY20250904 : 8BIN/통합결제 적용
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 서명패드 오류시 FALLBACK");

                        String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        PopupOpen(DetailDealResult.this, "FALLBACK VAN 취소 중입니다.");

                        PosClient posClient = new PosClient();
                        if (mDealtp.equals("UP") == true) //은련
                        {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 은련취소");
                            sendBuff = ("0437CUP" + mCatid + strDate + "042030H1          " + mCatid + "F" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "N").getBytes();
                        } else {
                            if (mDealgb.equals("0310")) //20200129 : 포인트거래
                            {
                                if (mDealtp.equals("10")) mDealtp = "30";

                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 포인트취소");
                                sendBuff = ("0437" + mMsgtxt + mCatid + strDate + "0420" + mDealtp + "H1          " + mCatid + "F" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "N").getBytes();
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 신용취소");
                                sendBuff = ("0437HPS" + mCatid + strDate + "042030H1          " + mCatid + "F" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "N").getBytes();
                            }
                        }
                        if (bRelease)
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(sendBuff)).substring(0, 56) + "*******************************************************************************************************************************" + (new String(sendBuff)).substring(183, sendBuff.length - 183));
                        else
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(sendBuff));
                        if (mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
                            System.arraycopy(String.format("%04d", sendBuff.length).getBytes(), 0, sendBuff, 0, 4);
                            recvBuff = posClient.service_line(mServerip, Integer.parseInt(mServerport), sendBuff);
                        } else if(mSharedManager.getPreferences().getInt("Enctype", 0) == 0) {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SEED 암복호화"); //OSM20250113
                            recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), sendBuff);
                        }
                        else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113
                            iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), sendBuff, recvBuff);   //OSM20250113 : DES암복호화 함수 추가
                        }
                        InsertRecv(recvBuff);
                        PopupClose();
                    } else if (func_code == 0x6C || func_code == 0x9C) { //LJY20250904 : 8BIN/통합결제 적용
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 서명패드 오류시 IC 결제");

                        String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        PopupOpen(DetailDealResult.this, "IC VAN 취소 중입니다.");

                        PosClient posClient = new PosClient();
                        //LJY20250904 : 통합결제 바코드 리딩 시 로직 추가 (매체구분 "B" && 결제구분 "B")
                        if (mDealtp.equals("UP") == true) //은련
                        {
                            if (Paygb[0] == 'B' && cMediagb[0] == 'B') {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 은련취소");
                                sendBuff = ("0437PRO" + mCatid + strDate + "042030H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                            } else
                            if(Paygb[0] == 'R' && CardBrand[0] == 'K') { //LJY20200713 : 동반위 JUST TOUCH
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF 은련취소(동반위)");
                                sendBuff = ("0694CUP" + mCatid + strDate + "042030H1          " + mCatid + "K" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                            }
                            else
                            if (Paygb[0] == 'I' || (Paygb[0] == 'R' && Integer.parseInt(new String(icdata, 0, 4)) > 0)) { //LJY20230818
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 은련취소");
                                sendBuff = ("0694CUP" + mCatid + strDate + "042030H1          " + mCatid + "I" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS 은련취소");
                                sendBuff = ("0437CUP" + mCatid + strDate + "042030H1          " + mCatid + "A" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                            }
                        } else {
                            if (Paygb[0] == 'B' && cMediagb[0] == 'B') {
                                if (mDealgb.equals("0310")) //20200129 : 포인트거래
                                {
                                    if (mDealtp.equals("10")) mDealtp = "30";

                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 포인트취소");
                                    sendBuff = ("0437PRO" + mCatid + strDate + "0420" + mDealtp + "H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 신용취소");
                                    sendBuff = ("0437PRO" + mCatid + strDate + "042030H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                                }
                            } else
                            if(Paygb[0] == 'R' && CardBrand[0] == 'K') { //LJY20200713 : 동반위 JUST TOUCH
                                if (mDealgb.equals("0310")) //20200129 : 포인트거래
                                {
                                    if (mDealtp.equals("10")) mDealtp = "30";

                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF 포인트취소(동반위)");
                                    sendBuff = ("0694" + mMsgtxt + mCatid + strDate + "0420" + mDealtp + "H1          " + mCatid + "K" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF 신용취소(동반위)");
                                    sendBuff = ("0694HPS" + mCatid + strDate + "042030H1          " + mCatid + "K" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                                }
                            }
                            else
                            if (Paygb[0] == 'I' || (Paygb[0] == 'R' && Integer.parseInt(new String(icdata, 0, 4)) > 0)) { //LJY20230818
                                if (mDealgb.equals("0310")) //20200129 : 포인트거래
                                {
                                    if (mDealtp.equals("10")) mDealtp = "30";

                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 포인트취소");
                                    sendBuff = ("0694" + mMsgtxt + mCatid + strDate + "0420" + mDealtp + "H1          " + mCatid + "I" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 신용취소");
                                    sendBuff = ("0694HPS" + mCatid + strDate + "042030H1          " + mCatid + "I" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                                }
                            }else {
                                if (mDealgb.equals("0310")) //20200129 : 포인트거래
                                {
                                    if (mDealtp.equals("10")) mDealtp = "30";

                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS 포인트취소");
                                    sendBuff = ("0437" + mMsgtxt + mCatid + strDate + "0420" + mDealtp + "H1          " + mCatid + "A" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS 신용취소");
                                    sendBuff = ("0437HPS" + mCatid + strDate + "042030H1          " + mCatid + "A" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                                }
                            }
                        }
                        if (bRelease)
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(sendBuff)).substring(0, 56) + "*******************************************************************************************************************************" + (new String(sendBuff)).substring(183, sendBuff.length - 183));
                        else
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(sendBuff));
                        if (mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
                            System.arraycopy(String.format("%04d", sendBuff.length).getBytes(), 0, sendBuff, 0, 4);
                            recvBuff = posClient.service_line(mServerip, Integer.parseInt(mServerport), sendBuff);
                        } else if(mSharedManager.getPreferences().getInt("Enctype", 0) == 0) {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SEED 암복호화"); //OSM20250113
                            recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), sendBuff);
                        }
                        else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113
                            iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), sendBuff, recvBuff);   //OSM20250113 : DES암복호화 함수 추가
                        }
                        InsertRecv(recvBuff);
                        PopupClose();
                    }
                } else {//서명 취소 버튼 클릭
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 서명취소 하였습니다.");
                    Toast.makeText(DetailDealResult.this, "서명취소 하였습니다.", Toast.LENGTH_LONG).show();
                    btnEnable();
                    btnBack.setEnabled(true);
                    return;
                }
            }
        } else if (requestCode == 2) //은련 PIN
        {
            if (resultCode == RESULT_OK) {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 은련PIN OK 버튼 클릭");
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PIN : " + data.getStringExtra("RESULT"));
                PopupClose();

                bEncPin = new byte[16];
                SignEnc nicesign = new SignEnc();

                if (data.getStringExtra("RESULT").length() > 0) {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PIN LENGTH OK!!");
                    int ret = nicesign.MakePinBlock("0000000000000000".getBytes(), data.getStringExtra("RESULT").getBytes(), bEncPin);
                    if (ret > 0) {
                        EncPin = new String(bEncPin);
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] ENC PIN : " + EncPin);

                        if (func_code == 0x6C || func_code == 0x9C) { //LJY20250904 : 8BIN/통합결제 적용
                            //LJY20250904 : 통합결제 바코드 리딩 시 로직 추가 (매체구분 "B" && 결제구분 "B")
                            if (mSharedManager.getPreferences().getBoolean("Nocvm", false) == false || (mSharedManager.getPreferences().getBoolean("Nocvm", false) && Long.parseLong(mMoney) > 50000)) {
                                if (Paygb[0] == 'B' && cMediagb[0] == 'B' && !IsBarcodeSign(new String(icdata))) {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 은련 노서명");
                                    PopupOpen(DetailDealResult.this, "IC VAN 취소 중입니다.");
                                    String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());

                                    PosClient posClient = new PosClient();
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 은련취소");
                                    sendBuff = ("0437PRO" + mCatid + strDate + "042030H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();

                                    if (bRelease)
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(sendBuff)).substring(0, 56) + "*******************************************************************************************************************************" + (new String(sendBuff)).substring(183, sendBuff.length - 183));
                                    else
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(sendBuff));

                                    if (mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
                                        System.arraycopy(String.format("%04d", sendBuff.length).getBytes(), 0, sendBuff, 0, 4);
                                        recvBuff = posClient.service_line(mServerip, Integer.parseInt(mServerport), sendBuff);
                                    } else if (mSharedManager.getPreferences().getInt("Enctype", 0) == 0) {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SEED 암복호화"); //OSM20250113
                                        recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), sendBuff);
                                    } else {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113
                                        iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), sendBuff, recvBuff);   //OSM20250113 : DES암복호화 함수 추가
                                    }
                                    InsertRecv(recvBuff);
                                    PopupClose();
                                }
                                else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 은련 서명");

                                    //LJY20220427 : 멀티패드 서명 연동
                                    if (mSharedManager.getPreferences().getInt("Readertype", 0) == 1 && mSharedManager.getPreferences().getBoolean("Signuse", false)) {
                                        if (usbService != null) { // if UsbService was correctly binded, Send data
                                            isrun = true;

                                            handlerThread = new handler_thread(handler);
                                            handlerThread.start();

                                            Arrays.fill(RECVBuf, (char) 0x00);
//                                        Arrays.fill(encdata, (char) 0x00);
//                                        Arrays.fill(icdata, (char) 0x00);

                                            initSerial();
                                            //isMultipad = true;
                                            btnDisable();
                                            isSign = true;
                                            btnBack.setEnabled(true);

                                            writeBuffer = new char[53];
                                            writeBuffer[0] = 0x02; //Header ID
                                            writeBuffer[1] = 0x42; //Command ID
                                            writeBuffer[2] = 0x00;
                                            writeBuffer[3] = 0x48; //Length(2)
                                            String sendstr = "19F316BA33A57729" + "  Please Sign!  " + "                "; //서명문구(48)
                                            System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 48);
                                            writeBuffer[52] = xor_sum(writeBuffer, 52);

                                            temp = new byte[53];
                                            for (int i = 0; i < 53; i++) {
                                                temp[i] = (byte) writeBuffer[i];
                                            }
                                            PopupOpenEOT(DetailDealResult.this, "서명 해주세요.");
                                            usbService.write(temp);
                                        } else {
                                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                                            Toast.makeText(DetailDealResult.this, "USB 서비스 불가능합니다.", Toast.LENGTH_LONG).show();
                                            btnEnable();
                                            PopupClose();
                                            btnBack.setEnabled(true);
                                            return;
                                        }
                                    } else
                                        //LJY20201005 : OKPOS 서명 연동
                                        if (mSharedManager.getPreferences().getInt("Readertype", 0) == 2) //OKPOS
                                        {
                                            mUart = new libUart();
                                            isrun = true;

                                            Arrays.fill(RECVBuf, (char) 0x00);
//                                    Arrays.fill(encdata, (char) 0x00);
//                                    Arrays.fill(icdata, (char) 0x00);

                                            initSerial();
                                            isSign = true;
                                            btnDisable();
                                            btnBack.setEnabled(false);

                                            writeBuffer = new char[53];
                                            writeBuffer[0] = 0x02; //Header ID
                                            writeBuffer[1] = 0x42; //Command ID
                                            writeBuffer[2] = 0x00;
                                            writeBuffer[3] = 0x48; //Length(2)
                                            String sendstr = "19F316BA33A57729" + "  Please Sign!  " + "                "; //서명문구(48)
                                            System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 48);
                                            writeBuffer[52] = xor_sum(writeBuffer, 52);

                                            temp = new byte[53];
                                            for (int i = 0; i < 53; i++) {
                                                temp[i] = (byte) writeBuffer[i];
                                            }
                                            PopupOpenEOT(DetailDealResult.this, "서명 해주세요.");

                                            //LJY20201217 : 포트번호/통신속도 가변
                                            mUart.Init(mSharedManager.getPreferences().getInt("sPortnum", 0));
                                            if (mUart.IsOpen(mSharedManager.getPreferences().getInt("sPortnum", 0)) == false) {
                                                mUart.Open(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")), 8, 0, 1, true);
                                                mUart.SetBaudrate(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")));
                                            }
                                            mUart.QueueClear(mSharedManager.getPreferences().getInt("sPortnum", 0));
                                            mUart.DataSend(mSharedManager.getPreferences().getInt("sPortnum", 0), temp, temp.length);

                                            handlerThread = new handler_thread(handler);
                                            handlerThread.start();
                                        } else {
                                            Intent intent = new Intent(DetailDealResult.this, SignPad.class);
                                            startActivityForResult(intent, 1);
                                        }
                                }
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 은련 노서명");
                                PopupOpen(DetailDealResult.this, "IC VAN 취소 중입니다.");
                                String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());

                                PosClient posClient = new PosClient();
                                if (Paygb[0] == 'B' && cMediagb[0] == 'B') {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 은련취소");
                                    sendBuff = ("0437PRO" + mCatid + strDate + "042030H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                                } else
                                if(Paygb[0] == 'R' && CardBrand[0] == 'K') { //LJY20200713 : 동반위 JUST TOUCH
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF 은련취소(동반위)");
                                    sendBuff = ("0694CUP" + mCatid + strDate + "042030H1          " + mCatid + "K" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                                }
                                else
                                if (Paygb[0] == 'I' || (Paygb[0] == 'R' && Integer.parseInt(new String(icdata, 0, 4)) > 0)) { //LJY20230818
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 은련취소");
                                    sendBuff = ("0694CUP" + mCatid + strDate + "042030H1          " + mCatid + "I" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS 은련취소");
                                    sendBuff = ("0437CUP" + mCatid + strDate + "042030H1          " + mCatid + "A" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                                }
                                if (bRelease)
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(sendBuff)).substring(0, 56) + "*******************************************************************************************************************************" + (new String(sendBuff)).substring(183, sendBuff.length - 183));
                                else
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(sendBuff));
                                if (mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
                                    System.arraycopy(String.format("%04d", sendBuff.length).getBytes(), 0, sendBuff, 0, 4);
                                    recvBuff = posClient.service_line(mServerip, Integer.parseInt(mServerport), sendBuff);
                                } else if(mSharedManager.getPreferences().getInt("Enctype", 0) == 0) {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SEED 암복호화"); //OSM20250113
                                    recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), sendBuff);
                                }
                                else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113
                                    iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), sendBuff, recvBuff);   //OSM20250113 : DES암복호화 함수 추가
                                }
                                InsertRecv(recvBuff);
                                PopupClose();
                            }
                        } else if (func_code == 0x6E || func_code == 0x9E) { //LJY20250904 : 8BIN/통합결제 적용
                            if (mSharedManager.getPreferences().getBoolean("Nocvm", false) == false || (mSharedManager.getPreferences().getBoolean("Nocvm", false) && Long.parseLong(mMoney) > 50000)) {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 은련 서명");

                                //LJY20220427 : 멀티패드 서명 연동
                                if( mSharedManager.getPreferences().getInt("Readertype", 0) == 1 && mSharedManager.getPreferences().getBoolean("Signuse", false) )
                                {
                                    if (usbService != null) { // if UsbService was correctly binded, Send data
                                        isrun = true;

                                        handlerThread = new handler_thread(handler);
                                        handlerThread.start();

                                        Arrays.fill(RECVBuf, (char) 0x00);
//                                        Arrays.fill(encdata, (char) 0x00);
//                                        Arrays.fill(icdata, (char) 0x00);

                                        initSerial();
                                        //isMultipad = true;
                                        btnDisable();
                                        isSign = true;
                                        btnBack.setEnabled(true);

                                        writeBuffer = new char[53];
                                        writeBuffer[0] = 0x02; //Header ID
                                        writeBuffer[1] = 0x42; //Command ID
                                        writeBuffer[2] = 0x00;
                                        writeBuffer[3] = 0x48; //Length(2)
                                        String sendstr = "19F316BA33A57729" + "  Please Sign!  " + "                "; //서명문구(48)
                                        System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 48);
                                        writeBuffer[52] = xor_sum(writeBuffer, 52);

                                        temp = new byte[53];
                                        for (int i = 0; i < 53; i++) {
                                            temp[i] = (byte) writeBuffer[i];
                                        }
                                        PopupOpenEOT(DetailDealResult.this, "서명 해주세요.");
                                        usbService.write(temp);
                                    } else {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                                        Toast.makeText(DetailDealResult.this, "USB 서비스 불가능합니다.", Toast.LENGTH_LONG).show();
                                        btnEnable();
                                        PopupClose();
                                        btnBack.setEnabled(true);
                                        return;
                                    }
                                }
                                else
                                    //LJY20201005 : OKPOS 서명 연동
                                    if (mSharedManager.getPreferences().getInt("Readertype", 0) == 2) //OKPOS
                                    {
                                        mUart = new libUart();
                                        isrun = true;

                                        Arrays.fill(RECVBuf, (char) 0x00);
//                                    Arrays.fill(encdata, (char) 0x00);
//                                    Arrays.fill(icdata, (char) 0x00);

                                        initSerial();
                                        isSign = true;
                                        btnDisable();
                                        btnBack.setEnabled(false);

                                        writeBuffer = new char[53];
                                        writeBuffer[0] = 0x02; //Header ID
                                        writeBuffer[1] = 0x42; //Command ID
                                        writeBuffer[2] = 0x00;
                                        writeBuffer[3] = 0x48; //Length(2)
                                        String sendstr = "19F316BA33A57729" + "  Please Sign!  " + "                "; //서명문구(48)
                                        System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 48);
                                        writeBuffer[52] = xor_sum(writeBuffer, 52);

                                        temp = new byte[53];
                                        for (int i = 0; i < 53; i++) {
                                            temp[i] = (byte) writeBuffer[i];
                                        }
                                        PopupOpenEOT(DetailDealResult.this, "서명 해주세요.");

                                        //LJY20201217 : 포트번호/통신속도 가변
                                        mUart.Init(mSharedManager.getPreferences().getInt("sPortnum", 0));
                                        if (mUart.IsOpen(mSharedManager.getPreferences().getInt("sPortnum", 0)) == false) {
                                            mUart.Open(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")), 8, 0, 1, true);
                                            mUart.SetBaudrate(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")));
                                        }
                                        mUart.QueueClear(mSharedManager.getPreferences().getInt("sPortnum", 0));
                                        mUart.DataSend(mSharedManager.getPreferences().getInt("sPortnum", 0), temp, temp.length);

                                        handlerThread = new handler_thread(handler);
                                        handlerThread.start();
                                    }
                                    else {
                                        Intent intent = new Intent(DetailDealResult.this, SignPad.class);
                                        startActivityForResult(intent, 1);
                                    }
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 은련 노서명");
                                PopupOpen(DetailDealResult.this, "FALLBACK VAN 취소 중입니다.");
                                String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());

                                PosClient posClient = new PosClient();
                                sendBuff = ("0437CUP" + mCatid + strDate + "042030H1          " + mCatid + "F" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "N").getBytes();
                                if (bRelease)
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(sendBuff)).substring(0, 56) + "*******************************************************************************************************************************" + (new String(sendBuff)).substring(183, sendBuff.length - 183));
                                else
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(sendBuff));
                                if (mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
                                    System.arraycopy(String.format("%04d", sendBuff.length).getBytes(), 0, sendBuff, 0, 4);
                                    recvBuff = posClient.service_line(mServerip, Integer.parseInt(mServerport), sendBuff);
                                } else if(mSharedManager.getPreferences().getInt("Enctype", 0) == 0) {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SEED 암복호화"); //OSM20250113
                                    recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), sendBuff);
                                }
                                else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113
                                    iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), sendBuff, recvBuff);   //OSM20250113 : DES암복호화 함수 추가
                                }
                                InsertRecv(recvBuff);
                                PopupClose();
                            }
                        }
                    } else {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PIN 암호화 실패");
                        Toast.makeText(DetailDealResult.this, "PIN 암호화 실패!!", Toast.LENGTH_LONG).show();
                        btnEnable();
                        btnBack.setEnabled(true);
                        return;
                    }
                } else {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 입력된 PIN 데이터가 없습니다.");
                    Toast.makeText(DetailDealResult.this, "입력된 PIN 데이터가 없습니다.", Toast.LENGTH_LONG).show();
                    btnEnable();
                    btnBack.setEnabled(true);
                    return;
                }
            } else {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PIN 입력 취소하셨습니다.");
                Toast.makeText(DetailDealResult.this, "PIN 입력 취소하셨습니다.", Toast.LENGTH_LONG).show();
                btnEnable();
                btnBack.setEnabled(true);
                return;
            }
        } else if (requestCode == 3) //현금
        {
            if (resultCode == RESULT_OK) //식별번호입력완료
            {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 현금PIN OK 버튼 클릭");
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PIN : " + data.getStringExtra("RESULT"));

                if (data.getStringExtra("RESULT").length() > 0) {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PIN LENGTH OK!!");
                    PopupOpen(DetailDealResult.this, "현금영수증 키인 VAN 취소 중입니다.");

                    String cashnum = "37" + data.getStringExtra("RESULT") + "=";
                    String space = "                                                                                                                                                      ";
                    String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());

                    PosClient posClient = new PosClient();
                    sendBuff = ("0437HPS" + mCatid + strDate + "042021H1          " + mCatid + "@" + cashnum + space.substring(0, 127 - cashnum.length()) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(1, 9) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                    if (bRelease)
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(sendBuff)).substring(0, 56) + "*******************************************************************************************************************************" + (new String(sendBuff)).substring(183, sendBuff.length - 183));
                    else
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(sendBuff));
                    if (mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
                        System.arraycopy(String.format("%04d", sendBuff.length).getBytes(), 0, sendBuff, 0, 4);
                        recvBuff = posClient.service_line(mServerip, Integer.parseInt(mServerport), sendBuff);
                    } else if(mSharedManager.getPreferences().getInt("Enctype", 0) == 0) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SEED 암복호화"); //OSM20250113
                        recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), sendBuff);
                    }
                    else {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113
                        iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), sendBuff, recvBuff);   //OSM20250113 : DES암복호화 함수 추가
                    }
                    InsertRecv(recvBuff);
                    PopupClose();
                } else {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 입력된 식별번호가 없습니다.");
                    Toast.makeText(DetailDealResult.this, "입력된 식별번호가 없습니다.", Toast.LENGTH_LONG).show();
                    btnEnable();
                    btnBack.setEnabled(true);
                    return;
                }
            } else {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 식별번호 입력 취소 하셨습니다.");
                Toast.makeText(DetailDealResult.this, "식별번호 입력 취소 하셨습니다.", Toast.LENGTH_LONG).show();
                btnEnable();
                btnBack.setEnabled(true);
                return;
            }
        }
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            isrun = false;
            PopupClose();

            if (status == 2) {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 시리얼 통신 타임아웃");
                Toast.makeText(DetailDealResult.this, "시리얼 통신 타임아웃", Toast.LENGTH_LONG).show();


                //OSM20250902 : 타임아웃일 때 리더기로 EOT 전송
                byte[] EOT = new byte[1];
                EOT[0] = 0x04;

                if (SharedManager.getInstance(DetailDealResult.this).getPreferences().getInt("Readertype", 0) == 3) //POSBANK
                    scr.sendEot();
                else if (SharedManager.getInstance(DetailDealResult.this).getPreferences().getInt("Readertype", 0) == 2) //OKPOS
                {
                    if (isMultipad || isSign)
                        mUart.DataSend(mSharedManager.getPreferences().getInt("sPortnum", 0), EOT, EOT.length);
                    else
                        mUart.DataSend(mSharedManager.getPreferences().getInt("Portnum", 0), EOT, EOT.length);
                } else
                    usbService.write(EOT);

                btnEnable();
                btnBack.setEnabled(true);
                return;
            }
            status = 0;

            if (bRelease == false) //20200108LJY : 디버깅로그
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 시리얼데이터 : [" + new String(RECVBuf) + "]");

            if (isMultipad) {
                isMultipad = false;
                if (RECVBuf[0] == 0x04 || (RECVBuf[0] == 0x00 && RECVBuf[4] == 0xCD)) //LJY20220520 : CD추가 //EOT 수신
                {
                    SharedManager.LogDebug(bLogUse, "debugjy", "암호화 PIN EOT 수신");
                    Toast.makeText(DetailDealResult.this, "[NVCAT] 암호화 PIN EOT 수신", Toast.LENGTH_SHORT).show();
                    btnEnable();
                    btnBack.setEnabled(true);
                    PopupClose();
                    return;
                } else {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 암호화 PIN 수신 정상");
                    EncPin = new String(RECVBuf, 4, 16);

                    if(String.format("%02X", RECVBuf[4] & 0xff).equals("9F"))
                    {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 멀티패드 아닙니다.");
                        Toast.makeText(DetailDealResult.this, "[NVCAT] 멀티패드 아닙니다.", Toast.LENGTH_SHORT).show();
                        btnEnable();
                        btnBack.setEnabled(true);
                        PopupClose();
                        return;
                    }

                    CUPfunc();
                    return;
                }
            }
            else if (isSign) { //LJY20201005 : OKPOS 서명 연동
                isSign = false;

                //LJY20220427 : 멀티패드 서명 연동 취소 예외 처리
                if(RECVBuf[0] == 0x04 || (RECVBuf[0] == 0x00 && RECVBuf[4] == 0xCD)) //LJY20220520 : CD추가 //EOT 수신
                {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 서명패드 EOT 수신");
                    Toast.makeText(DetailDealResult.this, "서명패드 EOT 수신", Toast.LENGTH_SHORT).show();
                    btnEnable();
                    PopupClose();
                    return;
                }
//                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 0x42 : " + new String(RECVBuf));
                String SignData = "";
                for(int kk=2; kk<RECVBuf.length-2; kk++) {
                    SignData = SignData + String.format("%02X", RECVBuf[kk] & 0xff);
                }
                SignData = SignData.substring(0, 4) + SignData.substring(4, Integer.parseInt(SignData.substring(0, 4))*2+4);
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SignData : " + SignData);

                if (func_code == 0x6E || func_code == 0x9E) { //LJY20250904 : 8BIN/통합결제 적용
                    String SignLen = String.format("%04d", Integer.parseInt(SignData.substring(0, 4)) + 34);
                    String TotalLen = String.format("%04d", 475 + Integer.parseInt(SignData.substring(0, 4)));

                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 서명 결제 취소");

                    String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    PopupOpen(DetailDealResult.this, "FALLBACK VAN 취소 중입니다.");

                    PosClient posClient = new PosClient();
                    temp = new byte[4096];
                    if (mDealtp.equals("UP") == true) //은련
                    {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 은련취소");
                        sendBuff = (TotalLen + "CUP" + mCatid + strDate + "042030H1          " + mCatid + "F" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                    } else {
                        if (mDealgb.equals("0310")) //20200129 : 포인트거래
                        {
                            if(mDealtp.equals("10")) mDealtp = "30";

                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 포인트취소");
                            sendBuff = (TotalLen + mMsgtxt + mCatid + strDate + "0420" + mDealtp + "H1          " + mCatid + "F" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                        } else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 신용취소");
                            sendBuff = (TotalLen + "HPS" + mCatid + strDate + "042030H1          " + mCatid + "F" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                        }
                    }
                    System.arraycopy(sendBuff, 0, temp, 0, sendBuff.length);
                    System.arraycopy(stringTobytes(SignData), 2, temp, sendBuff.length, stringTobytes(SignData).length - 2);
                    if (bRelease)
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(temp)).substring(0, 56) + "*******************************************************************************************************************************" + (new String(temp)).substring(183, temp.length - 183));
                    else
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(temp));
                    if (mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
                        System.arraycopy(String.format("%04d", sendBuff.length).getBytes(), 0, temp, 0, 4);
                        recvBuff = posClient.service_line(mServerip, Integer.parseInt(mServerport), temp);
                    } else if(mSharedManager.getPreferences().getInt("Enctype", 0) == 0) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SEED 암복호화"); //OSM20250113
                        recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), temp);
                    }
                    else {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113
                        iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), temp, recvBuff);   //OSM20250113 : DES암복호화 함수 추가
                    }
                    InsertRecv(recvBuff);
                    PopupClose();
                }
                else {
                    String SignLen = String.format("%04d", Integer.parseInt(SignData.substring(0, 4)) + 34);
                    String TotalLen = String.format("%04d", 475 + 257 + Integer.parseInt(SignData.substring(0, 4)));
                    String TotalLenSwipe = String.format("%04d", 475 + Integer.parseInt(SignData.substring(0, 4)));

                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 서명 결제 취소");

                    String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    PopupOpen(DetailDealResult.this, "IC VAN 취소 중입니다.");

                    PosClient posClient = new PosClient();
                    temp = new byte[4096];
                    //LJY20250904 : 통합결제 바코드 리딩 시 로직 추가 (매체구분 "B" && 결제구분 "B")
                    if (mDealtp.equals("UP")) //은련
                    {
                        if (Paygb[0] == 'B' && cMediagb[0] == 'B') {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS 은련취소");
                            sendBuff = (TotalLenSwipe + "PRO" + mCatid + strDate + "042030H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                        } else
                        if(Paygb[0] == 'R' && CardBrand[0] == 'K') { //LJY20200713 : 동반위 JUST TOUCH
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF 은련취소(동반위)");
                            sendBuff = (TotalLen + "CUP" + mCatid + strDate + "042030H1          " + mCatid + "K" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                        }
                        else
                        if (Paygb[0] == 'I' || (Paygb[0] == 'R' && Integer.parseInt(new String(icdata, 0, 4)) > 0)) { //LJY20230818
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 은련취소");
                            sendBuff = (TotalLen + "CUP" + mCatid + strDate + "042030H1          " + mCatid + "I" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                        } else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS 은련취소");
                            sendBuff = (TotalLenSwipe + "CUP" + mCatid + strDate + "042030H1          " + mCatid + "A" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                        }
                    } else {
                        if (Paygb[0] == 'B' && cMediagb[0] == 'B') {
                            if (mDealgb.equals("0310")) //20200129 : 포인트거래
                            {
                                if (mDealtp.equals("10")) mDealtp = "30";

                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 포인트취소");
                                sendBuff = (TotalLenSwipe + "PRO" + mCatid + strDate + "0420" + mDealtp + "H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 신용취소");
                                sendBuff = (TotalLenSwipe + "PRO" + mCatid + strDate + "042030H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                            }
                        } else
                        if(Paygb[0] == 'R' && CardBrand[0] == 'K') { //LJY20200713 : 동반위 JUST TOUCH
                            if (mDealgb.equals("0310"))
                            {
                                if (mDealtp.equals("10")) mDealtp = "30";

                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF 포인트취소(동반위)");
                                sendBuff = (TotalLen + mMsgtxt + mCatid + strDate + "0420" + mDealtp + "H1          " + mCatid + "K" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF 신용취소(동반위)");
                                sendBuff = (TotalLen + "HPS" + mCatid + strDate + "042030H1          " + mCatid + "K" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                            }
                        }
                        else
                        if (Paygb[0] == 'I' || (Paygb[0] == 'R' && Integer.parseInt(new String(icdata, 0, 4)) > 0)) { //LJY20230818
                            if (mDealgb.equals("0310")) //20200129 : 포인트거래
                            {
                                if (mDealtp.equals("10")) mDealtp = "30";

                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 포인트취소");
                                sendBuff = (TotalLen + mMsgtxt + mCatid + strDate + "0420" + mDealtp + "H1          " + mCatid + "I" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 신용취소");
                                sendBuff = (TotalLen + "HPS" + mCatid + strDate + "042030H1          " + mCatid + "I" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                            }
                        }else {
                            if (mDealgb.equals("0310")) //20200129 : 포인트거래
                            {
                                if (mDealtp.equals("10")) mDealtp = "30";

                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS 포인트취소");
                                sendBuff = (TotalLenSwipe + mMsgtxt + mCatid + strDate + "0420" + mDealtp + "H1          " + mCatid + "A" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS 신용취소");
                                sendBuff = (TotalLenSwipe + "HPS" + mCatid + strDate + "042030H1          " + mCatid + "A" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                            }
                        }
                    }
                    System.arraycopy(sendBuff, 0, temp, 0, sendBuff.length);
                    System.arraycopy(stringTobytes(SignData), 2, temp, sendBuff.length, stringTobytes(SignData).length - 2);
                    if (Paygb[0] == 'I' || (Paygb[0] == 'R' && CardBrand[0] == 'K') || (Paygb[0] == 'R' && Integer.parseInt(new String(icdata, 0, 4)) > 0)) //LJY20230818 //LJY20200713 : 동반위 JUST TOUCH
                        System.arraycopy(new String(icdata).getBytes(), 0, temp, sendBuff.length + stringTobytes(SignData).length - 2, new String(icdata).length());
                    if (bRelease)
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(temp)).substring(0, 56) + "*******************************************************************************************************************************" + (new String(temp)).substring(183, temp.length - 183));
                    else
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(temp));
                    if (mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
                        System.arraycopy(String.format("%04d", sendBuff.length).getBytes(), 0, temp, 0, 4);
                        recvBuff = posClient.service_line(mServerip, Integer.parseInt(mServerport), temp);
                    } else if(mSharedManager.getPreferences().getInt("Enctype", 0) == 0) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SEED 암복호화"); //OSM20250113
                        recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), temp);
                    }
                    else {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113
                        iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), temp, recvBuff);   //OSM20250113 : DES암복호화 함수 추가
                    }
                    InsertRecv(recvBuff);
                    PopupClose();
                }
            } else if (func_code == 0x6E || func_code == 0x9E) { //LJY20250904 : 8BIN/통합결제 적용
                String errcode = String.format("%02X", RECVBuf[4] & 0xff);
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 에러코드 : " + errcode);

                if (errcode.equals("00")) { //FALLBACK 카드리딩 정상
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 수신 정상");

                    System.arraycopy(RECVBuf, 9, encdata, 0, 104);
                    System.arraycopy(RECVBuf, 123, encdata, 104, 23);
                    System.arraycopy(RECVBuf, 5, icdata, 0, 2);

                    if(CompareKsn() == -1) {    //LJY20260109 : KSN 체크
                        Toast.makeText(DetailDealResult.this, "중복 거래 방지", Toast.LENGTH_LONG).show();
                        btnEnable();
                        btnBack.setEnabled(true);
                        PopupClose();
                        return;
                    }

                    String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

//                    if (mDealtp.equals("UP") == true) //은련PIN
                    if(false) //LJY20230713 : 은련취소 시 PIN 요청 안함
                    {
                        if (mSharedManager.getPreferences().getInt("Readertype", 0) == 1) //멀티패드
                        {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK - 은련");
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 암호화 PIN 요청입니다.");

                            if (usbService != null) { // if UsbService was correctly binded, Send data
                                isrun = true;

                                handlerThread = new handler_thread(handler);
                                handlerThread.start();

                                Arrays.fill(RECVBuf, (char) 0x00);
//                                Arrays.fill(encdata, (char) 0x00);
//                                Arrays.fill(icdata, (char) 0x00);

                                initSerial();
                                btnDisable();
                                isMultipad = true;

                                writeBuffer = new char[37];
                                writeBuffer[0] = 0x02; //Header ID
                                writeBuffer[1] = 0x43; //Command ID
                                writeBuffer[2] = 0x00;
                                writeBuffer[3] = 0x32; //Length(2)
                                writeBuffer[4] = 0x20;
                                writeBuffer[5] = 0x20;
                                writeBuffer[6] = 0x20;
                                writeBuffer[7] = 0x20;
                                writeBuffer[8] = 0x20;
                                writeBuffer[9] = 0x20;
                                writeBuffer[10] = 0x20;
                                writeBuffer[11] = 0x20;
                                writeBuffer[12] = 0x20;
                                writeBuffer[13] = 0x20;
                                writeBuffer[14] = 0x20;
                                writeBuffer[15] = 0x20;
                                writeBuffer[16] = 0x20;
                                writeBuffer[17] = 0x20;
                                writeBuffer[18] = 0x20;
                                writeBuffer[19] = 0x20;
                                writeBuffer[20] = 0x30;
                                writeBuffer[21] = 0x30;
                                writeBuffer[22] = 0x30;
                                writeBuffer[23] = 0x30;
                                writeBuffer[24] = 0x30;
                                writeBuffer[25] = 0x30;
                                writeBuffer[26] = 0x30;
                                writeBuffer[27] = 0x30;
                                writeBuffer[28] = 0x30;
                                writeBuffer[29] = 0x30;
                                writeBuffer[30] = 0x30;
                                writeBuffer[31] = 0x30;
                                writeBuffer[32] = 0x30;
                                writeBuffer[33] = 0x30;
                                writeBuffer[34] = 0x30;
                                writeBuffer[35] = 0x30;
                                writeBuffer[36] = 0x73;

                                temp = new byte[37];
                                for (int i = 0; i < 37; i++) {
                                    temp[i] = (byte) writeBuffer[i];
                                }
                                PopupOpenEOT(DetailDealResult.this, "암호화 PIN 입력해주세요.");
                                usbService.write(temp);
//                                btnBack.setEnabled(true);
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                                Toast.makeText(DetailDealResult.this, "USB 서비스 불가능합니다.", Toast.LENGTH_LONG).show();
                                btnEnable();
                                btnBack.setEnabled(true);
                                PopupClose();
                                return;
                            }
                        }
                        else if(mSharedManager.getPreferences().getInt("Readertype", 0) == 2) { //LJY20201005 : OKPOS 은련 PIN 연동
                            mUart = new libUart();
                            isrun = true;

                            Arrays.fill(RECVBuf, (char) 0x00);
//                            Arrays.fill(encdata, (char) 0x00);
//                            Arrays.fill(icdata, (char) 0x00);

                            initSerial();
                            btnDisable();
                            isMultipad = true;

                            writeBuffer = new char[37];
                            writeBuffer[0] = 0x02; //Header ID
                            writeBuffer[1] = 0x43; //Command ID
                            writeBuffer[2] = 0x00;
                            writeBuffer[3] = 0x32; //Length(2)
                            writeBuffer[4] = 0x20;
                            writeBuffer[5] = 0x20;
                            writeBuffer[6] = 0x20;
                            writeBuffer[7] = 0x20;
                            writeBuffer[8] = 0x20;
                            writeBuffer[9] = 0x20;
                            writeBuffer[10] = 0x20;
                            writeBuffer[11] = 0x20;
                            writeBuffer[12] = 0x20;
                            writeBuffer[13] = 0x20;
                            writeBuffer[14] = 0x20;
                            writeBuffer[15] = 0x20;
                            writeBuffer[16] = 0x20;
                            writeBuffer[17] = 0x20;
                            writeBuffer[18] = 0x20;
                            writeBuffer[19] = 0x20;
                            writeBuffer[20] = 0x30;
                            writeBuffer[21] = 0x30;
                            writeBuffer[22] = 0x30;
                            writeBuffer[23] = 0x30;
                            writeBuffer[24] = 0x30;
                            writeBuffer[25] = 0x30;
                            writeBuffer[26] = 0x30;
                            writeBuffer[27] = 0x30;
                            writeBuffer[28] = 0x30;
                            writeBuffer[29] = 0x30;
                            writeBuffer[30] = 0x30;
                            writeBuffer[31] = 0x30;
                            writeBuffer[32] = 0x30;
                            writeBuffer[33] = 0x30;
                            writeBuffer[34] = 0x30;
                            writeBuffer[35] = 0x30;
                            writeBuffer[36] = 0x73;

                            temp = new byte[37];
                            for (int i = 0; i < 37; i++) {
                                temp[i] = (byte) writeBuffer[i];
                            }
                            PopupOpenEOT(DetailDealResult.this, "암호화 PIN 입력해주세요.");

                            //LJY20201217 : 포트번호/통신속도 가변
                            mUart.Init(mSharedManager.getPreferences().getInt("sPortnum", 0));
                            if (mUart.IsOpen(mSharedManager.getPreferences().getInt("sPortnum", 0)) == false) {
                                mUart.Open(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")), 8, 0, 1, true);
                                mUart.SetBaudrate(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")));
                            }
                            mUart.QueueClear(mSharedManager.getPreferences().getInt("sPortnum", 0));
                            mUart.DataSend(mSharedManager.getPreferences().getInt("sPortnum", 0), temp, temp.length);

                            handlerThread = new handler_thread(handler);
                            handlerThread.start();
                        }
                        else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK - 은련 (PIN 사용 안됨)");
                            Toast.makeText(DetailDealResult.this, "FALLBACK - 은련 (PIN 사용 안됨)", Toast.LENGTH_LONG).show();
                            btnEnable();
                            btnBack.setEnabled(true);
                            PopupClose();
                            return; //TTA요청 : 은련터치 막아야 됨
                        }
                        return;
                    }

                    //신용이면서 서명
                    if ((mSharedManager.getPreferences().getBoolean("Nocvm", false) == false || (mSharedManager.getPreferences().getBoolean("Nocvm", false) && Long.parseLong(mMoney) > 50000)) && ((mDealtp.equals("10") || mDealtp.equals("UP")) && mDealgb.equals("0210"))) { //LJY20230713 : 은련 추가
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK - 서명");

                        //LJY20220427 : 멀티패드 서명 연동
                        if( mSharedManager.getPreferences().getInt("Readertype", 0) == 1 && mSharedManager.getPreferences().getBoolean("Signuse", false) )
                        {
                            if (usbService != null) { // if UsbService was correctly binded, Send data
                                isrun = true;

                                handlerThread = new handler_thread(handler);
                                handlerThread.start();

                                Arrays.fill(RECVBuf, (char) 0x00);
//                                        Arrays.fill(encdata, (char) 0x00);
//                                        Arrays.fill(icdata, (char) 0x00);

                                initSerial();
                                //isMultipad = true;
                                btnDisable();
                                isSign = true;
                                btnBack.setEnabled(true);

                                writeBuffer = new char[53];
                                writeBuffer[0] = 0x02; //Header ID
                                writeBuffer[1] = 0x42; //Command ID
                                writeBuffer[2] = 0x00;
                                writeBuffer[3] = 0x48; //Length(2)
                                String sendstr = "19F316BA33A57729" + "  Please Sign!  " + "                "; //서명문구(48)
                                System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 48);
                                writeBuffer[52] = xor_sum(writeBuffer, 52);

                                temp = new byte[53];
                                for (int i = 0; i < 53; i++) {
                                    temp[i] = (byte) writeBuffer[i];
                                }
                                PopupOpenEOT(DetailDealResult.this, "서명 해주세요.");
                                usbService.write(temp);
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                                Toast.makeText(DetailDealResult.this, "USB 서비스 불가능합니다.", Toast.LENGTH_LONG).show();
                                btnEnable();
                                PopupClose();
                                btnBack.setEnabled(true);
                                return;
                            }
                        }
                        else
                            //LJY20201005 : OKPOS 서명 연동
                            if (mSharedManager.getPreferences().getInt("Readertype", 0) == 2) //OKPOS
                            {
                                mUart = new libUart();
                                isrun = true;

                                Arrays.fill(RECVBuf, (char) 0x00);
//                            Arrays.fill(encdata, (char) 0x00);
//                            Arrays.fill(icdata, (char) 0x00);

                                initSerial();
                                isSign = true;
                                btnDisable();
                                btnBack.setEnabled(false);

                                writeBuffer = new char[53];
                                writeBuffer[0] = 0x02; //Header ID
                                writeBuffer[1] = 0x42; //Command ID
                                writeBuffer[2] = 0x00;
                                writeBuffer[3] = 0x48; //Length(2)
                                String sendstr = "19F316BA33A57729" + "  Please Sign!  " + "                "; //서명문구(48)
                                System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 48);
                                writeBuffer[52] = xor_sum(writeBuffer, 52);

                                temp = new byte[53];
                                for (int i = 0; i < 53; i++) {
                                    temp[i] = (byte) writeBuffer[i];
                                }
                                PopupOpenEOT(DetailDealResult.this, "서명 해주세요.");

                                //LJY20201217 : 포트번호/통신속도 가변
                                mUart.Init(mSharedManager.getPreferences().getInt("sPortnum", 0));
                                if (mUart.IsOpen(mSharedManager.getPreferences().getInt("sPortnum", 0)) == false) {
                                    mUart.Open(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")), 8, 0, 1, true);
                                    mUart.SetBaudrate(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")));
                                }
                                mUart.QueueClear(mSharedManager.getPreferences().getInt("sPortnum", 0));
                                mUart.DataSend(mSharedManager.getPreferences().getInt("sPortnum", 0), temp, temp.length);

                                handlerThread = new handler_thread(handler);
                                handlerThread.start();
                            }
                            else {
                                Intent intent = new Intent(DetailDealResult.this, SignPad.class);
                                startActivityForResult(intent, 1);
                            }
                    } else { //노서명
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK - 노서명");
                        PopupOpen(DetailDealResult.this, "FALLBACK VAN 취소 중입니다.");

                        PosClient posClient = new PosClient();
                        if (mDealtp.equals("21")) {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK - 현금취소");
                            sendBuff = ("0437HPS" + mCatid + strDate + "042021H1          " + mCatid + "F" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(1, 9) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "N").getBytes();
                        } else {
                            if (mDealgb.equals("0330")) //20200131 : 멤버쉽거래
                            {
                                if(mDealtp.equals("10")) mDealtp = "30";

                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 멤버쉽취소");
                                sendBuff = ("0343" + mMsgtxt + mCatid + strDate + "0420" + mDealtp + "H1          " + mCatid + mTax + mBongsa + new SimpleDateFormat("YYYYMMddHHmmss").format(new Date()) + "F" + new String(encdata, 0, 127) + mMoney.substring(3, 12) + mHalbu + mApprno.substring(0, 12) + mApprdate.substring(0, 6) + "                                " + mFiller + " ").getBytes();
                            }
                            else
                            if (mDealgb.equals("0310")) //20200129 : 포인트거래
                            {
                                if (mDealtp.equals("10")) mDealtp = "30";

                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK - 포인트취소");
                                sendBuff = ("0437" + mMsgtxt + mCatid + strDate + "0420" + mDealtp + "H1          " + mCatid + "F" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "N").getBytes();
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK - 신용취소");
                                sendBuff = ("0437HPS" + mCatid + strDate + "042030H1          " + mCatid + "F" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "N").getBytes();
                            }
                        }
                        if (bRelease)
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(sendBuff)).substring(0, 56) + "*******************************************************************************************************************************" + (new String(sendBuff)).substring(183, sendBuff.length - 183));
                        else
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(sendBuff));
                        if (mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
                            System.arraycopy(String.format("%04d", sendBuff.length).getBytes(), 0, sendBuff, 0, 4);
                            recvBuff = posClient.service_line(mServerip, Integer.parseInt(mServerport), sendBuff);
                        } else if(mSharedManager.getPreferences().getInt("Enctype", 0) == 0) {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SEED 암복호화"); //OSM20250113
                            recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), sendBuff);
                        }
                        else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113
                            iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), sendBuff, recvBuff);   //OSM20250113 : DES암복호화 함수 추가
                        }
                        InsertRecv(recvBuff);
                        PopupClose();
                    }
                } else {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 에러코드 : " + errcode);
                    Toast.makeText(DetailDealResult.this, "FALLBACK 에러코드 : " + errcode, Toast.LENGTH_SHORT).show();
                    btnEnable();
                    btnBack.setEnabled(true);
                    return;
                }
            } else if (func_code == 0x6C || func_code == 0x9C) { //LJY20250904 : 8BIN/통합결제 적용 //IC카드리딩
                String errcode = String.format("%02X", RECVBuf[4] & 0xff);
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 에러코드 : " + errcode);

                if (errcode.equals("00")) {//IC카드리딩 정상
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 수신 정상");

                    System.arraycopy(RECVBuf, 12, encdata, 0, 104);
                    System.arraycopy(RECVBuf, 126, encdata, 104, 23);
                    System.arraycopy(RECVBuf, 6, Paygb, 0, 1); //결제구분 : "I":IC, "M":MSR
                    System.arraycopy(RECVBuf, 8, CardBrand, 0, 1); //LJY20200713 : 동반위 JUST TOUCH
                    System.arraycopy(RECVBuf, 9, CardCvm, 0, 1); //LJY20230713 : 은련PIN 체크
                    //LJY20250904 : 통합결제 바코드 리딩 시 로직 추가 (매체구분 "B" && 결제구분 "B")
                    System.arraycopy(RECVBuf, 5, cMediagb, 0, 1); //매체구분 : Mobile – “M”, Plastic – “P”, 바코드/QR – “B”

                    if(CompareKsn() == -1) {    //LJY20260109 : KSN 체크
                        Toast.makeText(DetailDealResult.this, "중복 거래 방지", Toast.LENGTH_LONG).show();
                        btnBack.setEnabled(true);
                        btnEnable();
                        return;
                    }

                    if(func_code == 0x6C)   System.arraycopy(RECVBuf, 180, icdata, 0, 257); //LJY20250904
                    else {
                        char[] cFillerLen = new char[4];
                        System.arraycopy(RECVBuf, 244, cFillerLen, 0, 4);
                        int iFillerLen = Integer.parseInt(new String(cFillerLen).trim());

                        System.arraycopy(RECVBuf, 244+4+iFillerLen, icdata, 0, 257);
                    }

                    String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

//                    if (mDealtp.equals("UP") && (CardBrand[0] == 'C' && CardCvm[0] == '1')) //LJY20230706 : 은련PIN 체크 //은련PIN
                    if(false) //LJY20230713 : 은련취소 시 PIN 요청 안함
                    {
                        if (mSharedManager.getPreferences().getInt("Readertype", 0) == 1) //멀티패드
                        {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC - 은련");
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 암호화 PIN 요청입니다.");

                            if (usbService != null) { // if UsbService was correctly binded, Send data
                                isrun = true;

                                handlerThread = new handler_thread(handler);
                                handlerThread.start();

                                Arrays.fill(RECVBuf, (char) 0x00);
//                                Arrays.fill(encdata, (char) 0x00);
//                                Arrays.fill(icdata, (char) 0x00);

                                initSerial();
                                btnDisable();
                                isMultipad = true;

                                writeBuffer = new char[37];
                                writeBuffer[0] = 0x02; //Header ID
                                writeBuffer[1] = 0x43; //Command ID
                                writeBuffer[2] = 0x00;
                                writeBuffer[3] = 0x32; //Length(2)
                                writeBuffer[4] = 0x20;
                                writeBuffer[5] = 0x20;
                                writeBuffer[6] = 0x20;
                                writeBuffer[7] = 0x20;
                                writeBuffer[8] = 0x20;
                                writeBuffer[9] = 0x20;
                                writeBuffer[10] = 0x20;
                                writeBuffer[11] = 0x20;
                                writeBuffer[12] = 0x20;
                                writeBuffer[13] = 0x20;
                                writeBuffer[14] = 0x20;
                                writeBuffer[15] = 0x20;
                                writeBuffer[16] = 0x20;
                                writeBuffer[17] = 0x20;
                                writeBuffer[18] = 0x20;
                                writeBuffer[19] = 0x20;
                                writeBuffer[20] = 0x30;
                                writeBuffer[21] = 0x30;
                                writeBuffer[22] = 0x30;
                                writeBuffer[23] = 0x30;
                                writeBuffer[24] = 0x30;
                                writeBuffer[25] = 0x30;
                                writeBuffer[26] = 0x30;
                                writeBuffer[27] = 0x30;
                                writeBuffer[28] = 0x30;
                                writeBuffer[29] = 0x30;
                                writeBuffer[30] = 0x30;
                                writeBuffer[31] = 0x30;
                                writeBuffer[32] = 0x30;
                                writeBuffer[33] = 0x30;
                                writeBuffer[34] = 0x30;
                                writeBuffer[35] = 0x30;
                                writeBuffer[36] = 0x73;

                                temp = new byte[37];
                                for (int i = 0; i < 37; i++) {
                                    temp[i] = (byte) writeBuffer[i];
                                }
                                PopupOpenEOT(DetailDealResult.this, "암호화 PIN 입력해주세요.");
                                usbService.write(temp);
                                btnBack.setEnabled(true);
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                                Toast.makeText(DetailDealResult.this, "USB 서비스 불가능합니다.", Toast.LENGTH_LONG).show();
                                btnBack.setEnabled(true);
                                btnEnable();
                                return;
                            }
                        }
                        else if(mSharedManager.getPreferences().getInt("Readertype", 0) == 2) { //LJY20201005 : OKPOS 은련 PIN 연동
                            mUart = new libUart();
                            isrun = true;

                            Arrays.fill(RECVBuf, (char) 0x00);
//                            Arrays.fill(encdata, (char) 0x00);
//                            Arrays.fill(icdata, (char) 0x00);

                            initSerial();
                            btnDisable();
                            isMultipad = true;

                            writeBuffer = new char[37];
                            writeBuffer[0] = 0x02; //Header ID
                            writeBuffer[1] = 0x43; //Command ID
                            writeBuffer[2] = 0x00;
                            writeBuffer[3] = 0x32; //Length(2)
                            writeBuffer[4] = 0x20;
                            writeBuffer[5] = 0x20;
                            writeBuffer[6] = 0x20;
                            writeBuffer[7] = 0x20;
                            writeBuffer[8] = 0x20;
                            writeBuffer[9] = 0x20;
                            writeBuffer[10] = 0x20;
                            writeBuffer[11] = 0x20;
                            writeBuffer[12] = 0x20;
                            writeBuffer[13] = 0x20;
                            writeBuffer[14] = 0x20;
                            writeBuffer[15] = 0x20;
                            writeBuffer[16] = 0x20;
                            writeBuffer[17] = 0x20;
                            writeBuffer[18] = 0x20;
                            writeBuffer[19] = 0x20;
                            writeBuffer[20] = 0x30;
                            writeBuffer[21] = 0x30;
                            writeBuffer[22] = 0x30;
                            writeBuffer[23] = 0x30;
                            writeBuffer[24] = 0x30;
                            writeBuffer[25] = 0x30;
                            writeBuffer[26] = 0x30;
                            writeBuffer[27] = 0x30;
                            writeBuffer[28] = 0x30;
                            writeBuffer[29] = 0x30;
                            writeBuffer[30] = 0x30;
                            writeBuffer[31] = 0x30;
                            writeBuffer[32] = 0x30;
                            writeBuffer[33] = 0x30;
                            writeBuffer[34] = 0x30;
                            writeBuffer[35] = 0x30;
                            writeBuffer[36] = 0x73;

                            temp = new byte[37];
                            for (int i = 0; i < 37; i++) {
                                temp[i] = (byte) writeBuffer[i];
                            }
                            PopupOpenEOT(DetailDealResult.this, "암호화 PIN 입력해주세요.");

                            //LJY20201217 : 포트번호/통신속도 가변
                            mUart.Init(mSharedManager.getPreferences().getInt("sPortnum", 0));
                            if (mUart.IsOpen(mSharedManager.getPreferences().getInt("sPortnum", 0)) == false) {
                                mUart.Open(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")), 8, 0, 1, true);
                                mUart.SetBaudrate(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")));
                            }
                            mUart.QueueClear(mSharedManager.getPreferences().getInt("sPortnum", 0));
                            mUart.DataSend(mSharedManager.getPreferences().getInt("sPortnum", 0), temp, temp.length);

                            handlerThread = new handler_thread(handler);
                            handlerThread.start();
                        }
                        else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC - 은련 (PIN 사용 안됨)");
                            Toast.makeText(DetailDealResult.this, "IC - 은련 (PIN 사용 안됨)", Toast.LENGTH_LONG).show();
                            btnBack.setEnabled(true);
                            btnEnable();
                            return; //TTA요청 : 은련터치 막아야 됨
                        }
                        return;
                    }

                    //신용이면서 서명
                    if ((mSharedManager.getPreferences().getBoolean("Nocvm", false) == false || (mSharedManager.getPreferences().getBoolean("Nocvm", false) && Long.parseLong(mMoney) > 50000)) && (mDealtp.equals("10") == true || mDealtp.equals("UP")) && mDealgb.equals("0210")) { //LJY20230713 : 은련 추가
                        //LJY20250904 : 통합결제 바코드 리딩 시 로직 추가 (매체구분 "B" && 결제구분 "B")
                        if (Paygb[0] == 'B' && cMediagb[0] == 'B' && !IsBarcodeSign(new String(icdata))) {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO - 노서명");
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAY구분 : " + Paygb[0]);
                            PopupOpen(DetailDealResult.this, "IC VAN 취소 중입니다.");

                            PosClient posClient = new PosClient();
                            if (mDealtp.equals("21") == true) {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO - 현금취소");
                                sendBuff = ("0437PRO" + mCatid + strDate + "042021H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(1, 9) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                            } else {
                                if (mDealgb.equals("0330")) //20200131 : 멤버쉽거래
                                {
                                    if (mDealtp.equals("10")) mDealtp = "30";

                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS - 멤버쉽취소");
                                    sendBuff = ("0343PRO" + mCatid + strDate + "0420" + mDealtp + "H1          " + mCatid + mTax + mBongsa + new SimpleDateFormat("YYYYMMddHHmmss").format(new Date()) + "L37" + BarcodeToTrack2(new String(icdata)) + mMoney.substring(3, 12) + mHalbu + mApprno.substring(0, 12) + mApprdate.substring(0, 6) + "                                " + mFiller + " ").getBytes();
                                } else if (mDealgb.equals("0310")) //20200129 : 포인트거래
                                {
                                    if (mDealtp.equals("10")) mDealtp = "30";

                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS - 포인트취소");
                                    sendBuff = ("0437PRO" + mCatid + strDate + "0420" + mDealtp + "H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS - 신용취소");
                                    sendBuff = ("0437PRO" + mCatid + strDate + "042030H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                                }
                            }

                            if (bRelease)
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(sendBuff)).substring(0, 56) + "*******************************************************************************************************************************" + (new String(sendBuff)).substring(183, sendBuff.length - 183));
                            else
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(sendBuff));

                            if (mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
                                System.arraycopy(String.format("%04d", sendBuff.length).getBytes(), 0, sendBuff, 0, 4);
                                recvBuff = posClient.service_line(mServerip, Integer.parseInt(mServerport), sendBuff);
                            } else if (mSharedManager.getPreferences().getInt("Enctype", 0) == 0) {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SEED 암복호화"); //OSM20250113
                                recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), sendBuff);
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113
                                iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), sendBuff, recvBuff);   //OSM20250113 : DES암복호화 함수 추가
                            }
                            InsertRecv(recvBuff);
                            PopupClose();
                        }
                        else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC - 서명"); //PAYPRO 개발 필요

                            //LJY20220427 : 멀티패드 서명 연동
                            if (mSharedManager.getPreferences().getInt("Readertype", 0) == 1 && mSharedManager.getPreferences().getBoolean("Signuse", false)) {
                                if (usbService != null) { // if UsbService was correctly binded, Send data
                                    isrun = true;

                                    handlerThread = new handler_thread(handler);
                                    handlerThread.start();

                                    Arrays.fill(RECVBuf, (char) 0x00);
//                                        Arrays.fill(encdata, (char) 0x00);
//                                        Arrays.fill(icdata, (char) 0x00);

                                    initSerial();
                                    //isMultipad = true;
                                    btnDisable();
                                    isSign = true;
                                    btnBack.setEnabled(true);

                                    writeBuffer = new char[53];
                                    writeBuffer[0] = 0x02; //Header ID
                                    writeBuffer[1] = 0x42; //Command ID
                                    writeBuffer[2] = 0x00;
                                    writeBuffer[3] = 0x48; //Length(2)
                                    String sendstr = "19F316BA33A57729" + "  Please Sign!  " + "                "; //서명문구(48)
                                    System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 48);
                                    writeBuffer[52] = xor_sum(writeBuffer, 52);

                                    temp = new byte[53];
                                    for (int i = 0; i < 53; i++) {
                                        temp[i] = (byte) writeBuffer[i];
                                    }
                                    PopupOpenEOT(DetailDealResult.this, "서명 해주세요.");
                                    usbService.write(temp);
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                                    Toast.makeText(DetailDealResult.this, "USB 서비스 불가능합니다.", Toast.LENGTH_LONG).show();
                                    btnEnable();
                                    PopupClose();
                                    btnBack.setEnabled(true);
                                    return;
                                }
                            } else
                                //LJY20201005 : OKPOS 서명 연동
                                if (mSharedManager.getPreferences().getInt("Readertype", 0) == 2) //OKPOS
                                {
                                    mUart = new libUart();
                                    isrun = true;

                                    Arrays.fill(RECVBuf, (char) 0x00);
//                            Arrays.fill(encdata, (char) 0x00);
//                            Arrays.fill(icdata, (char) 0x00);

                                    initSerial();
                                    isSign = true;
                                    btnDisable();
                                    btnBack.setEnabled(false);

                                    writeBuffer = new char[53];
                                    writeBuffer[0] = 0x02; //Header ID
                                    writeBuffer[1] = 0x42; //Command ID
                                    writeBuffer[2] = 0x00;
                                    writeBuffer[3] = 0x48; //Length(2)
                                    String sendstr = "19F316BA33A57729" + "  Please Sign!  " + "                "; //서명문구(48)
                                    System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 48);
                                    writeBuffer[52] = xor_sum(writeBuffer, 52);

                                    temp = new byte[53];
                                    for (int i = 0; i < 53; i++) {
                                        temp[i] = (byte) writeBuffer[i];
                                    }
                                    PopupOpenEOT(DetailDealResult.this, "서명 해주세요.");

                                    //LJY20201217 : 포트번호/통신속도 가변
                                    mUart.Init(mSharedManager.getPreferences().getInt("sPortnum", 0));
                                    if (mUart.IsOpen(mSharedManager.getPreferences().getInt("sPortnum", 0)) == false) {
                                        mUart.Open(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")), 8, 0, 1, true);
                                        mUart.SetBaudrate(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")));
                                    }
                                    mUart.QueueClear(mSharedManager.getPreferences().getInt("sPortnum", 0));
                                    mUart.DataSend(mSharedManager.getPreferences().getInt("sPortnum", 0), temp, temp.length);

                                    handlerThread = new handler_thread(handler);
                                    handlerThread.start();
                                } else {
                                    Intent intent = new Intent(DetailDealResult.this, SignPad.class);
                                    startActivityForResult(intent, 1);
                                }
                        }
                    } else {//노서명
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC - 노서명");
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAY구분 : " + Paygb[0]);
                        PopupOpen(DetailDealResult.this, "IC VAN 취소 중입니다.");

                        PosClient posClient = new PosClient();//LJY20250904 : 통합결제 바코드 리딩 시 로직 추가 (매체구분 "B" && 결제구분 "B")
                        if (mDealtp.equals("21") == true) {
                            if (Paygb[0] == 'B' && cMediagb[0] == 'B') {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO - 현금취소");
                                sendBuff = ("0437PRO" + mCatid + strDate + "042021H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(1, 9) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                            } else
                            if(Paygb[0] == 'R' && CardBrand[0] == 'K') { //LJY20200713 : 동반위 JUST TOUCH
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF - 현금취소(동반위)");
                                sendBuff = ("0694HPS" + mCatid + strDate + "042021H1          " + mCatid + "K" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(1, 9) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                            }
                            else
                            if (Paygb[0] == 'I' || (Paygb[0] == 'R' && Integer.parseInt(new String(icdata, 0, 4)) > 0)) { //LJY20230818
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC - 현금취소");
                                sendBuff = ("0694HPS" + mCatid + strDate + "042021H1          " + mCatid + "I" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(1, 9) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS - 현금취소");
                                sendBuff = ("0437HPS" + mCatid + strDate + "042021H1          " + mCatid + "A" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(1, 9) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                            }
                        } else {
                            if (Paygb[0] == 'B' && cMediagb[0] == 'B') {
                                if (mDealgb.equals("0330")) //20200131 : 멤버쉽거래
                                {
                                    if(mDealtp.equals("10")) mDealtp = "30";

                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS - 멤버쉽취소");
                                    sendBuff = ("0343PRO" + mCatid + strDate + "0420" + mDealtp + "H1          " + mCatid + mTax + mBongsa + new SimpleDateFormat("YYYYMMddHHmmss").format(new Date()) + "L37" + BarcodeToTrack2(new String(icdata)) + mMoney.substring(3, 12) + mHalbu + mApprno.substring(0, 12) + mApprdate.substring(0, 6) + "                                " + mFiller + " ").getBytes();
                                }
                                else
                                if (mDealgb.equals("0310")) //20200129 : 포인트거래
                                {
                                    if (mDealtp.equals("10")) mDealtp = "30";

                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS - 포인트취소");
                                    sendBuff = ("0437PRO" + mCatid + strDate + "0420" + mDealtp + "H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS - 신용취소");
                                    sendBuff = ("0437PRO" + mCatid + strDate + "042030H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                                }
                            } else
                            if(Paygb[0] == 'R' && CardBrand[0] == 'K') { //LJY20200713 : 동반위 JUST TOUCH
                                if (mDealgb.equals("0330")) //20200131 : 멤버쉽거래
                                {
                                    if(mDealtp.equals("10")) mDealtp = "30";

                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF - 멤버쉽취소(동반위)");
                                    sendBuff = ("0343" + mMsgtxt + mCatid + strDate + "0420" + mDealtp + "H1          " + mCatid + mTax + mBongsa + new SimpleDateFormat("YYYYMMddHHmmss").format(new Date()) + "A" + new String(encdata, 0, 127) + mMoney.substring(3, 12) + mHalbu + mApprno.substring(0, 12) + mApprdate.substring(0, 6) + "                                " + mFiller + " ").getBytes();
                                }
                                else
                                if (mDealgb.equals("0310")) //20200129 : 포인트거래
                                {
                                    if (mDealtp.equals("10")) mDealtp = "30";

                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF - 포인트취소(동반위)");
                                    sendBuff = ("0694" + mMsgtxt + mCatid + strDate + "0420" + mDealtp + "H1          " + mCatid + "K" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF - 신용취소(동반위)");
                                    sendBuff = ("0694HPS" + mCatid + strDate + "042030H1          " + mCatid + "K" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                                }
                            }
                            else
                            if (Paygb[0] == 'I' || (Paygb[0] == 'R' && Integer.parseInt(new String(icdata, 0, 4)) > 0)) { //LJY20230818
                                if (mDealgb.equals("0330")) //20200131 : 멤버쉽거래
                                {
                                    if(mDealtp.equals("10")) mDealtp = "30";

                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC - 멤버쉽취소");
                                    sendBuff = ("0343" + mMsgtxt + mCatid + strDate + "0420" + mDealtp + "H1          " + mCatid + mTax + mBongsa + new SimpleDateFormat("YYYYMMddHHmmss").format(new Date()) + "A" + new String(encdata, 0, 127) + mMoney.substring(3, 12) + mHalbu + mApprno.substring(0, 12) + mApprdate.substring(0, 6) + "                                " + mFiller + " ").getBytes();
                                }
                                else
                                if (mDealgb.equals("0310")) //20200129 : 포인트거래
                                {
                                    if (mDealtp.equals("10")) mDealtp = "30";

                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC - 포인트취소");
                                    sendBuff = ("0694" + mMsgtxt + mCatid + strDate + "0420" + mDealtp + "H1          " + mCatid + "I" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC - 신용취소");
                                    sendBuff = ("0694HPS" + mCatid + strDate + "042030H1          " + mCatid + "I" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                                }
                            }else {
                                if (mDealgb.equals("0330")) //20200131 : 멤버쉽거래
                                {
                                    if(mDealtp.equals("10")) mDealtp = "30";

                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS - 멤버쉽취소");
                                    sendBuff = ("0343" + mMsgtxt + mCatid + strDate + "0420" + mDealtp + "H1          " + mCatid + mTax + mBongsa + new SimpleDateFormat("YYYYMMddHHmmss").format(new Date()) + "A" + new String(encdata, 0, 127) + mMoney.substring(3, 12) + mHalbu + mApprno.substring(0, 12) + mApprdate.substring(0, 6) + "                                " + mFiller + " ").getBytes();
                                }
                                else
                                if (mDealgb.equals("0310")) //20200129 : 포인트거래
                                {
                                    if (mDealtp.equals("10")) mDealtp = "30";

                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS - 포인트취소");
                                    sendBuff = ("0437" + mMsgtxt + mCatid + strDate + "0420" + mDealtp + "H1          " + mCatid + "A" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS - 신용취소");
                                    sendBuff = ("0437HPS" + mCatid + strDate + "042030H1          " + mCatid + "A" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                                }
                            }
                        }
                        if (bRelease)
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(sendBuff)).substring(0, 56) + "*******************************************************************************************************************************" + (new String(sendBuff)).substring(183, sendBuff.length - 183));
                        else
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(sendBuff));
                        if (mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
                            System.arraycopy(String.format("%04d", sendBuff.length).getBytes(), 0, sendBuff, 0, 4);
                            recvBuff = posClient.service_line(mServerip, Integer.parseInt(mServerport), sendBuff);
                        } else if(mSharedManager.getPreferences().getInt("Enctype", 0) == 0) {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SEED 암복호화"); //OSM20250113
                            recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), sendBuff);
                        }
                        else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113
                            iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), sendBuff, recvBuff);   //OSM20250113 : DES암복호화 함수 추가
                        }
                        InsertRecv(recvBuff);
                        PopupClose();
                    }
                } else if (errcode.equals("CF")) {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC FALLBACK 코드 수신");

                    if (mSharedManager.getPreferences().getBoolean("Retry", false)) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 재시도 사용 안함");
                        Toast.makeText(DetailDealResult.this, "FALLBACK 재시도 사용 안함", Toast.LENGTH_LONG).show();
                        btnBack.setEnabled(true);
                        btnEnable();
                        return;
                    }

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 카드리딩 요청입니다.");

                    String sReaderBinVer = mSharedManager.getPreferences().getString("READERBINVER", "  "); //LJY20250904 : 8BIN/통합결제 적용
                    if(sReaderBinVer.equals("02") || sReaderBinVer.equals("01")) //통합결제 //8BIN
                        func_code = 0x9E;
                    else
                        func_code = 0x6E;

                    if(mSharedManager.getPreferences().getInt("Readertype", 0) == 3) //POSBANK
                    {
                        isrun = true;

                        Arrays.fill(RECVBuf, (char) 0x00);
                        Arrays.fill(encdata, (char) 0x00);
                        Arrays.fill(icdata, (char) 0x00);

                        initSerial();
//                        func_code = 0x6E;     //LJY20250904 : 8BIN/통합결제 적용
                        btnDisable();

                        scr = new ScrProtocolCom(DetailDealResult.this, "COM" + (mSharedManager.getPreferences().getInt("Portnum", 0)+1), mSharedManager.getPreferences().getString("BaudrateStr", "115200"));

                        // Serial Port Check
                        int readState = scr.checkSerialPortOpened();
                        if (readState != RTN_COMM_OK) {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 연결 상태 체크해주시길 바랍니다.");
                            Toast.makeText(DetailDealResult.this, "리더기 연결 상태 체크해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
                            btnEnable();
                            return;
                        }

                        scr.clearTxBuffer();

                        writeBuffer = new char[7];
                        writeBuffer[0] = 0x02; //Header ID
                        writeBuffer[1] = func_code; //Command ID
                        writeBuffer[2] = 0x00;
                        writeBuffer[3] = 0x02; //Length(2)
                        String sendstr = mTimeout; //Card 대기시간(2)
                        System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 2);
                        writeBuffer[6] = xor_sum(writeBuffer, 6);

                        temp = new byte[7];
                        for (int i = 0; i < 7; i++) {
                            temp[i] = (byte) writeBuffer[i];
                        }
                        scr.sendMsg(temp, temp.length);
                        PopupOpenEOT(DetailDealResult.this, "FALLBACK 카드리딩 해주세요.");

                        scr.clearRxBuffer();
                        handlerThread = new handler_thread(handler);
                        handlerThread.start();
                    }
                    else
                    if(mSharedManager.getPreferences().getInt("Readertype", 0) == 2) //OKPOS
                    {
                        mUart = new libUart();
                        isrun = true;

                        Arrays.fill(RECVBuf, (char) 0x00);
                        Arrays.fill(encdata, (char) 0x00);
                        Arrays.fill(icdata, (char) 0x00);

                        initSerial();
//                        func_code = 0x6E;     //LJY20250904 : 8BIN/통합결제 적용
                        btnDisable();

                        writeBuffer = new char[7];
                        writeBuffer[0] = 0x02; //Header ID
                        writeBuffer[1] = func_code; //Command ID
                        writeBuffer[2] = 0x00;
                        writeBuffer[3] = 0x02; //Length(2)
                        String sendstr = mTimeout; //Card 대기시간(2)
                        System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 2);
                        writeBuffer[6] = xor_sum(writeBuffer, 6);

                        temp = new byte[7];
                        for (int i = 0; i < 7; i++) {
                            temp[i] = (byte) writeBuffer[i];
                        }
                        PopupOpenEOT(DetailDealResult.this, "FALLBACK 카드리딩 해주세요.");

                        //LJY20201217 : 리더기 포트번호/통신속도 가변
                        mUart.Init(mSharedManager.getPreferences().getInt("Portnum", 0));
                        if (mUart.IsOpen(mSharedManager.getPreferences().getInt("Portnum", 0)) == false) {
                            mUart.Open(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")), 8, 0, 1, true);
                            mUart.SetBaudrate(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")));
                        }
                        mUart.QueueClear(mSharedManager.getPreferences().getInt("Portnum", 0));
                        mUart.DataSend(mSharedManager.getPreferences().getInt("Portnum", 0), temp, temp.length);

                        handlerThread = new handler_thread(handler);
                        handlerThread.start();
                    }
                    else {
                        if(!SharedManager.isBizdown)
                        {
                            //LJY20200812 : 가맹점다운로드 예외처리
//                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 가맹점다운로드 해주시길 바랍니다.");
//                            Toast.makeText(DetailDealResult.this, "가맹점다운로드 해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
//                            btnEnable();
//                            return;
                        }
                        if (SharedManager.isStatus == false) {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 연결 상태 체크해주시길 바랍니다.");
                            Toast.makeText(DetailDealResult.this, "리더기 연결 상태 체크해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
                            btnEnable();
                            return;
                        }

                        if (usbService != null) { // if UsbService was correctly binded, Send data
                            isrun = true;

                            handlerThread = new handler_thread(handler);
                            handlerThread.start();

                            Arrays.fill(RECVBuf, (char) 0x00);
                            Arrays.fill(encdata, (char) 0x00);
                            Arrays.fill(icdata, (char) 0x00);

                            initSerial();
//                        func_code = 0x6E;     //LJY20250904 : 8BIN/통합결제 적용
                            btnDisable();

                            writeBuffer = new char[7];
                            writeBuffer[0] = 0x02; //Header ID
                            writeBuffer[1] = func_code; //Command ID
                            writeBuffer[2] = 0x00;
                            writeBuffer[3] = 0x02; //Length(2)
                            String sendstr = mTimeout; //Card 대기시간(2)
                            System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 2);
                            writeBuffer[6] = xor_sum(writeBuffer, 6);

                            temp = new byte[7];
                            for (int i = 0; i < 7; i++) {
                                temp[i] = (byte) writeBuffer[i];
                            }
                            PopupOpenEOT(DetailDealResult.this, "FALLBACK 카드리딩 해주세요.");
                            usbService.write(temp);
                        } else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                            Toast.makeText(DetailDealResult.this, "USB 서비스 불가능합니다.", Toast.LENGTH_LONG).show();
                            btnBack.setEnabled(true);
                            btnEnable();
                            return;
                        }
                    }
                } else { //IC카드리딩 에러코드
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 에러코드 : " + errcode);
                    Toast.makeText(DetailDealResult.this, "IC 에러코드 : " + errcode, Toast.LENGTH_LONG).show();
                    btnBack.setEnabled(true);
                    btnEnable();
                    return;
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!bRelease) {
            if (new File(Environment.getExternalStorageDirectory(), "ANDROIDNVCAT.db").exists())
                new File(Environment.getExternalStorageDirectory(), "ANDROIDNVCAT.db").delete();
        }

        System.gc(); //가비지 컬렉션
        //Runtime.getRuntime().gc();
    }

    private class handler_thread extends Thread implements Runnable {
        Handler mHandler;
        int itimeover;
        byte cData;

        handler_thread(Handler h) {
            mHandler = h;
        }

        public void run() {
            Message msg;

            tstart = System.currentTimeMillis();

            //20190108LJY
            if(mSharedManager.getPreferences().getInt("Readertype", 0) == 3) //POSBANK
            {
                int readState = 0;
                long startTimeTick = Utils.GetStartTimeTick();
                while (Utils.CheckTickTimeOut(startTimeTick, 3000)) { //ACK타임아웃 3초
                    readState = scr.readMsg(250);
                    if (readState != RTN_CONTINUE) break;
                }

                // 통신버퍼 Clear
                scr.clearBuffer();

                if (readState == RTN_COMM_OK) {
                    //응답전문수신
                    if (scr.respMsg.rxCommandID == CH_NAK) { //NAK 수신
                        status = 2;
                        msg = mHandler.obtainMessage();
                        mHandler.sendMessage(msg);
                    } else if (scr.respMsg.rxCommandID == CH_ACK) { // ACK수신 - 응답전문 수신 대기.
                        scr.clearRxBuffer();
                        startTimeTick = Utils.GetStartTimeTick();
                        while (Utils.CheckTickTimeOut(startTimeTick, 60000)) {
                            readState = scr.readMsg(250);
                            if (readState != RTN_CONTINUE) break;
                        }

                        // 통신버퍼 Clear
                        scr.clearBuffer();

                        if (readState == RTN_COMM_OK) {
                            //응답전문수신
                            if (scr.respMsg.rxCommandID == (byte) func_code) {
                                for (int k = 0; k < scr.respMsg.rxDataValueLength; k++) {
                                    RECVBuf[k] = (char) scr.respMsg.rxDataValuebyte[k];
                                    scr.respMsg.rxDataValuebyte[k] = 0x00;
                                }

                                length_recv = Integer.parseInt(String.format("%02X", RECVBuf[2] & 0xff) + String.format("%02X", RECVBuf[3] & 0xff)); //JDK20230110 : 포스뱅크 바코드리딩 버그 개선

                                status = 1;
                                msg = mHandler.obtainMessage();
                                mHandler.sendMessage(msg);
                            } else { //RTN_INVALID_DATA
                                status = 2;
                                msg = mHandler.obtainMessage();
                                mHandler.sendMessage(msg);
                            }
                        } else if (readState == RTN_CONTINUE) { //RTN_TIMEOUT
                            status = 2;
                            msg = mHandler.obtainMessage();
                            mHandler.sendMessage(msg);
                        }
                        //return readState;
                    } else if (scr.respMsg.rxCommandID == (byte) func_code) { //응답전문수신
                        for (int k = 0; k < scr.respMsg.rxDataValueLength; k++) {
                            RECVBuf[k] = (char) scr.respMsg.rxDataValuebyte[k];
                            scr.respMsg.rxDataValuebyte[k] = 0x00;
                        }

                        status = 1;
                        msg = mHandler.obtainMessage();
                        mHandler.sendMessage(msg);
                    } else { //RTN_INVALID_DATA
                        status = 2;
                        msg = mHandler.obtainMessage();
                        mHandler.sendMessage(msg);
                    }
                } else if (readState == RTN_CONTINUE) { //RTN_TIMEOUT
                    status = 2;
                    msg = mHandler.obtainMessage();
                    mHandler.sendMessage(msg);
                }
                //return readState;
            }
            else
            if(mSharedManager.getPreferences().getInt("Readertype", 0) == 2) //OKPOS
            {
                while (isrun) {
                    if(mSharedManager.getPreferences().getInt("Readertype", 0) == 5 && (func_code == 'A' || func_code == 'R' || func_code == 'S' || func_code == 'e' || func_code == 'E') || isReaderCheck) //OSM20250123 : 리더기 Health Check    //LJY20231006 : TITENG 리더기 연동 시 타임아웃 시간 변경
                        itimeover = calculate_interval(3);
                    else {
                        int timeoutValue = 0; // 기본값
                        try {
                            if (mTimeout != null && !mTimeout.trim().isEmpty())
                                timeoutValue = Integer.parseInt(mTimeout.trim());
                        } catch (NumberFormatException e) {
                            // 로그로 기록만 하고 기본값 유지
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] Invalid mTimeout value : " + mTimeout);
                        }
                        itimeover = calculate_interval(timeoutValue + 10);  //OSM20250902 : 타임아웃 시간 변경 (리더기 요청 시간 + 10초)
                        mSharedManager.getPreferences().edit().putInt("NewTimeout", timeoutValue);
                    }

                    if (itimeover == 1) {
                        status = 2;
                        msg = mHandler.obtainMessage();
                        mHandler.sendMessage(msg);
                    }

                    //OSM20241017 : TITENG 리더기 연동 시, func_code (0x04) 로직 제외
                    //카드리더UART 응답데이터 있을시
                    //LJY20201005 : OKPOS 서명 연동
                    //LJY20201217 : 리더기/서명패드 포트번호/통신속도 가변
                    if (mUart.IsRxData(mSharedManager.getPreferences().getInt("sPortnum", 0)) || mUart.IsRxData(mSharedManager.getPreferences().getInt("Portnum", 0)) == true) {
                        //카드리더UART 문자 꺼내기
                        //LJY20201005 : OKPOS 서명 연동
                        if(mUart.IsRxData(mSharedManager.getPreferences().getInt("sPortnum", 0)))
                            cData = mUart.GetCh(mSharedManager.getPreferences().getInt("sPortnum", 0));
                        else
                            cData = mUart.GetCh(mSharedManager.getPreferences().getInt("Portnum", 0));

                        if (istep == 0 && cData == 0x06) //ACK수신
                        {
                            slen = 0;
                            istep = 0;
                        } else if (istep == 0 && cData == 0x04) //EOT수신
                        {
                            slen = 0;
                            istep = 0;
                        } else if (istep == 0 && cData == 0x02) //STX수신
                        {
                            slen = 0;
                            istep = 15;
                            RECVBuf[slen++] = (char)cData;
                        } else if(istep == 0 && isSign && cData == 0x0F) //좌표시작
                        {
                            slen = 0;
                            istep = 10;
                        }
                        else if(istep == 10 && slen < 2 && isSign) //좌표입력
                        {
                            RECVBuf[slen++] = (char)cData;
                            istep = 10;
                        }
                        else if(istep == 10 && slen == 2 && isSign && cData == 0x0E) //좌표종료
                        {
                            slen = 0;
                            istep = 0;
                        } else if (istep == 15) //COMMAND수신 //OSM20241017 : 커맨드 수신 단계에서 커맨드, sign, 멀티패드 체크 제외
                        {
                            istep = 20;
                            RECVBuf[slen++] = (char)cData;
                        } else if (istep == 20) //길이수신
                        {
                            RECVBuf[slen++] = (char)cData;
                            if (slen == 4) {
                                istep = 25;
                                length_recv = Integer.parseInt(String.format("%02X", RECVBuf[2] & 0xff) + String.format("%02X", RECVBuf[3] & 0xff));
                            }
                        } else if (istep == 25) { //데이터수신
                            RECVBuf[slen++] = (char)cData;
                            if (length_recv == slen - 4) {
                                istep = 30;
                            }
                        } else if (istep == 30) { //데이터수신완료
                            istep = 0;
                            status = 1;
                        }

                        if (status == 1 && slen > 0) {
//                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] OKPOS");
//                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RECVBuf : " + new String(RECVBuf));
                            msg = mHandler.obtainMessage();
                            mHandler.sendMessage(msg);
                        }
                    }
                }
            }
            else {
                while (isrun) {
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                    }

                    if(mSharedManager.getPreferences().getInt("Readertype", 0) == 5 && (func_code == 'A' || func_code == 'R' || func_code == 'S' || func_code == 'e' || func_code == 'E') || isReaderCheck) //OSM20250123 : 리더기 Health Check    //LJY20231006 : TITENG 리더기 연동 시 타임아웃 시간 변경
                        itimeover = calculate_interval(3);
                    else {
                        int timeoutValue = 0; // 기본값
                        try {
                            if (mTimeout != null && !mTimeout.trim().isEmpty())
                                timeoutValue = Integer.parseInt(mTimeout.trim());
                        } catch (NumberFormatException e) {
                            // 로그로 기록만 하고 기본값 유지
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] Invalid mTimeout value : " + mTimeout);
                        }
                        itimeover = calculate_interval(timeoutValue + 10);  //OSM20250902 : 타임아웃 시간 변경 (리더기 요청 시간 + 10초)
                        mSharedManager.getPreferences().edit().putInt("NewTimeout", timeoutValue);
                    }

                    if (itimeover == 1) {
                        status = 2;
                        msg = mHandler.obtainMessage();
                        mHandler.sendMessage(msg);
                    }

                    //LJY20220520 : 서명/멀티패드 요청취소에 대한 예외처리
                    if((isSign || isMultipad) && RECVBuf[0] == 0x04)
                    {
                        slen = 1;
                        istep = 0;
                        status = 1;

                        if (status == 1 && slen > 0 && RECVBuf[0] == 0x04) {
//                            RECVBuf[0] = 0x00; //LJY20220520 : 초기화 처리 후 진행
                            msg = mHandler.obtainMessage();
                            mHandler.sendMessage(msg);
                        }
                    }
                    else
                    if (status == 1 && slen > 0) {
                        msg = mHandler.obtainMessage();
                        mHandler.sendMessage(msg);
                    }
                }
            }
        }
    }

    private void initViews() {
        Intent intent = getIntent();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM CHKDEALTABLE WHERE _id ='" + intent.getStringExtra("str_id") + "'", null);

        ArrayList<DealItem> arrDealitem = new ArrayList<>();

        while (cursor.moveToNext()) {
            DealItem mdealitem = new DealItem();
            mdealitem.str_id = cursor.getString(0);
            mdealitem.str_date = cursor.getString(1);
            mdealitem.str_dealtp = cursor.getString(2);
            mDealtp = mdealitem.str_dealtp;
            mdealitem.str_dealgb = cursor.getString(3);
            mDealgb = mdealitem.str_dealgb;
            mdealitem.str_cardno = cursor.getString(4);
            mdealitem.str_money = cursor.getString(5);
            if(mDealgb.equals("0330")) //20200206 : 멤버쉽거래
                mMoney = String.format("%012d", Long.parseLong(mdealitem.str_money));
            else
                mMoney = mdealitem.str_money;
            mdealitem.str_tax = cursor.getString(6);
            mTax = mdealitem.str_tax;
            mdealitem.str_bongsa = cursor.getString(7);
            if(mDealgb.equals("0330")) //20200206 : 멤버쉽거래
            {
                mBongsa = mdealitem.str_bongsa;
                if(mBongsa.equals("01"))    mBongsa = "11";
                else if(mBongsa.equals("02"))    mBongsa = "12";
                else if(mBongsa.equals("06"))    mBongsa = "15";
            }
            else
                mBongsa = mdealitem.str_bongsa;
            mdealitem.str_halbu = cursor.getString(8);
            mHalbu = mdealitem.str_halbu;
            mdealitem.str_apprno = cursor.getString(9);
            mApprno = mdealitem.str_apprno;
            mdealitem.str_apprdate = cursor.getString(10);
            mApprdate = mdealitem.str_apprdate;
            mdealitem.str_tid = cursor.getString(11);
            mCatid = mdealitem.str_tid;
            mdealitem.str_bgnm = cursor.getString(12);
            mdealitem.str_minm = cursor.getString(13);
            mdealitem.str_storeno = cursor.getString(14);
            mdealitem.str_recvmsg = cursor.getString(15);
            mdealitem.str_recvcode = cursor.getString(16);
            mdealitem.str_bal = cursor.getString(17);
            mdealitem.str_wcc = cursor.getString(18);
            mWCC = mdealitem.str_wcc;
            mdealitem.str_cardgb = cursor.getString(19);
            mdealitem.str_msgno = cursor.getString(20);
            mdealitem.str_dealno = cursor.getString(21);
            mdealitem.str_msgtxt = cursor.getString(22); //20200129 : 포인트거래
            mMsgtxt = mdealitem.str_msgtxt; //20200129 : 포인트거래
            mdealitem.str_micode = cursor.getString(23); //OSM20250814 : 매입사,발급사코드 추가
            mdealitem.str_bgcode = cursor.getString(24); //OSM20250814 : 매입사,발급사코드 추가

            arrDealitem.add(mdealitem);
        }

        mlistview = (ListView) findViewById(R.id.listViewdealdetail);
        DetailDealListAdapter mlistadapter = new DetailDealListAdapter(arrDealitem);
        mlistview.setAdapter(mlistadapter);

        //mFiller = "NVC" + mSharedManager.getPreferences().getString("READERSN", "          ") + SharedManager.ROMVER + "                               ";
        //LJY20220816 : READERSN 예외 처리
        String sReaderSn = mSharedManager.getPreferences().getString("READERSN", "          ");
        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] READERSN : " + sReaderSn);
        if(sReaderSn.length() != 10) {
            mFiller = "NVC" + "          " + SharedManager.ROMVER + "                               ";
        }
        else
            mFiller = "NVC" + mSharedManager.getPreferences().getString("READERSN", "          ") + SharedManager.ROMVER + "                               "; //20200318 : 리더기일련번호
    }

    private void InsertRecv(byte[] recvBuff) {
        try {
            if (new String(recvBuff, "EUC-KR").equals("-1") == true || iresult == -1) {
                Toast.makeText(DetailDealResult.this, "-1", Toast.LENGTH_SHORT).show();
            } else if (new String(recvBuff, "EUC-KR").equals("-2") == true || iresult == -2) {
                Toast.makeText(DetailDealResult.this, "-2", Toast.LENGTH_SHORT).show();
            } else if (new String(recvBuff, "EUC-KR").equals("-3") == true || iresult == -3) {
                Toast.makeText(DetailDealResult.this, "-3", Toast.LENGTH_SHORT).show();
            } else if (new String(recvBuff, "EUC-KR").equals("-4") == true || iresult == -4) {
                Toast.makeText(DetailDealResult.this, "-4", Toast.LENGTH_SHORT).show();
            } else {
                if (bRelease)
                    SharedManager.LogDebug(bLogUse, "debugjy", "[VAN->NVCAT] Recvdata : " + (new String(recvBuff, "EUC-KR")).substring(0, 68) + "**********" + (new String(recvBuff, "EUC-KR")).substring(78, recvBuff.length - 78));
                else
                    SharedManager.LogDebug(bLogUse, "debugjy", "[VAN->NVCAT] Recvdata : " + new String(recvBuff, "EUC-KR"));

                RecvFormat recv = new RecvFormat();
                recv.str_Msglen = new String(recvBuff, 0, 4, "EUC-KR");
                recv.str_Msgtxt = new String(recvBuff, 4, 3, "EUC-KR");
                recv.str_Msgno = new String(recvBuff, 7, 20, "EUC-KR");
                if (mDealgb.equals("0330")) //20200129 : 멤버쉽거래
                    recv.str_Msggb = "0550";
                else if (mDealgb.equals("0310")) //20200129 : 포인트거래
                    recv.str_Msggb = "0530";
                else
                    recv.str_Msggb = new String(recvBuff, 27, 4, "EUC-KR");
                if (mDealtp.equals("UP"))
                    recv.str_Dealgb = "UP";
                else
                    recv.str_Dealgb = new String(recvBuff, 31, 2, "EUC-KR");
                recv.str_Devicegb = new String(recvBuff, 33, 2, "EUC-KR");
                recv.str_Deviceno = new String(recvBuff, 35, 10, "EUC-KR");
                recv.str_Tid = new String(recvBuff, 45, 10, "EUC-KR");
                recv.str_Recvcode = new String(recvBuff, 55, 4, "EUC-KR");
                if (recv.str_Recvcode.equals("0000")) {
                    Toast.makeText(DetailDealResult.this, "취소결제완료", Toast.LENGTH_SHORT).show();
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 취소결제완료");
                } else {
                    Toast.makeText(DetailDealResult.this, "취소결제실패", Toast.LENGTH_SHORT).show();
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 취소결제실패");
                }
                if (mDealgb.equals("0330")) //20200131 : 멤버쉽거래
                {
                    recv.str_Tax = new String(recvBuff, 59, 2, "EUC-KR"); //적립구분(2)
                    recv.str_Bongsa = new String(recvBuff, 61, 2, "EUC-KR"); //포인트구분(2)
                    recv.str_Apprdate = new String(recvBuff, 65, 12, "EUC-KR"); //쇼핑몰거래일시(12)
                    recv.str_Wcc = new String(recvBuff, 77, 1, "EUC-KR"); //WCC(1)
                    recv.str_Carddata = new String(recvBuff, 80, 6, "EUC-KR") + "**********"; //카드BIN(6)
                    recv.str_Money = new String(recvBuff, 118, 9, "EUC-KR"); //거래금액(9)
                    recv.str_Halbu = new String(recvBuff, 127, 16, "EUC-KR"); //비밀번호(16)
                    recv.str_Apprno = new String(recvBuff, 143, 15, "EUC-KR"); //승인번호(15)
                    recv.str_Bgcode = new String(recvBuff, 158, 2, "EUC-KR"); //발급사코드(2)
                    recv.str_Bgnm = new String(recvBuff, 160, 20, "EUC-KR"); //발급사명(20)
                    recv.str_Storeno = new String(recvBuff, 180, 15, "EUC-KR"); //가맹점번호(15)
                    recv.str_P1 = new String(recvBuff, 195, 9, "EUC-KR"); //발생포인트(9)
                    recv.str_P2 = new String(recvBuff, 204, 9, "EUC-KR"); //가용포인트(9)
                    recv.str_P3 = new String(recvBuff, 213, 9, "EUC-KR"); //누적포인트(9)
                    recv.str_Msg1 = new String(recvBuff, 222, 40, "EUC-KR"); //알림메시지1(40)
                    recv.str_Msg2 = new String(recvBuff, 262, 24, "EUC-KR"); //알림메시지2(24)
                    recv.str_Msg3 = new String(recvBuff, 286, 24, "EUC-KR"); //알림메시지3(24)
                    recv.str_Msg4 = "";
                    recv.str_CBStore = new String(recvBuff, 310, 9, "EUC-KR"); //신판실승인금액(9)
                    recv.str_CBApprno = new String(recvBuff, 319, 131, "EUC-KR"); //Filler(131)
                    recv.str_Bizno = "";
                    recv.str_Perno = "";
                    recv.str_Pinno = "";
                    recv.str_Micode = "";
                    recv.str_Minm = "";
                    recv.str_Dealno = "";
                    recv.str_Dccyn = "";
                    recv.str_RealApprmoney = "";
                    recv.str_DealCardno = "";

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    dbHelper.insertDeal(simpleDateFormat.format(new Date()), recv.str_Dealgb, recv.str_Msggb, recv.str_Carddata, recv.str_Money, recv.str_Tax, recv.str_Bongsa, recv.str_Halbu, recv.str_Apprno, recv.str_Apprdate, recv.str_Tid, recv.str_Bgnm, recv.str_Minm, recv.str_Storeno, recv.str_Msg1 + recv.str_Msg2 + recv.str_Msg3 + recv.str_Msg4, recv.str_Recvcode, recv.str_P3, recv.str_Wcc, recv.str_RealApprmoney, recv.str_Msgno, recv.str_DealCardno, recv.str_Msgtxt, recv.str_Micode, recv.str_Bgcode); //OSM20250814 : 매입사, 발급사코드 추가//20200129 : 포인트거래
                } else {
                    recv.str_Wcc = new String(recvBuff, 59, 1, "EUC-KR");
                    if (mDealtp.equals("21"))
                        recv.str_Carddata = new String(recvBuff, 60, 6, "EUC-KR") + "**********";
                    else
                        recv.str_Carddata = new String(recvBuff, 62, 6, "EUC-KR") + "**********";
                    recv.str_Halbu = new String(recvBuff, 99, 2, "EUC-KR");
                    recv.str_Bongsa = new String(recvBuff, 101, 12, "EUC-KR");
                    recv.str_Tax = new String(recvBuff, 113, 12, "EUC-KR");
                    recv.str_Money = new String(recvBuff, 125, 12, "EUC-KR");
                    recv.str_Bizno = new String(recvBuff, 137, 10, "EUC-KR");
                    recv.str_Perno = new String(recvBuff, 147, 13, "EUC-KR");
                    recv.str_Pinno = new String(recvBuff, 160, 16, "EUC-KR");
                    recv.str_Bgcode = new String(recvBuff, 176, 2, "EUC-KR");
                    recv.str_Bgnm = new String(recvBuff, 178, 20, "EUC-KR");
                    recv.str_Micode = new String(recvBuff, 198, 2, "EUC-KR");
                    recv.str_Minm = new String(recvBuff, 200, 20, "EUC-KR");
                    recv.str_Storeno = new String(recvBuff, 220, 15, "EUC-KR");
                    recv.str_Apprdate = new String(recvBuff, 235, 12, "EUC-KR");
                    recv.str_Apprno = new String(recvBuff, 247, 12, "EUC-KR");
                    recv.str_Dealno = new String(recvBuff, 259, 12, "EUC-KR");
                    recv.str_Dccyn = new String(recvBuff, 271, 1, "EUC-KR");
                    recv.str_Msg1 = new String(recvBuff, 272, 40, "EUC-KR");
                    recv.str_Msg2 = new String(recvBuff, 312, 24, "EUC-KR");
                    recv.str_Msg3 = new String(recvBuff, 336, 24, "EUC-KR");
                    recv.str_Msg4 = new String(recvBuff, 360, 24, "EUC-KR");
                    recv.str_P1 = new String(recvBuff, 384, 9, "EUC-KR");
                    recv.str_P2 = new String(recvBuff, 393, 9, "EUC-KR");
                    recv.str_P3 = new String(recvBuff, 402, 9, "EUC-KR");
                    recv.str_CBStore = new String(recvBuff, 411, 15, "EUC-KR");
                    recv.str_CBApprno = new String(recvBuff, 426, 12, "EUC-KR");
                    recv.str_RealApprmoney = new String(recvBuff, 438, 21, "EUC-KR");
                    recv.str_DealCardno = new String(recvBuff, 459, 20, "EUC-KR");

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    dbHelper.insertDeal(simpleDateFormat.format(new Date()), recv.str_Dealgb, recv.str_Msggb, recv.str_Carddata, recv.str_Money, recv.str_Tax, recv.str_Bongsa, recv.str_Halbu, recv.str_Apprno, recv.str_Apprdate, recv.str_Tid, recv.str_Bgnm, recv.str_Minm, recv.str_Storeno, recv.str_Msg1 + recv.str_Msg2 + recv.str_Msg3 + recv.str_Msg4, recv.str_Recvcode, recv.str_P3, recv.str_Wcc, recv.str_RealApprmoney.substring(9, 10), recv.str_Msgno, recv.str_DealCardno, recv.str_Msgtxt, recv.str_Micode, recv.str_Bgcode);    //OSM20250814 : 매입사,발급사코드 추가

                    Arrays.fill(recvBuff, (byte)0x00);
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Arrays.fill(recvBuff, (byte)0x00);
        btnBack.setEnabled(true);
        btnEnable();
    }

    private void CUPfunc() {
        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 은련PIN OK 버튼 클릭");
        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT]  PIN : " + EncPin);
        PopupClose();

        if (func_code == 0x6C || func_code == 0x9C) { //LJY20250904 : 8BIN/통합결제 적용
            if (mSharedManager.getPreferences().getBoolean("Nocvm", false) == false || (mSharedManager.getPreferences().getBoolean("Nocvm", false) && Long.parseLong(mMoney) > 50000)) {
                //LJY20250904 : 통합결제 바코드 리딩 시 로직 추가 (매체구분 "B" && 결제구분 "B")
                if (Paygb[0] == 'B' && cMediagb[0] == 'B' && !IsBarcodeSign(new String(icdata))) {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 은련 노서명");
                    PopupOpen(DetailDealResult.this, "IC VAN 취소 중입니다.");
                    String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());

                    PosClient posClient = new PosClient();
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 은련취소");
                    sendBuff = ("0437PRO" + mCatid + strDate + "042030H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();

                    if (bRelease)
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(sendBuff)).substring(0, 56) + "*******************************************************************************************************************************" + (new String(sendBuff)).substring(183, sendBuff.length - 183));
                    else
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(sendBuff));

                    if (mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
                        System.arraycopy(String.format("%04d", sendBuff.length).getBytes(), 0, sendBuff, 0, 4);
                        recvBuff = posClient.service_line(mServerip, Integer.parseInt(mServerport), sendBuff);
                    } else if (mSharedManager.getPreferences().getInt("Enctype", 0) == 0) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SEED 암복호화"); //OSM20250113
                        recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), sendBuff);
                    } else {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113
                        iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), sendBuff, recvBuff);   //OSM20250113 : DES암복호화 함수 추가
                    }
                    InsertRecv(recvBuff);
                    PopupClose();
                }
                else {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 은련 서명"); //PAYPRO SKIP

                    //LJY20220427 : 멀티패드 서명 연동
                    if (mSharedManager.getPreferences().getInt("Readertype", 0) == 1 && mSharedManager.getPreferences().getBoolean("Signuse", false)) {
                        if (usbService != null) { // if UsbService was correctly binded, Send data
                            isrun = true;

                            handlerThread = new handler_thread(handler);
                            handlerThread.start();

                            Arrays.fill(RECVBuf, (char) 0x00);
//                                        Arrays.fill(encdata, (char) 0x00);
//                                        Arrays.fill(icdata, (char) 0x00);

                            initSerial();
                            //isMultipad = true;
                            btnDisable();
                            isSign = true;
                            btnBack.setEnabled(true);

                            writeBuffer = new char[53];
                            writeBuffer[0] = 0x02; //Header ID
                            writeBuffer[1] = 0x42; //Command ID
                            writeBuffer[2] = 0x00;
                            writeBuffer[3] = 0x48; //Length(2)
                            String sendstr = "19F316BA33A57729" + "  Please Sign!  " + "                "; //서명문구(48)
                            System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 48);
                            writeBuffer[52] = xor_sum(writeBuffer, 52);

                            temp = new byte[53];
                            for (int i = 0; i < 53; i++) {
                                temp[i] = (byte) writeBuffer[i];
                            }
                            PopupOpenEOT(DetailDealResult.this, "서명 해주세요.");
                            usbService.write(temp);
                        } else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                            Toast.makeText(DetailDealResult.this, "USB 서비스 불가능합니다.", Toast.LENGTH_LONG).show();
                            btnEnable();
                            PopupClose();
                            btnBack.setEnabled(true);
                            return;
                        }
                    } else
                        //LJY20201005 : OKPOS 서명 연동
                        if (mSharedManager.getPreferences().getInt("Readertype", 0) == 2) //OKPOS
                        {
                            mUart = new libUart();
                            isrun = true;

                            Arrays.fill(RECVBuf, (char) 0x00);
//                    Arrays.fill(encdata, (char) 0x00);
//                    Arrays.fill(icdata, (char) 0x00);

                            initSerial();
                            isSign = true;
                            btnDisable();
                            btnBack.setEnabled(false);

                            writeBuffer = new char[53];
                            writeBuffer[0] = 0x02; //Header ID
                            writeBuffer[1] = 0x42; //Command ID
                            writeBuffer[2] = 0x00;
                            writeBuffer[3] = 0x48; //Length(2)
                            String sendstr = "19F316BA33A57729" + "  Please Sign!  " + "                "; //서명문구(48)
                            System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 48);
                            writeBuffer[52] = xor_sum(writeBuffer, 52);

                            temp = new byte[53];
                            for (int i = 0; i < 53; i++) {
                                temp[i] = (byte) writeBuffer[i];
                            }
                            PopupOpenEOT(DetailDealResult.this, "서명 해주세요.");

                            //LJY20201217 : 포트번호/통신속도 가변
                            mUart.Init(mSharedManager.getPreferences().getInt("sPortnum", 0));
                            if (mUart.IsOpen(mSharedManager.getPreferences().getInt("sPortnum", 0)) == false) {
                                mUart.Open(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")), 8, 0, 1, true);
                                mUart.SetBaudrate(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")));
                            }
                            mUart.QueueClear(mSharedManager.getPreferences().getInt("sPortnum", 0));
                            mUart.DataSend(mSharedManager.getPreferences().getInt("sPortnum", 0), temp, temp.length);

                            handlerThread = new handler_thread(handler);
                            handlerThread.start();
                        } else {
                            Intent intent = new Intent(DetailDealResult.this, SignPad.class);
                            startActivityForResult(intent, 1);
                        }
                }
            } else {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 은련 노서명");
                PopupOpen(DetailDealResult.this, "IC VAN 취소 중입니다.");
                String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());

                PosClient posClient = new PosClient();
                //LJY20250904 : 통합결제 바코드 리딩 시 로직 추가 (매체구분 "B" && 결제구분 "B")
                if (Paygb[0] == 'B' && cMediagb[0] == 'B') {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 은련취소");
                    sendBuff = ("0437PRO" + mCatid + strDate + "042030H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                } else
                if(Paygb[0] == 'R' && CardBrand[0] == 'K') { //LJY20200713 : 동반위 JUST TOUCH
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF 은련취소(동반위)");
                    sendBuff = ("0694CUP" + mCatid + strDate + "042030H1          " + mCatid + "K" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                }
                else
                if (Paygb[0] == 'I' || (Paygb[0] == 'R' && Integer.parseInt(new String(icdata, 0, 4)) > 0)) { //LJY20230818
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 은련취소");
                    sendBuff = ("0694CUP" + mCatid + strDate + "042030H1          " + mCatid + "I" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                } else {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS 은련취소");
                    sendBuff = ("0437CUP" + mCatid + strDate + "042030H1          " + mCatid + "A" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                }
                if (bRelease)
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(sendBuff)).substring(0, 56) + "*******************************************************************************************************************************" + (new String(sendBuff)).substring(183, sendBuff.length - 183));
                else
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(sendBuff));
                if (mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
                    System.arraycopy(String.format("%04d", sendBuff.length).getBytes(), 0, sendBuff, 0, 4);
                    recvBuff = posClient.service_line(mServerip, Integer.parseInt(mServerport), sendBuff);
                } else if(mSharedManager.getPreferences().getInt("Enctype", 0) == 0) {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SEED 암복호화"); //OSM20250113
                    recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), sendBuff);
                }
                else {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113
                    iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), sendBuff, recvBuff);   //OSM20250113 : DES암복호화 함수 추가
                }
                InsertRecv(recvBuff);
                PopupClose();
            }
        } else if (func_code == 0x6E || func_code == 0x9E) { //LJY20250904 : 8BIN/통합결제 적용
            if (mSharedManager.getPreferences().getBoolean("Nocvm", false) == false || (mSharedManager.getPreferences().getBoolean("Nocvm", false) && Long.parseLong(mMoney) > 50000)) {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 은련 서명");

                //LJY20220427 : 멀티패드 서명 연동
                if( mSharedManager.getPreferences().getInt("Readertype", 0) == 1 && mSharedManager.getPreferences().getBoolean("Signuse", false) )
                {
                    if (usbService != null) { // if UsbService was correctly binded, Send data
                        isrun = true;

                        handlerThread = new handler_thread(handler);
                        handlerThread.start();

                        Arrays.fill(RECVBuf, (char) 0x00);
//                                        Arrays.fill(encdata, (char) 0x00);
//                                        Arrays.fill(icdata, (char) 0x00);

                        initSerial();
                        //isMultipad = true;
                        btnDisable();
                        isSign = true;
                        btnBack.setEnabled(true);

                        writeBuffer = new char[53];
                        writeBuffer[0] = 0x02; //Header ID
                        writeBuffer[1] = 0x42; //Command ID
                        writeBuffer[2] = 0x00;
                        writeBuffer[3] = 0x48; //Length(2)
                        String sendstr = "19F316BA33A57729" + "  Please Sign!  " + "                "; //서명문구(48)
                        System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 48);
                        writeBuffer[52] = xor_sum(writeBuffer, 52);

                        temp = new byte[53];
                        for (int i = 0; i < 53; i++) {
                            temp[i] = (byte) writeBuffer[i];
                        }
                        PopupOpenEOT(DetailDealResult.this, "서명 해주세요.");
                        usbService.write(temp);
                    } else {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                        Toast.makeText(DetailDealResult.this, "USB 서비스 불가능합니다.", Toast.LENGTH_LONG).show();
                        btnEnable();
                        PopupClose();
                        btnBack.setEnabled(true);
                        return;
                    }
                }
                else
                    //LJY20201005 : OKPOS 서명 연동
                    if (mSharedManager.getPreferences().getInt("Readertype", 0) == 2) //OKPOS
                    {
                        mUart = new libUart();
                        isrun = true;

                        Arrays.fill(RECVBuf, (char) 0x00);
//                                    Arrays.fill(encdata, (char) 0x00);
//                                    Arrays.fill(icdata, (char) 0x00);

                        initSerial();
                        isSign = true;
                        btnDisable();
                        btnBack.setEnabled(false);

                        writeBuffer = new char[53];
                        writeBuffer[0] = 0x02; //Header ID
                        writeBuffer[1] = 0x42; //Command ID
                        writeBuffer[2] = 0x00;
                        writeBuffer[3] = 0x48; //Length(2)
                        String sendstr = "19F316BA33A57729" + "  Please Sign!  " + "                "; //서명문구(48)
                        System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 48);
                        writeBuffer[52] = xor_sum(writeBuffer, 52);

                        temp = new byte[53];
                        for (int i = 0; i < 53; i++) {
                            temp[i] = (byte) writeBuffer[i];
                        }
                        PopupOpenEOT(DetailDealResult.this, "서명 해주세요.");

                        //LJY20201217 : 포트번호/통신속도 가변
                        mUart.Init(mSharedManager.getPreferences().getInt("sPortnum", 0));
                        if (mUart.IsOpen(mSharedManager.getPreferences().getInt("sPortnum", 0)) == false) {
                            mUart.Open(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")), 8, 0, 1, true);
                            mUart.SetBaudrate(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")));
                        }
                        mUart.QueueClear(mSharedManager.getPreferences().getInt("sPortnum", 0));
                        mUart.DataSend(mSharedManager.getPreferences().getInt("sPortnum", 0), temp, temp.length);

                        handlerThread = new handler_thread(handler);
                        handlerThread.start();
                    }
                    else {
                        Intent intent = new Intent(DetailDealResult.this, SignPad.class);
                        startActivityForResult(intent, 1);
                    }
            } else {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 은련 노서명");
                PopupOpen(DetailDealResult.this, "FALLBACK VAN 취소 중입니다.");
                String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());

                PosClient posClient = new PosClient();
                sendBuff = ("0437CUP" + mCatid + strDate + "042030H1          " + mCatid + "F" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + mApprno.substring(0, 8) + mApprdate.substring(0, 6) + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "N").getBytes();
                if (bRelease)
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(sendBuff)).substring(0, 56) + "*******************************************************************************************************************************" + (new String(sendBuff)).substring(183, sendBuff.length - 183));
                else
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(sendBuff));
                if (mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
                    System.arraycopy(String.format("%04d", sendBuff.length).getBytes(), 0, sendBuff, 0, 4);
                    recvBuff = posClient.service_line(mServerip, Integer.parseInt(mServerport), sendBuff);
                } else if(mSharedManager.getPreferences().getInt("Enctype", 0) == 0) {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SEED 암복호화"); //OSM20250113
                    recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), sendBuff);
                }
                else {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113
                    iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), sendBuff, recvBuff);   //OSM20250113 : DES암복호화 함수 추가
                }
                InsertRecv(recvBuff);
                PopupClose();
            }
        }
    }
}