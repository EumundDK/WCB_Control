
package com.example.wcbcontrol;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;

import static android.widget.Toast.LENGTH_SHORT;

public class ListViewAdapter extends RecyclerView.Adapter<ListViewAdapter.ViewHolder>{

    public static final String MyPREFERENCES = "MyPrefs" ;

    SharedPreferences sharedPreferences;

    private LayoutInflater mInflater;
    private Context mContext;
    private ArrayList<DeviceData> mDeviceDataList;

    private BluetoothLeService mBluetoothLeService = DeviceDataTabActivity.mBluetoothLeService;
    private int itemSelectedCard = RecyclerView.NO_POSITION;
    private int previousItemSelectCard = RecyclerView.NO_POSITION;
    private byte[] myCommand = {0x08, 0x00, 0x03, 0x00, 0x00};

    ListViewAdapter(Context context, ArrayList<DeviceData> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mDeviceDataList = data;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.data_info_item_list, parent, false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DeviceData readerData = mDeviceDataList.get(position);
        holder.mTagName.setText(readerData.getName());
        holder.mDeviceName.setText(sharedPreferences.getString(String.valueOf(position), "Device"));
        holder.mCutOffCount.setText(String.valueOf(readerData.getCutOffCount()));
        holder.mNoRfCount.setText(String.valueOf(readerData.getNoRfCount()));
        if(checkDuplicate(holder, readerData, position)) {
            holder.mCurrent.setText(readerData.getCurrent());
            checkStatus(holder, readerData, position);
        }
    }

