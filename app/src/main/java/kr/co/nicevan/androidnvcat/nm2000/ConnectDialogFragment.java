package kr.co.nicevan.androidnvcat.nm2000;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.zoacardreader.ZOABLEManager;
import com.zoacardreader.ZOABLEManagerCallback;
import com.zoacardreader.ZOACardPeripheral;

import kr.co.nicevan.androidnvcat.R;

public class ConnectDialogFragment extends DialogFragment
{
    private ConnectDialogFragment self;
    private String mMAC;
    private ScannerCallback mCallback;

    public static ConnectDialogFragment newInstance(String mac, ScannerCallback callback)
    {
        ConnectDialogFragment instance =  new ConnectDialogFragment();

        instance.mMAC = mac;
        instance.mCallback = callback;

        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        self = this;
        ZOABLEManager.getInstance(getContext()).setCallback(mScanCallback);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        ZOABLEManager.getInstance(getContext()).setCallback(null);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        ZOABLEManager.getInstance(getContext()).startDiscover();
    }

    @Override
    public void onStop()
    {
        super.onStop();

        ZOABLEManager.getInstance(getContext()).stopDiscovery();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_progress, null);

        //OSM20250805 : SDK Version 26으로 변경함에 따른 처리
        Context context = getContext();
        if (context == null) {
            return super.onCreateDialog(savedInstanceState); // fallback 처리
        }

        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext());

//        alertDialogBuilder
//                .setView(view)
//                .setCancelable(false)
//                .setNegativeButton("취소", null);
//        final AlertDialog alertDialog = alertDialogBuilder.show();

       AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.
                setView(view)
            .setCancelable(false)
            .setNegativeButton("취소", null);
        final AlertDialog alertDialog = alertDialogBuilder.show();

        // disable touch outside cancel
        alertDialog.setCanceledOnTouchOutside(false);

        return alertDialog;
    }

    ZOABLEManagerCallback mScanCallback = new ZOABLEManagerCallback()
    {
        @Override
        public void onDeviceDiscovered(ZOACardPeripheral peripheral)
        {
            Log.d("Scan", peripheral.getMACAddress());

            if (peripheral.getMACAddress().equals(mMAC))
            {
                ZOABLEManager.getInstance(getContext()).stopDiscovery();
                ZOABLEManager.getInstance(getContext()).setCallback(null);

                self.mCallback.onScannerDeviceSelected(peripheral);

                self.dismiss();
            }
        }
    };
}
