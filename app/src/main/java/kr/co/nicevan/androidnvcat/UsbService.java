package kr.co.nicevan.androidnvcat;

import static kr.co.nicevan.androidnvcat.shared.SharedArray.handlertemp;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.bLogUse;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.bRelease;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.felhr.usbserial.CDCSerialDevice;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.devmel.communication.IUart;
import com.devmel.communication.android.UartUsbOTG;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import kr.co.nicevan.androidnvcat.shared.SharedManager;


public class UsbService extends Service {

    public static final String ACTION_USB_READY = "com.felhr.connectivityservices.USB_READY";
    public static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    public static final String ACTION_USB_NOT_SUPPORTED = "com.felhr.usbservice.USB_NOT_SUPPORTED";
    public static final String ACTION_NO_USB = "com.felhr.usbservice.NO_USB";
    public static final String ACTION_USB_PERMISSION_GRANTED = "com.felhr.usbservice.USB_PERMISSION_GRANTED";
    public static final String ACTION_USB_PERMISSION_NOT_GRANTED = "com.felhr.usbservice.USB_PERMISSION_NOT_GRANTED";
    public static final String ACTION_USB_DISCONNECTED = "com.felhr.usbservice.USB_DISCONNECTED";
    public static final String ACTION_CDC_DRIVER_NOT_WORKING = "com.felhr.connectivityservices.ACTION_CDC_DRIVER_NOT_WORKING";
    public static final String ACTION_USB_DEVICE_NOT_WORKING = "com.felhr.connectivityservices.ACTION_USB_DEVICE_NOT_WORKING";
    public static final int MESSAGE_FROM_SERIAL_PORT = 0;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final int BAUD_RATE = 115200; // BaudRate. Change this value if you need
    public static boolean SERVICE_CONNECTED = false;

    private IBinder binder = new UsbBinder();

    private Context context;
    private Handler mHandler;
    private UsbManager usbManager;
    private UsbDevice device;
    private UsbDeviceConnection connection;
    private UsbSerialDevice serialPort;

