package com.posbank.device.serialDevice;


import android.content.Context;

import com.posbank.hardware.serial.SerialPortDevice;
import com.posbank.hardware.serial.SerialPortManager;
import com.posbank.hardware.serial.SerialPortPreference;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

public class SerialPortService {

    // SerialPortDevice Objects
    private HashMap<String, SerialPortDevice> mSerialPortDeviceMap = null;

    /**
     * Get SerialPortDevice List
     * @param _context : Context
     */
    public void loadSerialPortDeviceMaps(Context _context) {
        /* Clear a MAP data of devices */
        if (null != mSerialPortDeviceMap && mSerialPortDeviceMap.size() > 0) {
            mSerialPortDeviceMap.clear();
            mSerialPortDeviceMap = null;
        }

        /* Get a device list from SerialPortManager */
        mSerialPortDeviceMap = SerialPortManager.getDeviceList();
        if (null == mSerialPortDeviceMap) {
            return;
        }

        /* Load configurations for all SerialPorts */
        SerialPortPreference preference = new SerialPortPreference(_context);
        Set<String> keys = mSerialPortDeviceMap.keySet();
        for (String key : keys) {
            SerialPortDevice device = mSerialPortDeviceMap.get(key);

            /* load config from preference */
            preference.setDevice(device.getDeviceName());
            preference.load();

            /* Set a config to the device */
            device.setBaudrate(preference.getBaudrate());
            device.setDataBits(preference.getDataBit());
            device.setStopBits(preference.getStopBit());
            device.setParityBits(preference.getParityBit());
            device.setFlowControl(preference.getFlowControl());
        }
    }

    /**
     * Get Serial Port Device
     * @param _deviceName : Use Serial Port Number
     * @return : SerialPortDevice
     */
    private SerialPortDevice getSerialPortDeviceByWinName(String _deviceName) {
        SerialPortDevice device = null;
        if (null != mSerialPortDeviceMap && mSerialPortDeviceMap.size() > 0) {
            /* Using sort */
            TreeMap<String, SerialPortDevice> tm = new TreeMap<>(mSerialPortDeviceMap);
            Iterator<String> iteratorKey = tm.keySet().iterator();
            while(iteratorKey.hasNext()) {
                String key = iteratorKey.next();
                device = mSerialPortDeviceMap.get(key);
                if (device.getWindowName().equals(_deviceName)) {
                    break;
                } else {
                    device = null;
                }
            }
        }
        return device;
    }

    /**
     * Get Serial Port Device
     * @param _context  : Context
     * @param _portName : Use Serial Port Name
     * @param _baudRate : Use Serial Port BaudRate
     * @return : SerialPortDevice
     */
    public SerialPortDevice getSerialPortDevice(Context _context, String _portName, String _baudRate) {
        SerialPortDevice serialPortDevice = getSerialPortDeviceByWinName(_portName);
        if(null == serialPortDevice) {
            return null;
        }

        // Get SerialPort System Device Name
        String serialDeviceName = serialPortDevice.getDeviceName();

        // Load SerialPort Preference
        SerialPortPreference serialPortPreference = new SerialPortPreference(_context, serialDeviceName, _portName);
        serialPortPreference.load();

        // Set BaudRate
        serialPortPreference.setBaudRate(Integer.parseInt(_baudRate));

        // Set SerialPortDevice
        serialPortDevice = getSerialPortDeviceByWinName(_portName);
        if(serialPortDevice != null) {
            // Baud rate
            serialPortDevice.setBaudrate(serialPortPreference.getBaudrate());
            // Data bits
            serialPortDevice.setDataBits(serialPortPreference.getDataBit());
            // Parity bits
            serialPortDevice.setParityBits(serialPortPreference.getParityBit());
            // Stop bits
            serialPortDevice.setStopBits(serialPortPreference.getStopBit());
            // Flow Control
            serialPortDevice.setFlowControl(serialPortPreference.getFlowControl());
        }

        return serialPortDevice;
    }
}
