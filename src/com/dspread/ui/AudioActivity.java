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
import com.dspread.xpos.QPOSService.TransactionType;
import com.dspread.xpos.QPOSService.QPOSServiceListener;
import com.dspread.xpos.QPOSService.UpdateInformationResult;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.util.Log;


public class AudioActivity extends Activity {
	
	private Button doTradeButton;
	private EditText amountEditText;
	private EditText statusEditText;
	private ListView appListView;
	private Dialog dialog;
	public final static String TAG="AudioActivity";
	
	
	private QPOSService pos;
	private MyPosListener listener;
	
	private String amount = "";
	private String cashbackAmount = "";
	private boolean isPinCanceled = false;
	public static final String POS_BLUETOOTH_ADDRESS = "POS_BLUETOOTH_ADDRESS";

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
	Log.d(TAG, "onCreate start");
	
    	//getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
       
	//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);	
        setContentView(R.layout.audio_activity_main);
        
	Log.d(TAG, "onCreate start ui init");
        doTradeButton = (Button)findViewById(R.id.audioDoTradeBtn);
        amountEditText = (EditText)findViewById(R.id.audioAmountEditText);
        statusEditText = (EditText)findViewById(R.id.audioStatusEditText);  
		
        MyOnClickListener myOnClickListener = new MyOnClickListener();
        doTradeButton.setOnClickListener(myOnClickListener);
        
