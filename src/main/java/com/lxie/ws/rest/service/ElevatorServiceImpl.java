package com.lxie.ws.rest.service;

import com.lxie.ws.rest.domain.Elevator;
import com.lxie.ws.rest.domain.User;
import com.lxie.ws.rest.pojo.StatusResponse;
import com.lxie.ws.rest.pojo.Workload;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ElevatorServiceImpl implements ElevatorService {

    private static List<Elevator> elevators;
    private static LinkedList[] waitUpLists;  //each floor has a want-up queue
    private static LinkedList[] waitDownLists; //also a want-down queue

    public ElevatorServiceImpl() {
        init();
    }

    private static void init() {
        elevators = new ArrayList<>();
        IntStream.range(0, 3)
                .forEach(index -> elevators.add(new Elevator(index)));

        waitUpLists = new LinkedList[12];
        IntStream.range(0, 12)
                .forEach(index -> waitUpLists[index] = new LinkedList());

        waitDownLists = new LinkedList[12];
        IntStream.range(0, 12)
                .forEach(index -> waitDownLists[index] = new LinkedList());
    }

    @Override
    public StatusResponse reset() {
        IntStream.range(0, 3)
                .forEach(index -> elevators.get(index).reset());
        return new StatusResponse(elevators.get(0), elevators.get(1), elevators.get(2));
    }

    @Override
    public StatusResponse workload(Workload workload) {
        if (workload != null) {
            prepare(workload);
        }

        return new StatusResponse(elevators.get(0), elevators.get(1), elevators.get(2));
    }

    public void prepare(Workload workload) {
        //goto part
        List<int[]> gotos = workload.getGotos();
        Map<Integer, Integer> gotoMap = new HashMap<Integer, Integer>();
        gotos.stream()
             .forEach(aGoto -> gotoMap.put(aGoto[0], aGoto[1]));

        for(int i=0; i<elevators.size(); i++) {
            Set<User> users = elevators.get(i).getCurrentUsers();
            users.stream()
                    .filter(user -> users.size() > 0 && gotoMap.containsKey(user.getUserId()))
                    .collect(Collectors.toList())
                    .forEach(user -> user.setDestFloor(gotoMap.get(user.getUserId())));
        }

        //request part
        List<int[]> requests = workload.getRequests();
        List<User> comers = new ArrayList<User>();
        requests.stream()
                .forEach(req -> comers.add(new User(req[0], req[1], req[2], -1)));
        comers.stream()
                .filter(comer -> comer.getDirection() == 1)
                .collect(Collectors.toList())
                .forEach(comer -> waitUpLists[comer.getStartFloor()].add(comer));
        comers.stream()
                .filter(comer -> comer.getDirection() == 0)
                .collect(Collectors.toList())
                .forEach(comer -> waitDownLists[comer.getStartFloor()].add(comer));

        for(Elevator e : elevators) {
            unload(e);
            pickup(e);
            if(!hasRequestBelow(e.getCurrentFloor()) && !hasRequestAbove(e.getCurrentFloor()) && e.getCurrentUserIds().size() == 0) {
                e.setDirection(-1);
            }
            if(e.getDirection() != -1) e.move();
        }
        sstf(elevators);
    }

    // search an elevator near heavy requests every tick, consider both user waiting time and power cost, but time first
    public void sstf(List<Elevator> elevators) {
        Elevator closestElevator = elevators.get(0);
        int cost = 0, floor = closestElevator.getCurrentFloor();
        for(int i=1; i<=11; i++) {
            if(waitUpLists[i].size() + waitDownLists[i].size() == 0) continue; //skip empty floor
            cost += i<floor ? (floor-i)*(100+6) : (i-floor)*(100+4);  // time has much more weight, i use 100 here
        }
        for(int i=1; i<elevators.size(); i++) {
            int tmpCost = 0, tmpFloor = elevators.get(i).getCurrentFloor();
            for(int j=1; j<=11; j++) {
                if(waitUpLists[j].size() + waitDownLists[j].size() == 0) continue;
                tmpCost += j<tmpFloor ? (tmpFloor-j)*(100+6) : (j-tmpFloor)*(100+4);
            }
            if(tmpCost < cost) {
                closestElevator = elevators.get(i);
            }
        }
        if(closestElevator.getDirection() != -1) return;
        else {
            if(closestElevator.getCurrentUserIds().size() == 0) {
                if(hasRequestBelow(closestElevator.getCurrentFloor())) closestElevator.setDirection(0);
                else closestElevator.setDirection(1);
            }
            else {
                closestElevator.setDirection(((User)closestElevator.getCurrentUsers().iterator().next()).getDirection());
            }
        }
    }

    public boolean hasRequestAbove(int floor) {
        for(int i=floor+1; i<=11; i++) {
            if(waitUpLists[i].size() + waitDownLists[i].size() > 0) return true;
        }
        return false;
    }

    public boolean hasRequestBelow(int floor) {
        for(int i=floor-1; i>0; i--) {
            if(waitUpLists[i].size() + waitDownLists[i].size() > 0) return true;
        }
        return false;
    }

    public void unload(Elevator e) {
        if(e.getCurrentUserIds().size()== 0) return; // empty elevator, no person to unload

        Iterator it = e.getCurrentUsers().iterator();
        while(it.hasNext()) {
            User u = (User)it.next();
            if(u.getDestFloor() == e.getCurrentFloor()) {
                it.remove();
                e.getCurrentUserIds().remove(u.getUserId());
            }
        }
    }

    public void pickup(Elevator e) {
        if(waitUpLists[e.getCurrentFloor()].size() + waitDownLists[e.getCurrentFloor()].size() == 0)
            return; // empty floor, no person to pickup

        if(e.getDirection() == 1 || e.getCurrentFloor()== 1) {
            while(e.getCurrentUsers().size() < 20) {
                if(waitUpLists[e.getCurrentFloor()].size() > 0) {
                    User out = (User)waitUpLists[e.getCurrentFloor()].remove();
                    e.getCurrentUsers().add(out);
                    e.getCurrentUserIds().add(out.getUserId());
                }
                else
                    break;
            }
        }
        else if(e.getDirection() == 0 || e.getCurrentFloor() == 11) {
            while(e.getCurrentUsers().size() < 20) {
                if(waitDownLists[e.getCurrentFloor()].size() > 0) {
                    User out = (User)waitDownLists[e.getCurrentFloor()].remove();
                    e.getCurrentUsers().add(out);
                    e.getCurrentUserIds().add(out.getUserId());
                }
                else
                    break;
            }
        }
        else {

        }
    }

}
