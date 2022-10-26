package com.example.wcbcontrol;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;

import static android.widget.Toast.LENGTH_SHORT;
import static com.example.wcbcontrol.DeviceGatt.FTM_COMMAND;

public class GridViewAdapter extends RecyclerView.Adapter<GridViewAdapter.ViewHolder>{
    private LayoutInflater mInflater;
    private Context mContext;
    private ArrayList<DeviceData> mDeviceDataList;

    private BluetoothLeService mBluetoothLeService = DeviceDataTabActivity.mBluetoothLeService;
    private int itemSelectedCard = RecyclerView.NO_POSITION;
    private int previousItemSelectCard = RecyclerView.NO_POSITION;

    // data is passed into the constructor
    GridViewAdapter(Context context, ArrayList<DeviceData> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mDeviceDataList = data;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.data_info_item_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DeviceData readerData = mDeviceDataList.get(position);
        holder.mTagName.setText(readerData.getName());
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
        TextView mCurrent;
        TextView mCurrentSymbolTextView;
        ImageButton mSwitch;
        CardView mCardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTagName = itemView.findViewById(R.id.itemNameTextView);
            mStatusOff = itemView.findViewById(R.id.itemStatusOnOff);
            mCurrent = itemView.findViewById(R.id.itemCurrentTextView);
            mCurrentSymbolTextView = itemView.findViewById(R.id.currentSymbolTextView);
            mSwitch = itemView.findViewById(R.id.onOffSwitch);
            mCardView = itemView.findViewById(R.id.card_view);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemSelectedCard = getLayoutPosition();
                    FTM_COMMAND[1] = Byte.parseByte(mDeviceDataList.get(itemSelectedCard).getName());
                    FTM_COMMAND[2] = (byte) DeviceGatt.SELECT_COMMAND;
                    mBluetoothLeService.writeCharacteristic(mBluetoothLeService.UUID_DEVICE_BLE_RECEIVE, FTM_COMMAND);
                    Toast.makeText(mInflater.getContext(), "Tag No. " + Arrays.toString(FTM_COMMAND) + " Selected", LENGTH_SHORT).show();
                    notifyItemChanged(itemSelectedCard);
                }
            });

            mSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int itemSelectedSwitch = getLayoutPosition();
                    byte itemStatus = Byte.parseByte(mDeviceDataList.get(itemSelectedSwitch).getStatus());
                    byte itemName = Byte.parseByte(mDeviceDataList.get(itemSelectedSwitch).getName());
                    if((itemStatus & DeviceGatt.STATUS_ON_OFF) == DeviceGatt.STATUS_OFF) {
                        FTM_COMMAND[1] = itemName;
                        FTM_COMMAND[2] = (byte) DeviceGatt.ON_COMMAND;
                        mBluetoothLeService.writeCharacteristic(mBluetoothLeService.UUID_DEVICE_BLE_RECEIVE, FTM_COMMAND);
                        Toast.makeText(mInflater.getContext(), "Tag No. " + Arrays.toString(FTM_COMMAND) + " ON", LENGTH_SHORT).show();
                        mDeviceDataList.get(itemSelectedSwitch).setStatus(String.valueOf(DeviceGatt.STATUS_ON));
                        notifyItemChanged(itemSelectedSwitch);
                    }
                    if((itemStatus & DeviceGatt.STATUS_ON_OFF) == DeviceGatt.STATUS_ON) {
                        FTM_COMMAND[1] = itemName;
                        FTM_COMMAND[2] = (byte) DeviceGatt.OFF_COMMAND;
                        mBluetoothLeService.writeCharacteristic(mBluetoothLeService.UUID_DEVICE_BLE_RECEIVE, FTM_COMMAND);
                        Toast.makeText(mInflater.getContext(), "Tag No. " + Arrays.toString(FTM_COMMAND) + " OFF", LENGTH_SHORT).show();
                        mDeviceDataList.get(itemSelectedSwitch).setStatus(String.valueOf(DeviceGatt.STATUS_OFF));
                        notifyItemChanged(itemSelectedSwitch);
                    }
                }
            });
        }
    }

    // convenience method for getting data at click position
    DeviceData getItem(int id) {
        return mDeviceDataList.get(id);
    }

    public boolean checkDuplicate(ViewHolder holder, DeviceData readerData, int position) {
        if(readerData.getDuplicate() == 0) { //NO DATA BLANK OUT
            holder.mCardView.setCardBackgroundColor(mInflater.getContext().getColor(R.color.bright_grey));
            holder.mStatusOff.setImageDrawable(mInflater.getContext().getDrawable(R.drawable.ic_baseline_circle_grey_12));
            holder.mCurrent.setText(R.string.blank);
            holder.itemView.setClickable(false);
            holder.mSwitch.setClickable(false);
            return false;
        } else if(readerData.getDuplicate() == 1) {
            if(position != previousItemSelectCard) { //GOOD DATA NOT PREVIOUSLY SELECTED
                holder.mCardView.setCardBackgroundColor(mInflater.getContext().getColor(R.color.white));
            }
            holder.mCurrentSymbolTextView.setVisibility(View.VISIBLE);
            holder.itemView.setClickable(true);
            holder.mSwitch.setClickable(true);
            return true;
        } else { // DUPE DATA ERROR
            holder.mCurrent.setText("Err");
            holder.mCurrentSymbolTextView.setVisibility(View.INVISIBLE);
            holder.mCardView.setCardBackgroundColor(mInflater.getContext().getColor(R.color.light_red));
            holder.mStatusOff.setImageDrawable(mInflater.getContext().getDrawable(R.drawable.ic_baseline_circle_grey_12));
            holder.itemView.setClickable(true);
            holder.mSwitch.setClickable(false);
            return true;
        }
    }

    public void checkStatus(ViewHolder holder, DeviceData readerData, int position) {
        int itemStatus = Integer.parseUnsignedInt(readerData.getStatus());
        if (((itemStatus & DeviceGatt.STATUS_SELECT) == DeviceGatt.STATUS_SELECT)) {
            holder.mCardView.setCardBackgroundColor(mInflater.getContext().getColor(R.color.light_blue));
        }
        if ((itemStatus & DeviceGatt.STATUS_ON_OFF) == DeviceGatt.STATUS_ON) {
            holder.mStatusOff.setImageDrawable(mInflater.getContext().getDrawable(R.drawable.ic_baseline_circle_green_12));
        } else if ((itemStatus & DeviceGatt.STATUS_ON_OFF) == DeviceGatt.STATUS_OFF) {
            holder.mStatusOff.setImageDrawable(mInflater.getContext().getDrawable(R.drawable.ic_baseline_circle_red_12));
        }
    }
}
