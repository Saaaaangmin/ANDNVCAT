package kr.co.nicevan.androidnvcat.nm2000;


import com.zoacardreader.ZOACardPeripheral;

public interface ScannerCallback
{
    void onScannerDeviceSelected(ZOACardPeripheral device);
    void onScannerCancelled();
}
