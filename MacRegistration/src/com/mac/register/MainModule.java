package com.mac.register;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;

public class MainModule {
	static String mac;
	static String actualMac = "";
	static String userName;
	static byte[] hash;
	
	public static void main(String[] args) {
		if (args.length < 1 || args.length > 1) {
			System.out
					.println("Usage : Pass only the MAC address as the arguement.");
			return;
		}

		mac = args[0];
		if(!validateMac(mac)) {
			System.out.println("Wrong MAC address format.");
		}
		
		String[] chunks = mac.split(":");
		for(int i=0; i< chunks.length; i++)
			actualMac += chunks[i];
		
		hash = Encryptor.encrypt(actualMac);
		userName = getUserName();
		
		storeData(userName, hash);
	}

	static String getUserName() {
		try {
			String line;
			Process p = Runtime.getRuntime().exec("whoami");

			BufferedReader in = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			if ((line = in.readLine()) != null) {
				return line.toString();
			}
			in.close();
			return null;
		} catch (Exception e) {
			return null;
		}
	}
	
	static boolean validateMac(String address) {
		return address.matches("^([0-9A-F]{2}[:-]){5}([0-9A-F]{2})$");

	}
	
	static void storeData(String userName, byte[] mac) {
		try {
			FileOutputStream fos = null;		
			fos = new FileOutputStream("/home/" + getUserName() + "/."
					+ getUserName() + ".dat");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(mac);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}