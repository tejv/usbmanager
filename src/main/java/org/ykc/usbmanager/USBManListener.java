/* 
 * Copyright (C) 2016 teju <tejendersheoran@gmail.com>
 * See License file
*/
package org.ykc.usbmanager;

public interface USBManListener {
	void deviceAttached(USBManEvent e);
	void deviceDetached(USBManEvent e);
}
