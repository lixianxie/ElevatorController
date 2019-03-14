package com.lxie.ws.rest.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lxie.ws.rest.domain.Elevator;


public class StatusResponse {

    @JsonProperty("1")
    private Elevator elevator1;

    @JsonProperty("2")
    private Elevator elevator2;

    @JsonProperty("3")
    private Elevator elevator3;

    public StatusResponse(Elevator elevator1, Elevator elevator2, Elevator elevator3) {
        this.elevator1 = elevator1;
        this.elevator2 = elevator2;
        this.elevator3 = elevator3;
    }

    public Elevator getElevator1() {
        return elevator1;
    }

    public void setElevator1(Elevator elevator1) {
        this.elevator1 = elevator1;
    }

    public Elevator getElevator2() {
        return elevator2;
    }

    public void setElevator2(Elevator elevator2) {
        this.elevator2 = elevator2;
    }

    public Elevator getElevator3() {
        return elevator3;
    }

    public void setElevator3(Elevator elevator3) {
        this.elevator3 = elevator3;
    }
}
