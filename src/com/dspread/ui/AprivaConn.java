package com.dspread.ui;

/*
 *	  @brief Tests a SSL/TLS connection to the Apriva server
 *	  @date 09/22/2016
 *	  @version 1.3
 *
 * 	  Copyright (c) 2016 by Apriva
 *	  All rights reserved
 *
 *	  CONFIDENTIAL AND PROPRIETARY
 *
 *	  This software may contain confidential and trade secret
 *	  information and technology and may not be used, disclosed or
 *	  made available to others without the permission of Apriva.
 *	  Copies may only be made with permission and must contain the
 *	  above copyright notice. Neither title to the software nor
 *	  ownership of the software is hereby transferred.
 *************************************************************************/
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.*;
import java.util.HashMap;
import java.util.Hashtable;

import javax.net.ssl.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.dspread.xpos.Tlv;

import android.content.Context;
import android.util.Log;

class AprivaConn {
	
	private Context mContext;
	private Hashtable<String, String> decodeData;
	private Hashtable<String, String> otherInfo;
	private HashMap<Integer, Tlv> hm;
	
	public AprivaConn(Context mContext,Hashtable<String, String> decodeData,Hashtable<String, String> otherInfo) {
		super();
		this.mContext = mContext;
		this.decodeData = decodeData;
		this.otherInfo = otherInfo;
	}

	public AprivaConn(Context mContext, HashMap<Integer, Tlv> hm) {
		super();
		this.mContext = mContext;
		this.hm = hm;
	}
	
	// Create an SSL socket factory to use to connect to the Apriva server with
	// Read the appropriate certificate chains and keys from files into the SSL factory
	protected javax.net.ssl.SSLSocketFactory createSSLFactory () {
		Log.i("POS_LOG", "AprivaConn:createSSLFactory");
		try {
			// *** Client Side Certificate *** //
			System.out.println ("2. Loading p12 file");

			// Load the certificate file into the keystore
			KeyStore keystore = KeyStore.getInstance("BKS");
			InputStream inputFile = mContext.getAssets().open(clientCertFileName);

			char [] clientPassphrase = clientCertPassword.toCharArray ();
			keystore.load (inputFile, clientPassphrase);

			// Create the factory
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("X509");
			keyManagerFactory.init (keystore, clientPassphrase);

			//The following section demonstrates how to configure the server trust for production.
			//It is not required for test environments and that is why the code is commented out.
			//Each line required will have the term "JKS line needed for production" following it.
			//The AprivaTrust.jks file included in this project can be used for production.
			
			// *** Server Trust *** //
			//System.out.println ("3. Loading JKS file");
			//KeyStore truststore = KeyStore.getInstance("JKS"); //JKS line needed for production
			//FileInputStream trustInputFile = new FileInputStream (serverTrustFileName); //JKS line needed for production

			//char [] serverTrustPassphrase = serverTrustPassword.toCharArray (); //JKS line needed for production
			//truststore.load (trustInputFile, serverTrustPassphrase); //JKS line needed for production

			//TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()); //JKS line needed for production
			//tmf.init (truststore); //JKS line needed for production

			//TrustManager[] trustManagers = tmf.getTrustManagers (); //JKS line needed for production

			// Create the SSL context and use it to initialize the factory
			SSLContext ctx = SSLContext.getInstance("TLSv1.2");
			//ctx.init (keyManagerFactory.getKeyManagers(), trustManagers, null); //JKS line needed for production
			ctx.init (keyManagerFactory.getKeyManagers(), null, null); //This line should be removed in production, the line above replaces it

			SSLSocketFactory sslFactory = ctx.getSocketFactory();
			return sslFactory;

		} catch (Exception e) {

			e.printStackTrace ();
		}

		return null;
	}

