package org.dogfeeder.model;

import com.pi4j.io.gpio.*;
import org.dogfeeder.Utils.Tools;
import org.dogfeeder.cli.CLInterface;

/**
 * Clase UltraSonic
 * Clase encargada de modelar el comportamiento del módulo de ultrasonidos empleado para determinar la cantidad de alimento
 * disponible en la tolva.
 */
public class UltraSonic {
    private enum HOPPER_STATUS{
        DANGER,WARNING,OK
    }

    // Los niveles se estableceen de forma inversa, debido a que cuanto mas vacía está la tolva mas distancia en cm mide
    // el sensor de ultrasonidos.
    private static final double MIN_LEVEL = 13;// [cm]
    private static final double MED_LEVEL = 7; // [cm]
    private static final double MAX_LEVEL = 3; // [cm]
    private static final double SOUND_SPEED = 34300; // Velocidad del sonido en cm/s

    private GpioPinDigitalOutput triggerPin;
    private GpioPinDigitalInput echoPin;

    /**
     * Constructor con parametros
     * @param trigger Pin de salida
     * @param echo Pin de entrada
     */
    public UltraSonic(GpioPinDigitalOutput trigger,GpioPinDigitalInput echo){
        this.triggerPin = trigger;
        this.echoPin = echo;
    }

    /**
     * Método getDistance
     * @return double con el valor de distancia en cm desde el medidor de ultrasonidos hasta donde rebote el sonido
     */
    public double getDistance(){
        // Step 1. Generamos un pulso corto en el pin TRIGGER [Pin de disparo o salida]
        triggerPin.high();
        // Se realiza una espera y se establece el Pin de disparo a estado bajo o desactivado.
        try {
            Thread.sleep(0, 10000); // 10 microsegundos
        } catch (InterruptedException e) {}
        triggerPin.low();

        // Step 2. Esperamos hasta que el pin ECHO se encuentre en pulso alto o activo
        while (!echoPin.isHigh()) {
            // [ESTADO DE ESPERA] Esperar a que el pin ECHO se levante
        }

        // Step 3. Inicializamos el tiempo de salida
        long startTime = System.nanoTime();

        // Step 4. Esperamos de forma inversa hasta que el pin ECHO se encuentre en estado bajo
        while (echoPin.isHigh()) {
            // [ESTADO DE ESPERA] Esperar a que el pin ECHO se baje
        }

        // Step 5. Inicializamos el tiempo de finalización entre estados
        long endTime = System.nanoTime();

        // Step 6. Se calcula la duración del pulso de eco en segundos, dividiendo la diferencia entre 1000 millones
        double pulseDuration = (endTime - startTime) / 1000000000.0;

        // Step 7. Finalmente calculamos la distancia en centímetros. Dividimos entre 2 debido a que la distancia real
        //         hasta el final es la mitad [El sonido va y luego rebota en la pared de la tolva o en el alimento
        //         volviendo al sensor y es capturado]
        double distance = (pulseDuration * SOUND_SPEED) / 2.0;

        // Step 8. Retornamos el valor de la medición. Si el valor es superior a 20 [Hardcoreado][La tolva no tiene ese
        //         tamaño y el sensor de ultrasonido no es capar de realizar mediciones mayores de 17cm en la disposición
        //         que se encuentra]
        //         Es necesario destacar que esta unidad de ultrasonido no funciona tan bien como se espera y se obtienen
        //         valores de medición imprecisos.
        return distance > 20.00 ? 0.00 : distance;
    }


    /**
     * Método getFoodLevel
     * Este método se encarga de determinar el estado del volumen de alimento de la tolva.
     * DANGER -> Tolva sin alimento o con poco alimento
     * WARNING -> Tolva por debajo del nivel optimo y por encima del nivel crítico
     * OK -> Tolva en estado optimo
     * @return String Cadena que representa el estado de la tolva
     */
    public String getFoodLevel(){
        var distance = getAverageHopperMeasures();
        var response = HOPPER_STATUS.OK.name();
        if(distance >= MIN_LEVEL){
            response = HOPPER_STATUS.DANGER.name();
            CLInterface.showAlertDanger("Nivel crítico de alimento en la tolva");
        }else if(distance < MIN_LEVEL & distance >= MED_LEVEL){
            response = HOPPER_STATUS.WARNING.name();
            CLInterface.showAlertWarning("Nivel bajo de alimento en la tolva");
        }else{
            CLInterface.showAlertInfo("Nivel optimo de alimento en la tolva");
        }

        return response;
    }

    /**
     * Método getAverageHopperMeasures
     * Este método se encarga de realizar una tanda de mediciones con el objetivo de obtener una media para determinar
     * el estado del volumen de alimento con el que cuenta la tolva. Se podría realizar una unica medición, no obstante,
     * el módulo de ultrasonido ha resultado no ser muy fiable.
     * @return double Valor que representa el volumen de la tolva.
     */
    private double getAverageHopperMeasures(){
        final int MAX_TURNS = 10;
        int turns = MAX_TURNS;
        int zeroCounts = 0;
        double sum = 0.0;
        while(turns > 0){
            var distance = Tools.roundToDecimals(getDistance(),2);
            if(distance > 0) {
                sum += distance;
                turns--;
            }else{
                zeroCounts++;
            }

            if(zeroCounts > MAX_TURNS) return MAX_LEVEL;
        }

        return sum > 0 ? sum / MAX_TURNS : 0.0;
    }

    /**
     * Método isLowFoodLevel
     * Determina si la tolva se encuentra en un nivel bajo o crítico
     * @return boolean
     */
    public boolean isLowFoodLevel(){
        return this.getAverageHopperMeasures() >= MIN_LEVEL;
    }
}
