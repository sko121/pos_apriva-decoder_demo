package com.dspread.ui;

import java.io.UnsupportedEncodingException;
import android.content.Context;
import android.media.AudioManager;

public class QPOSUtil {
	  static final String HEXES = "0123456789ABCDEF";
	  public static String byteArray2Hex( byte [] raw ) {
	    if ( raw == null ) {
	      return null;
	    }
	    final StringBuilder hex = new StringBuilder( 2 * raw.length );
	    for ( final byte b : raw ) {
	      hex.append(HEXES.charAt((b & 0xF0) >> 4))
	         .append(HEXES.charAt((b & 0x0F)));
	    }
	    return hex.toString();
	  }
	  /**
		 * 16进制格式的字符串转成16进制byte 44 --> byte 0x44
		 * 
		 * @param hexString
		 * @return
		 */
		public static byte[] HexStringToByteArray(String hexString) {//
			if (hexString == null || hexString.equals("")) {
				return new byte[]{};
			}
			if (hexString.length() == 1 || hexString.length() % 2 != 0) {
				hexString = "0" + hexString;
			}
			hexString = hexString.toUpperCase();
			int length = hexString.length() / 2;
			char[] hexChars = hexString.toCharArray();
			byte[] d = new byte[length];
			for (int i = 0; i < length; i++) {
				int pos = i * 2;
				d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
			}
			return d;
		}

		private static byte charToByte(char c) {
			return (byte) "0123456789ABCDEF".indexOf(c);
		}

		/**
		 * 中文字符串转成16进制数组
		 * 
		 * @param str
		 * @return
		 */
		public static byte[] CNToHex(String str) {
			// String string = "";
			// for (int i = 0; i < str.length(); i++) {
			// String s = String.valueOf(str.charAt(i));
			// byte[] bytes = null;
			// try {
			// bytes = s.getBytes("gbk");
			// } catch (UnsupportedEncodingException e) {
			// e.printStackTrace();
			// }
			// for (int j = 0; j < bytes.length; j++) {
			// string += Integer.toHexString(bytes[j] & 0xff);
			// }
			// }
			byte[] b = null;
			try {
				b = str.getBytes("GBK");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return b;
		}

		/**
		 * byte 转换成16进制格式字符串显示
		 * 
		 * @param b
		 * @return
		 */
		public static String getHexString(byte[] b) {
			StringBuffer result = new StringBuffer("");
			for (int i = 0; i < b.length; i++) {
				result.append("0x" + Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1) + ",");
			}
			return result.substring(0, result.length() - 1);
		}

		/**
		 * int转成16进制的byte
		 * 
		 * @param i
		 * @return
		 */
		public static byte[] IntToHex(int i) {
			String string = null;
			if (i >= 0 && i < 10) {
				string = "0" + i;
			} else {
				string = Integer.toHexString(i);
			}
			return HexStringToByteArray(string);
		}

		/**
		 * 将指定byte数组以16进制的形式打印到控制台
		 * @param b
		 */
		public static void printHexString(byte[] b) {
			for (int i = 0; i < b.length; i++) {
				String hex = Integer.toHexString(b[i] & 0xFF);
				if (hex.length() == 1) {
					hex = '0' + hex;
				}
				System.out.print(hex.toUpperCase());
			}

		}

		/**
		 *把16进制字节转换成int
		 * 
		 * @param b
		 * @return
		 */
		public static int byteArrayToInt(byte[] b) {
			int result = 0;
			for (int i = 0; i < b.length; i++) {
				result <<= 8;
				result |= (b[i] & 0xff); //
			}
			return result;
		}

		/**
		 * 异或输入字节流
		 * 
		 * @param b
		 * @param startPos
		 * @param Len
		 * @return
		 */
		public static byte XorByteStream(byte[] b, int startPos, int Len) {
			byte bRet = 0x00;
			for (int i = 0; i < Len; i++) {
				bRet ^= b[startPos + i];
			}
			return bRet;
		}
	  /**
	   * Gets the subarray from <tt>array</tt> that starts at <tt>offset</tt>.
	   */
	  public static byte[] get(byte[] array, int offset) {
	    return get(array, offset, array.length - offset);
	  }

	  /**
	   * Gets the subarray of length <tt>length</tt> from <tt>array</tt>
	   * that starts at <tt>offset</tt>.
	   */
	  public static byte[] get(byte[] array, int offset, int length) {
	    byte[] result = new byte[length];
	    System.arraycopy(array, offset, result, 0, length);
	    return result;
	  }	 
	  
	public static void turnUpVolume(Context context,int factor) {
			int sv;
			
			AudioManager audioManager = (AudioManager) context
					.getSystemService(Context.AUDIO_SERVICE);
			sv=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,sv*factor/10, AudioManager.FLAG_PLAY_SOUND);
	}
	/**
	 * 将毫秒值转换成时分秒（时长）
	 * 
	 * @param l
	 * @return
	 */
	public static String formatLongToTimeStr(Long l) {
		String str = "";
		long hour = 0;
		long minute = 0;
		float second = 0;
		second = (float) l / (float) 1000;
		if (second > 60) {
			minute = (long) (second / 60);
			second = second % 60;
			if (minute > 60) {
				hour = minute / 60;
				minute = minute % 60;
				str = hour + "小时" + minute + "分" + second + "秒";
			} else {
				str = minute + "分" + second + "秒";
			}
		} else {
			str = second + "秒";
		}
		return str;

	}
	//	cd /sdcard
	//	insmod cdc_acm.ko
	//	chmod 0777 /dev/ttyACM&
}
