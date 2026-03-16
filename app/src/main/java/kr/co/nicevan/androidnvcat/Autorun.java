//LJY20210927 : 일부 기기에서 재부팅시 APP 켜지지 않는 증상 해결
package kr.co.nicevan.androidnvcat;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class Autorun extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        try {
            ComponentName compName = new ComponentName("kr.co.nicevan.androidnvcat", "kr.co.nicevan.androidnvcat.MainActivity");
            Intent newIntent = new Intent(Intent.ACTION_MAIN);
            newIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            newIntent.setComponent(compName);

            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //LJY20220215 : 에러 팝업 삭제
            newIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            context.startActivity(newIntent);

            System.exit(0);
        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
