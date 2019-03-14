package com.lxie.ws.rest.domain;

public class User {

    private int userId;

    private int startFloor;

    private int direction = -1;

    private int destFloor = -1;

    public User(int userId, int startFloor, int direction, int destFloor) {
        this.userId = userId;
        this.startFloor = startFloor;
        this.direction = direction;
        this.destFloor = destFloor;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getStartFloor() {
        return startFloor;
    }

    public void setStartFloor(int startFloor) {
        this.startFloor = startFloor;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getDestFloor() {
        return destFloor;
    }

    public void setDestFloor(int destFloor) {
        this.destFloor = destFloor;
    }
}
