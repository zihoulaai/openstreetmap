package com.map;


public class Position {
    private String name;
    private String endpointAddress;
    private double endpointLongitude;
    private double endpointLatitude;
    private String createTime;

    public Position(String name, String endpointAddress, double endpointLongitude, double endpointLatitude) {
        this.name = name;
        this.endpointAddress = endpointAddress;
        this.endpointLongitude = endpointLongitude;
        this.endpointLatitude = endpointLatitude;
    }

    public String getEndpointAddress() {
        return endpointAddress;
    }

    public void setEndpointAddress(String endpointAddress) {
        this.endpointAddress = endpointAddress;
    }

    public double getEndpointLongitude() {
        return endpointLongitude;
    }

    public void setEndpointLongitude(double endpointLongitude) {
        this.endpointLongitude = endpointLongitude;
    }

    public double getEndpointLatitude() {
        return endpointLatitude;
    }

    public void setEndpointLatitude(double endpointLatitude) {
        this.endpointLatitude = endpointLatitude;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
