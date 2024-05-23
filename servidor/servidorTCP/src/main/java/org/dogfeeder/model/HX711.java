package org.dogfeeder.model;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;

public class HX711 {
    private final GpioPinDigitalOutput pinCLK;
    private final GpioPinDigitalInput pinDAT;
    private int gain;

    public long emptyValue = 8388436;//0;
    public double emptyWeight = 0.0d;
    public long calibrationValue = 8229924;//0;
    public double calibrationWeight =  150d;//0.0d;

    public double fullCupWeight = 200.0d;

    public double weight = 0.0d;
    public long value = 0;

    public HX711(GpioPinDigitalInput pinDAT, GpioPinDigitalOutput pinSCK, int gain) {
        this.pinCLK = pinSCK;
        this.pinDAT = pinDAT;
        setGain(gain);
    }

    public void read() {
        pinCLK.setState(PinState.LOW);
        while (!isReady()) {
            sleep(1);
        }

        long count = 0;
        for (int i = 0; i < this.gain; i++) {
            pinCLK.setState(PinState.HIGH);
            count = count << 1;
            pinCLK.setState(PinState.LOW);
            if (pinDAT.isHigh()) {
                count++;
            }
        }

        pinCLK.setState(PinState.HIGH);
        count = count ^ 0x800000;
        pinCLK.setState(PinState.LOW);
        value = count;

        weight = (value - emptyValue)*((calibrationWeight - emptyWeight)/(calibrationValue - emptyValue));

        //weight = ((value - emptyValue) * ((calibrationWeight - emptyWeight) / (calibrationValue - emptyValue))) / 1000.0; // Convertir a gramos
    }

    public double getNetWeight(){
        return this.weight - calibrationWeight;
    }

    public void setGain(int gain) {
        switch (gain) {
            case 128:       // channel A, gain factor 128
                this.gain = 24;
                break;
            case 64:        // channel A, gain factor 64
                this.gain = 26;
                break;
            case 32:        // channel B, gain factor 32
                this.gain = 25;
                break;
        }

        pinCLK.setState(PinState.LOW);
        read();
    }

    public boolean isReady() {
        return (pinDAT.isLow());
    }

    private void sleep(long delay) {
        try {
            Thread.sleep(delay);
        } catch (Exception ex) {
        }
    }

    public void calibrate(long emptyValue, double emptyWeight, long calibrationValue, double calibrationWeight) {
        this.emptyValue = emptyValue;
        this.emptyWeight = emptyWeight;
        this.calibrationValue = calibrationValue;
        this.calibrationWeight = calibrationWeight;
    }
}
