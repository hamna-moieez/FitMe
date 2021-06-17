package com.example.fitmeandroid;

public class CalorieConsumption {
    private Double Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday;


    public CalorieConsumption() {

    }

    public CalorieConsumption(Double Sun, Double Mon, Double Tue, Double Wed, Double Thu, Double Fri, Double Sat){
        this.Sunday = Sun;
        this.Monday = Mon;
        this.Tuesday = Tue;
        this.Wednesday = Wed;
        this.Thursday = Thu;
        this.Friday = Fri;
        this.Saturday = Sat;
    }

    public Double getSunday() {
        return Sunday;
    }

    public void setSunday(Double Sun) {
        Sunday = Sun;
    }

    public Double getMonday() {
        return Monday;
    }

    public void setMonday(Double Mon) {
        Monday = Mon;
    }

    public Double getTuesday() {
        return Tuesday;
    }

    public void setTuesday(Double Tue) {
        Tuesday = Tue;
    }

    public Double getWednesday() {
        return Wednesday;
    }

    public void setWednesday(Double Wed) {
        Wednesday = Wed;
    }

    public Double getThursday() {
        return Thursday;
    }

    public void setThursday(Double Thu) {
        Thursday = Thu;
    }

    public Double getFriday() {
        return Friday;
    }

    public void setFriday(Double Fri) {
        Friday = Fri;
    }

    public Double getSaturday() {
        return Saturday;
    }

    public void setSaturday(Double Sat) {
        Saturday = Sat;
    }

}