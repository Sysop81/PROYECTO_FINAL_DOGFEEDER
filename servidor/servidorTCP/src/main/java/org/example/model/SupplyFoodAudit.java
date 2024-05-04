package org.example.model;

public class SupplyFoodAudit {
    private int ID;
    private User user;
    private double weight;
    private String timeStamp;

    public SupplyFoodAudit(){}

    public SupplyFoodAudit(int ID,User user, String timeStamp, double weight){
        this.ID = ID;
        this.user = user;
        this.weight = weight;
        this.timeStamp = timeStamp;
    }

    public SupplyFoodAudit(User user, double weight){
        this.user = user;
        this.weight = weight;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public double getWeight() {
        return weight;
    }

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
