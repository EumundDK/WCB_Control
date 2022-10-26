package com.example.wcbcontrol;

import android.os.Parcel;
import android.os.Parcelable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class DeviceData implements Parcelable {

    static final int totalTag = 100;

    //rawData for GridView
    String reader;
    String name;
    String status;
    String current;
    int duplicate;
    //extra rawData for ListView
    double currentSetting;
    int cutoffPeriod;
    int onOffSetting;
    int autoReconnect;
    String ownerName;

    int cutOffCount;
    int noRfCount;

    private ArrayList<String> existingReader = new ArrayList<>();
    private ArrayList<DeviceData> deviceDataList  = new ArrayList<>();

    ArrayList<Integer> prevDevice  = new ArrayList<>();;
    ArrayList<Integer> currDevice  = new ArrayList<>();;

    int prevStatus;
    int currStatus;

    public DeviceData() {

    }

    public DeviceData(Parcel source) {
        reader = source.readString();
        name = source.readString();
        status = source.readString();
        current = source.readString();
        duplicate = source.readInt();

        currentSetting = source.readDouble();
        cutoffPeriod = source.readInt();
        onOffSetting = source.readInt();
        autoReconnect = source.readInt();
        ownerName = source.readString();

        cutOffCount = source.readInt();
        noRfCount = source.readInt();
    }

    public void setReader(String reader) {
        this.reader = reader;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    public void setDuplicate(int duplicate) {
        this.duplicate = duplicate;
    }

    public void setCurrentSetting(double currentSetting) {
        this.currentSetting = currentSetting;
    }

    public void setCutoffPeriod(int cutoffPeriod) {
        this.cutoffPeriod = cutoffPeriod;
    }

    public void setOnOffSetting(int onOffSetting) {
        this.onOffSetting = onOffSetting;
    }

    public void setAutoReconnect(int autoReconnect) {
        this.autoReconnect = autoReconnect;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public void setCutOffCount(int cutOffCount) {
        this.cutOffCount = cutOffCount;
    }

    public void setNoRfCount(int noRfCount) {
        this.noRfCount = noRfCount;
    }

    public String getReader() {
        return reader;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getCurrent() {
        return current;
    }

    public int getDuplicate() {
        return duplicate;
    }

    public double getCurrentSetting() {
        return currentSetting;
    }

    public int getCutoffPeriod() {
        return cutoffPeriod;
    }

    public int getOnOffSetting() {
        return onOffSetting;
    }

    public int getAutoReconnect() {
        return autoReconnect;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public int getCutOffCount() {
        return cutOffCount;
    }

    public int getNoRfCount() {
        return noRfCount;
    }

    public ArrayList<DeviceData> getDeviceDataList() {
        return deviceDataList;
    }

    public void setDeviceDataList(ArrayList<DeviceData> deviceDataList) {
        this.deviceDataList = deviceDataList;
    }

    public ArrayList<Integer> getPrevDevice() {
        return prevDevice;
    }

    public void setPrevDevice(ArrayList<Integer> prevDevice) {
        this.prevDevice = prevDevice;
    }

    public ArrayList<Integer> getCurrDevice() {
        return currDevice;
    }

    public void setCurrDevice(ArrayList<Integer> currDevice) {
        this.currDevice = currDevice;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel data, int i) {
        data.writeString(reader);
        data.writeString(name);
        data.writeString(status);
        data.writeString(current);
        data.writeInt(duplicate);

        data.writeDouble(currentSetting);
        data.writeInt(cutoffPeriod);
        data.writeInt(onOffSetting);
        data.writeInt(autoReconnect);
        data.writeString(ownerName);

        data.writeInt(cutOffCount);
        data.writeInt(noRfCount);
    }

    public static final Creator<DeviceData> CREATOR = new Creator<DeviceData>() {

        @Override
        public DeviceData createFromParcel(Parcel source) {
            return new DeviceData(source);
        }

        @Override
        public DeviceData[] newArray(int size) {
            return new DeviceData[size];
        }
    };

    public void initialiseData() {
        for(int i = 0; i < totalTag; i++) {
            //GRID VIEW
            DeviceData mDeviceData = new DeviceData();
            mDeviceData.setReader(String.valueOf(0));
            mDeviceData.setName(String.valueOf(i + 1));
            mDeviceData.setStatus(String.valueOf(64));
            mDeviceData.setCurrent("-");
            mDeviceData.setDuplicate(0);
            //LIST VIEW
            mDeviceData.setCurrentSetting(0.0);
            mDeviceData.setCutoffPeriod(0);
            mDeviceData.setOnOffSetting(0);
            mDeviceData.setAutoReconnect(0);
            mDeviceData.setOwnerName("");
            //EXTRA
            mDeviceData.setCutOffCount(0);
            mDeviceData.setNoRfCount(0);

            mDeviceData.setCurrDevice(prevDevice);
            mDeviceData.setPrevDevice(currDevice);

            deviceDataList.add(mDeviceData);
        }
    }

    public void clearData() {
        for(int i = 0; i < totalTag; i++) {
            //GRID VIEW
            deviceDataList.get(i).setReader(String.valueOf(0));
            deviceDataList.get(i).setName(String.valueOf(i + 1));
            deviceDataList.get(i).setStatus(String.valueOf(64));
            deviceDataList.get(i).setCurrent("-");
            deviceDataList.get(i).setDuplicate(0);
            //LIST VIEW
            deviceDataList.get(i).setCurrentSetting(0.0);
            deviceDataList.get(i).setCutoffPeriod(0);
            deviceDataList.get(i).setOnOffSetting(0);
            deviceDataList.get(i).setAutoReconnect(0);
            deviceDataList.get(i).setOwnerName("");
        }
    }

    public void removeData() {
        deviceDataList.clear();
    }

    public void updateData(String data) {
        int deviceNumber;
        double totalCurrent;
        int duplicateValue;
        int header;

        if (data != null) {
            String[] filterData = data.split(" ");
            header = Integer.parseUnsignedInt(filterData[0], 16);
            resetDuplicateValue(String.valueOf(Integer.parseUnsignedInt(filterData[1])));
            if(!prevDevice.isEmpty()) {
                prevDevice.clear();
            }
            prevDevice.addAll(currDevice);
            if(header != 0) {
                currDevice.clear();
                for(int i = 2; i < (filterData.length - 2); i+=4) {
                    deviceNumber = Integer.parseUnsignedInt(filterData[i], 16);
                    if(deviceNumber > 0 && deviceNumber <= totalTag) {
                        currDevice.add(deviceNumber);
                        deviceNumber = deviceNumber - 1;
                        duplicateValue = deviceDataList.get(deviceNumber).getDuplicate() + 1;
                        totalCurrent = calculateTotalCurrent(Integer.parseUnsignedInt(filterData[i+2], 16), Integer.parseUnsignedInt(filterData[i+3], 16));
                        prevStatus = Integer.parseInt(deviceDataList.get(deviceNumber).getStatus());

                        deviceDataList.get(deviceNumber).setReader(String.valueOf(Integer.parseUnsignedInt(filterData[1], 16)));
                        deviceDataList.get(deviceNumber).setName(String.valueOf(Integer.parseUnsignedInt(filterData[i], 16)));
                        deviceDataList.get(deviceNumber).setStatus(String.valueOf(Integer.parseUnsignedInt(filterData[i + 1], 16)));
                        deviceDataList.get(deviceNumber).setCurrent(String.valueOf(totalCurrent));
                        deviceDataList.get(deviceNumber).setDuplicate(duplicateValue);

                        currStatus = Integer.parseInt(deviceDataList.get(deviceNumber).getStatus());
                        if((prevStatus == DeviceGatt.STATUS_ON) && (currStatus == DeviceGatt.STATUS_OFF)) {
                            deviceDataList.get(deviceNumber).setCutOffCount(deviceDataList.get(deviceNumber).getCutOffCount() + 1);
                        }
                    }
                }
                compareTag(prevDevice, currDevice);
            }
        }
    }

    public void updateEepData(String data) {
        int deviceNumber;
        double totalCurrent;
        int totalPeriod;
        int header;
        byte[] ownerName = new byte[16];
        String ownerNameText;

        if (data != null) {
            String[] filterData = data.split(" ");
            header = Integer.parseUnsignedInt(filterData[0], 16);

            if(header == 0) {
                deviceNumber = Integer.parseUnsignedInt(filterData[3], 16);
                if(deviceNumber > 0 && deviceNumber <= totalTag) {
                    deviceNumber = deviceNumber - 1;
                    totalCurrent = calculateTotalCurrent(Integer.parseUnsignedInt(filterData[1], 16), Integer.parseUnsignedInt(filterData[2], 16));
                    totalPeriod = calculateTotalPeriod(Integer.parseUnsignedInt(filterData[5], 16), Integer.parseUnsignedInt(filterData[6], 16));

                    deviceDataList.get(deviceNumber).setCurrentSetting(totalCurrent);
                    deviceDataList.get(deviceNumber).setName(String.valueOf(Integer.parseUnsignedInt(filterData[3], 16)));
                    deviceDataList.get(deviceNumber).setCutoffPeriod(totalPeriod);
                    deviceDataList.get(deviceNumber).setOnOffSetting(Integer.parseUnsignedInt(filterData[7], 16));
                    deviceDataList.get(deviceNumber).setAutoReconnect(Integer.parseUnsignedInt(filterData[8], 16));
                    for(int j = 0; j < 15; j++) {
                        ownerName[j] = Byte.parseByte(filterData[9 + j], 16);
                    }
                    ownerNameText = new String(ownerName, StandardCharsets.UTF_8);
                    deviceDataList.get(deviceNumber).setOwnerName(ownerNameText);
                }
            }
        }

    }

    private void resetDuplicateValue(String readerNo) {
        if(existingReader.contains(readerNo)){
            for(int i = 0; i < totalTag; i++){
                if(deviceDataList.get(i).getReader().equals(readerNo)) {
                    deviceDataList.get(i).setDuplicate(0);
                }
            }
        } else {
            existingReader.add(readerNo);
        }
    }

    private double calculateTotalCurrent(int current1, int current2) {
        double totalCurrent;
        double maxCurrent = 99.9;
        totalCurrent = ((current1 + (current2 * 256)) / 10.0);
        if(totalCurrent > maxCurrent) {
            totalCurrent = maxCurrent;
        }
        return totalCurrent;
    }

    private int calculateTotalPeriod(int period1, int period2) {
        int totalPeriod;
        int maxPeriod = 300;
        totalPeriod = (period1 + (period2 * 256));
        if(totalPeriod > maxPeriod) {
            totalPeriod = maxPeriod;
        }
        return totalPeriod;
    }

    //Every one second add to prev
    private void compareTag(ArrayList<Integer> previousList,ArrayList<Integer> currentList) {
        int size1 = previousList.size();
        int size2 = currentList.size();
        if(previousList.size() > currentList.size()) {
            previousList.removeAll(currentList);
            for(int i = 0; i < previousList.size(); i++) {
                deviceDataList.get(previousList.get(i) - 1).setNoRfCount( deviceDataList.get(previousList.get(i) - 1).getNoRfCount() + 1);
            }
        }
    }

}
