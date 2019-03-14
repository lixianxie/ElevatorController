package com.lxie.ws.rest.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class Workload {

    @JsonProperty("request")
    private List<int[]> requests;

    @JsonProperty("goto")
    private List<int[]> gotos;

    public List<int[]> getRequests() {
        return Optional.ofNullable(requests)
                .orElse(new ArrayList<>());
    }

    public void setRequests(List<int[]> requests) {
        this.requests = requests;
    }

    public List<int[]> getGotos() {
        return Optional.ofNullable(gotos)
                .orElse(new ArrayList<>());
    }

    public void setGotos(List<int[]> gotos) {
        this.gotos = gotos;
    }

}
