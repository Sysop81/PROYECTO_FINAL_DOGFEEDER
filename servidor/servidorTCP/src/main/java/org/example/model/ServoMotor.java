package org.example.model;

import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.SoftPwm;

import java.sql.SQLOutput;

public class ServoMotor {

    private int pin;
    private int time;



    public ServoMotor(int pin, int time){
        this.pin = pin;
        this.time = time;
        Gpio.wiringPiSetup();

    }

    public boolean supplyFood(){
        try {
            SoftPwm.softPwmCreate(this.pin,0,100);

            // Mover el servo motor a la posición 25 grados
            SoftPwm.softPwmWrite(this.pin, 10);
            Thread.sleep(this.time); // Esperar 1 segundo

            // Mover el servo motor a la posición 0 grados
            SoftPwm.softPwmWrite(this.pin, 1);
            Thread.sleep(1000); // Esperar 1 segundo

            // Detener la generación de pulsos PWM suave en el pin PWM
            //System.out.println("Deteniendo el servomotor");
            SoftPwm.softPwmStop(this.pin);

            return true;
        }catch (InterruptedException e){
            System.err.println("Ocurrió un error durante el suministro de comida.\n" + e.getMessage());
        }

        return false;
    }
}