	// Perform the test by connecting to the Apriva server
	protected String test (String host, int port) {
		Log.i("POS_LOG", "AprivaConn:test()");

		try {
			// Create an SSL factory and use it to create an SSL socket
			SSLSocketFactory sslFactory = createSSLFactory ();
//			SSLSocketFactory sslFactory = Test.createSSLFactory(mContext, clientCertFileName, clientCertPassword);

			System.out.println ("4. Connecting to " + host +  " port " + port);
			SSLSocket socket = (SSLSocket) sslFactory.createSocket (host, port);

			// Connect
			socket.startHandshake();

			// Send the XML request to the server
			OutputStream outputstream = socket.getOutputStream();
			OutputStreamWriter outputstreamwriter = new OutputStreamWriter(outputstream);

			BufferedWriter bufferedWriter = new BufferedWriter(outputstreamwriter);
			
			/***
			 * Transaction Number – Stan
			 * 
			***/
			
			//get ksn/enctrack2 etc information from decodeData 
			//pass it to testXML
			DUKPK2009_CBC.ksn = decodeData.get("trackksn");  
			String result = "";
			
			//i don't know stan(Transaction Number),so use a random number between 0-100000 when testing
			int stan = (int)(Math.random() * 100000);
//			String cardNumber = DUKPK2009_CBC.getCardNumber(decodeData.get("encTrack2"));
			String cardNumber = "";
			
			String  Amount =  otherInfo.get("amount") ;
			String track1 = decodeData.get("encTrack1");
			String track2 = decodeData.get("encTrack2");
			String ksn = decodeData.get("trackksn");
			String keyID = decodeData.get("formatID");
			
			//generate expiredate
			String ExpireDate = decodeData.get("expiryDate").substring(0,2) + "/" + decodeData.get("expiryDate").substring(2,4);
			
			System.out.println ("expiredate:" + decodeData.get("expiryDate") + "\n");
			System.out.println ("expiredate:" + ExpireDate + "\n");
			System.out.println ("cardnumber:" + cardNumber + "\n");
			
			String formatID = decodeData.get("formatID");
			System.out.println ("formatID:" + formatID + "\n");
			if (formatID.equals("31") || formatID.equals("40") || formatID.equals("37") || formatID.equals("17")
					|| formatID.equals("11") || formatID.equals("10")) {
				return "";
			}else {
				//解密	
				if(decodeData.get("encTrack1") != null && !"".equals(decodeData.get("encTrack1"))) {
					track1Decrypt = DUKPK2009_CBC.getCardNumber(decodeData.get("encTrack1"));  
				}
				result +=  "CardHolder:" + decodeData.get("cardholderName") + "\n";  
				result +=  "CardNumber:" + DUKPK2009_CBC.getCardNumber(decodeData.get("encTrack2")) + "\n\n";   
				result += "***************Response from Apriva****************\n";   
				
			}
			
			//the following value for test...
//			ExpireDate = "17/08"; //java test
//			cardNumber = "4111111111111112";//java test
//			cardNumber = "4003000123456781";
			cardNumber = decodeData.get("maskedPAN");
//			ksn = "01116062000001E00024";
//			track2 = "42325DEB54A724297D8C93334A9B54B3C7471064A03B8BE9";
//			cardNumber = "5499990123456781";
//			stan = 99913;
			
//			String testXML =
//					"<AprivaPosXml DeviceAddress=\"7771314\">"
//					+ "<Credit MessageType=\"Request\" Version=\"5.0\" ProcessingCode=\"Sale\">"
//					+ "<Stan>"+ stan + "</Stan>"
//					+ "<CardPresent>YES</CardPresent>"
//					+ "<EntryMode>Manual</EntryMode>"
//					+ "<EntryModeType>Standard</EntryModeType>"
//					+ "<ExpireDate>" + ExpireDate + "</ExpireDate>"
//					+ "<Amount>" + Amount + "</Amount>"
//					+ "<AccountNumber>" +  cardNumber+ "</AccountNumber>"
//					+ "</Credit></AprivaPosXml>";
			//"<AprivaPosXml DeviceAddress=\"0000000008\">" 
			
			//use  :   Amount   stan  Track2  ksn  track2   cardNumber  
			String testXML =
					"<AprivaPosXml DeviceAddress=\"00000008\">" 
							+ "<Credit MessageType=\"Request\" Version=\"5.0\" ProcessingCode=\"Sale\">"
								+ "<Amount>" + 1.23 + "</Amount>"
								+ "<Stan>"+ stan + "</Stan>"
								+ "<EntryModeType>Standard</EntryModeType>"
								+ "<EntryMode>Track2</EntryMode>"
								+ "<EncryptedCardDataEncoding>ASCIIISOBCD</EncryptedCardDataEncoding>"
								+ "<CardInfo IsEncrypted=\"1\">"
									+"<CardData>"
										+"<EncryptedCardDataKeyID>" + "11" + "</EncryptedCardDataKeyID>"
										+"<EncryptedCardDataKsn>" + ksn + "</EncryptedCardDataKsn>"
										+"<EncryptedCardDataEncryptedData>" + track2 + "</EncryptedCardDataEncryptedData>"
										+"<EncryptedCardDataMaskedPAN>" +  cardNumber + "</EncryptedCardDataMaskedPAN>"
									+"</CardData>"
								+"</CardInfo>"
							+ "</Credit>"
					+ "</AprivaPosXml>";     
			
			//insert card
//			String testXML =
//					"<AprivaPosXml DeviceAddress=\"0000000456\">" 
//							+ "<Credit MessageType=\"Request\" Version=\"5.0\" ProcessingCode=\"Sale\">"
//								+ "<Amount>" + 1.23 + "</Amount>"
//								+ "<Stan>"+ stan + "</Stan>"
//								+ "<EntryModeType>SmartChipCard</EntryModeType>"
//								+ "<EntryMode>Track2</EntryMode>"
//								+"<CardPresent>YES</CardPresent>"
//								+"<EMVData>"
//									+"<EncryptedTLVData>FCEE3DA068D3766A085116D8DBC09245BA4D23BDA55BF6F6830C480A2FEF287E7299FACFF8E56C19DDC2D31CCEED7138F13D5B247A203E36CD98395C3AFB743F7FDDBBCF6DC14BD8E0A2748E6277DA92321A70BFBBF31CBFFB3DD49E2A19DBAEA0AF11B27E241C13FF1B24B0B45FFF4141CCAC73B2CA32FBBC53784A543E539FF1EBB3B40393FA66AF7C73CE8B444D3C69D29D8A687CB29C5792824D909E8646CDC042B1CEB1588B6A9C8FD94D5F80E9E83E40A94AC424C98BBFC44B570C5840CBF97AD0239D001127B1C1583E4FE4D71BACD8A0B436024050CA795FADBD2AFBDF7AA9CDB6DCB15103AE8F82B0E0219354A8E5E506AC2298D1E963FF24BA6A17C6C4547136EE5D70642CB0616561C257459362E82A412D8F</EncryptedTLVData>"
//									+"<EncryptedEMVDataKsn>217832CA010000600004</EncryptedEMVDataKsn>"
//									+"<EncryptedCardDataEncryptedData>537E156D128C322EAF43F2130F98FCCB7BB74FD39B6F772CA5C5E4B86D163350</EncryptedCardDataEncryptedData>"
//									+"<EncryptedCardDataKsn>217832CA010000600004</EncryptedCardDataKsn>"
//								+"</EMVData>"
//								+ "<EncryptedCardDataEncoding>ASCIIISOBCD</EncryptedCardDataEncoding>"
//							+ "</Credit>"
//					+ "</AprivaPosXml>";     
			
			//success
//			String testXML =
//					"<AprivaPosXml DeviceAddress=\"00000008\">" 
//							+ "<Credit MessageType=\"Request\" Version=\"5.0\" ProcessingCode=\"Sale\">"
//								+ "<Amount>" + 0.01 + "</Amount>"
//								+ "<Stan>"+ stan + "</Stan>"
//								+ "<EntryModeType>Standard</EntryModeType>"
//								+ "<EntryMode>Track2</EntryMode>"
//								+ "<EncryptedCardDataEncoding>ASCIIISOBCD</EncryptedCardDataEncoding>"
//								+ "<CardInfo IsEncrypted=\"1\">"
//									+"<CardData>"
//										+"<EncryptedCardDataKeyID>" + "11" + "</EncryptedCardDataKeyID>"
//										+"<EncryptedCardDataKsn>" + "01116062000001E00002" + "</EncryptedCardDataKsn>"
//										+"<EncryptedCardDataEncryptedData>" + "D30750C6483167F39CD725AF4597E4D4AEB1FEBED5E5E4E7" + "</EncryptedCardDataEncryptedData>"
//										+"<EncryptedCardDataMaskedPAN>" +  "549999XXXXXX6781" + "</EncryptedCardDataMaskedPAN>"
//									+"</CardData>"
//								+"</CardInfo>"
//							+ "</Credit>"
//					+ "</AprivaPosXml>";   
			
			
//			String testXML =
//					<AprivaPosXml DeviceAddress="0000000008">
//			  <Credit MessageType="Request" Version="5.0" ProcessingCode="Sale">
//			    <Amount>1.23</Amount>
//			    <Stan>12</Stan>
//			    <EntryModeType>Standard</EntryModeType>
//			    <EntryMode>Track2</EntryMode>
//			    <EncryptedCardDataEncoding>ASCIIISOBCD</EncryptedCardDataEncoding>
//			    <CardInfo IsEncrypted="1">
//			      <CardData>
//			        <EncryptedCardDataKeyID>11</EncryptedCardDataKeyID>
//			        <EncryptedCardDataKsn>01126062000001E00003</EncryptedCardDataKsn>
//			        <EncryptedCardDataEncryptedData>61C91A928FD9CDED76D2B13162FD2CFF402621FE45C57DAB</EncryptedCardDataEncryptedData>
//			        <EncryptedCardDataMaskedPAN>549999XXXXXX6781</EncryptedCardDataMaskedPAN>
//			      </CardData>
//			    </CardInfo>
//			    <CardPresent>YES</CardPresent>
//			  </Credit>
//			</AprivaPosXml>
			
//			String testXML = "<AprivaPosXml DeviceAddress=\"7771314\"><Credit MessageType=\"Request\" Version=\"5.0\" ProcessingCode=\"Sale\"><Stan>5</Stan><CardPresent>YES</CardPresent><EntryMode>Manual</EntryMode><EntryModeType>Standard</EntryModeType><ExpireDate>17/08</ExpireDate><Amount>123.00</Amount><AccountNumber></AccountNumber></Credit></AprivaPosXml>";

			System.out.println ("5. Sending Request --->>>>>>");
			System.out.println (formatPrettyXML(testXML));
			
			bufferedWriter.write (testXML);
			bufferedWriter.flush ();

			System.out.println ("6. Waiting for Response <<<<<<--------");
			InputStream inputstream = socket.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedReader = new BufferedReader(inputstreamreader);
			
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				System.out.println(formatPrettyXML(line));
				result += formatPrettyXML(line);
			}
			inputstream.close();
			outputstream.close();
			socket.close();
			sslFactory = null;
			
			return result;

		} catch (Exception e) {
			e.printStackTrace ();
			return null;
		}
		
	}

	protected static String formatPrettyXML(String unformattedXML) {
		String prettyXMLString = null;
		
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			StreamResult result = new StreamResult(new StringWriter());
			StreamSource source = new StreamSource(new StringReader(unformattedXML));
			transformer.transform(source, result);
			prettyXMLString = result.getWriter().toString();			
		} catch (TransformerConfigurationException e) {
			System.out.println("Unable to transform XML " + e.getMessage());
		} catch (TransformerFactoryConfigurationError e) {
			System.out.println("Unable to transform XML " + e.getMessage());
		} catch (TransformerException e) {
			System.out.println("Unable to transform XML " + e.getMessage());
		}
		
		return prettyXMLString;
	}
	
	// Main Function (EntryPoint)
	public  String connect() throws IOException
	{	
		
		// Display the current local directory
		String current = new java.io.File( "." ).getCanonicalPath();
	    System.out.println("Current dir: "+current);
		
	    //Apriva后台服务器域名和端口
//		String HostName = "aibapp53.aprivaeng.com";
	    	String HostName = "aibapp144.aprivaeng.com";
		String HostPort = "11098";
		
		// The file containing the client certificate, private key, and chain
		clientCertFileName = "LimePC11BKS.p12";
//		clientCertFileName = "AprivaDeveloperBKS.p12";
		clientCertPassword = "P@ssword";

		// The file containing the server trust chain
		serverTrustFileName = "cert/AprivaTrust.jks";
		serverTrustPassword = "P@ssword";

		String host = HostName;
		int port = Integer.parseInt(HostPort);
		System.out.println ("Java Sample App v1.2 - AIB .53");
		System.out.println ("1. Running Test");
		return test (host, port);
//		return httpTest();
	}
	
	public  String httpTest() {
		URL loginUrl = null;
		HttpsURLConnection connection = null;
		String result = null;
		try {
			// 创建URL对象
//			loginUrl = new URL("http","aibapp144.aprivaeng.com",11098,null);
			loginUrl = new URL("https://aibapp144.aprivaeng.com");
			Proxy proxy = new Proxy(java.net.Proxy.Type.HTTP,new InetSocketAddress("aibapp144.aprivaeng.com", 11098));
			// 初始化连接对象
			connection = (HttpsURLConnection) loginUrl
					.openConnection(proxy);
			SSLSocketFactory sslFactory = createSSLFactory ();
			
			// 设置连接参数
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setConnectTimeout(6000);
			connection.setReadTimeout(6000);
			// HttpURLConnection.setFollowRedirects(true);connection.connect();
			connection.setInstanceFollowRedirects(false);
			connection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			connection.setSSLSocketFactory(sslFactory);
			// 开始连接
			connection.connect();
			// 创建输出流对象
			DataOutputStream out = new DataOutputStream(
					connection.getOutputStream());
			result = String.valueOf(connection.getResponseCode());
			System.out.println("connection.getResponseCode() :::::::::::" + result);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connection.disconnect();
		}
		return result;
	}

	
	static String clientCertFileName;   //证书名称
	static String clientCertPassword;  //证书密钥
	static String serverTrustFileName; 
	static String serverTrustPassword;
	static String clientCertFileNameBKS;  
	private String decodeDataResult;
	private String track1Decrypt;
}



