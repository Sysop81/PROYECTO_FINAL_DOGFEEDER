package org.dogfeeder.model;

import com.pi4j.io.gpio.*;

/**
 * Clase Relay
 * Clase encargada de modelar un Relé empleado para la activación / desactivación de dispositivos controlados por
 * DOG-FEEDER. En este caso, activa / desactiva el suministro de energía a la Raspberry Pi PICO
 */
public class Relay {
    private GpioPinDigitalOutput relay;

    /**
     * Constructor con parámetros
     * @param pin PIN al que se encuentra conectado el relé
     * @param gpio Instancia del GpioController de Pi4J para interactuar con el PIN GPIO
     */
    public Relay(Pin pin, GpioController gpio){
        this.relay = gpio.provisionDigitalOutputPin(
                pin,
                "Relay",
                PinState.LOW);
    }

    /**
     * Método relayOn
     * Método encargado de activar o permitir el paso de energia del relé. Enciende el microcontrolador
     */
    public void relayOn(){
        relay.high();
    }

    /**
     * Método relayOff
     * Método encargado de desactivar el paso de energia del relé. Apaga el microcontrolador
     */
    public void relayOff(){
        relay.low();
    }

    /**
     * Método isRelayOn
     * Método que permite saber si el relé se encuentra activo
     * @return boolean con el resultado del método de instancia isHigh
     */
    public boolean isRelayOn(){
        return relay.isHigh();
    }

    /**
     * Método isRelayOff
     * Método que permite conocer si el relé se encuentra desactivado.
     * @return boolean con el resultado del método de instancia isLow
     */
    public boolean isRelayOff(){
        return relay.isLow();
    }

}
