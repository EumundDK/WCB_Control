package com.example.wcbcontrol;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import java.util.ArrayList;

public class GridviewFragment extends Fragment {

    private static final String DEVICE_DATA_LIST = "device_data_list";

    private RecyclerView recyclerView;
    private ArrayList<DeviceData> mDeviceDataList = new ArrayList<>();
    private int mGridCount;
    private GridViewAdapter gridViewAdapter;

    public GridviewFragment() {
        // Required empty public constructor
    }

    public static GridviewFragment newInstance(ArrayList<DeviceData> deviceDataList) {
        GridviewFragment fragment = new GridviewFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(DEVICE_DATA_LIST, deviceDataList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        int columnWidth = Math.round(displayMetrics.widthPixels / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        mGridCount = DeviceDataTabActivity.getSpanCount(columnWidth);
        if(getArguments() != null) {
            mDeviceDataList = getArguments().getParcelableArrayList(DEVICE_DATA_LIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gridview, container, false);
        recyclerView = view.findViewById(R.id.gridRecyclerView);
        gridViewAdapter = new GridViewAdapter(getContext(), mDeviceDataList);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), mGridCount));
        recyclerView.setAdapter(gridViewAdapter);
        SimpleItemAnimator itemAnimator = (SimpleItemAnimator) recyclerView.getItemAnimator();
        itemAnimator.setSupportsChangeAnimations(false);
        return view;
    }

    public void updateDeviceData(int position) {
        gridViewAdapter.notifyItemRangeChanged(position, 20);
    }

    public void refreshDeviceData() {
        gridViewAdapter.notifyDataSetChanged();
    }
}