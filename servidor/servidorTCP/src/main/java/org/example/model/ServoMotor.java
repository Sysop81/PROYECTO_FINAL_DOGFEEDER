package org.example.model;

import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.SoftPwm;

public class ServoMotor {

    private int pin;
    private int time;
    private final int DEFAULT_TIME = 1000;
    private final int INITIAL_POS = 0;
    private final int FINAL_POS = 100;
    private static boolean isInUse = false;



    public ServoMotor(int pin, int time){
        this.pin = pin;
        this.time = time;
        Gpio.wiringPiSetup();
    }

    public ServoMotor(int pin){
        this.pin = pin;
        this.time =DEFAULT_TIME;
        Gpio.wiringPiSetup();
    }

    public void resetServoPosition(){
        try{
            SoftPwm.softPwmCreate(this.pin,0,100);
            SoftPwm.softPwmWrite(this.pin, 1);
            Thread.sleep(1000);
            SoftPwm.softPwmStop(this.pin);
        }catch (InterruptedException e){
            System.err.println("Ocurrió un error durante el reseteo de posición.\n" + e.getMessage());
        }
    }

    public boolean supplyFood(){
        if(isInUse) return false;
        boolean isOk = false;
        try {
            isInUse = true;
            SoftPwm.softPwmCreate(this.pin,INITIAL_POS,FINAL_POS);

            // Mover el servo motor a la posición 10 "OPEN"
            SoftPwm.softPwmWrite(this.pin, 10);
            Thread.sleep(this.time); // Esperar el tiempo marcado

            // Mover el servo motor a la posición 1 "CLOSED"
            SoftPwm.softPwmWrite(this.pin, 1);
            Thread.sleep(1000); // Esperar 1 segundo

            // Detener la generación de pulsos en el pin PWM
            SoftPwm.softPwmStop(this.pin);

            isOk = true;
        }catch (InterruptedException e){
            System.err.println("Ocurrió un error durante el suministro de comida.\n" + e.getMessage());
        }finally {
            isInUse = false;
        }

        return isOk;
    }


    public boolean supplyFlashesFood(){
        if(isInUse) return false;
        boolean isOk = false;
        try {
            isInUse = true;
            SoftPwm.softPwmCreate(this.pin,INITIAL_POS,FINAL_POS);

            SoftPwm.softPwmWrite(this.pin, 9);
            Thread.sleep(600);

            int flashes = 4;
            while(flashes > 1){

                SoftPwm.softPwmWrite(this.pin, 5);
                Thread.sleep(100);
                SoftPwm.softPwmWrite(this.pin, 1);
                Thread.sleep(100);
                flashes--;
            }

            // Detener la generación de pulsos en el pin PWM
            SoftPwm.softPwmStop(this.pin);

            isOk = true;
        }catch (InterruptedException e){
            System.err.println("Ocurrió un error durante el suministro de tipo flash de comida.\n" + e.getMessage());
        }finally {
            isInUse = false;
        }

        return isOk;
    }

    public boolean isBussy(){
        return isInUse;
    }


    public void open(){
        SoftPwm.softPwmCreate(this.pin,INITIAL_POS,FINAL_POS);
        for(int i = 1; i < 16; i++){
            SoftPwm.softPwmWrite(this.pin, i);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        SoftPwm.softPwmStop(this.pin);
    }

    public void close(){
        SoftPwm.softPwmCreate(this.pin,INITIAL_POS,FINAL_POS);
        for(int i = 16; i >= 1; i-=2){
            SoftPwm.softPwmWrite(this.pin, i);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        SoftPwm.softPwmStop(this.pin);
    }
}
