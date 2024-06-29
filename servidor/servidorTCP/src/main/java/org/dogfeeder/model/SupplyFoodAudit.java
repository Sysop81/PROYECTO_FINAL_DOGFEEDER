package org.dogfeeder.model;

/**
 * Clase SupplyFoodAudit
 * Clase encargada de modelar los registros de auditoría para las tomas de alimento realizadas
 */
public class SupplyFoodAudit {
    private int ID;
    private User user;
    private double weight;
    private String timeStamp;
    private String time;
    private String date;

    /**
     * Constructor
     */
    public SupplyFoodAudit(){}

    /**
     * Constructor con parámetros
     * @param ID Identificador del registro de auditoría
     * @param user Usuario que realiza el suministro de alimento
     * @param timeStamp Fecha y hora en la que se registró la operación
     * @param weight Peso en gramos del alimento suministrado
     */
    public SupplyFoodAudit(int ID,User user, String timeStamp, double weight){
        this.ID = ID;
        this.user = user;
        this.weight = weight;
        this.timeStamp = timeStamp;

        var aTimeStamp = timeStamp.split(" ");
        this.date = aTimeStamp[0];
        this.time = aTimeStamp[1];
    }

    /**
     * Constructor con parametros [Sobrecarga]
     * @param user Usuario que realizó el suministro de alimento
     * @param weight Peso en gramos del alimento suministrado.
     */
    public SupplyFoodAudit(User user, double weight){
        this.user = user;
        this.weight = weight;
    }

    /**
     * Setter para la propiedad ID
     * @return int Identificado del registro de auditoría
     */
    public int getID() {
        return ID;
    }

    /**
     * Setter para la propiedad ID
     * @param ID Identificador del registro de auditoría
     */
    public void setID(int ID) {
        this.ID = ID;
    }

    /**
     * Getter para la propiedad User
     * @return Instancia de usuario
     */
    public User getUser() {
        return user;
    }

    /**
     * Setter para la propiedad user
     * @param user Usuario que realiza el suministro de alimento
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Getter para la propiedad timeStamp
     * @return String Cadena que reprenta fecha y hora de la operación
     */
    public String getTimeStamp() {
        return timeStamp;
    }

    /**
     * Setter para la propiedad timeStamp
     * @param timeStamp Cadena que reprenta fecha y hora de la operación
     */
    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * Getter para la propiedad date
     * @return String con la fecha de la operación de auditoría
     */
    public String getDate(){
        return this.date;
    }

    /**
     * Getter para la propiedad time
     * @return String con la hora de la operación de auditoría
     */
    public String getTime(){
        return this.time;
    }

    /**
     * Getter para la propiedad weight
     * @return double Valor en gramos del alimento suministrado
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Setter para la propiedad weight
     * @param weight Valor en gramos del alimento suministrado
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "SupplyFoodAudit{" +
                "ID=" + ID +
                ", user=" + user +
                ", weight=" + weight +
                ", timeStamp='" + timeStamp + '\'' +
                '}';
    }
}
