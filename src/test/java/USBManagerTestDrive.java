/*
 * Copyright (C) 2016 teju <tejendersheoran@gmail.com>
 * See License file
*/
import org.ykc.usbmanager.*;
import java.util.ArrayList;
import javax.usb.UsbDevice;
import javax.usb.UsbException;



public class USBManagerTestDrive implements USBManListener{
	USBManager um;
	static final short TEST_VID = 0x4B4;
	static final short TEST_PID = 0x0072;

	ArrayList<UsbDevice> attachList;
	ArrayList<UsbDevice> detachList;

	USBManagerTestDrive() throws SecurityException, UsbException{
		um = USBManager.getInstance();
		printDevices(um.getDeviceList());
		um.addUSBManListener(this);
	}
	public static void main(String[] args) {
		System.out.println("Start");
		try {
			USBManagerTestDrive test = new USBManagerTestDrive();
		} catch (SecurityException | UsbException e) {
			System.out.println("Error in USB Manager");
			return;
		}

		while(true);
	}

	@Override
	public void deviceAttached(USBManEvent e) {
		System.out.println("Attached Listener");
		System.out.println("Printing all attached devices...");
		printDevices(e.getDeviceList());
		printTestDevices(e.getDeviceList());

		if(false)
		{
			/* Data test */
		    byte[] send_cmd = {1,0,0,0};
		    for(UsbDevice dev : attachList)
		    {
		        if(USBManager.epXfer(dev, (byte)0x03, send_cmd) == 4)
		        {
		            System.out.println("Success");
		        }
		        else
		        {
		            System.out.println("Fail");
		        }
		    }
		}
	}

	@Override
	public void deviceDetached(USBManEvent e) {
		System.out.println("Detached Listener");
		System.out.println("Printing all attached devices...");
		printDevices(e.getDeviceList());
		printTestDevices(e.getDeviceList());

	}

	static void printDevices(ArrayList<UsbDevice> devList){
		for(UsbDevice dev : devList){
			System.out.println(dev.toString());
		}
	}

	public void printTestDevices(ArrayList<UsbDevice> devList)
	{
		System.out.println("Printing Test Device Status...");
		detachList = USBManager.filterDeviceList(devList, TEST_VID, TEST_PID);
		if(detachList.size() > 0)
		{
			printDevices(detachList);
		}
		else
		{
			System.out.println("-> Device not connected");
		}
	}
}
