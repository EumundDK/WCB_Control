package com.example.wcbcontrol;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import java.util.ArrayList;


public class ListviewFragment extends Fragment {

    private static final String DEVICE_DATA_LIST = "device_data_list";

    private RecyclerView recyclerView;
    private ArrayList<DeviceData> mDeviceDataList;
    private ListViewAdapter listViewAdapter;

    public ListviewFragment() {
        // Required empty public constructor
    }

    public static ListviewFragment newInstance(ArrayList<DeviceData> deviceDataList) {
        ListviewFragment fragment = new ListviewFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(DEVICE_DATA_LIST, deviceDataList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            mDeviceDataList = getArguments().getParcelableArrayList(DEVICE_DATA_LIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_listview, container, false);
        recyclerView = view.findViewById(R.id.listRecyclerView);
        listViewAdapter = new ListViewAdapter(getContext(), mDeviceDataList);
        recyclerView.setLayoutManager(new LinearLayoutManager((getActivity())));
        recyclerView.setAdapter(listViewAdapter);
        SimpleItemAnimator itemAnimator = (SimpleItemAnimator) recyclerView.getItemAnimator();
        itemAnimator.setSupportsChangeAnimations(false);
        return view;
    }

    public void updateDeviceData(int position) {
        listViewAdapter.notifyItemRangeChanged(position, 20);
    }

    public void refreshDeviceData() {
        listViewAdapter.notifyDataSetChanged();
    }
}