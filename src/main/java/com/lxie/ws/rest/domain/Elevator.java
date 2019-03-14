package com.lxie.ws.rest.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.Set;


public class Elevator {

    @JsonIgnore
    private int id;

    @JsonIgnore
    private int direction = -1;      // 1 going up, 0 going down, -1 stop

    @JsonProperty("floor")
    private int currentFloor = 1;

    @JsonProperty("users")
    private Set<Integer> currentUserIds = new HashSet<Integer>();

    @JsonIgnore
    private Set<User> currentUsers = new HashSet<User>();     // used for result display

    public Elevator(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public void setCurrentFloor(int currentFloor) {
        this.currentFloor = currentFloor;
    }

    public Set<Integer> getCurrentUserIds() {
        return currentUserIds;
    }

    public void setCurrentUserIds(Set<Integer> currentUserIds) {
        this.currentUserIds = currentUserIds;
    }

    public Set<User> getCurrentUsers() {
        return currentUsers;
    }

    public void setCurrentUsers(Set<User> currentUsers) {
        this.currentUsers = currentUsers;
    }

    public void reset() {
        currentFloor = 1;
        direction = -1;
    }

    public void move() {
        if(direction == 1 && currentFloor < 11) currentFloor++;
        if(direction == 0 && currentFloor > 1) currentFloor--;
    }

}
