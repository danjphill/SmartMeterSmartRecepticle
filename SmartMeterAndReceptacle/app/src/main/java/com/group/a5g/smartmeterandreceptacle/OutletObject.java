package com.group.a5g.smartmeterandreceptacle;

/**
 * Created by Daniel Phillips on 10/26/2016.
 */
//Outlet Object used for the MyOutles Activity ListView

public class OutletObject {
    private String Address;
    private String Name;
    private String Number;
    private String Power;

    OutletObject(String Address, String Name, String Number, String Power) {
        this.Address = Address;
        this.Name = Name;
        this.Number = Number;
        this.Power = Power;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setNumber(String number) {
        Number = number;
    }

    public void setPower(String power) {
        Power = power;
    }

    public String getAddress() {
        return Address;
    }

    public String getName() {
        return Name;
    }

    public String getNumber() {
        return Number;
    }

    public String getPower() {
        return Power;
    }
}