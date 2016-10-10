/*
 * Copyright (C) 2016 teju <tejendersheoran@gmail.com>
 * See License file for licensing information
 * Based on usb4java library <http://github.com/usb4java/usb4java>
*/
package org.ykc.usbmanager;

import java.util.List;
import java.util.ArrayList;

import javax.usb.UsbClaimException;
import javax.usb.UsbConfiguration;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbInterfacePolicy;
import javax.usb.UsbNotActiveException;
import javax.usb.UsbPipe;
import javax.usb.UsbServices;
import javax.usb.event.UsbServicesEvent;
import javax.usb.event.UsbServicesListener;


public class USBManager{

	private static USBManager instance;
	private ArrayList<USBManListener> listenerList = new ArrayList<USBManListener>();
	private UsbServices services;

	private USBManager() throws SecurityException, UsbException {
	  services = UsbHostManager.getUsbServices();
      services.addUsbServicesListener(new UsbServicesListener() {
	      @Override
	      public void usbDeviceAttached(UsbServicesEvent use) {
	          dispatchAttachEvent();
	      }

	      @Override
	      public void usbDeviceDetached(UsbServicesEvent use) {
	    	  dispatchDetachEvent();
	      }
	   });
	}

	public static USBManager getInstance() throws SecurityException, UsbException {
		if (instance == null) {
			instance = new USBManager();
		}
		return instance;
	}

	private ArrayList<UsbDevice> tryScan(short vid, short pid)
	{
		ArrayList<UsbDevice> devList = new ArrayList<UsbDevice>();
		try {
			scanDevices(devList, services.getRootUsbHub(), vid, pid);
		} catch (SecurityException | UsbException e) {
		}
		return devList;
	}

	private void scanDevices(ArrayList<UsbDevice> devList, UsbHub hub, short vid, short pid)
	{
        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices())
        {
            try {
				if (device.isUsbHub())
				{
				    scanDevices(devList,(UsbHub) device, vid, pid);
				}
				else
				{
					UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
					if((vid == 0) && (pid == 0))
					{
						devList.add(device);
					}
					else
					{
				        if ((desc.idVendor() == vid) && (desc.idProduct() == pid))
				        {
				        	devList.add(device);
				        }
					}
				}
			}
            catch (NullPointerException e) {
			}
        }
	}

	private void dispatchAttachEvent()
	{
		ArrayList<UsbDevice> devlist = getDeviceList();
		for(USBManListener ls : listenerList)
		{
			ls.deviceAttached(new USBManEvent(this, devlist));
		}
	}

	private void dispatchDetachEvent()
	{
		ArrayList<UsbDevice> devlist = getDeviceList();
		for(USBManListener ls : listenerList)
		{
			ls.deviceDetached(new USBManEvent(this, devlist));
		}
	}

	/* List all the usb devices */
	public ArrayList<UsbDevice> getDeviceList()
	{
		return getDeviceList((short)0,(short)0);
	}

	/*
	 * List only specific VID PID devices
	 * Passing vid = 0 and pid = 0 will retrive all devices connected
	 */
	public ArrayList<UsbDevice> getDeviceList(short vid, short pid)
	{
		return tryScan(vid, pid);
	}

	public void addUSBManListener(USBManListener newListener)
	{
		listenerList.add(newListener);
	}

	public void removeUSBManListener(USBManListener listener)
	{
		listenerList.remove(listener);
	}

	static public ArrayList<UsbDevice> filterDeviceList(ArrayList<UsbDevice> inputList, short vid, short pid)
	{
		ArrayList<UsbDevice> newList = new ArrayList<UsbDevice>();
		for(UsbDevice dev : inputList)
		{
			try {
				UsbDeviceDescriptor desc = dev.getUsbDeviceDescriptor();
				if((desc.idVendor() == vid) && (desc.idProduct() == pid))
				{
					newList.add(dev);
				}
			} catch (NullPointerException e) {
			}
		}
		return newList;
	}

	static public boolean isDevicePresent(ArrayList<UsbDevice> inputList, UsbDevice dev)
	{
		for(UsbDevice device : inputList)
		{
			try {
				if(dev == device)
				{
					return true;
				}
			} catch (NullPointerException e) {
				if(dev == null)
				{
					return false;
				}
			}
		}
		return false;
	}

	/* Blocking Endpoint transfer */
    public static int epXfer(UsbDevice dev, byte epAddr, byte[] datArray)
    {
    	if(dev == null)
    	{
    		return -1;
    	}

        UsbEndpoint endpoint;
        UsbPipe pipe = null;
        UsbInterface iface = null;
        try
        {
	        UsbConfiguration configuration = dev.getActiveUsbConfiguration();
	        iface = configuration.getUsbInterface((byte) 0);

            iface.claim(new UsbInterfacePolicy()
            {
                @Override
                public boolean forceClaim(UsbInterface usbInterface)
                {
                    return true;
                }
            });

            endpoint = iface.getUsbEndpoint(epAddr);
            pipe = endpoint.getUsbPipe();
            pipe.open();
            int size = pipe.syncSubmit(datArray);
            return size;

        }
        catch ( Exception ex ) {
        	return -1;
        }

        finally
        {
            try {
                pipe.close();
                iface.release();
            } catch (Exception ex) {
            }
        }
    }

}
