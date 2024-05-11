package org.dogfeeder.model;

public class StatisticFood {
    private int year;
    private int month_number;
    private String month_name;
    private int count_takes;
    private double total_weight;

    public StatisticFood(){}

    public StatisticFood(int year, int month_number, String month_name, int count_takes, double total_weight) {
        this.year = year;
        this.month_number = month_number;
        this.month_name = month_name;
        this.count_takes = count_takes;
        this.total_weight = total_weight;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth_number() {
        return month_number;
    }

    public void setMonth_number(int month_number) {
        this.month_number = month_number;
    }

    public String getMonth_name() {
        return month_name;
    }

    public void setMonth_name(String month_name) {
        this.month_name = month_name;
    }

    public int getCount_takes() {
        return count_takes;
    }

    public void setCount_takes(int count_takes) {
        this.count_takes = count_takes;
    }

    public double getTotal_weight() {
        return total_weight;
    }

    public void setTotal_weight(double total_weight) {
        this.total_weight = total_weight;
    }

    @Override
    public String toString() {
        return "StatisticFood{" +
                "year=" + year +
                ", month_number=" + month_number +
                ", month_name=" + month_name +
                ", count_takes=" + count_takes +
                ", total_weight=" + total_weight +
                '}';
    }
}