    public boolean serialPortConnected;
    /*
     *  Data received from serial port will be received here. Just populate onReceivedData with your code
     *  In this particular example. byte stream is converted to String and send to UI thread to
     *  be treated there.
     */
    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] arg0) {
            try {
                String data = new String(arg0, "UTF-8");
                if (mHandler != null)
                    //mHandler.obtainMessage(MESSAGE_FROM_SERIAL_PORT, data).sendToTarget();
                    mHandler.obtainMessage(MESSAGE_FROM_SERIAL_PORT, arg0).sendToTarget();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };
    /*
     * Different notifications from OS will be received here (USB attached, detached, permission responses...)
     * About BroadcastReceiver: http://developer.android.com/reference/android/content/BroadcastReceiver.html
     */
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if (arg1.getAction().equals(ACTION_USB_PERMISSION)) {
                synchronized (this) {
                    boolean granted = arg1.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                    if (granted) // User accepted our USB connection. Try to open the device as a serial port
                    {
                        Intent intent = new Intent(ACTION_USB_PERMISSION_GRANTED);
                        arg0.sendBroadcast(intent);
                        connection = usbManager.openDevice(device);
                        serialPortConnected = true;
                        new ConnectionThread().run();
                    } else // User not accepted our USB connection. Send an Intent to the Main Activity
                    {
                        Intent intent = new Intent(ACTION_USB_PERMISSION_NOT_GRANTED);
                        arg0.sendBroadcast(intent);
                    }
                }
            } else if (arg1.getAction().equals(ACTION_USB_ATTACHED)) {
                if (!serialPortConnected)
                    findSerialPortDevice(); // A USB device has been attached. Try to open it as a Serial port
            } else if (arg1.getAction().equals(ACTION_USB_DETACHED)) {
                // Usb device was disconnected. send an intent to the Main Activity
                Intent intent = new Intent(ACTION_USB_DISCONNECTED);
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] ACTION_USB_DISCONNECTED");

                arg0.sendBroadcast(intent);
                serialPortConnected = false;
                //LJY20251205 : NULL 체크를 안해서 서비스가 죽어버리는 걸 방지
                //serialPort 초기화 안 된 상태로 DETACHED 받으면 UsbService 크래시로 NULL이 될 가능성 있음
                if (serialPort != null) {
                    try {
                        serialPort.close();
                    } catch (Exception e) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 2@@@serialPort 객체 널 체크 중입니다. " + e.toString());
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    /*
     * onCreate will be executed when service is started. It configures an IntentFilter to listen for
     * incoming Intents (USB ATTACHED, USB DETACHED...) and it tries to open a serial port.
     */
    @Override
    public void onCreate() {
        this.context = this;
        serialPortConnected = false;
        UsbService.SERVICE_CONNECTED = true;
        setFilter();
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        findSerialPortDevice();
    }

    /* MUST READ about services
     * http://developer.android.com/guide/components/services.html
     * http://developer.android.com/guide/components/bound-services.html
     */
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 3@@@UsbService 죽었습니다. 자동으로 다시 시작합니다.");
        //return Service.START_NOT_STICKY;
        return Service.START_STICKY; //LJY20251205 : 서비스가 죽어도 다시 살아나도록 변경
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UsbService.SERVICE_CONNECTED = false;

        //LJY20251205 : 리시버 중복 등록 방지
        try {
            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 4@@@리시버 중복 방지를 위해 실행합니다.");
            unregisterReceiver(usbReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /*
     * This function will be called from MainActivity to write data through Serial Port
     */
    public void write(byte[] data) {
        if(!bRelease) SharedManager.LogBinHex("Serial SData", data); //LJY20230911 : 로그 추가

        if (serialPort != null)
            serialPort.write(data);
    }

    public void setHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    private void findSerialPortDevice() {
        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] findSerialPortDevice");
        // This snippet will try to open the first encountered usb device connected, excluding usb root hubs
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            SharedManager.getInstance(getApplicationContext()).getPreferences().edit().putBoolean("DeviceList", true).commit(); //LJY20251208 : UsbService 자동 재실행 시 MainActivty 의존하면 죽는 증상 발생하여 서비스 스스로 SharedManager 가져오도록 수정
            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DeviceList 있음");

            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                int devicePID = device.getProductId();

                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] deviceVID : " + deviceVID);
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] devicePID : " + devicePID);

                if(SharedManager.getInstance(getApplicationContext()).getPreferences().getInt("Readertype", 0) == 4 && deviceVID == 1027 && devicePID != 24577) //LJY20220816 : 조은소프트웨어이면서 VID 1027이면서 PID 24577아니면 사용 불가
                {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 조은소프트웨어 연결 강제 거절");
                    connection = null;
                    device = null;
                }
                else
                    //if (UsbSerialDevice.isSupported(device)){
                    if (deviceVID == 1027) { //20200413 : FTDI ATTACH시
                        //if (deviceVID != 0x1d6b && (devicePID != 0x0001 || devicePID != 0x0002 || devicePID != 0x0003)) {
                        // There is a device connected to our Android device. Try to open it as a Serial Port.
                        requestUserPermission();
                        keep = false;
                    } else {
                        connection = null;
                        device = null;
                    }

                if (!keep)
                    break;
            }
            if (!keep) {
                // There is no USB devices connected (but usb host were listed). Send an intent to MainActivity.
                Intent intent = new Intent(ACTION_NO_USB);
                sendBroadcast(intent);
            }
            else { //LJY20230807 : 리더기 연결 없이 RESTART 시 죽는 증상 방지
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] ACTION_USB_NOT_SUPPORTED");
                Intent intent = new Intent(ACTION_USB_NOT_SUPPORTED);
                sendBroadcast(intent);
            }
        } else {
            // There is no USB devices connected. Send an intent to MainActivity
            SharedManager.getInstance(getApplicationContext()).getPreferences().edit().putBoolean("DeviceList", false).commit(); //LJY20251208 : UsbService 자동 재실행 시 MainActivty 의존하면 죽는 증상 발생하여 서비스 스스로 SharedManager 가져오도록 수정
            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DeviceList 없음");
            Intent intent = new Intent(ACTION_NO_USB);
            sendBroadcast(intent);
        }
    }

    private void setFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_DETACHED);
        filter.addAction(ACTION_USB_ATTACHED);
        registerReceiver(usbReceiver, filter);
    }

    /*
     * Request user permission. The response will be received in the BroadcastReceiver
     */
    private void requestUserPermission() {
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        usbManager.requestPermission(device, mPendingIntent);
    }

    public class UsbBinder extends Binder {
        public UsbService getService() {
            return UsbService.this;
        }
    }

    /*
     * A simple thread to open a serial port.
     * Although it should be a fast operation. moving usb operations away from UI thread is a good thing.
     */
    private class ConnectionThread extends Thread {
        @Override
        public void run() {
            serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
            if (serialPort != null) {
                if (serialPort.open()) {
                    int baudrate = 0;

                    switch (SharedManager.getInstance(getApplicationContext()).getPreferences().getInt("Baudrate", 0)) {
                        case 0:
                            baudrate = 9600;
                            break;
                        case 1:
                            baudrate = 19200;
                            break;
                        case 2:
                            baudrate = 38400;
                            break;
                        case 3:
                            baudrate = 57600;
                            break;
                        case 4:
                            baudrate = 115200;
                            break;
                        default:
                            baudrate = 115200;
                            break;
                    }
                    //serialPort.setBaudRate(BAUD_RATE);
                    serialPort.setBaudRate(baudrate);

                    serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                    serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                    serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                    serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                    serialPort.read(mCallback);

                    // Everything went as expected. Send an intent to MainActivity
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] ACTION_USB_READY");
                    Intent intent = new Intent(ACTION_USB_READY);
                    context.sendBroadcast(intent);
                } else {
                    // Serial port could not be opened, maybe an I/O error or if CDC driver was chosen, it does not really fit
                    // Send an Intent to Main Activity
                    if (serialPort instanceof CDCSerialDevice) {
                        Intent intent = new Intent(ACTION_CDC_DRIVER_NOT_WORKING);
                        context.sendBroadcast(intent);
                    } else {
                        Intent intent = new Intent(ACTION_USB_DEVICE_NOT_WORKING);
                        context.sendBroadcast(intent);
                    }
                }
            } else {
                // No driver for given device, even generic CDC driver could not be loaded
                Intent intent = new Intent(ACTION_USB_NOT_SUPPORTED);
                context.sendBroadcast(intent);
            }
        }
    }
}
