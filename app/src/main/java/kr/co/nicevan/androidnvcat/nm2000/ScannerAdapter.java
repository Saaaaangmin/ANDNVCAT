package kr.co.nicevan.androidnvcat.nm2000;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zoacardreader.ZOABLEManagerCallback;
import com.zoacardreader.ZOACardPeripheral;

import java.util.ArrayList;

import kr.co.nicevan.androidnvcat.R;

public class ScannerAdapter extends BaseAdapter implements ZOABLEManagerCallback
{
    ArrayList<ZOACardPeripheral> mPeripherals = new ArrayList<ZOACardPeripheral>();
    Context mContext;
    ScannerCallback mCallback;

    public ScannerAdapter(Context context)
    {
        mContext = context;
    }

    @Override
    public int getCount()
    {
        return mPeripherals.size();
    }

    @Override
    public Object getItem(int i)
    {
        return mPeripherals.get(i);
    }

    @Override
    public long getItemId(int i)
    {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent)
    {
        final LayoutInflater inflater = LayoutInflater.from(mContext);

        if (view == null)
        {
            view = inflater.inflate(R.layout.device_list_row, parent, false);

            final ViewHolder holder = new ViewHolder();
            holder.name = (TextView) view.findViewById(R.id.name);
            holder.address = (TextView) view.findViewById(R.id.address);
            holder.rssi = (ImageView) view.findViewById(R.id.rssi);
            view.setTag(holder);
        }

        final ZOACardPeripheral device = (ZOACardPeripheral)getItem(i);
        final ViewHolder holder = (ViewHolder)view.getTag();
        final String name = device.name;
        holder.name.setText(name);
        holder.address.setText(device.mac);

        final int rssiPercent = (int)(100.0f * (127.0f + device.rssi) / (127.0f + 20.0f));
        holder.rssi.setImageLevel(rssiPercent);
        holder.rssi.setVisibility(view.VISIBLE);

        return view;
    }

    private ZOACardPeripheral findDevice(ZOACardPeripheral target)
    {
        for (final ZOACardPeripheral device : mPeripherals)
            if (device.matches(target.mac))
                return device;
        return null;
    }

    public void clearDevices()
    {
        this.mPeripherals.clear();
    }

    @Override
    public void onDeviceDiscovered(ZOACardPeripheral device)
    {
        try
        {
            if (findDevice(device) != null)
                return;

            this.mPeripherals.add((device));
        }
        finally
        {
            this.notifyDataSetChanged();
        }
    }

    public class ViewHolder {
        private TextView name;
        private TextView address;
        private ImageView rssi;
    }
}