    @Override
    public int getItemCount() {
        return mDeviceDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTagName;
        ImageView mStatusOff;
        TextView mDeviceName;
        TextView mCutOffCount;
        TextView mNoRfCount;
        TextView mCurrent;
        CardView mCardView;
//        ImageButton mButton;
        Button mOnSwitch;
        Button mOffSwitch;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTagName = itemView.findViewById(R.id.itemNameTextView);
            mStatusOff = itemView.findViewById(R.id.itemStatusOnOff);
            mDeviceName = itemView.findViewById(R.id.itemDeviceName);
            mCutOffCount = itemView.findViewById(R.id.itemCutOffCounter);
            mNoRfCount = itemView.findViewById(R.id.itemNoRfCounter);
            mCurrent = itemView.findViewById(R.id.itemCurrentTextView);
            mCardView = itemView.findViewById(R.id.card_view);
//            mButton = itemView.findViewById(R.id.onOffButton);
            mOnSwitch = itemView.findViewById(R.id.onButton);
            mOffSwitch = itemView.findViewById(R.id.offButton);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemSelectedCard = getLayoutPosition();
                    notifyItemChanged(itemSelectedCard);
                    deviceSelection(itemSelectedCard);
                }
            });

            mOnSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int itemSelectedSwitch = getLayoutPosition();
                    byte itemName = Byte.parseByte(mDeviceDataList.get(itemSelectedSwitch).getName());
                    myCommand[1] = itemName;
                    myCommand[2] = (byte) DeviceGatt.ON_COMMAND;
                    mBluetoothLeService.writeCharacteristic(mBluetoothLeService.UUID_DEVICE_BLE_RECEIVE, myCommand);
                    Toast.makeText(mInflater.getContext(), "Tag No. " + Arrays.toString(myCommand) + " ON", LENGTH_SHORT).show();
                }
            });

            mOffSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int itemSelectedSwitch = getLayoutPosition();
                    byte itemName = Byte.parseByte(mDeviceDataList.get(itemSelectedSwitch).getName());
                    myCommand[1] = itemName;
                    myCommand[2] = (byte) DeviceGatt.OFF_COMMAND;
                    mBluetoothLeService.writeCharacteristic(mBluetoothLeService.UUID_DEVICE_BLE_RECEIVE, myCommand);
                    Toast.makeText(mInflater.getContext(), "Tag No. " + Arrays.toString(myCommand) + " OFF", LENGTH_SHORT).show();
                }
            });
        }
    }

    DeviceData getItem(int id) {
        return mDeviceDataList.get(id);
    }

    public boolean checkDuplicate(ListViewAdapter.ViewHolder holder, DeviceData readerData, int position) {
        if(readerData.getDuplicate() == 0) { //NO DATA BLANK OUT
            holder.mCardView.setCardBackgroundColor(mInflater.getContext().getColor(R.color.bright_grey));
//            holder.mStatusOff.setImageDrawable(mInflater.getContext().getDrawable(R.drawable.ic_baseline_circle_grey_24));
            holder.mCurrent.setText(R.string.blank);
            holder.itemView.setClickable(false);
            holder.mOnSwitch.setVisibility(View.INVISIBLE);
            holder.mOffSwitch.setVisibility(View.INVISIBLE);
            return false;
        } else if(readerData.getDuplicate() == 1) {
            if(position != previousItemSelectCard) { //GOOD DATA NOT PREVIOUSLY SELECTED
                holder.mCardView.setCardBackgroundColor(mInflater.getContext().getColor(R.color.white));
            }
            holder.itemView.setClickable(true);
            holder.mOnSwitch.setVisibility(View.VISIBLE);
            holder.mOffSwitch.setVisibility(View.VISIBLE);
            return true;
        } else { // DUPE DATA ERROR
            holder.mCurrent.setText("Err");
            holder.mCardView.setCardBackgroundColor(mInflater.getContext().getColor(R.color.light_red));
//            holder.mStatusOff.setImageDrawable(mInflater.getContext().getDrawable(R.drawable.ic_baseline_circle_grey_24));
            holder.itemView.setClickable(true);
            holder.mOnSwitch.setVisibility(View.INVISIBLE);
            holder.mOffSwitch.setVisibility(View.INVISIBLE);
            return true;
        }
    }

    public void checkStatus(ListViewAdapter.ViewHolder holder, DeviceData readerData, int position) {
        int itemStatus = Integer.parseUnsignedInt(readerData.getStatus(), 16);
        if ((itemStatus & DeviceGatt.STATUS_ON_OFF) == DeviceGatt.STATUS_ON) {
//            holder.mStatusOff.setImageDrawable(mInflater.getContext().getDrawable(R.drawable.ic_baseline_circle_green_24));
            holder.mOnSwitch.setBackgroundTintList(mInflater.getContext().getColorStateList(R.color.button_color_on));
            holder.mOffSwitch.setBackgroundTintList(mInflater.getContext().getColorStateList(R.color.button_color_default));

        } else if ((itemStatus & DeviceGatt.STATUS_ON_OFF) == DeviceGatt.STATUS_OFF) {
//            holder.mStatusOff.setImageDrawable(mInflater.getContext().getDrawable(R.drawable.ic_baseline_circle_red_24));
            holder.mOffSwitch.setBackgroundTintList(mInflater.getContext().getColorStateList(R.color.button_color_off));
            holder.mOnSwitch.setBackgroundTintList(mInflater.getContext().getColorStateList(R.color.button_color_default));
        }
    }

