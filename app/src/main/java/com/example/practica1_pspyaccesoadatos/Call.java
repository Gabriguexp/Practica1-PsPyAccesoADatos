package com.example.practica1_pspyaccesoadatos;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Call{
    String ContactName;
    int number;
    LocalDateTime dateTime;

    public Call() {
    }

    public Call(String contactName, int number, LocalDateTime dateTime) {
        ContactName = contactName;
        this.number = number;
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return "Call{" +
                "ContactName='" + ContactName + '\'' +
                ", number=" + number +
                ", dateTime=" + dateTime +
                '}';
    }

    public String getContactName() {
        return ContactName;
    }

    public void setContactName(String contactName) {
        ContactName = contactName;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String callToCsv1(){
        String s ="";
        int year = this.getDateTime().getYear();
        int month = this.getDateTime().getMonthValue();
        int day = this.getDateTime().getDayOfMonth();
        int hour = this.getDateTime().getHour();
        int min = this.getDateTime().getMinute();
        int sec = this.getDateTime().getSecond();
        this.getNumber();
        this.getContactName();
        s+= year + "; " + month + "; " + day + "; " + hour + "; " + min + "; " + sec + "; " +
                this.getNumber() + "; " +this.getContactName();
        /*año, mes, día, hora, minutos, segundos, número entrante, nombre del contacto */
        return s;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public String callToCsv2(){

        String s ="";
        int year = this.getDateTime().getYear();
        int month = this.getDateTime().getMonthValue();
        int day = this.getDateTime().getDayOfMonth();
        int hour = this.getDateTime().getHour();
        int min = this.getDateTime().getMinute();
        int sec = this.getDateTime().getSecond();
        this.getNumber();
        this.getContactName();

        /* nombre del contacto; año, mes, día, hora, minutos y segundos*/
        s+= this.getContactName() + "; " + year + "; " + month + "; " + day + "; " + hour + "; " + min + "; " +
                sec + "; " +this.getNumber();
        return s;
    }



}
