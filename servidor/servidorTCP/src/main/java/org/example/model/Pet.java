package org.example.model;


import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


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
    private int getVaccineMonth;

    public Pet(){}

    public Pet(String[] data){
        this.name = data[0];
        this.breed = data[1];
        this.birthday = data[2];
        this.weight = Double.parseDouble(data[3]);
        this.type = this.convertType(data[4]);
        this.vaccineNotify = Boolean.parseBoolean(data[5]);
        this.vaccineDay = Integer.parseInt(data[6]);
        this.getVaccineMonth = Integer.parseInt(data[7]);
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getBirthday() {
        return birthday;
    }

    public Date getBirthdayToDate(){
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return  Date.valueOf(LocalDate.parse(birthday, fmt));
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public TypeOfSize getType() {
        return type;
    }

    public void setType(TypeOfSize type) {
        this.type = type;
    }

    public void setType(String type){
        this.type = convertType(type);
    }

    private TypeOfSize convertType(String type){
        switch (type.toLowerCase()){
            case "medium" : return  TypeOfSize.medium;
            case "big" : return TypeOfSize.big;
        }

        return TypeOfSize.small;
    }

    public boolean isVaccineNotify() {
        return vaccineNotify;
    }

    public void setVaccineNotify(boolean vaccineNotify) {
        this.vaccineNotify = vaccineNotify;
    }

    public int getVaccineDay() {
        return vaccineDay;
    }

    public void setVaccineDay(int vaccineDay) {
        this.vaccineDay = vaccineDay;
    }

    public int getGetVaccineMonth() {
        return getVaccineMonth;
    }

    public void setGetVaccineMonth(int getVaccineMonth) {
        this.getVaccineMonth = getVaccineMonth;
    }

    @Override
    public String toString() {
        return  ID + "_" + name + "_" + breed + "_" + birthday + "_" + weight +
                "_" + type + "_" + vaccineNotify + "_" + vaccineDay + "_" + getVaccineMonth ;
    }
}
