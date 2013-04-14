package com.bluetooth.daemon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

public class BluetoothListener {

	static String lineRead = "";
	static String lock = "gnome-screensaver-command -l";
	static boolean exit = false;
	static String[] env;

	// start server
	private void startListener() throws IOException {
		
		// get environment variables
		String line;
		int i = 0;
		BufferedReader br;

		br = new BufferedReader(
				new FileReader(new File("/tmp/environment.log")));
		while ((line = br.readLine()) != null)
			i++;
		env = new String[i];
		i = 0;
		br.close();
		br = new BufferedReader(
				new FileReader(new File("/tmp/environment.log")));
		while ((line = br.readLine()) != null) {
			env[i++] = line;
		}
		
		while (!exit) {
			exit = true;

			// Create a UUID for SPP
			javax.bluetooth.UUID uuid = new javax.bluetooth.UUID("1101", true);
			// Create the service url
			String connectionString = "btspp://localhost:" + uuid
					+ ";name=Sample SPP Server";

			// open server url
			StreamConnectionNotifier streamConnNotifier = (StreamConnectionNotifier) Connector
					.open(connectionString);

			// Wait for client connection
			System.out
					.println("\nServer Started. Waiting for clients to connect...");
			StreamConnection connection = streamConnNotifier.acceptAndOpen();

			// verify MAC address
			RemoteDevice dev = RemoteDevice.getRemoteDevice(connection);
			byte[] macFromFile = read();
			byte[] actualMac = Encryptor.encrypt(dev.getBluetoothAddress());
			for (int counter = 0; counter < actualMac.length; counter++) {
				if (macFromFile[counter] != actualMac[counter]) {
					exit = false;
					break;
				}
			}

			// read string from spp client
			InputStream inStream = connection.openInputStream();
			BufferedReader bReader = new BufferedReader(new InputStreamReader(
					inStream));
			lineRead = bReader.readLine();
			System.out.println(lineRead);

			// close connection
			connection.close();
			streamConnNotifier.close();

			System.out.println("exiting from BLuetoothListener");
		}
	}

	public static byte[] read() {
		byte[] hash = null;
		try {
			FileInputStream fis = null;
			fis = new FileInputStream("/tmp/." + getUserName() + ".dat");
			ObjectInputStream ois = new ObjectInputStream(fis);
			hash = (byte[]) ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hash;
	}

	static String getUserName() {
		try {
			String line;
			Process p = Runtime.getRuntime().exec("whoami", env);

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

	public static void main(String[] args) {

		// display local device address and name
		LocalDevice localDevice = null;
		try {
			localDevice = LocalDevice.getLocalDevice();
		} catch (BluetoothStateException e) {
			e.printStackTrace();
		}
		System.out.println("Address: " + localDevice.getBluetoothAddress());
		System.out.println("Name: " + localDevice.getFriendlyName());

		try {
			new BluetoothListener().startListener();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}