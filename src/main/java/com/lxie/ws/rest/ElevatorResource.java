package com.lxie.ws.rest;

import com.lxie.ws.rest.pojo.StatusResponse;
import com.lxie.ws.rest.pojo.Workload;
import com.lxie.ws.rest.service.ElevatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class ElevatorResource {

    @Autowired
    private ElevatorService elevatorService;

    @RequestMapping(path = "/reset", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    public StatusResponse reset() {
        return elevatorService.reset();
    }

    @RequestMapping(path = "/workload", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    public StatusResponse workload(@RequestBody Workload workload ) {
        return elevatorService.workload(workload);
    }

}
