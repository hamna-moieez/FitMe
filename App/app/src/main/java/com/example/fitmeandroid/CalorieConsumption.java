package com.example.fitmeandroid;

public class CalorieConsumption {
    public Double Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday;


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

    public void setSunday(Double sunday) {
        Sunday = sunday;
    }

    public Double getMonday() {
        return Monday;
    }

    public void setMonday(Double monday) {
        Monday = monday;
    }

    public Double getTuesday() {
        return Tuesday;
    }

    public void setTuesday(Double tuesday) {
        Tuesday = tuesday;
    }

    public Double getWednesday() {
        return Wednesday;
    }

    public void setWednesday(Double wednesday) {
        Wednesday = wednesday;
    }

    public Double getThursday() {
        return Thursday;
    }

    public void setThursday(Double thursday) {
        Thursday = thursday;
    }

    public Double getFriday() {
        return Friday;
    }

    public void setFriday(Double friday) {
        Friday = friday;
    }

    public Double getSaturday() {
        return Saturday;
    }

    public void setSaturday(Double saturday) {
        Saturday = saturday;
    }

}