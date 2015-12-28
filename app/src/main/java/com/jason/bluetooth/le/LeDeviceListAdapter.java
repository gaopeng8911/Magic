package com.jason.bluetooth.le;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class LeDeviceListAdapter extends BaseAdapter {

	// Adapter for holding devices found through scanning.

	private ArrayList<BluetoothDevice> mLeDevices;
	private LayoutInflater mInflator;
	private Activity mContext;

	public LeDeviceListAdapter(Activity c) {
		super();
		mContext = c;
		mLeDevices = new ArrayList<BluetoothDevice>();
		mInflator = mContext.getLayoutInflater();
	}

	public void addDevice(BluetoothDevice device) {
		if (!mLeDevices.contains(device)) {
			mLeDevices.add(device);
		}
	}

	public BluetoothDevice getDevice(int position) {
		return mLeDevices.get(position);
	}

	public void clear() {
		mLeDevices.clear();
	}

	//写入数据到textview
	public void writeResult(String result){
//		View v = mInflator.inflate(R.layout.listitem_device, null);
//		TextView tv = (TextView) v
//				.findViewById(R.id.result);
//		String ss = tv.getText()+result;
//		tv.setText(ss);

	}

	@Override
	public int getCount() {
		return mLeDevices.size();
	}

	@Override
	public Object getItem(int i) {
		return mLeDevices.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		ViewHolder viewHolder;
		// General ListView optimization code.
		if (view == null) {
			view = mInflator.inflate(R.layout.listitem_device, null);
			viewHolder = new ViewHolder();
			viewHolder.deviceAddress = (TextView) view
					.findViewById(R.id.device_address);
			viewHolder.deviceName = (TextView) view
					.findViewById(R.id.device_name);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		BluetoothDevice device = mLeDevices.get(i);
		final String deviceName = device.getName();
		if (deviceName != null && deviceName.length() > 0)
			viewHolder.deviceName.setText(deviceName);
		else
			viewHolder.deviceName.setText(R.string.unknown_device);
		viewHolder.deviceAddress.setText(device.getAddress());

		return view;
	}

	class ViewHolder {
		TextView deviceName;
		TextView deviceAddress;
	}
}
