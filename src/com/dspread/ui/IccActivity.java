package com.dspread.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;

import com.dspread.xpos.QPOSService;
import com.dspread.xpos.QPOSService.CommunicationMode;
import com.dspread.xpos.QPOSService.DoTradeResult;
import com.dspread.xpos.QPOSService.Display;
import com.dspread.xpos.QPOSService.EmvOption;
import com.dspread.xpos.QPOSService.Error;
import com.dspread.xpos.QPOSService.TransactionResult;
import com.dspread.xpos.QPOSService.QPOSServiceListener;
import com.dspread.xpos.QPOSService.UpdateInformationResult;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.pm.ActivityInfo;

public class IccActivity extends Activity
{

	private Button powerOnIccBtn;
	private Button powerOffIccBtn;
	private Button apduBtn;
	private EditText apduEditText;
	private EditText statusEditText;
	private ListView appListView;
	private Dialog dialog;

	private QPOSService pos;
	private MyPosListener listener;

	private boolean isAudio = true;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.icc_activity_main);

		powerOnIccBtn = (Button) findViewById(R.id.powerOnIccBtn);
		powerOffIccBtn = (Button) findViewById(R.id.powerOffIccBtn);
		apduBtn = (Button) findViewById(R.id.apduBtn);
		apduEditText = (EditText) findViewById(R.id.apduEditText);
		statusEditText = (EditText) findViewById(R.id.statusEditText);
		//
		MyOnClickListener myOnClickListener = new MyOnClickListener();
		powerOnIccBtn.setOnClickListener(myOnClickListener);
		powerOffIccBtn.setOnClickListener(myOnClickListener);
		apduBtn.setOnClickListener(myOnClickListener);

		listener = new MyPosListener();
		if (isAudio)
		{
			pos = QPOSService.getInstance(CommunicationMode.AUDIO);
		}
		else
		{
			pos = QPOSService.getInstance(CommunicationMode.BLUETOOTH_VER2);
		}

		if (pos == null)
		{
			statusEditText.setText("CommunicationMode unknow");
			return;
		}
		pos.setConext(getApplicationContext());
		Handler handler = new Handler(Looper.myLooper());
		pos.initListener(handler, listener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.icc_activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.menu_get_deivce_info)
		{
			statusEditText.setText(R.string.getting_info);
			pos.getQposInfo();
		}
		else if (item.getItemId() == R.id.menu_get_pos_id)
		{
			statusEditText.setText(R.string.getting_pos_id);
			pos.getQposId();
		}
		else if (item.getItemId() == R.id.menu_get_pin)
		{
			statusEditText.setText(R.string.input_pin);
			pos.getPin("201402121520");
		}
		// else if(item.getItemId() == R.id.menu_bluetooth) {
		// finish();
		// Intent intent = new Intent(this, MainActivity.class);
		// startActivity(intent);
		// } else if(item.getItemId() == R.id.menu_audio) {
		// finish();
		// Intent intent = new Intent(this, AudioActivity.class);
		// startActivity(intent);
		// }
		return true;
	}

	@Override
	public void onPause()
	{
		super.onPause();
		if (isAudio)
		{
			pos.closeAudio();
		}
		else
		{
			pos.disconnectBT();
		}

	}

	@Override
	public void onResume()
	{
		super.onResume();
		if (isAudio)
		{
			pos.openAudio();
		}

	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		finish();
		pos.onDestroy();
	}

	public void dismissDialog()
	{
		if (dialog != null)
		{
			dialog.dismiss();
			dialog = null;
		}
	}

	class MyPosListener implements QPOSServiceListener
	{

		@Override
		public void onRequestWaitingUser()
		{
			dismissDialog();
			statusEditText.setText(getString(R.string.waiting_for_card));
		}

		@Override
		public void onDoTradeResult(DoTradeResult result, Hashtable<String, String> decodeData)
		{
			dismissDialog();
			if (result == DoTradeResult.NONE)
			{
				statusEditText.setText(getString(R.string.no_card_detected));
			}
			else if (result == DoTradeResult.ICC)
			{
				statusEditText.setText(getString(R.string.icc_card_inserted));

				TRACE.d("EMV ICC Start");
				pos.doEmvApp(EmvOption.START);
			}
			else if (result == DoTradeResult.NOT_ICC)
			{
				statusEditText.setText(getString(R.string.card_inserted));
			}
			else if (result == DoTradeResult.BAD_SWIPE)
			{
				statusEditText.setText(getString(R.string.bad_swipe));
			}
			else if (result == DoTradeResult.MCR)
			{
				String formatID = decodeData.get("formatID");
				String maskedPAN = decodeData.get("maskedPAN");
				String expiryDate = decodeData.get("expiryDate");
				String cardHolderName = decodeData.get("cardholderName");
				String ksn = decodeData.get("ksn");
				String serviceCode = decodeData.get("serviceCode");
				String track1Length = decodeData.get("track1Length");
				String track2Length = decodeData.get("track2Length");
				String track3Length = decodeData.get("track3Length");
				String encTracks = decodeData.get("encTracks");
				String encTrack1 = decodeData.get("encTrack1");
				String encTrack2 = decodeData.get("encTrack2");
				String encTrack3 = decodeData.get("encTrack3");
				String partialTrack = decodeData.get("partialTrack");
				// TODO
				String pinKsn = decodeData.get("pinKsn");
				String trackksn = decodeData.get("trackksn");
				String pinBlock = decodeData.get("pinBlock");

				String content = getString(R.string.card_swiped);

				content += getString(R.string.format_id) + " " + formatID + "\n";
				content += getString(R.string.masked_pan) + " " + maskedPAN + "\n";
				content += getString(R.string.expiry_date) + " " + expiryDate + "\n";
				content += getString(R.string.cardholder_name) + " " + cardHolderName + "\n";
				content += getString(R.string.ksn) + " " + ksn + "\n";
				content += getString(R.string.pinKsn) + " " + pinKsn + "\n";
				content += getString(R.string.trackksn) + " " + trackksn + "\n";
				content += getString(R.string.service_code) + " " + serviceCode + "\n";
				content += getString(R.string.track_1_length) + " " + track1Length + "\n";
				content += getString(R.string.track_2_length) + " " + track2Length + "\n";
				content += getString(R.string.track_3_length) + " " + track3Length + "\n";
				content += getString(R.string.encrypted_tracks) + " " + encTracks + "\n";
				content += getString(R.string.encrypted_track_1) + " " + encTrack1 + "\n";
				content += getString(R.string.encrypted_track_2) + " " + encTrack2 + "\n";
				content += getString(R.string.encrypted_track_3) + " " + encTrack3 + "\n";
				content += getString(R.string.partial_track) + " " + partialTrack + "\n";
				content += getString(R.string.pinBlock) + " " + pinBlock + "\n";
				TRACE.i("swipe card:" + content);
				statusEditText.setText(content);
			}
			else if (result == DoTradeResult.NO_RESPONSE)
			{
				statusEditText.setText(getString(R.string.card_no_response));
			}
		}

		@Override
		public void onQposInfoResult(Hashtable<String, String> posInfoData)
		{
			String isSupportedTrack1 = posInfoData.get("isSupportedTrack1") == null ? "" : posInfoData
					.get("isSupportedTrack1");
			String isSupportedTrack2 = posInfoData.get("isSupportedTrack2") == null ? "" : posInfoData
					.get("isSupportedTrack2");
			String isSupportedTrack3 = posInfoData.get("isSupportedTrack3") == null ? "" : posInfoData
					.get("isSupportedTrack3");
			String bootloaderVersion = posInfoData.get("bootloaderVersion") == null ? "" : posInfoData
					.get("bootloaderVersion");
			String firmwareVersion = posInfoData.get("firmwareVersion") == null ? "" : posInfoData
					.get("firmwareVersion");
			String isUsbConnected = posInfoData.get("isUsbConnected") == null ? "" : posInfoData.get("isUsbConnected");
			String isCharging = posInfoData.get("isCharging") == null ? "" : posInfoData.get("isCharging");
			String batteryLevel = posInfoData.get("batteryLevel") == null ? "" : posInfoData.get("batteryLevel");
			String hardwareVersion = posInfoData.get("hardwareVersion") == null ? "" : posInfoData
					.get("hardwareVersion");

			String content = "";
			content += getString(R.string.bootloader_version) + bootloaderVersion + "\n";
			content += getString(R.string.firmware_version) + firmwareVersion + "\n";
			content += getString(R.string.usb) + isUsbConnected + "\n";
			content += getString(R.string.charge) + isCharging + "\n";
			content += getString(R.string.battery_level) + batteryLevel + "\n";
			content += getString(R.string.hardware_version) + hardwareVersion + "\n";
			content += getString(R.string.track_1_supported) + isSupportedTrack1 + "\n";
			content += getString(R.string.track_2_supported) + isSupportedTrack2 + "\n";
			content += getString(R.string.track_3_supported) + isSupportedTrack3 + "\n";

			statusEditText.setText(content);
		}

		@Override
		public void onRequestTransactionResult(TransactionResult transactionResult)
		{

			dismissDialog();
			// statusEditText.setText("");
			dialog = new Dialog(IccActivity.this);
			dialog.setContentView(R.layout.alert_dialog);
			dialog.setTitle(R.string.transaction_result);
			TextView messageTextView = (TextView) dialog.findViewById(R.id.messageTextView);

			if (transactionResult == TransactionResult.APPROVED)
			{

			}
			else if (transactionResult == TransactionResult.TERMINATED)
			{
				clearDisplay();
				messageTextView.setText(getString(R.string.transaction_terminated));
			}
			else if (transactionResult == TransactionResult.DECLINED)
			{
				messageTextView.setText(getString(R.string.transaction_declined));
			}
			else if (transactionResult == TransactionResult.CANCEL)
			{
				clearDisplay();
				messageTextView.setText(getString(R.string.transaction_cancel));
			}
			else if (transactionResult == TransactionResult.CAPK_FAIL)
			{
				messageTextView.setText(getString(R.string.transaction_capk_fail));
			}
			else if (transactionResult == TransactionResult.NOT_ICC)
			{
				messageTextView.setText(getString(R.string.transaction_not_icc));
			}
			else if (transactionResult == TransactionResult.SELECT_APP_FAIL)
			{
				messageTextView.setText(getString(R.string.transaction_app_fail));
			}
			else if (transactionResult == TransactionResult.DEVICE_ERROR)
			{
				messageTextView.setText(getString(R.string.transaction_device_error));
			}
			else if (transactionResult == TransactionResult.CARD_NOT_SUPPORTED)
			{
				messageTextView.setText(getString(R.string.card_not_supported));
			}
			else if (transactionResult == TransactionResult.MISSING_MANDATORY_DATA)
			{
				messageTextView.setText(getString(R.string.missing_mandatory_data));
			}
			else if (transactionResult == TransactionResult.CARD_BLOCKED_OR_NO_EMV_APPS)
			{
				messageTextView.setText(getString(R.string.card_blocked_or_no_evm_apps));
			}
			else if (transactionResult == TransactionResult.INVALID_ICC_DATA)
			{
				messageTextView.setText(getString(R.string.invalid_icc_data));
			}

			dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					dismissDialog();
				}
			});

			dialog.show();

			apduEditText.setText("");
		}

		@Override
		public void onRequestBatchData(String tlv)
		{
			TRACE.d("ICC交易结束");
			// dismissDialog();
			String content = getString(R.string.batch_data);
			TRACE.i("onRequestBatchData tlv:" + tlv);
			content += tlv;
			statusEditText.setText(content);
		}

		@Override
		public void onRequestTransactionLog(String tlv)
		{
			dismissDialog();
			String content = getString(R.string.transaction_log);
			content += tlv;
			statusEditText.setText(content);
		}

		@Override
		public void onQposIdResult(Hashtable<String, String> posIdTable)
		{
			String posId = posIdTable.get("posId") == null ? "" : posIdTable.get("posId");

			String content = "";
			content += getString(R.string.posId) + posId + "\n";

			statusEditText.setText(content);
		}

		@Override
		public void onRequestSelectEmvApp(ArrayList<String> appList)
		{
			TRACE.d("请选择App -- S");
			dismissDialog();

			dialog = new Dialog(IccActivity.this);
			dialog.setContentView(R.layout.emv_app_dialog);
			dialog.setTitle(R.string.please_select_app);

			String[] appNameList = new String[appList.size()];
			for (int i = 0; i < appNameList.length; ++i)
			{
				TRACE.d("i=" + i + "," + appList.get(i));
				appNameList[i] = appList.get(i);
			}

			appListView = (ListView) dialog.findViewById(R.id.appList);
			appListView.setAdapter(new ArrayAdapter<String>(IccActivity.this, android.R.layout.simple_list_item_1,
					appNameList));
			appListView.setOnItemClickListener(new OnItemClickListener()
			{

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{

					pos.selectEmvApp(position);
					TRACE.d("请选择App -- 结束 position = " + position);
					dismissDialog();
				}

			});
			dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					pos.cancelSelectEmvApp();
					dismissDialog();
				}
			});
			dialog.show();
		}

		@Override
		public void onRequestSetAmount()
		{

		}

		@Override
		public void onRequestIsServerConnected()
		{
			TRACE.d("在线过程请求");
			dismissDialog();
			dialog = new Dialog(IccActivity.this);
			dialog.setContentView(R.layout.alert_dialog);
			dialog.setTitle(R.string.online_process_requested);

			((TextView) dialog.findViewById(R.id.messageTextView)).setText(R.string.replied_connected);

			dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					pos.isServerConnected(true);
					dismissDialog();
				}
			});

			dialog.show();
		}

		@Override
		public void onRequestOnlineProcess(String tlv)
		{

		}

		@Override
		public void onRequestTime()
		{
			TRACE.d("要求终端时间。已回覆");
			dismissDialog();
			String terminalTime = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
			pos.sendTime(terminalTime);
			statusEditText.setText(getString(R.string.request_terminal_time) + " " + terminalTime);
		}

		@Override
		public void onRequestDisplay(Display displayMsg)
		{
			dismissDialog();

			String msg = "";
			if (displayMsg == Display.CLEAR_DISPLAY_MSG)
			{
				msg = "";
			}
			else if (displayMsg == Display.PLEASE_WAIT)
			{
				msg = getString(R.string.wait);
			}
			else if (displayMsg == Display.REMOVE_CARD)
			{
				msg = getString(R.string.remove_card);
			}
			else if (displayMsg == Display.TRY_ANOTHER_INTERFACE)
			{
				msg = getString(R.string.try_another_interface);
			}
			else if (displayMsg == Display.PROCESSING)
			{
				msg = getString(R.string.processing);
			}
			statusEditText.setText(msg);
		}

		@Override
		public void onRequestFinalConfirm()
		{

		}

		@Override
		public void onRequestNoQposDetected()
		{
			dismissDialog();
			statusEditText.setText(getString(R.string.no_device_detected));
		}

		@Override
		public void onRequestQposConnected()
		{
			dismissDialog();
			statusEditText.setText(getString(R.string.device_plugged));
		}

		@Override
		public void onRequestQposDisconnected()
		{
			dismissDialog();
			statusEditText.setText(getString(R.string.device_unplugged));
		}

		@Override
		public void onError(Error errorState)
		{
			dismissDialog();
			apduEditText.setText("");
			if (errorState == Error.CMD_NOT_AVAILABLE)
			{
				statusEditText.setText(getString(R.string.command_not_available));
			}
			else if (errorState == Error.TIMEOUT)
			{
				statusEditText.setText(getString(R.string.device_no_response));
			}
			else if (errorState == Error.DEVICE_RESET)
			{
				statusEditText.setText(getString(R.string.device_reset));
			}
			else if (errorState == Error.UNKNOWN)
			{
				statusEditText.setText(getString(R.string.unknown_error));
			}
			else if (errorState == Error.DEVICE_BUSY)
			{
				statusEditText.setText(getString(R.string.device_busy));
			}
			else if (errorState == Error.INPUT_OUT_OF_RANGE)
			{
				statusEditText.setText(getString(R.string.out_of_range));
			}
			else if (errorState == Error.INPUT_INVALID_FORMAT)
			{
				statusEditText.setText(getString(R.string.invalid_format));
			}
			else if (errorState == Error.INPUT_ZERO_VALUES)
			{
				statusEditText.setText(getString(R.string.zero_values));
			}
			else if (errorState == Error.INPUT_INVALID)
			{
				statusEditText.setText(getString(R.string.input_invalid));
			}
			else if (errorState == Error.CASHBACK_NOT_SUPPORTED)
			{
				statusEditText.setText(getString(R.string.cashback_not_supported));
			}
			else if (errorState == Error.CRC_ERROR)
			{
				statusEditText.setText(getString(R.string.crc_error));
			}
			else if (errorState == Error.COMM_ERROR)
			{
				statusEditText.setText(getString(R.string.comm_error));
			}
			else if (errorState == Error.MAC_ERROR)
			{
				statusEditText.setText(getString(R.string.mac_error));
			}
			else if (errorState == Error.CMD_TIMEOUT)
			{
				statusEditText.setText(getString(R.string.cmd_timeout));
			}
		}

		@Override
		public void onReturnReversalData(String tlv)
		{
			// dismissDialog();
			String content = getString(R.string.reversal_data);
			content += tlv;
			TRACE.i("listener: onReturnReversalData: " + tlv);
			statusEditText.setText(content);

		}

		@Override
		public void onReturnGetPinResult(Hashtable<String, String> result)
		{
			String pinBlock = result.get("pinBlock");
			String pinKsn = result.get("pinKsn");
			String content = "get pin result\n";

			content += getString(R.string.pinKsn) + " " + pinKsn + "\n";
			content += getString(R.string.pinBlock) + " " + pinBlock + "\n";
			statusEditText.setText(content);
			TRACE.i(content);
		}

		@Override
		public void onReturnPowerOnIccResult(boolean isSuccess, String ksn, String atr, int atrLen)
		{
			if (isSuccess)
			{
				String content = "";
				content += getString(R.string.power_on_icc_success) + "\n";
				content += getString(R.string.ksn) + ksn + "\n";
				content += getString(R.string.atr) + atr + "\n";
				content += getString(R.string.atr_length) + atrLen + "\n";
				statusEditText.setText(content);
			}
			else
			{
				statusEditText.setText(getString(R.string.power_on_icc_failed));
			}

		}

		@Override
		public void onReturnPowerOffIccResult(boolean isSuccess)
		{
			if (isSuccess)
			{
				statusEditText.setText(getString(R.string.power_off_icc_success));
			}
			else
			{
				statusEditText.setText(getString(R.string.power_off_icc_failed));
			}

		}

		@Override
		public void onReturnApduResult(boolean isSuccess, String apdu, int apduLen)
		{
			if (isSuccess)
			{
				statusEditText.setText(getString(R.string.apdu_result) + apdu);
			}
			else
			{
				statusEditText.setText(getString(R.string.apdu_failed));
			}

		}

		@Override
		public void onReturnSetSleepTimeResult(boolean arg0)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetCardNoResult(String arg0)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onRequestCalculateMac(String arg0)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onRequestSignatureResult(byte[] arg0)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onRequestUpdateWorkKeyResult(UpdateInformationResult arg0)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onReturnCustomConfigResult(boolean arg0, String arg1)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onRequestSetPin()
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onReturnSetMasterKeyResult(boolean arg0)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onReturnBatchSendAPDUResult(LinkedHashMap<Integer, String> batchAPDUResult)
		{
			StringBuilder sb = new StringBuilder();
			sb.append("APDU Responses: \n");
			for (HashMap.Entry<Integer, String> entry : batchAPDUResult.entrySet())
			{
				sb.append("[" + entry.getKey() + "]: " + entry.getValue() + "\n");
			}
			statusEditText.setText("\n" + sb.toString());

		}

		@Override
		public void onBluetoothBondFailed()
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onBluetoothBondTimeout()
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onBluetoothBonded()
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onBluetoothBonding()
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onReturniccCashBack(Hashtable<String, String> arg0)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onLcdShowCustomDisplay(boolean arg0)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onUpdatePosFirmwareResult(UpdateInformationResult arg0)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onReturnDownloadRsaPublicKey(HashMap<String, String> arg0)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetPosComm(int arg0, String arg1, String arg2)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onPinKey_TDES_Result(String arg0)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onUpdateMasterKeyResult(boolean arg0, Hashtable<String, String> arg1)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onAddKey(boolean arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onBluetoothBoardStateResult(boolean arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onCbcMacResult(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onConfirmAmountResult(boolean arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onDeviceFound(BluetoothDevice arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onEmvICCExceptionData(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onEncryptData(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onGetInputAmountResult(boolean arg0, String arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onQposDoGetTradeLogNum(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onQposDoTradeLog(boolean arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onQposIsCardExist(boolean arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onReadBusinessCardResult(boolean arg0, String arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onRequestDeviceScanFinished() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onRequestUpdateKey(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onReturnGetEMVListResult(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onReturnGetQuickEmvResult(boolean arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onReturnNFCApduResult(boolean arg0, String arg1,
				int arg2) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onReturnPowerOffNFCResult(boolean arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onReturnPowerOnNFCResult(boolean arg0, String arg1,
				String arg2, int arg3) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onReturnUpdateEMVRIDResult(boolean arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onReturnUpdateEMVResult(boolean arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onReturnUpdateIPEKResult(boolean arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSearchMifareCardResult(
				Hashtable<String, String> arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSetBuzzerResult(boolean arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSetManagementKey(boolean arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSetParamsResult(boolean arg0,
				Hashtable<String, Object> arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSetSleepModeTime(boolean arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onWaitingforData(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onWriteBusinessCardResult(boolean arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getMifareCardVersion(Hashtable<String, String> arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getMifareFastReadData(Hashtable<String, String> arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getMifareReadData(Hashtable<String, String> arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onFinishMifareCardResult(boolean arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onGetShutDownTime(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onGetSleepModeTime(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onOperateMifareCardResult(
				Hashtable<String, String> arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onQposDoGetTradeLog(String arg0, String arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onQposDoSetRsaPublicKey(boolean arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onQposGenerateSessionKeysResult(
				Hashtable<String, String> arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onQposKsnResult(Hashtable<String, String> arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onReadMifareCardResult(
				Hashtable<String, String> arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onVerifyMifareCardResult(boolean arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onWriteMifareCardResult(boolean arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void transferMifareData(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void verifyMifareULData(Hashtable<String, String> arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void writeMifareULData(String arg0) {
			// TODO Auto-generated method stub
			
		}

	}

	private void clearDisplay()
	{
		statusEditText.setText("");
	}

	class MyOnClickListener implements OnClickListener
	{

		@Override
		public void onClick(View v)
		{
			statusEditText.setText("");
			if (v == powerOffIccBtn)
			{
				pos.powerOffIcc();
				statusEditText.setText(getString(R.string.powering_off_icc));
			}
			else if (v == powerOnIccBtn)
			{
				pos.powerOnIcc();
				statusEditText.setText(getString(R.string.powering_on_icc));
			}
			else if (v == apduBtn)
			{
				String apduString = apduEditText.getText().toString().trim();
				apduString = "00A404000E315041592E5359532E444446303100";
				pos.sendApdu(apduString);
				statusEditText.setText(getString(R.string.sending) + apduEditText.getText().toString());
			}
		}
	}

}
