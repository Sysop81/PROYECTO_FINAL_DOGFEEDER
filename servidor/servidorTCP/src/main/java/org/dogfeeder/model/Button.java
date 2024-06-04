package org.dogfeeder.model;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;

/**
 * Clase Button
 * Clase estática encargada de modelar los botones físicos empleados en la parte hardware del proyecto [DOG-FEEDER]
 */
public class Button {
    /**
     * Método getButton
     * ESte método se encarga de devolver una instancia del PIN GPIO configurado para el botón físico.
     * @param pin Pin físico asociado al botón
     * @return GpioPinDigitalInput o PIN de entrada digital configurado
     */
    public static GpioPinDigitalInput getButton(Pin pin){
        var gpio = GpioFactory.getInstance();
        return gpio.provisionDigitalInputPin(pin);
    }
}
