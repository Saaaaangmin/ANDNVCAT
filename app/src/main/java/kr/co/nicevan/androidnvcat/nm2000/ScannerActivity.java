package kr.co.nicevan.androidnvcat.nm2000;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.zoacardreader.ZOABLEManager;
import com.zoacardreader.ZOACardPeripheral;

import kr.co.nicevan.androidnvcat.R;

public class ScannerActivity extends DialogFragment
{
    public final static String LogTag = ScannerActivity.class.getName();
    ScannerAdapter      mAdapter;
    ZOABLEManager mManager;
    ScannerCallback     mCallback;

    @Override
    public void onAttach(final Context context)
    {
        super.onAttach(context);
        this.mCallback = (ScannerCallback) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy()
    {
        this.stop();
        super.onDestroy();
    }

    @Override
    public void onCancel(DialogInterface dialog)
    {
        super.onCancel(dialog);

        mCallback.onScannerCancelled();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        try
        {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.activity_scanner, null);
            final ListView listView = (ListView) dialogView.findViewById(R.id.list);
            listView.setAdapter(mAdapter = new ScannerAdapter(getActivity()));
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
                {
                    stop();
                    getDialog().dismiss();
                    final ZOACardPeripheral device = (ZOACardPeripheral)mAdapter.getItem(i);
                    mCallback.onScannerDeviceSelected(device);
                }
            });
            builder.setTitle("Scanning Card reader");
            final AlertDialog dialog = builder.setView(dialogView).create();

            mManager = ZOABLEManager.getInstance(this.getContext());
            mManager.setCallback(mAdapter);

            if (savedInstanceState == null)
                this.start();

            return dialog;
        }
        catch (Exception ex)
        {
            Log.e(LogTag, ex.getMessage());
            return null;
        }
    }

    private void start()
    {
        try
        {
            this.mAdapter.clearDevices();
            this.mManager.startDiscover();
        }
        catch (Exception ex)
        {
            makeText(this.getContext(), ex.getMessage(), LENGTH_SHORT).show();
        }
    }


    private void stop()
    {
        this.mManager.stopDiscovery();
    }
}
