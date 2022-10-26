package com.example.wcbcontrol;

import java.util.HashMap;

public class DeviceGatt {
    private static HashMap<String, String> attributes = new HashMap();
    public static String DEVICE_CUSTOM_SERVICE = "36a125be-49c7-462f-8e34-8d5b9dc883ea";
    public static String DEVICE_BLE_SEND = "44122a22-fc66-48e6-92c8-7d02cc9d16fd";
    public static String DEVICE_BLE_RECEIVE = "3dad49ff-61f8-4c7c-a474-74ad70b7c81a";
    public static String DEVICE_EEP_DATA = "6a26d1e2-e8b5-438a-b9f7-a470cdc58ff2";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    public static int READ_EEP_COMMAND = 65;
    public static int ON_COMMAND = 241;
    public static int OFF_COMMAND = 242;
    public static int SELECT_COMMAND = 248;
    public static int UPDATE_CURRENT_COMMAND = 250;
    public static int UPDATE_CUTOFF_COMMAND = 251;

    public static int STATUS_ON = 16;
    public static int STATUS_OFF = 0;
    public static int STATUS_SELECT = 8;
    public static int STATUS_ON_OFF = 16;

    public static byte HEADER = 8;
    public static byte DEVICE = 0;
    public static byte SET_STATUS = 3;
    public static byte VALUE_MSB = 0;
    public static byte VALUE_LSB = 0;

    public static byte[] EEP_COMMAND = new byte[] {(byte) READ_EEP_COMMAND};
    public static byte[] FTM_COMMAND = new byte[] {HEADER, DEVICE, SET_STATUS, VALUE_MSB, VALUE_LSB};

    static {
        // Sample Services.
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }

}
