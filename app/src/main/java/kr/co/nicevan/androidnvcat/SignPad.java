package kr.co.nicevan.androidnvcat;

import static kr.co.nicevan.androidnvcat.MainActivity.presentationDisplays;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.presentation;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.LogDebug;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.bLogUse;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

import com.github.gcacace.signaturepad.views.SignaturePad;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import kr.co.nicevan.androidnvcat.shared.OverlayPopupManager;
import kr.co.nicevan.androidnvcat.shared.SharedArray;
import kr.co.nicevan.androidnvcat.shared.SharedManager;
import kr.co.nicevan.nicesigncomp.NiceSignComp;
import kr.co.nicevan.signenc.SignEnc;

public class SignPad extends Activity {

    private SignaturePad mSignaturePad;
    private Button mClearButton;
    public static  Button mSaveButton; //LJY20221202 : public 변경
    public static ImageButton mCancelButton; //LJY20221202 : public 변경

    SignEnc nicesign;
    NiceSignComp signcomp;
    public static int iTSuse = 0; //LJY20221202 : 듀얼 스크린 서명 데이터 유무 체크

    public static byte[] in = new byte[1024]; //LJY20221202 : 선언 추가
    byte[] out = new byte[1048]; //LJY20221202 : static 제거

    CountDownTimer mcountdowntimer = null;

    private void PopupSetting() {
        // 팝업이 올라오면 배경 블러처리
//        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
//        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
//        layoutParams.dimAmount = 0.2f;
//        getWindow().setAttributes(layoutParams);
//
//        Display dp = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); //디스플레이 화면 사이즈 구하기
//        getWindow().getAttributes().width = (int) (dp.getWidth() * 0.8); //가로 전체에 80프로
//        getWindow().getAttributes().height = (int) (dp.getHeight() * 0.5); //세로 전체에 50프로
        setFinishOnTouchOutside(false); //액티비티 바깥화면이 클릭되어도 종료되지 않게 설정하기
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_sign_pad);
        getWindow().setStatusBarColor(Color.TRANSPARENT); //LJY20211119 : 터치패드 호출시 상태 바 안보이게 변경
        getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);

        //LJY20211119 : 터치패드 호출시 네비게이션 바 안보이게 변경
        View decorView = getWindow().getDecorView();
        int	uiOption;
        uiOption = getWindow().getDecorView().getSystemUiVisibility();
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH )
            uiOption |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN )
            uiOption |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT )
            uiOption |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        decorView.setSystemUiVisibility( uiOption );
        PopupSetting();

        nicesign = new SignEnc();
        signcomp = new NiceSignComp();

        mSignaturePad = (SignaturePad) findViewById(R.id.signature_pad);
        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
                //Toast.makeText(MainActivity.this, "OnStartSigning", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSigned() {
                mSaveButton.setEnabled(true);
                mClearButton.setEnabled(true);
                mCancelButton.setEnabled(true);
            }

            @Override
            public void onClear() {
                mSaveButton.setEnabled(false);
                mClearButton.setEnabled(false);
                mCancelButton.setEnabled(true);
            }
        });

        mClearButton = (Button) findViewById(R.id.clear_button);
        mSaveButton = (Button) findViewById(R.id.save_button);
        //mCancelButton = (Button) findViewById(R.id.cancel_button);
        mCancelButton = (ImageButton) findViewById(R.id.cancel_button);

        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSignaturePad.clear();
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (iTSuse == 0) { //LJY20221202 : 듀얼 스크린 서명 데이터 없는 경우만
                    Bitmap signatureBitmap = mSignaturePad.getSignatureBitmap();

                    Bitmap largeMono = Bitmap.createScaledBitmap(signatureBitmap, 128, 64, true); //128x64 단색 비트맵
                    Bitmap smallMono = createBlackAndWhite(largeMono);

                    ByteBuffer saveBuffer = null;
                    try {
                        saveBuffer = bmpSaveBuffer(smallMono); //BMP 버퍼 저장
                    } catch (Exception e) {

                    }

                    in = saveBuffer.array();
                }
                iTSuse = 0;

                int ret = nicesign.GetEncData(in, out);

                saveBmp(in, getApplicationContext());

                StringBuffer sb = new StringBuffer(out.length * 2);
                String hexNumber;
                for (int x = 0; x < out.length; x++) {
                    hexNumber = "0" + Integer.toHexString(0xff & out[x]);
                    sb.append(hexNumber.substring(hexNumber.length() - 2));
                }
                Log.d("JY", sb.toString());

                /////////////////////////////////////////////////////////////////
                // 나이스 캡처 서명 데이터 -> 나이스 압축 서명 데이터 변환
                byte[] compsigndata1 = new byte[4096];

                ret = signcomp.PdaToComp(sb.toString().getBytes(), compsigndata1);
                if (ret == 1) {
                    Log.d("JY", new String(compsigndata1));
                } else {
                    Log.d("JY", "에러");
                }

                /////////////////////////////////////////////////////////////////
                // bmp 이미지 -> 나이스 압축 서명 데이터 변환
                byte[] compsigndata2 = new byte[4096];

                ret = signcomp.BmpToComp(in, compsigndata2);
                if (ret == 1) {
                    mcountdowntimer.cancel();

                    Log.d("JY", new String(compsigndata2));
                    Intent i = new Intent();
                    i.putExtra("RESULT", new String(compsigndata2));
                    i.putExtra("SIGN", out);
                    setResult(RESULT_OK, i);
                    finish();
                } else {
                    Log.d("JY", "에러");
                    mcountdowntimer.cancel();

                    Intent i = new Intent();
                    setResult(RESULT_CANCELED, i);
                    finish();
                }
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mcountdowntimer.cancel();

                Intent i = new Intent();
                setResult(RESULT_CANCELED, i);
                finish();
            }
        });

        //카운트다운 변수에 제한시간동안 시간간격동안 타이머 설정
        mcountdowntimer = new CountDownTimer((Long.parseLong(SharedManager.getInstance(getApplicationContext()).getPreferences().getString("Timeout", "30")) + 30) * 1000, 500) {
            @Override
            //타이머가 종료될때까지 동작하는 함수
            public void onTick(long millisUntilFinished) {
            }

            @Override
            //타이머가 종료될때 실행하는 함수
            public void onFinish() {
                Intent i = new Intent();
                setResult(RESULT_CANCELED, i);
                finish();
            }
        }.start();


        //LJY20221202 : 듀얼 스크린 사용 시 서명패드 팝업
