package org.dogfeeder.model;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;

public class Button {
    public static GpioPinDigitalInput getButton(Pin pin){
        var gpio = GpioFactory.getInstance();
        return gpio.provisionDigitalInputPin(pin);
    }
}
