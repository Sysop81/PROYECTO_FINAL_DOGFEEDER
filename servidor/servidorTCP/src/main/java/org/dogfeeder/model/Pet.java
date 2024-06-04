package org.dogfeeder.model;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Clase para modelar la mascota asociada a DOG-FEEDER
 */
public class Pet {
    public enum TypeOfSize{small,medium,big}
    private int ID;
    private String name;
    private String breed;
    private String birthday;
    private double weight;
    private TypeOfSize type;
    private boolean vaccineNotify;
    private int vaccineDay;
    private int vaccineMonth;

    /**
     * Constructor
     */
    public Pet(){}

    /**
     * Getter para la propiedad ID
     * @return int Identificador de la mascota
     */
    public int getID() {
        return ID;
    }

    /**
     * Setter para la propiedad ID
     * @param ID Identificador para la mascota
     */
    public void setID(int ID) {
        this.ID = ID;
    }

    /**
     * Getter para la propiedad name
     * @return String Cadena que representa el nombre de la mascota
     */
    public String getName() {
        return name;
    }

    /**
     * Setter para la propiedad name
     * @param name Cadena que representa el nombre de la mascota
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter para la propiedad breed
     * @return String Cadena que representa la raza de la mascota
     */
    public String getBreed() {
        return breed;
    }

    /**
     * Setter para la propiedad breed
     * @param breed Cadena que representa la raza de la mascota
     */
    public void setBreed(String breed) {
        this.breed = breed;
    }

    /**
     * Getter para la propiedad birthday
     * @return String cadena con la fecha de nacimiento de la mascota
     */
    public String getBirthday() {
        return birthday;
    }

    /**
     * Getter para la propiedad birthday [Date]
     * @return Date fecha de nacimiento de la mascota
     */
    public Date getBirthdayToDate(){
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return  Date.valueOf(LocalDate.parse(birthday, fmt));
    }

    /**
     * Setter para la propiedad birthday
     * @param birthday Cadena que representa la fecha de nacimiento de la mascota
     */
    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    /**
     * Getter para la propiedad weight
     * @return double Valor que determina el peso de la mascota
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Setter para la propiedad weight
     * @param weight Valor que determina el peso de la mascota
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    /**
     * Getter para la propiedad type
     * @return type Tipo de tamaño segun la raza
     */
    public TypeOfSize getType() {
        return type;
    }

    /**
     * Setter para la propiedad type
     * @param type Tipo de tamaño segun la raza
     */
    public void setType(TypeOfSize type) {
        this.type = type;
    }

    /**
     * Setter para la propiedad type
     * @param type Tipo de tamaño segun la raza
     */
    public void setType(String type){
        this.type = convertType(type);
    }

    /**
     * Método convertType
     * @param type Tipo en cadena de caracteres
     * @return TypeOfSize Valor equivalente convertido a la enumeracion
     */
    private TypeOfSize convertType(String type){
        switch (type.toLowerCase()){
            case "medium" : return  TypeOfSize.medium;
            case "big" : return TypeOfSize.big;
        }

        return TypeOfSize.small;
    }

    /**
     * Getter para la propiedad vaccineNotify
     * @return boolean con el resultado de la propiedad
     */
    public boolean isVaccineNotify() {
        return vaccineNotify;
    }

    /**
     * Setter para la propiedad vaccineNotify
     * @param vaccineNotify Boleano que representa la activación / desactivación de las notificaciones
     */
    public void setVaccineNotify(boolean vaccineNotify) {
        this.vaccineNotify = vaccineNotify;
    }

    /**
     * Getter para la propiedad vaccineDay
     * @return int Valor que representa el día de vacunación
     */
    public int getVaccineDay() {
        return vaccineDay;
    }

    /**
     * Setter para la propiedad vaccineDay
     * @param vaccineDay Valor entero que representa el día de vacunación
     */
    public void setVaccineDay(int vaccineDay) {
        this.vaccineDay = vaccineDay;
    }

    /**
     * Getter para el mes de vacunación
     * @return int Valor que represnta el mes de vacunación
     */
    public int getGetVaccineMonth() {
        return vaccineMonth;
    }

    /**
     * Setter para la propiedad vaccineMonth
     * @param vaccineMonth Valor que represnta el mes de vacunación
     */
    public void setGetVaccineMonth(int vaccineMonth) {
        this.vaccineMonth = vaccineMonth;
    }

    @Override
    public String toString() {
        return "Pet{" +
                "ID=" + ID +
                ", name='" + name + '\'' +
                ", breed='" + breed + '\'' +
                ", birthday='" + birthday + '\'' +
                ", weight=" + weight +
                ", type=" + type +
                ", vaccineNotify=" + vaccineNotify +
                ", vaccineDay=" + vaccineDay +
                ", vaccineMonth=" + vaccineMonth +
                '}';
    }
}
