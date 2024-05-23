package org.dogfeeder.model;

import com.pi4j.io.gpio.*;
import org.apache.commons.compress.archivers.sevenz.CLI;
import org.dogfeeder.cli.CLInterface;

public class UltraSonic {
    //private static final int TRIGGER_PIN = RaspiPin.GPIO_28.getAddress(); // GPIO 28
    //private static final int ECHO_PIN = RaspiPin.GPIO_29.getAddress();    // GPIO 29
    private static final double MIN_LEVEL = 13;
    private static final double MED_LEVEL = 7;
    private static final double MAX_LEVEL = 3;
    private static final double SOUND_SPEED = 34300; // Velocidad del sonido en cm/s


    private GpioController gpio;
    private GpioPinDigitalOutput triggerPin;
    private GpioPinDigitalInput echoPin;

    public UltraSonic(GpioPinDigitalOutput trigger,GpioPinDigitalInput echo){
        this.triggerPin = trigger;
        this.echoPin = echo;
    }

    public double getDistance(){
        // Generar un pulso corto en el pin TRIGGER
        triggerPin.high();
        try {
            Thread.sleep(0, 10000); // 10 microsegundos
        } catch (InterruptedException e) {
            System.out.println("Error en la espera del hilo");
        }
        triggerPin.low();

        // Esperar hasta que el pin ECHO se vuelva alto
        while (!echoPin.isHigh()) {
            // Esperar a que el pin ECHO se levante
        }
        long startTime = System.nanoTime();

        // Esperar hasta que el pin ECHO vuelva a bajo
        while (echoPin.isHigh()) {
            // Esperar a que el pin ECHO se baje
        }
        long endTime = System.nanoTime();

        // Calcular la duración del pulso de eco en segundos
        double pulseDuration = (endTime - startTime) / 1000000000.0;

        // Calcular la distancia en centímetros
        double distance = (pulseDuration * SOUND_SPEED) / 2.0;

        return distance > 20.00 ? 0.00 : distance;
    }

    public void showFoodLevel(){
        var distance = this.getDistance();
        System.out.println("Nivel : " + distance);
        if(distance >= MIN_LEVEL){
            CLInterface.showAlertDanger("Nivel crítico de alimento en la tolva");
        }else if(distance < MIN_LEVEL & distance <= MED_LEVEL){
            CLInterface.showAlertWarning("Nivel bajo de alimento en la tolva");
        }else{
            CLInterface.showAlertInfo("Nivel optimo de alimento en la tolva");
        }
    }

    public boolean isLowFoodLevel(){
        return this.getDistance() >= MIN_LEVEL;
    }
}
