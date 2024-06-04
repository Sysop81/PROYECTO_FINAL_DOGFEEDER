package org.dogfeeder.model;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;

/**
 * Clase HX711
 * Esta clase se encarga de modelar el modulo controlador HX711 empleado para manejar la galga de pesaje incluida en
 * DOG-FEEDER
 */
public class HX711 {
    private final GpioPinDigitalOutput pinCLK;
    private final GpioPinDigitalInput pinDAT;
    private int gain;
    
    // ** Valores implícitos de calibración[Adaptados al cuenco de comida impreso].
    // [TODO] Futuras mejoras, añadir sistema de calibración a la aplicación cliente.
    private final long EMPTY_VALUE = 8388436;
    private final double EMPTY_WEIGHT = 0.0d;
    private final long CALIBRATION_VALUE = 8229924;
    private final double CALIBRATION_WEIGHT = 150d;

    private double weight = 0.0d;
    private long value = 0;

    /**
     * Constructor con parametros
     * @param pinDAT Pin GPIO configurado como entrada de datos
     * @param pinSCK Pin GPIO configurado como salida de datos
     * @param gain Rango de media o precición de la medición
     */
    public HX711(GpioPinDigitalInput pinDAT, GpioPinDigitalOutput pinSCK, int gain) {
        this.pinCLK = pinSCK;
        this.pinDAT = pinDAT;
        setGain(gain);
    }

    /**
     * Método read
     * Método encargado de realizar lectura del pesaje de la galga obteniedo el peso e instanciado la propiedad weight
     */
    public void read() {
        // Step 1. Se establece el Pin de salida en estado bajo
        pinCLK.setState(PinState.LOW);

        // Step 2. Esperamos a que el módulo HX711 se encuentre preparado para realizar envío de datos
        while (!isReady()) {
            sleep();
        }

        // Step 3. Se procede a realizar la lectura desde el módulo HX711
        long count = 0;
        for (int i = 0; i < this.gain; i++) {
            pinCLK.setState(PinState.HIGH); // Se activa PIN de salida
            count = count << 1; // Se realiza un desplazamiento binario a la izquierda
            pinCLK.setState(PinState.LOW); // Se desactiva el pin de salida
            if (pinDAT.isHigh()) {   // Si el pin de entrada esta activo o en pesaje se incrementa el valor de count
                count++;
            }
        }
        // Step 4. Se vuelve a activar pin de salida y se ajusta el valor leido de count realizando un XOR y finalmente se
        //         vuelve a desactivar el pin de salida
        pinCLK.setState(PinState.HIGH);
        count = count ^ 0x800000;
        pinCLK.setState(PinState.LOW);

        // Step 5. Almacenamos el valor de count en la propiedad value [Valor leído]
        value = count;
        // Step 6. Se calcula el peso aplicando la formula y se establece el valor de la propiedad weight
        weight = (value - EMPTY_VALUE)*((CALIBRATION_WEIGHT - EMPTY_WEIGHT)/(CALIBRATION_VALUE - EMPTY_VALUE));

    }

    /**
     * Método getNetWeight
     * Método encargado de devolver el peso neto del contenido del cuenco de alimento.
     * @return double Valor neto del pesaje del alimento [Descontado el peso del cuenco]
     */
    public double getNetWeight(){
        return this.weight - CALIBRATION_WEIGHT;
    }

    /**
     * Método setGain
     * Setter para la propiedad gain. Ajuste de la tolerancia o precisión de la medición
     * @param gain
     */
    public void setGain(int gain) {
        // Step 1. Se establece la ganancia en función del valor entero que llega como parámetro
        switch (gain) {
            case 128:       // Factor de ganancia de 128
                this.gain = 24;
                break;
            case 64:        // Factor de ganancia de 64
                this.gain = 26;
                break;
            case 32:        // Factor de ganancia de 32
                this.gain = 25;
                break;
        }
        // Step 2. Se establece el PIN de salida en estado bajo o desactivado y se procede a realizar lectura de la galga
        pinCLK.setState(PinState.LOW);
        read();
    }

    /**
     * Método isReady
     * Método que determina si el PIN marcado como entrada se encuentra en estado bajo o alto, necesario para poder realizar
     * lecturas de pesaje desde el módulo HX711
     * @return boolean con el estado del PIN de entrada
     */
    public boolean isReady() {
        return pinDAT.isLow();
    }

    /**
     * Método sleep.
     * Método sleep. Igual que hacer un Thread sleep directamente, pero así evitamos advertencia de uso en un bucle
     */
    private void sleep() {
        try {
            Thread.sleep(1);
        } catch (Exception ex) {
        }
    }

}
