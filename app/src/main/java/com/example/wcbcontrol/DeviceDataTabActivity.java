package com.example.wcbcontrol;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import static android.content.ContentValues.TAG;

public class DeviceDataTabActivity extends AppCompatActivity {
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mRawData;
    private TextView mEepData;
    private TextView mDeviceAddress;
    private TextView mDeviceName;

    private String nDeviceName;
    private String nDeviceAddress;
    public static BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;

    int position = 0;

    DeviceData deviceData;

    PagerAdapter mPagerAdapter;
    ViewPager2 viewPager2;
    TabLayout tabLayout;
    GridviewFragment gridviewFragment;
    ListviewFragment listviewFragment;

    CountDownTimer countDownTimer = new CountDownTimer(1000, 200) {
        @Override
        public void onTick(long l) {
            if(gridviewFragment != null) {
                gridviewFragment.updateDeviceData(position);
            }
            if(listviewFragment != null) {
                listviewFragment.updateDeviceData(position);
            }
            position += 20;
            if(position >= 80) {
                position = 0;
            }
        }

        @Override
        public void onFinish() {
            countDownTimer.start();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_data_tab);
        deviceData = new DeviceData();

        Intent intent = getIntent();
        nDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        nDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        mDeviceName = findViewById(R.id.device_name);
        mDeviceAddress = findViewById(R.id.device_address);
        mConnectionState = findViewById(R.id.connection_state);
        mRawData = findViewById(R.id.gatt_services_raw_data);
        mEepData = findViewById(R.id.gatt_services_eep_data);

        mDeviceName.setText(nDeviceName);
        mDeviceAddress.setText(nDeviceAddress);

        deviceData.initialiseData();

        tabLayout = findViewById(R.id.tabLayout);
        viewPager2 = findViewById(R.id.viewPager);

        getSupportActionBar().setTitle(nDeviceName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        startService(gattServiceIntent);

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(nDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothLeService.disconnect();
        stopService(new Intent(this, BluetoothLeService.class));
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        deviceData.removeData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_refresh:
                clearUI();
                if(gridviewFragment != null) {
                    gridviewFragment.refreshDeviceData();
                }
                if(listviewFragment != null) {
                    listviewFragment.refreshDeviceData();
                }
                mBluetoothLeService.setCharacteristicNotification(mBluetoothLeService.UUID_DEVICE_EEP_DATA,true);
                break;
            case R.id.menu_connect:
                mBluetoothLeService.connect(nDeviceAddress);
                updateConnectionState(R.string.connecting);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                mBluetoothLeService.disconnect();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(nDeviceAddress);
            updateConnectionState(R.string.connecting);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.discovering_services);
                invalidateOptionsMenu();

                mPagerAdapter = new PagerAdapter(DeviceDataTabActivity.this);
                viewPager2.setAdapter(mPagerAdapter);

                new TabLayoutMediator(tabLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        switch(position) {
                            case 0:
                                tab.setIcon(R.drawable.ic_baseline_grid_view_24);
                                break;
                            case 1:
                                tab.setIcon(R.drawable.ic_baseline_view_list_24);
                                break;
                        }
                    }
                }).attach();

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
                if(gridviewFragment != null) {
                    gridviewFragment.refreshDeviceData();
                }
                if(listviewFragment != null) {
                    listviewFragment.refreshDeviceData();
                }
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                mBluetoothLeService.setCharacteristicNotification(mBluetoothLeService.UUID_DEVICE_BLE_SEND,true);
                updateConnectionState(R.string.connected);
                countDownTimer.start();
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                deviceData.updateData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                mRawData.setText(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            } else if (BluetoothLeService.ACTION_EEP_DATA_AVAILABLE.equals(action)) {
                deviceData.updateEepData(intent.getStringExtra(BluetoothLeService.EEP_DATA));
                mEepData.setText(intent.getStringExtra(BluetoothLeService.EEP_DATA));
            }

        }
    };

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void clearUI() {
        deviceData.clearData();
        mRawData.setText(R.string.no_data);
        mEepData.setText(R.string.no_data);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_EEP_DATA_AVAILABLE);
        return intentFilter;
    }

    public static int getSpanCount(int columnWidth) {
        int minWidth = 800;
        int spanCount;
        if(columnWidth >= minWidth) {
            spanCount = 10;
        } else {
            spanCount = 5;
        }
        return spanCount;
    }

    public class PagerAdapter extends FragmentStateAdapter {

        public PagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {

            switch (position) {
                case 0:
                    gridviewFragment = GridviewFragment.newInstance(deviceData.getDeviceDataList());
                    return gridviewFragment;
                case 1:
                    listviewFragment = ListviewFragment.newInstance(deviceData.getDeviceDataList());
                    return listviewFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }

    }

}
