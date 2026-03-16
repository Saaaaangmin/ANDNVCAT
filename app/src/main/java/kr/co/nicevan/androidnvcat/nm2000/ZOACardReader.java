package kr.co.nicevan.androidnvcat.nm2000;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.zoacardreader.ZOACardPeripheral;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ZOACardReader implements com.zoacardreader.ZOACardPeripheralCallback
{
    private static final String LogTag = YTICallback.class.getName();

    public static ZOACardReader currentDevice;

    private final ZOACardPeripheral device;
    private command_table_t cur_command = null;
    public static boolean connected = false;
    private Timer respTimer;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private ArrayList<YTICallback> delegates = new ArrayList<YTICallback>();

    public ZOACardReader(ZOACardPeripheral device)
    {
        this.device = device;
        this.device.setCallback(this);

        currentDevice = this;
    }

    public ZOACardPeripheral getDevice()
    {
        return this.device;
    }

    public void addDelegate(YTICallback callback)
    {
        synchronized (delegates)
        {
            this.delegates.add(callback);
        }
    }

    public void removeDelegate(YTICallback callback)
    {
        synchronized (delegates)
        {
            for (int i=0; i<delegates.size(); i++)
            {
                if (this.delegates.get(i) == callback)
                {
                    this.delegates.remove(i);
                }
            }
        }
    }

    // 데이터를 송.수신 할 준비가 되었는지 확인
    public boolean isReady()
    {
        return this.device.getDeviceState() == ZOACardPeripheral.DeviceState.ready;
    }


    public void connect() throws Exception
    {
        this.device.connect();
    }

    public void disconnect()
    {
        this.device.disconnect();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // command
    public void cmd_send(int index) throws Exception
    {
        this.cmd_send(index, null, 0, 0);
    }


    private void notifyMessage(String message)
    {
        synchronized (this.delegates)
        {
            for (int i = 0; i < delegates.size(); i++)
            {
                this.delegates.get(i).CardReaderMessage(message);
            }
        }

    }

    public void cmd_send(int index, byte[] data, int offset, int len) throws Exception
    {
        if (!this.isReady())
            throw new Exception("Card Reader가 준비상태가 아님");

        if (index >= command_table.length)
            throw new Exception("명령어 index가 범위를 벗어남");

        if (this.cur_command != null)
            throw new Exception("이전 명령어가 완료되지 않음");

        final command_table_t cmd = command_table[index];

        // 응답을 기다리는 명령어라면
        if (cmd.res != cmd_response_type_t.cmd_res_none)
        {
            if (cmd.timeout > 0)
            {
                this.startResponseTimer(cmd.timeout);
            }

            this.cur_command = cmd;
        }

        this.notifyMessage("명령어 전송");

        //  명령어 전송 (2019-02-13 프린터 상태 명령어 전송을 위해 write 분기)
        if (cmd.cmd != 0x00)
            this.device.writeCommand(cmd.cmd, data, offset, len);
        else
            this.device.writeData(data, offset, len);
    }

    void startResponseTimer(int timeout)
    {
        // not implemented
        this.respTimer = new Timer();
        this.respTimer.schedule(new TimerTask(){

            @Override
            public void run()
            {
                clearTimer();

                mHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        notifyMessage("전송한 명령에 대한 응답이 없음");
                    }
                });
            }
        }, timeout * 1000);
    }

    void clearTimer()
    {
        this.cur_command = null;
        this.respTimer.cancel();
        this.respTimer = null;
    }

    public void print(byte[] buf) throws Exception
    {
        this.print(buf, 0, buf.length);
    }

    public void print(byte[] buf, int offset, int len) throws Exception
    {
        if (!this.isReady())
            throw new Exception("Card Reader가 준비상태가 아님");

        this.device.writeData(buf, offset, len);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // notify
    private void notifyConnected()
    {
        synchronized (this.delegates)
        {
            for (int i = 0; i < delegates.size(); i++)
            {
                this.delegates.get(i).CardReaderConnected();
            }
        }

    }

    private void notifyDisconnected()
    {
        synchronized (this.delegates)
        {
            for (int i = 0; i < delegates.size(); i++)
            {
                this.delegates.get(i).CardReaderDisconnected();
            }
        }

    }

    private void notifyStatusChanged(ZOACardPeripheral.DeviceState state)
    {
        synchronized (this.delegates)
        {
            for (int i = 0; i < delegates.size(); i++)
            {
                this.delegates.get(i).CardReaderStateChanged(state);
            }
        }

    }

    private void notifyPlainData(byte[] data)
    {
        synchronized (this.delegates)
        {
            for (int i = 0; i < delegates.size(); i++)
            {
                this.delegates.get(i).CardReaderReceivedPlainData(data);
            }
        }
    }

    private void notifyPacket(byte cmd, byte[] data)
    {
        synchronized (this.delegates)
        {
            for (int i = 0; i < delegates.size(); i++)
            {
                this.delegates.get(i).CardReaderReceivedPacket(cmd, data);
            }
        }
    }

    private void notifyDataRecv(byte[] data)
    {
        synchronized (this.delegates)
        {
            for (int i = 0; i < delegates.size(); i++)
            {
                this.delegates.get(i).CardReaderReceivedData(data);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // OKCardPeripheral implementation
    @Override
    public void onCardPeripheralConnected(ZOACardPeripheral device)
    {
        this.connected = true;
        this.notifyConnected();
    }

    @Override
    public void onCardPeripheralDisconnected(ZOACardPeripheral devide)
    {
        this.connected = false;
        this.notifyDisconnected();
    }

    @Override
    public void onDeviceStateChanged(ZOACardPeripheral device, ZOACardPeripheral.DeviceState state)
    {
        this.notifyStatusChanged(state);
    }

    @Override
    public void didReceivedPlainData(byte[] data)
    {
        Log.d(LogTag,"*** Plain data received : " + data);

        if (cur_command!=null)
        {
            if (cur_command.res == cmd_response_type_t.cmd_res_plain_data)
            {
                this.clearTimer();
                this.notifyMessage("평문 응답 수신");

                this.notifyPlainData(data);
            }
            else
            {
                Log.d(LogTag,"패킷을 원하는데 Plain data를 수신");
                this.notifyDataRecv(data);
            }
        }
        else
        {
            Log.d(LogTag,"전송한 명령이 없는데.. Plain data 응답");
            this.notifyDataRecv(data);
        }

    }

    private int getDataSize(byte[] packet)
    {
        byte h = packet[2];
        byte l = packet[3];


        int value = ((h>>4) & 0x0f) * 1000 + (h & 0x0f)*100 + ((l>>4)&0x0f)*10 + (l&0x0f)*1;

        return value;

    }

    @Override
    public void didReceivedPacket(byte[] packet)
    {
        Log.d(LogTag,"*** Packet Received : " + packet);

        byte command_id = packet[1];

        if (cur_command!=null)
        {
            if (cur_command.res == cmd_response_type_t.cmd_res_packet)
            {
                if (cur_command.cmd == command_id)
                {
                    int len = this.getDataSize(packet);
                    byte[] data = null;

                    if (len != 0)
                    {
                        data = new byte[len];
                        System.arraycopy(packet, 4, data, 0, len);
                    }

                    this.clearTimer();
                    this.notifyMessage("패킷 응답 수신");
                    this.notifyPacket(command_id, data);
                }
                else
                {
                    Log.d(LogTag,"Command ID가 다른 응답");
                    this.notifyDataRecv(packet);
                }
            }
            else
            {
                Log.d(LogTag,"Plain data 원하는데.. 패킷을 수신");
                this.notifyDataRecv(packet);
            }
        }
        else
        {
            Log.d(LogTag,"전송한 명령이 없는데.. 응답");
            this.notifyDataRecv(packet);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // define delegate
    public interface YTICallback
    {
        void CardReaderConnected();
        void CardReaderDisconnected();
        void CardReaderReceivedPacket(byte cmd, byte[] data);
        void CardReaderReceivedPlainData(byte[] data);
        void CardReaderReceivedData(byte[] data);
        void CardReaderMessage(String message);
        void CardReaderStateChanged(ZOACardPeripheral.DeviceState state);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // define external command
    public static final int CMD_GET_SYSTEMINFOMATION   = 0;
    public static final int CMD_RESET                   = 1;

    public static final int CMD_MUTUALF1_MSK            = 2;
    public static final int CMD_MUTUALF2_INIT           = 2;
    public static final int CMD_MUTUALF3_COMPLETE       = 2;

    public static final int CMD_INTEGRITY               = 3;
    public static final int CMD_SAFECARDKEYSYNC         = 4;
    public static final int CMD_SAFECARDKEYDOWNLOAD     = 5;
    public static final int CMD_SAFECARDKEYTRANSACTION  = 6;
    public static final int CMD_ICEMVCOMPLETE           = 7;
    public static final int CMD_MSFALLBACK              = 8;
    public static final int CMD_GETBATTERY              = 9;

    public static final int CMD_IFM                     = 10;
    public static final int CMD_MSR                     = 10;
    public static final int CMD_LED                     = 11;

    public static final int CMD_PRINT_STAT              = 12;

    public static final int CMD_SET_SLEEP_TIME          = 13;
    public static final int CMD_GET_SLEEP_TIME          = 14;

    public static final int CMD_SAFECARD_ENC          = 15;
    public static final int CMD_SAFECARD_FALLBACK     = 16;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // define command table
    enum cmd_response_type_t
    {
        cmd_res_none, cmd_res_packet, cmd_res_plain_data
    };


    static class command_table_t
    {
        public command_table_t(byte cmd, cmd_response_type_t res, int timeout)
        {
            this.cmd = cmd;
            this.res = res;
            this.timeout = timeout;
        }
        public byte cmd;
        public cmd_response_type_t res;
        public int timeout;
    }

    final static command_table_t[] command_table = new command_table_t[] {
            new command_table_t((byte)0x31, cmd_response_type_t.cmd_res_packet, 5),      //0  System info
            new command_table_t((byte)0x32, cmd_response_type_t.cmd_res_none,   0),      //1  Reset
            new command_table_t((byte)0xA0, cmd_response_type_t.cmd_res_packet, 5),     //2  F1 / F2 / F3
            new command_table_t((byte)0xA1, cmd_response_type_t.cmd_res_packet, 5),     //3  Integrity
            new command_table_t((byte)0x6A, cmd_response_type_t.cmd_res_packet, 5),     //4  Safe Card Sync
            new command_table_t((byte)0x6B, cmd_response_type_t.cmd_res_packet, 5),     //5  Safe Card Key download
            new command_table_t((byte)0x6C, cmd_response_type_t.cmd_res_packet, 5),     //6  Safe Card Key Transaction
            new command_table_t((byte)0x6D, cmd_response_type_t.cmd_res_packet, 5),     //7  ICEMV complete
            new command_table_t((byte)0x6E, cmd_response_type_t.cmd_res_packet, 5),     //8  MS Fallback
            new command_table_t((byte)0x39, cmd_response_type_t.cmd_res_packet, 5),     //9  Get Battery
            new command_table_t((byte)0x2E, cmd_response_type_t.cmd_res_plain_data, 0),  //10 IFM / MSR
            new command_table_t((byte)0x2E, cmd_response_type_t.cmd_res_none,       0),  //11 LED
            new command_table_t((byte)0x00, cmd_response_type_t.cmd_res_plain_data,2),   //12 Print stat
            new command_table_t((byte)0x3A, cmd_response_type_t.cmd_res_packet,2),       //13 set sleep time
            new command_table_t((byte)0x3B, cmd_response_type_t.cmd_res_packet,2),       //14 get sleep time
            new command_table_t((byte)0x9C, cmd_response_type_t.cmd_res_packet,5),       //15 Safe card ENC
            new command_table_t((byte)0x9E, cmd_response_type_t.cmd_res_packet,5),       //16 Safe card Fallback
    };
}
