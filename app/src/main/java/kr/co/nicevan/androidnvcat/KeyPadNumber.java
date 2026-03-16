package kr.co.nicevan.androidnvcat;

import static kr.co.nicevan.androidnvcat.MainActivity.presentationDisplays;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.deleteStatusBar;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.presentation;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.LogDebug;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.bLogUse;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.MainThread;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicBoolean;

import kr.co.nicevan.androidnvcat.shared.OverlayPopupManager;
import kr.co.nicevan.androidnvcat.shared.SharedManager;

public class KeyPadNumber extends Activity {

    public static Button buttonOK, buttonExit, buttonClear, buttonBackspace, button1, button2, button3, button4, button5, button6, button7, button8, button9, button0, button010; //LJY20221202 : public으로 변경
    EditText etCashNum;
    TextView tvKeypadtitle;

    CountDownTimer mcountdowntimer = null;

    private void PopupSetting() {
        // 팝업이 올라오면 배경 블러처리
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.2f;
        getWindow().setAttributes(layoutParams);

//        Presentation의 기존 LayoutParams를 가져온 후 수정
//        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
//        layoutParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
//        layoutParams.dimAmount = 0.2f;
//        getWindow().setAttributes(layoutParams);


        Display dp = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); //디스플레이 화면 사이즈 구하기

        getWindow().getAttributes().width = (int) (dp.getWidth() * 0.6); //가로 전체에 60프로
        getWindow().getAttributes().height = (int) (dp.getHeight() * 0.4); //세로 전체에 40프로 //LJY20221202 : 사이즈 변경
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
        setContentView(R.layout.activity_key_pad_number);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        deleteStatusBar(getWindow()); //OSM20230911 : 결제 팝업 시 하단 네비게이션 바 제거

        PopupSetting();

        etCashNum = (EditText) findViewById(R.id.etcashinputnum);
        etCashNum.setInputType(0);

        buttonOK = (Button) findViewById(R.id.ButtonOK);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.putExtra("RESULT", etCashNum.getText().toString());
                setResult(Activity.RESULT_OK, i);
                finish();
            }
        });
        buttonExit = (Button) findViewById(R.id.ButtonExit);
        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });
        buttonClear = (Button) findViewById(R.id.ButtonClear);
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etCashNum.setText("");
            }
        });
        buttonBackspace = (Button) findViewById(R.id.ButtonBackSpace);
        buttonBackspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mCashNum = etCashNum.getText().toString();
                int mCashNumLen = etCashNum.getText().length();
                if (mCashNumLen < 1)
                    etCashNum.setText("");
                else {
                    etCashNum.setText(mCashNum.substring(0, mCashNumLen - 1));
                }
            }
        });
        button1 = (Button) findViewById(R.id.Button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etCashNum.append("1");
            }
        });
        button2 = (Button) findViewById(R.id.Button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etCashNum.append("2");
            }
        });
        button3 = (Button) findViewById(R.id.Button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etCashNum.append("3");
            }
        });
        button4 = (Button) findViewById(R.id.Button4);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etCashNum.append("4");
            }
        });
        button5 = (Button) findViewById(R.id.Button5);
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etCashNum.append("5");
            }
        });
        button6 = (Button) findViewById(R.id.Button6);
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etCashNum.append("6");
            }
        });
        button7 = (Button) findViewById(R.id.Button7);
        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etCashNum.append("7");
            }
        });
        button8 = (Button) findViewById(R.id.Button8);
        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etCashNum.append("8");
            }
        });
        button9 = (Button) findViewById(R.id.Button9);
        button9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etCashNum.append("9");
            }
        });
        button0 = (Button) findViewById(R.id.Button0);
        button0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etCashNum.append("0");
            }
        });
        button010 = (Button) findViewById(R.id.Button010);
        button010.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etCashNum.append("010");
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
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        }.start();


//        if (SharedManager.getInstance(KeyPadNumber.this).getPreferences().getBoolean("DualScreenuse", false)) {
//            DisplayManager dm = (DisplayManager) KeyPadNumber.this.getSystemService(Context.DISPLAY_SERVICE);
//            Display[] displays = dm.getDisplays();
//
//            // 디버깅 로그 출력
//            for (Display d : displays) {
//                LogDebug(bLogUse, "debugjy", "[NVCAT] ID: " + d.getDisplayId() + ", Name: " + d.getName() + ", Flags: " + d.getFlags());
//            }
//
//            // 기본 디스플레이(ID 0 제외) 중 첫 번째를 외부 디스플레이로 선택
//            Display targetDisplay = null;
//            for (Display d : displays) {
//                if (d.getDisplayId() != 0) {
//                    targetDisplay = d;
//                    break;
//                }
//            }
//
//            if (targetDisplay != null) {
//                try {
//                    // Android 12(API 31)+부터는 createWindowContext 사용 필요
//                    Context windowContext;
//
//                    windowContext = KeyPadNumber.this.createDisplayContext(targetDisplay);
//
//
//                    // Presentation 생성 및 표시
//                    presentation = new DifferentDisplay(windowContext, targetDisplay, 5, "");
//                    presentation.show();
//
//                    LogDebug(bLogUse, "debugjy", "[NVCAT] Presentation started on display ID: " + targetDisplay.getDisplayId());
//
//                } catch (Exception e) {
//                    LogDebug(bLogUse, "debugjy", "[NVCAT] Presentation error: " + e.toString());
//                }
//            } else {
//                LogDebug(bLogUse, "debugjy", "[NVCAT] No valid external display found.");
//            }
//        }

//        //LJY20221202 : 듀얼 스크린 사용 시 키패드 팝업
//        if (SharedManager.getInstance(KeyPadNumber.this).getPreferences().getBoolean("DualScreenuse", false)) {
//            if (presentationDisplays != null && presentationDisplays.length > 0) {
//                for (int i = 0; i < presentationDisplays.length; i++) {
//                    if (presentationDisplays[i].getDisplayId() == 1) {
//                        presentation = (DifferentDisplay) new DifferentDisplay(KeyPadNumber.this, presentationDisplays[presentationDisplays.length - 1], 5, "");
//                        presentation.show();
//                    }
//                }
//            }
//        }

        //OSM20260205 : 외부 디스플레이 표시
        if (SharedManager.getInstance(KeyPadNumber.this).getPreferences().getBoolean("DualScreenuse", false)) {
            OverlayPopupManager.showOnAllExternalDisplaysByGb(KeyPadNumber.this, 5, "");
        }


    }
}


