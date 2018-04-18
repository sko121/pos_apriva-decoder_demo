package com.dspread.ui;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

public class USBClass {

	private static UsbManager mManager = null;
	
	private static HashMap<String, UsbDevice> mdevices;
	protected static HashMap<String, UsbDevice> getMdevices() {
		return mdevices;
	}

	private static PendingIntent mPermissionIntent;
	private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
	//对打开的设备进行监听
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbDevice device = (UsbDevice) intent
							.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					if (intent.getBooleanExtra(
							UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						if (device != null) {
							// call method to set up device communication
							TRACE.i("usb"+ "permission granted for device "
									+ device);
						}
					} else {
						TRACE.i("usb"+ "permission denied for device " + device);
					}
				}
			}
		}
	};
	
	/**
	 * 获取usb设备列表
	 * @param context
	 * @return
	 */
	@SuppressLint("NewApi")
	public ArrayList<String> GetUSBDevices(Context context) {
		mManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
		
		
		mdevices = new HashMap<String, UsbDevice>();
		ArrayList<String> deviceList = new ArrayList<String>();
		mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(
				"com.android.example.USB_PERMISSION"), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		context.registerReceiver(mUsbReceiver, filter);

		// check for existing devices
		//循环添加设备
		for (UsbDevice device : mManager.getDeviceList().values()) {
			// 判断是否有权限
	        if(!mManager.hasPermission(device)) {
	            // 打开设备，获取 UsbDeviceConnection 对象，连接设备，用于后面的通讯
	        	mManager.requestPermission(device, mPermissionIntent);
	            return null;
	        }
			String deviceName = null;
			UsbDeviceConnection connection = null;
//			Toast.makeText(context, "VID: "+device.getVendorId(), Toast.LENGTH_SHORT).show();
			if (device.getVendorId() == 2965 || device.getVendorId() == 0x03EB ) {//0x6133
				mManager.requestPermission(device, mPermissionIntent);
				connection = mManager.openDevice(device);
				byte rawBuf[] = new byte[255];
				int len = connection.controlTransfer(0x80, 0x06, 0x0302,
						0x0409, rawBuf, 0x00FF, 60);
				rawBuf = Arrays.copyOfRange(rawBuf, 2, len);
				deviceName = new String(rawBuf);
				deviceList.add(deviceName);
				mdevices.put(deviceName, device);
			}

		}
		context.unregisterReceiver(mUsbReceiver);
		return deviceList;
	}
	
}
