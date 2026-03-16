package com.posbank.device.serialDevice;


import android.util.Log;

import com.posbank.hardware.serial.SerialPort;
import com.posbank.hardware.serial.SerialPortDevice;
import com.posbank.hardware.serial.SerialPortIOException;
import com.posbank.hardware.serial.SerialPortManager;

import java.io.IOException;


public class SerialCom {
    private static final String TAG = "SerialCom";

    // SerialPortDevice
    private SerialPortDevice serialPortDevice = null;

    // SerialPort
    private SerialPort serial = null;

    /**
     * Constructor
     * @param serialPortDevice : SerialPortDevice
     */
    public SerialCom(SerialPortDevice serialPortDevice) {
        this.serialPortDevice = serialPortDevice;
    }

    /**
     * Open Serial Port
     * @return true/false
     */
    public boolean open() {
        if (null == this.serialPortDevice) return false;

        this.serial = SerialPortManager.openDevice(this.serialPortDevice);
        if (null != this.serial) {
            // Check Valid Serial Port
            if (this.serial.isValid()) {
                this.serial.flush();
//                this.serial.setTimeout(1000, 1000, 0, 1000, 0);
                Log.d(TAG, "Serial Port Device Open() OK.");
                return true;
            } else {
                Log.d(TAG, "Serial Port Device Open() Fail!");
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Check Opened Status
     */
    public boolean isOpened() {
        if(null != this.serial) {
            // Opened
            if(this.serial.isOpen()) {
                // Valid
                if(this.serial.isValid()) {
                    this.serial.flush();
                    return true;
                } else {
                    try {
                        this.serial.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    this.serial = null;
                    return false;
                }
            } else {
                this.serial = null;
                return false;
            }
        }
        return false;
    }

    /**
     * Send Data to Serial Port
     * @param _txData : send Data Buffer
     * @param _size : send Data Size
     * @return : Send Length
     */
    public int send(byte[] _txData, int _size) {
        if (null == this.serial) return (0);

        this.serial.flush();

        int txSize=0;
        try {
            txSize = this.serial.write(_txData, _size);
        } catch (SerialPortIOException e) {
            e.printStackTrace();
        }

        return txSize;
    }

    /**
     * Receive Data to Serial Port
     * @param _rxData : Receive Buffer
     * @param _offSet : Receive Buffer offset
     * @return : Received Size
     */
    public int receive(byte[] _rxData, int _offSet) {
        if(this.serial == null) return (0);

        int rxSize=0;
        try {
            this.serial.waitByteTimes(6);
            int availableSize = this.serial.available();
            if(availableSize > 0) {
                rxSize = this.serial.read(_rxData, _offSet, availableSize);
            }
        } catch (SerialPortIOException e) {
            e.printStackTrace();
        }

        return rxSize;
    }

    /**
     * Check Available
     * @return : Available Size
     */
    public int checkAvailable() {
        int availableSize = 0;

        if(this.serial == null) return (0);

        try {
            availableSize = this.serial.available();
        } catch (SerialPortIOException e) {
            e.printStackTrace();
        }

        return availableSize;
    }

    /**
     * Close Serial Port
     */
    public void close() {
        if(null != this.serial) {
            if(this.serial.isOpen()) {
                try {
                    this.serial.close();
                    Log.d(TAG, "Serial Port Device close() OK.");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "Serial Port Device close() Fail.");
                }
            }
            this.serial = null;
        }
    }
}
