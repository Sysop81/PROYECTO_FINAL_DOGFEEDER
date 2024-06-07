package org.dogfeeder.model;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Clase Notification
 * Maneja instancias de tipo notificación empleadas por el servicio de notificaciones de DOG-FEEDER
 */
public class Notification {
    public enum NotificationType { VACCINE, // Tipo vacunación anual       [Se notifica anualmente]
                                   HOPPER, // Tolva nivel alimento bajo.   [Se notifica diariamente]
                                   FEEDER // Cuenco vacío o poco alimento. [Se notifica diariamente]
    }
    private int ID;
    private  NotificationType type;
    private String timeStamp;
    private int day;
    private int month;
    private int year;

    /**
     * Constructor sin parametros
     */
    public Notification(){}


    /**
     * Constructor con parametros
     * @param ID Identificador de la notificación
     * @param type tipo de notificacion
     * @param timeStamp fecha y hora de la notificacion
     */
    public Notification(int ID, String type, String timeStamp) {
        this.ID = ID;
        this.type = getType(type);
        this.timeStamp = timeStamp;

        setDayAndYear();
    }


    /**
     * Constructor con nparametros
     * @param type tipo de notificación
     */
    public Notification(NotificationType type){
        this.type = type;
    }


    /**
     * Getter para la propiedad ID
     * @return int Valor del identificador
     */
    public int getID() {
        return ID;
    }


    /**
     * Setter para la propiedad ID
     * @param ID Identificador de la notificación
     */
    public void setID(int ID) {
        this.ID = ID;
    }


    /**
     * Getter para la propiedad type
     * @return NotificationType Tipo de notificación
     */
    public NotificationType getType() {
        return type;
    }


    /**
     * Setter para la propiedad type
     * @param type Tipo de notificación
     */
    public void setType(NotificationType type) {
        this.type = type;
    }


    /**
     * Getter para la propiedad type
     * Realiza la conversión de la cadena a NotificationType
     * @param type Cadena de caracteres que representa el tipo de notificacion
     * @return NotificationType Tipo de notificación
     */
    public NotificationType getType(String type){
        if (type.equals(NotificationType.VACCINE.toString())) return  NotificationType.VACCINE;
        else if (type.equals(NotificationType.HOPPER.toString())) return  NotificationType.HOPPER;
        else return NotificationType.FEEDER;
    }


    /**
     * Getter para la propiedad timeStamp
     * @return String Cadena con la fecha y hora
     */
    public String getTimeStamp() {
        return timeStamp;
    }


    /**
     * Setter para la propiedad timeStamp
     * @param timeStamp Cadena con la fecha y hora
     */
    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }


    /**
     * Setter para la propiedad timeStamp
     * Establece la fecha y hora actual en la propiedad timesstamp
     */
    public void setTimeStamp(){
        this.timeStamp = String.valueOf(Timestamp.valueOf(LocalDateTime.now()));
    }


    /**
     * Método isNotifyToday
     * Determina si la instancia que lo solicita ha sido notificada hoy, evaluando los valores de las propiedades day,
     * month y year (Obtenidos de la DB) contra la fecha actual.
     * @return boolean Resultado de la evaluación
     */
    public boolean isNotifyToday(){
        var current = LocalDate.now();
        return day == current.getDayOfMonth()   &
               month == current.getMonthValue() &
               year  == current.getYear();
    }


    /**
     * Método isNotifyThisYear
     * Determina si para la instancia de notificación, se ha realizado notificación este año comparando la propiedad year
     * obtenida de la DB con el año en curso.
     * @return boolean Resultado de la operación
     */
    public boolean isNotifyThisYear(){
        return year == LocalDate.now().getYear();
    }


    /**
     * Método setDayAndYear
     * Método encargado de setear las propiedades day, month y year de la instancia con los valores obtenidos del
     * timestamp procedente de la DB.
     */
    private void setDayAndYear(){
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDate notifyDate = LocalDate.parse(this.timeStamp, fmt);
        day = notifyDate.getDayOfMonth();
        month = notifyDate.getMonthValue();
        year = notifyDate.getYear();
    }


    @Override
    public String toString() {
        return "Notification{" +
                "ID=" + ID +
                ", type=" + type +
                ", timeStamp='" + timeStamp + '\'' +
                '}';
    }
}