//    public void updateDeviceDataList(ArrayList<DeviceData> newDeviceDataList) {
//        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new BleDiffUtilCallbacks(mDeviceDataList, newDeviceDataList));
//        diffResult.dispatchUpdatesTo(this);
//        mDeviceDataList = newDeviceDataList;
//    }

    private void deviceSelection(int selectNo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = mInflater;
        View content = inflater.inflate(R.layout.dialog_device_setting, null);
        builder.setView(content).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        TextView dialogId = content.findViewById(R.id.dialogId);
        TextView dialogName = content.findViewById(R.id.dialogName);
        dialogName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameDialog(selectNo); //Select Device Index
            }
        });
        TextView dialogCurrent = content.findViewById(R.id.dialogCurrent);
        dialogCurrent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentSettingDialog();
            }
        });
        TextView dialogCutoffPeriod = content.findViewById(R.id.dialogCutoffPeriod);
        dialogCutoffPeriod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cutOffPeriodDialog();
            }
        });
        TextView dialogOnOff = content.findViewById(R.id.dialogOnOff);
        TextView dialogAutoReconnect = content.findViewById(R.id.dialogAutoReconnect);
        TextView dialogOwnerName = content.findViewById(R.id.dialogOwnerName);
        dialogId.setText(mDeviceDataList.get(selectNo).getName()); //Device ID Name
        dialogName.setText(sharedPreferences.getString(String.valueOf(selectNo), "Device"));
        dialogCurrent.setText(String.valueOf(mDeviceDataList.get(selectNo).getCurrentSetting()));
        dialogCutoffPeriod.setText(Integer.toString(mDeviceDataList.get(selectNo).getCutoffPeriod()));
        dialogOnOff.setText(Integer.toString(mDeviceDataList.get(selectNo).getOnOffSetting()));
        dialogAutoReconnect.setText(Integer.toString(mDeviceDataList.get(selectNo).getAutoReconnect()));
        dialogOwnerName.setText(mDeviceDataList.get(selectNo).getOwnerName());
        builder.create();
        builder.show();
    }

    private void nameDialog(int selectNo) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        AlertDialog.Builder builderCurrent = new AlertDialog.Builder(mContext);
        EditText inputName = new EditText(mContext);
        builderCurrent.setTitle(mDeviceDataList.get(selectNo).getName());
        builderCurrent.setMessage("Input Name:");
        builderCurrent.setView(inputName);
        inputName.setText(sharedPreferences.getString(String.valueOf(selectNo), "Device"));
        builderCurrent.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                editor.putString(String.valueOf(selectNo), inputName.getText().toString());
                editor.commit();
            }
        });
        builderCurrent.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builderCurrent.show();
    }

    private void currentSettingDialog() {
        AlertDialog.Builder builderCurrent = new AlertDialog.Builder(mContext);
        EditText inputCurrent = new EditText(mContext);
        builderCurrent.setTitle("Current Settings");
        builderCurrent.setMessage("Enter Current Settings(1.0 ~ 40.0):");
        builderCurrent.setView(inputCurrent);
        builderCurrent.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int dataInput;
                dataInput = (int) (Double.parseDouble(inputCurrent.getText().toString()) * 10);
                myCommand[1] = Byte.parseByte(mDeviceDataList.get(itemSelectedCard).getName());;
                myCommand[2] = (byte) DeviceGatt.UPDATE_CURRENT_COMMAND;
                myCommand[3] = (byte) (dataInput % 256);
                myCommand[4] = (byte) (dataInput / 256);
                mBluetoothLeService.writeCharacteristic(mBluetoothLeService.UUID_DEVICE_BLE_RECEIVE, myCommand);
            }
        });
        builderCurrent.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builderCurrent.show();
    }

    private void cutOffPeriodDialog() {
        AlertDialog.Builder builderCutOff = new AlertDialog.Builder(mContext);
        EditText inputCutOff = new EditText(mContext);
        builderCutOff.setTitle("Cut-off Period");
        builderCutOff.setMessage("Enter Cut-off Period (20 ~ 300):");
        builderCutOff.setView(inputCutOff);
        builderCutOff.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int dataInput;
                dataInput= Integer.parseInt(inputCutOff.getText().toString());
                myCommand[1] = Byte.parseByte(mDeviceDataList.get(itemSelectedCard).getName());;
                myCommand[2] = (byte) DeviceGatt.UPDATE_CUTOFF_COMMAND;
                myCommand[3] = (byte) (dataInput % 256);
                myCommand[4] = (byte) (dataInput / 256);
                DeviceDataTabActivity.mBluetoothLeService.writeCharacteristic(mBluetoothLeService.UUID_DEVICE_BLE_RECEIVE, myCommand);
            }
        });
        builderCutOff.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builderCutOff.show();
    }
}
