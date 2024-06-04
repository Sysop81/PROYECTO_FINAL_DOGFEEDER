package org.dogfeeder.model;

/**
 * Clase Settings
 * Clase encargada ded modelar las preferencias de configuración para DOG-FEEDER
 */
public class Settings {
    private int ID;
    private int foodRation;
    private boolean isLedOn;
    private boolean isNotifyHopperLow;
    private boolean isNotifyFeederWithOutFood;

    /**
     * Constructor
     */
    public Settings(){}

    /**
     * Constructor con parametros
     * @param ID Identificador de las preferencias
     * @param foodRation Cantidad maxima de suministro de alimento
     * @param isLedOn Estado de los leds de iluminación
     * @param isNotifyHopperLow Estado notificaciones tolva cuando se encuentra en nivel clítico
     * @param isNotifyFeederWithOutFood Estado notificaciones cuando el cuenco se encuentra sin alimento
     */
    public Settings(int ID, int foodRation, boolean isLedOn, boolean isNotifyHopperLow, boolean isNotifyFeederWithOutFood) {
        this.ID = ID;
        this.foodRation = foodRation;
        this.isLedOn = isLedOn;
        this.isNotifyHopperLow = isNotifyHopperLow;
        this.isNotifyFeederWithOutFood = isNotifyFeederWithOutFood;
    }

    /**
     * Getter para la propiedad ID
     * @return int Identificacdor de la conficuración
     */
    public int getID() {
        return ID;
    }

    /**
     * Setter para la propiedad ID
     * @param ID Identificador de la conficuración
     */
    public void setID(int ID) {
        this.ID = ID;
    }

    /**
     * Getter para la propiedad foodRation
     * @return int Cantidad maxima de alimento
     */
    public int getFoodRation() {
        return foodRation;
    }

    /**
     * Setter para la propiedad foodRation
     * @param foodRation Cantidad maxima de alimento
     */
    public void setFoodRation(int foodRation) {
        this.foodRation = foodRation;
    }

    /**
     * Getter para la propiedad ledOn
     * @return boolean con el estado de encendido
     */
    public boolean isLedOn() {
        return isLedOn;
    }

    /**
     * Setter para la propiedad ledOn
     * @param ledOn Estado del encedindo de led
     */
    public void setLedOn(boolean ledOn) {
        isLedOn = ledOn;
    }

    /**
     * Getter para la propiedad notifyHopperLow
     * @return boolean con el estado del aviso de notificación
     */
    public boolean isNotifyHopperLow() {
        return isNotifyHopperLow;
    }

    /**
     * Setter para la propiedad notifyHopperLow
     * @param notifyHopperLow boolean con el estado de aviso para la notificación
     */
    public void setNotifyHopperLow(boolean notifyHopperLow) {
        isNotifyHopperLow = notifyHopperLow;
    }

    /**
     * Getter para la propiedad notifyFeederWithOutFood
     * @return boolean con el estado del aviso de notificación
     */
    public boolean isNotifyFeederWithOutFood() {
        return isNotifyFeederWithOutFood;
    }

    /**
     * Setter para la propiedad notifyFeederWithOutFood
     * @param notifyFeederWithOutFood boolean con el estado de aviso para la notificación
     */
    public void setNotifyFeederWithOutFood(boolean notifyFeederWithOutFood) {
        isNotifyFeederWithOutFood = notifyFeederWithOutFood;
    }

    @Override
    public String toString() {
        return "Settings{" +
                "ID=" + ID +
                ", foodRation=" + foodRation +
                ", isLedOn=" + isLedOn +
                ", isNotifyHopperLow=" + isNotifyHopperLow +
                ", isNotifyFeederWithOutFood=" + isNotifyFeederWithOutFood +
                '}';
    }
}
