/*
 * Copyright (C) 2016 teju <tejendersheoran@gmail.com>
 * See License file
*/
package org.ykc.usbmanager;

import java.util.ArrayList;
import java.util.EventObject;
import javax.usb.UsbDevice;

@SuppressWarnings("serial")
public class USBManEvent extends EventObject{

	private ArrayList<UsbDevice> devList;

	public USBManEvent(Object source, ArrayList<UsbDevice> list) {
		super(source);
		devList = list;
	}

	public ArrayList<UsbDevice> getDeviceList()
	{
		return devList;
	}
}