	Log.d(TAG, "onCreate start PostListener");
        listener = new MyPosListener();
        pos = QPOSService.getInstance(CommunicationMode.AUDIO);
        if(pos==null){
        	statusEditText.setText("CommunicationMode unknow");
        	return;
        }
        pos.setConext(this);
        Handler handler = new Handler(Looper.myLooper());
        pos.initListener(handler, listener);
	Log.d(TAG, "onCreate end");
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.audio_activity_main, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if(item.getItemId() == R.id.menu_get_deivce_info) {
    		statusEditText.setText(R.string.getting_info);
    		pos.getQposInfo();
    	} else if(item.getItemId() == R.id.menu_get_pos_id) {
    		statusEditText.setText(R.string.getting_pos_id);
    		pos.getQposId();
    	} else if(item.getItemId() == R.id.menu_get_pin) {
    		statusEditText.setText(R.string.input_pin);
    		pos.getPin("201402121655");
    	}else if(item.getItemId() == R.id.menu_bluetooth) {
    		finish();
    		Intent intent = new Intent(this, MainActivity.class);
    		startActivity(intent);
    	}
    	return true;
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	pos.closeAudio();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	pos.openAudio();
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	finish();
    }
    
    public void dismissDialog() {
    	if(dialog != null) {
    		dialog.dismiss();
    		dialog = null;
    	}
    }
    
    class MyPosListener implements QPOSServiceListener {
    	
    	@Override
    	public void onRequestWaitingUser() {
    		dismissDialog();
    		statusEditText.setText(getString(R.string.waiting_for_card));
    	}

    	@Override
		public void onDoTradeResult(DoTradeResult result, Hashtable<String, String> decodeData) {
    		dismissDialog();
    		if(result == DoTradeResult.NONE) {
    			statusEditText.setText(getString(R.string.no_card_detected));
			} else if(result == DoTradeResult.ICC) {
				statusEditText.setText(getString(R.string.icc_card_inserted));
				
				TRACE.d("EMV ICC Start");
				pos.doEmvApp(EmvOption.START);
			} else if(result == DoTradeResult.NOT_ICC) {
				statusEditText.setText(getString(R.string.card_inserted));
			} else if(result == DoTradeResult.BAD_SWIPE) {
				statusEditText.setText(getString(R.string.bad_swipe));
			} else if(result == DoTradeResult.MCR) {
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
				//TODO 
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
    			TRACE.i("swipe card:"+content);
				statusEditText.setText(content);
			} else if(result == DoTradeResult.NO_RESPONSE) {
				statusEditText.setText(getString(R.string.card_no_response));
			} 
		}
		
    	@Override
		public void onQposInfoResult(Hashtable<String, String> posInfoData) {
			String isSupportedTrack1 = posInfoData.get("isSupportedTrack1") == null? "" : posInfoData.get("isSupportedTrack1");
			String isSupportedTrack2 = posInfoData.get("isSupportedTrack2") == null? "" : posInfoData.get("isSupportedTrack2");
			String isSupportedTrack3 = posInfoData.get("isSupportedTrack3") == null? "" : posInfoData.get("isSupportedTrack3");
			String bootloaderVersion = posInfoData.get("bootloaderVersion") == null? "" : posInfoData.get("bootloaderVersion");
			String firmwareVersion = posInfoData.get("firmwareVersion") == null? "" : posInfoData.get("firmwareVersion");
			String isUsbConnected = posInfoData.get("isUsbConnected") == null? "" : posInfoData.get("isUsbConnected");
			String isCharging = posInfoData.get("isCharging") == null? "" : posInfoData.get("isCharging");
			String batteryLevel = posInfoData.get("batteryLevel") == null? "" : posInfoData.get("batteryLevel");
			String hardwareVersion = posInfoData.get("hardwareVersion") == null? "" : posInfoData.get("hardwareVersion");
			
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
		public void onRequestTransactionResult(TransactionResult transactionResult) {
    		
			dismissDialog();
			//statusEditText.setText("");
			dialog = new Dialog(AudioActivity.this);
			dialog.setContentView(R.layout.alert_dialog);
			dialog.setTitle(R.string.transaction_result);
			TextView messageTextView = (TextView)dialog.findViewById(R.id.messageTextView);
			
			if(transactionResult == TransactionResult.APPROVED) {
				String message = getString(R.string.transaction_approved) + "\n"
						+ getString(R.string.amount) + ": $" + amount + "\n";
				if(!cashbackAmount.equals("")) {
					message += getString(R.string.cashback_amount) + ": $" + cashbackAmount;
				}
				messageTextView.setText(message);
			} else if(transactionResult == TransactionResult.TERMINATED) {
				clearDisplay();
				messageTextView.setText(getString(R.string.transaction_terminated));
			} else if(transactionResult == TransactionResult.DECLINED) {
				messageTextView.setText(getString(R.string.transaction_declined));
			} else if(transactionResult == TransactionResult.CANCEL) {
				clearDisplay();
				messageTextView.setText(getString(R.string.transaction_cancel));
			} else if(transactionResult == TransactionResult.CAPK_FAIL) {
				messageTextView.setText(getString(R.string.transaction_capk_fail));
			} else if(transactionResult == TransactionResult.NOT_ICC) {
				messageTextView.setText(getString(R.string.transaction_not_icc));
			} else if(transactionResult == TransactionResult.SELECT_APP_FAIL) {
				messageTextView.setText(getString(R.string.transaction_app_fail));
			} else if(transactionResult == TransactionResult.DEVICE_ERROR) {
				messageTextView.setText(getString(R.string.transaction_device_error));
			} else if(transactionResult == TransactionResult.CARD_NOT_SUPPORTED) {
				messageTextView.setText(getString(R.string.card_not_supported));
			} else if(transactionResult == TransactionResult.MISSING_MANDATORY_DATA) {
				messageTextView.setText(getString(R.string.missing_mandatory_data));
			} else if(transactionResult == TransactionResult.CARD_BLOCKED_OR_NO_EMV_APPS) {
				messageTextView.setText(getString(R.string.card_blocked_or_no_evm_apps));
			} else if(transactionResult == TransactionResult.INVALID_ICC_DATA) {
				messageTextView.setText(getString(R.string.invalid_icc_data));
			}
			
			dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					dismissDialog();
				}
			});
			
			dialog.show();
			
			amount = "";
			cashbackAmount = "";
			amountEditText.setText("");
		}
		
		@Override
		public void onRequestBatchData(String tlv) {
			TRACE.d("ICC交易结束");
//			dismissDialog();
			String content = getString(R.string.batch_data);
			TRACE.i("onRequestBatchData tlv:"+tlv);
			content += tlv;
			statusEditText.setText(content);
		}
		
		@Override
		public void onRequestTransactionLog(String tlv) {
			dismissDialog();
			String content = getString(R.string.transaction_log);
			content += tlv;
			statusEditText.setText(content);
		}
		
		
		
		@Override
		public void onQposIdResult(Hashtable<String, String> posIdTable) {
			String posId = posIdTable.get("posId") == null? "" : posIdTable.get("posId");
			
			String content = "";
			content += getString(R.string.posId) + posId + "\n";
			
			statusEditText.setText(content);
		}
		
		@Override
		public void onRequestSelectEmvApp(ArrayList<String> appList) {
			TRACE.d("请选择App -- S");
			dismissDialog();
			
			dialog = new Dialog(AudioActivity.this);
			dialog.setContentView(R.layout.emv_app_dialog);
			dialog.setTitle(R.string.please_select_app);
			
			String[] appNameList = new String[appList.size()];
			for(int i = 0; i < appNameList.length; ++i) {
				TRACE.d("i="+i+","+appList.get(i));
				appNameList[i] = appList.get(i);
			}
			
			appListView = (ListView)dialog.findViewById(R.id.appList);
			appListView.setAdapter(new ArrayAdapter<String>(AudioActivity.this, android.R.layout.simple_list_item_1, appNameList));
			appListView.setOnItemClickListener(new OnItemClickListener() {
				
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					
					pos.selectEmvApp(position);
					TRACE.d("请选择App -- 结束 position = "+position);
					dismissDialog();
				}
				
			});
			dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					pos.cancelSelectEmvApp();
					dismissDialog();
				}
			});
			dialog.show();
		}
		
		@Override
		public void onRequestSetAmount() {
			//pos.setAmount("123", cashbackAmount, "384", TransactionType.GOODS);
			
			
			TRACE.d("输入金额 -- S");
			dismissDialog();
			dialog = new Dialog(AudioActivity.this);
    		dialog.setContentView(R.layout.amount_dialog);
    		dialog.setTitle(getString(R.string.set_amount));
    		
    		String[] transactionTypes = new String[] {
    				"GOODS",
    				"SERVICES",
    				"CASHBACK",
    				"INQUIRY",
    				"TRANSFER",
    				"PAYMENT"
    		};
    		((Spinner)dialog.findViewById(R.id.transactionTypeSpinner)).setAdapter(new ArrayAdapter<String>(AudioActivity.this, android.R.layout.simple_spinner_item, transactionTypes));
    		
    		dialog.findViewById(R.id.setButton).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					String amount = ((EditText)(dialog.findViewById(R.id.amountEditText))).getText().toString();
					String cashbackAmount = ((EditText)(dialog.findViewById(R.id.cashbackAmountEditText))).getText().toString();
					String transactionTypeString = (String)((Spinner)dialog.findViewById(R.id.transactionTypeSpinner)).getSelectedItem();
					
					TransactionType transactionType = TransactionType.GOODS;
					if(transactionTypeString.equals("GOODS")) {
						transactionType = TransactionType.GOODS;
					} else if(transactionTypeString.equals("SERVICES")) {
						transactionType = TransactionType.SERVICES;
					} else if(transactionTypeString.equals("CASHBACK")) {
						transactionType = TransactionType.CASHBACK;
					} else if(transactionTypeString.equals("INQUIRY")) {
						transactionType = TransactionType.INQUIRY;
					} else if(transactionTypeString.equals("TRANSFER")) {
						transactionType = TransactionType.TRANSFER;
					} else if(transactionTypeString.equals("PAYMENT")) {
						transactionType = TransactionType.PAYMENT;
					}
//					pos.setAmountIcon("$");
					pos.setAmountIcon("RMB");
					amountEditText.setText("$" + amount(amount));
					pos.setAmount(amount, cashbackAmount, "384", transactionType);
					AudioActivity.this.amount = amount(amount);
					AudioActivity.this.cashbackAmount = cashbackAmount;
					TRACE.d("输入金额  -- 结束");
					dismissDialog();
				}
    			
    		});
    		
    		dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					pos.cancelSetAmount();
					dialog.dismiss();
				}
    			
    		});
    		
    		dialog.show();
    		
		}
		
		
		@Override
		public void onRequestIsServerConnected() {
			TRACE.d("在线过程请求");
			dismissDialog();
			dialog = new Dialog(AudioActivity.this);
			dialog.setContentView(R.layout.alert_dialog);
			dialog.setTitle(R.string.online_process_requested);
			
			((TextView)dialog.findViewById(R.id.messageTextView)).setText(R.string.replied_connected);
			
			dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					pos.isServerConnected(true);
					dismissDialog();
				}
			});
			
			dialog.show();
		}
		
		@Override
		public void onRequestOnlineProcess(String tlv) {
			TRACE.d("向服务器请求数据");
			dismissDialog();
			dialog = new Dialog(AudioActivity.this);
			dialog.setContentView(R.layout.alert_dialog);
			dialog.setTitle(R.string.request_data_to_server);
			TRACE.i("onRequestOnlineProcess tlv:"+tlv);
			//TODO final String str = Client.send(tlv);
//			TRACE.d("str:"+str);
			
			if(isPinCanceled) {
				((TextView)dialog.findViewById(R.id.messageTextView)).setText(R.string.replied_failed);
			} else {
				((TextView)dialog.findViewById(R.id.messageTextView)).setText(R.string.replied_success);
			}
			
			
			
			dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(isPinCanceled) {
						pos.sendOnlineProcessResult(null);
					} else {
						pos.sendOnlineProcessResult("8A023030");
//						emvSwipeController.sendOnlineProcessResult(str);
					}
					dismissDialog();
				}
			});
			
			dialog.show();
		}
		
		@Override
		public void onRequestTime() {
			TRACE.d("要求终端时间。已回覆");
			dismissDialog();
			String terminalTime = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
			pos.sendTime(terminalTime);
			statusEditText.setText(getString(R.string.request_terminal_time) + " " + terminalTime);
		}
		
		@Override
		public void onRequestDisplay(Display displayMsg) {
			dismissDialog();
			
			String msg = "";
			if(displayMsg == Display.CLEAR_DISPLAY_MSG){
				msg = "";
			}else if(displayMsg == Display.PLEASE_WAIT) {
				msg = getString(R.string.wait); 
			}else if(displayMsg == Display.REMOVE_CARD) {
				msg = getString(R.string.remove_card);
			} else if(displayMsg == Display.TRY_ANOTHER_INTERFACE) {
				msg = getString(R.string.try_another_interface);
			} else if(displayMsg==Display.PROCESSING){
				msg = getString(R.string.processing);
			}
			
			statusEditText.setText(msg);
		}
		
		
		@Override
		public void onRequestFinalConfirm() {
			TRACE.d("确认金额-- S");
			dismissDialog();
			if(!isPinCanceled) {
				dialog = new Dialog(AudioActivity.this);
				dialog.setContentView(R.layout.confirm_dialog);
				dialog.setTitle(getString(R.string.confirm_amount));
				
				String message = getString(R.string.amount) + ": $" + amount;
				if(!cashbackAmount.equals("")) {
					message += "\n" + getString(R.string.cashback_amount) + ": $" + cashbackAmount;
				}
				
				((TextView)dialog.findViewById(R.id.messageTextView)).setText(message);
				
				dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						pos.finalConfirm(true);
						dialog.dismiss();
						TRACE.d("确认金额-- 结束");
					}
				});
				
				dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						pos.finalConfirm(false);
						dialog.dismiss();
					}
				});
				
				dialog.show();
			} else {
				pos.finalConfirm(false);
			}
		}

		
		@Override
		public void onRequestNoQposDetected() {
			dismissDialog();
			statusEditText.setText(getString(R.string.no_device_detected));
		}
		
		@Override
		public void onRequestQposConnected() {
			dismissDialog();
			statusEditText.setText(getString(R.string.device_plugged));
		}
		
		@Override
		public void onRequestQposDisconnected() {
			dismissDialog();
			statusEditText.setText(getString(R.string.device_unplugged));
		}

		@Override
		public void onError(Error errorState) {
			dismissDialog();
			amountEditText.setText("");
			if(errorState == Error.CMD_NOT_AVAILABLE) {
				statusEditText.setText(getString(R.string.command_not_available));
			} else if(errorState == Error.TIMEOUT) {
				statusEditText.setText(getString(R.string.device_no_response));
			} else if(errorState == Error.DEVICE_RESET) {
				statusEditText.setText(getString(R.string.device_reset));
			} else if(errorState == Error.UNKNOWN) {
				statusEditText.setText(getString(R.string.unknown_error));
			} else if(errorState == Error.DEVICE_BUSY) {
				statusEditText.setText(getString(R.string.device_busy));
			} else if(errorState == Error.INPUT_OUT_OF_RANGE) {
				statusEditText.setText(getString(R.string.out_of_range));
			} else if(errorState == Error.INPUT_INVALID_FORMAT) {
				statusEditText.setText(getString(R.string.invalid_format));
			} else if(errorState == Error.INPUT_ZERO_VALUES) {
				statusEditText.setText(getString(R.string.zero_values));
			} else if(errorState == Error.INPUT_INVALID) {
				statusEditText.setText(getString(R.string.input_invalid));
			} else if(errorState == Error.CASHBACK_NOT_SUPPORTED) {
				statusEditText.setText(getString(R.string.cashback_not_supported));
			} else if(errorState == Error.CRC_ERROR) {
				statusEditText.setText(getString(R.string.crc_error));
			} else if(errorState == Error.COMM_ERROR) {
				statusEditText.setText(getString(R.string.comm_error));
			} else if(errorState==Error.MAC_ERROR){
				statusEditText.setText(getString(R.string.mac_error));
			}else if(errorState == Error.CMD_TIMEOUT){
				statusEditText.setText(getString(R.string.cmd_timeout));
			}
		}

		@Override
		public void onReturnReversalData(String tlv) {
//			dismissDialog();
			String content = getString(R.string.reversal_data);
			content += tlv;
			TRACE.i( "listener: onReturnReversalData: "+tlv);
			statusEditText.setText(content);
			
		}

		@Override
		public void onReturnGetPinResult(Hashtable<String, String> result) {
			String pinBlock = result.get("pinBlock");
			String pinKsn = result.get("pinKsn");
			String content = "get pin result\n";
			
			content += getString(R.string.pinKsn) + " " + pinKsn + "\n";
			content += getString(R.string.pinBlock) + " " + pinBlock + "\n";
			statusEditText.setText(content);
			TRACE.i(content);
			
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
		public void onGetCardNoResult(String arg0)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onGetPosComm(int arg0, String arg1, String arg2)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onLcdShowCustomDisplay(boolean arg0)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onPinKey_TDES_Result(String arg0)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onRequestCalculateMac(String arg0)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onRequestSetPin()
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
		public void onReturnApduResult(boolean arg0, String arg1, int arg2)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onReturnBatchSendAPDUResult(LinkedHashMap<Integer, String> arg0)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onReturnCustomConfigResult(boolean arg0, String arg1)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onReturnDownloadRsaPublicKey(HashMap<String, String> arg0)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onReturnPowerOffIccResult(boolean arg0)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onReturnPowerOnIccResult(boolean arg0, String arg1, String arg2, int arg3)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onReturnSetMasterKeyResult(boolean arg0)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onReturnSetSleepTimeResult(boolean arg0)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onReturniccCashBack(Hashtable<String, String> arg0)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onUpdateMasterKeyResult(boolean arg0, Hashtable<String, String> arg1)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onUpdatePosFirmwareResult(UpdateInformationResult arg0)
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
    
    private void clearDisplay() {
		statusEditText.setText("");
	}
    
    private String amount(String tradeAmount){
    	String rs = "";
    	int a = 0;
    	if (tradeAmount == null || "".equals(tradeAmount)) {
			return rs;
		} 
    	try {
			Integer.parseInt(tradeAmount);
		} catch (NumberFormatException e) {
			return rs;
		}
    	TRACE.d("---------------:"+tradeAmount);
    	if(tradeAmount.startsWith("0")){
    		return rs;
    	}
    	a = tradeAmount.length();
    	if(tradeAmount.length()==1){
    		rs = "0.0" +tradeAmount;
    	}else if(tradeAmount.length()==2){
    		rs = "0." +tradeAmount;
    	}else if(tradeAmount.length()>2){
    		rs = tradeAmount.substring(0,a-2) +"." + tradeAmount.substring(a-2,a);
    	}
    	return rs;
    }
    class MyOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			statusEditText.setText("");
			if(v == doTradeButton) {
				isPinCanceled = false;
				amountEditText.setText("");
				statusEditText.setText(R.string.starting);
				pos.doTrade();
//				pos.doCheckCard();
			}
		}
    }
    
}
