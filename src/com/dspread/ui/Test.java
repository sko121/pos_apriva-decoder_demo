package com.dspread.ui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.Security;
import java.util.Enumeration;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import android.content.Context;

public class Test {
	// Create an SSL socket factory to use to connect to the Apriva server with 
	// Read the appropriate certificate chains and keys from files into the SSL factory 
	public static SSLSocketFactory createSSLFactory (Context mContext, String clientCertFileName, String clientCertPassword) { 
		try { 
			// Display the current local directory 
			String current = new java.io.File( "." ).getCanonicalPath(); 
			System.out.println("Current dir: "+current); 

			String HostName = "aibapp144.aprivaeng.com"; 
			String HostPort = "11098"; 

			// The file containing the client certificate, private key, and chain 
//			clientCertFileName = "cert/AprivaDeveloper.p12"; 
//			clientCertFileName = "LimePC.p12";
			clientCertFileName = "AprivaDeveloperBKS.p12";
			clientCertPassword = "P@ssword"; 

			// The file containing the server trust chain 
			//serverTrustFileName = "cert/AprivaTrust.jks"; 
			//serverTrustPassword = "P@ssword"; 

			String host = HostName; 
			int port = Integer.parseInt(HostPort); 

			// *** Client Side Certificate *** // 
			System.out.println ("2. Loading p12 file"); 

			// Load the certificate file into the keystore 
			KeyStore keystore = KeyStore.getInstance("BKS"); 
//			FileInputStream inputFile = new FileInputStream (clientCertFileName); 
			InputStream inputFile = mContext.getAssets().open(clientCertFileName);

			char [] clientPassphrase = clientCertPassword.toCharArray (); 
			keystore.load (inputFile, clientPassphrase); 

			// Create the factory 
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509"); 
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
			SSLContext ctx = SSLContext.getInstance("TLS"); 
			//ctx.init (keyManagerFactory.getKeyManagers(), trustManagers, null); //JKS line needed for production 
			ctx.init (keyManagerFactory.getKeyManagers(), null, null); //This line should be removed in production, the line above replaces it 

			SSLSocketFactory sslFactory = ctx.getSocketFactory(); 
			return sslFactory; 
		} catch (Exception e) { 
			e.printStackTrace (); 
		} 
		return null; 
	}
	
	public static void jks2bks(String jkspath, String jkspass, String bkspath,
			String bkspass) {
		FileInputStream jksFileInputStream = null;
		FileOutputStream bksFileOutputStream = null;
		try {
			KeyStore jksKeyStore = KeyStore.getInstance("JKS");
			jksFileInputStream = new FileInputStream(jkspath);
			jksKeyStore.load(jksFileInputStream, jkspass.toCharArray());

			KeyStore bksKeyStore = KeyStore.getInstance("BKS",
					new BouncyCastleProvider());
			Security.addProvider(new BouncyCastleProvider());
			bksFileOutputStream = new FileOutputStream(bkspath);
			bksKeyStore.load(null, bkspass.toCharArray());

			Enumeration aliases = jksKeyStore.aliases();
			while (aliases.hasMoreElements()) {
				String alias = (String) aliases.nextElement();

				if (jksKeyStore.isCertificateEntry(alias)) {
					System.out.println("isCertificateEntry:" + alias);
					java.security.cert.Certificate certificate = jksKeyStore
							.getCertificate(alias);
					bksKeyStore.setCertificateEntry(alias, certificate);
				} else if (jksKeyStore.isKeyEntry(alias)) {
					System.out.println("isKeyEntry:" + alias);
					Key key = jksKeyStore.getKey(alias, jkspass.toCharArray());
					java.security.cert.Certificate[] certificates = jksKeyStore
							.getCertificateChain(alias);
					bksKeyStore.setKeyEntry(alias, key, bkspass.toCharArray(),
							certificates);
				}
			}
			bksKeyStore.store(bksFileOutputStream, bkspass.toCharArray());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jksFileInputStream != null) {
				try {
					jksFileInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (bksFileOutputStream != null) {
				try {
					bksFileOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