//        if (SharedManager.getInstance(SignPad.this).getPreferences().getBoolean("DualScreenuse", false)) {
//            if (presentationDisplays != null && presentationDisplays.length > 0) {
//                for (int i = 0; i < presentationDisplays.length; i++) {
//                    if (presentationDisplays[i].getDisplayId() == 1) {
//                        presentation = (DifferentDisplay) new DifferentDisplay(SignPad.this, presentationDisplays[presentationDisplays.length - 1], 4, "");
//                        presentation.show();
//                    }
//                }
//            }
//        }

        //OSM20260205 : 외부 디스플레이 표시
        if (SharedManager.getInstance(SignPad.this).getPreferences().getBoolean("DualScreenuse", false)) {
            OverlayPopupManager.showOnAllExternalDisplaysByGb(SignPad.this, 4, "");
        }
    }

    public void saveBmp(byte[] in, Context contest) {
        File photo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), String.format("Signature_%d.bmp", System.currentTimeMillis()));
        try {
            FileOutputStream output = new FileOutputStream(photo);
            output.write(in);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(photo);
        mediaScanIntent.setData(contentUri);
        contest.sendBroadcast(mediaScanIntent);
    }

    public static Bitmap createBlackAndWhite(Bitmap src) {
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color informations
        int A, R, G, B;
        int pixel;

        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                int gray = (int) (0.2989 * R + 0.5870 * G + 0.1140 * B);

                // use 128 as threshold, above -> white, below -> black
                if (gray > 128)
                    gray = 255;
                else
                    gray = 0;
                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, gray, gray, gray));
            }
        }
        return bmOut;
    }

    public ByteBuffer bmpSaveBuffer(Bitmap mBitmap) throws Exception {

        // 이미지 사이즈
        int mWidth = mBitmap.getWidth();
        int mHeight = mBitmap.getHeight();

        // 픽셀 갯수
        int[] mPixel = new int[mWidth * mHeight];

        // 이미지 크기 (1비트로 구성된 이미지이므로 8을 나누어 줘야 함)
        int imgSize = mPixel.length / 8;

        // 비트맵 데이터를 찾을 수 있는 시작 주소
        int imgOffset = 0x3E;

        // 비트맵 이미지의 최종 사이즈
        int fileSize = imgSize + imgOffset;

        // 받아온 Bitmap으로부터 픽셀 정보를 int 배열로 받아옴
        // 픽셀 한 개당 32비트의 데이터로 가져오게 됨 (FF FF FF FF)
        // 순서대로 (Alpha Red Green Blue)임
        mBitmap.getPixels(mPixel, 0, mWidth, 0, 0, mWidth, mHeight);

        // 파일 정보를 담아둘 바이트 버퍼를 생성한다
        ByteBuffer mBuffer = ByteBuffer.allocate(fileSize);

        try {

            // 헤더 시작
            mBuffer.put((byte) 0x42);
            mBuffer.put((byte) 0x4D);
            mBuffer.put(putInt(fileSize));
            mBuffer.put(putShort((short) 0));
            mBuffer.put(putShort((short) 0));
            mBuffer.put(putInt(imgOffset));
            mBuffer.put(putInt(0x28));
            mBuffer.put(putInt(mWidth));
            mBuffer.put(putInt(mHeight));
            mBuffer.put(putShort((short) 1));
            mBuffer.put(putShort((short) 1));
            mBuffer.put(putInt(0));
            mBuffer.put(putInt(imgSize));
            mBuffer.put(putInt(0));
            mBuffer.put(putInt(0));
            mBuffer.put(putInt(0));
            mBuffer.put(putInt(0));
            // 여기서 사용할 색상 선택
            // 1비트로 표현되므로 두 가지 색상만 선택 가능
            mBuffer.put(putInt(0)); // 검정색
            mBuffer.put(putInt(0x00FFFFFF)); // 흰색

            // 여기부터 비트맵 데이터
            int height = mHeight;
            int width = mWidth;
            int startPosition = 0;
            int endPosition = 0;

            while (height > 0) {
                startPosition = (height - 1) * width;
                endPosition = height * width;
                int[] iTemp = new int[8];

                for (int i = startPosition; i < endPosition; ) {
                    if ((i + 7) <= endPosition) {
                        for (int j = 7; j >= 0; j--) {
                            iTemp[j] = mPixel[i++];
                        }
                    } else {
                        int mLength = endPosition - i;
                        for (int j = 7; j >= (8 - mLength); j++) {
                            iTemp[j] = mPixel[i++];
                        }
                        for (int j = (7 - mLength); j >= 0; j++) {
                            iTemp[j] = 0x00FFFFFF;
                        }
                    }

                    mBuffer.put(putIntToBit(iTemp));

                }
                height--;
            }

        } catch (IOException e1) {
            e1.printStackTrace();
            throw e1;
        } finally {

        }

        return mBuffer;

    }

    private byte[] putInt(int value) throws IOException {

        byte[] b = new byte[4];
        b[0] = (byte) (value & 0x000000FF);
        b[1] = (byte) ((value & 0x0000FF00) >> 8);
        b[2] = (byte) ((value & 0x00FF0000) >> 16);
        b[3] = (byte) ((value & 0xFF000000) >> 24);

        return b;
    }

    private byte[] putShort(short value) throws IOException {

        byte[] b = new byte[2];

        b[0] = (byte) (value & 0x00FF);
        b[1] = (byte) ((value & 0xFF00) >> 8);

        return b;
    }

    private byte putIntToBit(int[] value) throws IOException {

        byte bReturn = (byte) 0xFF;
        byte[] b = new byte[3];

        for (int i = 0; i < value.length; i++) {
            // 알파값은 버리고 RGB값만 사용함
            b[0] = (byte) (value[i] & 0x000000FF);
            b[1] = (byte) ((value[i] & 0x0000FF00) >> 8);
            b[2] = (byte) ((value[i] & 0x00FF0000) >> 16);

            if ((b[0] != (byte) 0xFF) || (b[1] != (byte) 0xFF) || (b[2] != (byte) 0xFF)) {
                // 셋중 하나라도 FF가 아니면 검정색처리
                bReturn -= Math.pow(2, i);
            }
        }
        return (byte) bReturn;
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        private String resp;
        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... params) {
            publishProgress("Sleeping..."); // Calls onProgressUpdate()
            try {
                int time = Integer.parseInt(params[0]) * 1000;

                Thread.sleep(time);
                resp = "Slept for " + params[0] + " seconds";
            } catch (InterruptedException e) {
                e.printStackTrace();
                resp = e.getMessage();
            } catch (Exception e) {
                e.printStackTrace();
                resp = e.getMessage();
            }
            return resp;
        }


        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
        }


        @Override
        protected void onPreExecute() {
        }


        @Override
        protected void onProgressUpdate(String... text) {
        }
    }
}
