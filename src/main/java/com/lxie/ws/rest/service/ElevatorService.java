package com.lxie.ws.rest.service;

import com.lxie.ws.rest.pojo.StatusResponse;
import com.lxie.ws.rest.pojo.Workload;

/**
 * Created by wl185056 on Mar, 2019
 */
public interface ElevatorService {

    StatusResponse reset();

    StatusResponse workload(Workload workLoad);
}
