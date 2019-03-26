package com.lxie.ws.rest.service;

import com.lxie.ws.rest.domain.Elevator;
import com.lxie.ws.rest.domain.User;
import com.lxie.ws.rest.exception.JsonException;
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
        validateJsonParams(workload);
        prepare(workload);
        work();
        return new StatusResponse(elevators.get(0), elevators.get(1), elevators.get(2));
    }

    private void validateJsonParams(Workload workload) {
        List<Integer> allUsers = new ArrayList<>();
        elevators.stream()
                .map(Elevator::getCurrentUserIds)
                .forEach(allUsers::addAll);

        if(workload.getGotos().size() > 0 &&
                workload.getGotos()
                .stream()
                .filter(aGoto -> allUsers.contains(aGoto[0]))
                .collect(Collectors.toList())
                .size() == 0)
        {
            throw new JsonException("user not in elevator yet!");
        }

        if(workload.getGotos()
                .stream()
                .filter(aGoto -> aGoto[1] <1 || aGoto[1] > 11)
                .collect(Collectors.toList())
                .size() > 0)
        {
            throw new JsonException("invalid destination floor!");
        }

        if(workload.getRequests()
                .stream()
                .filter(req -> req[2] != 0 && req[2] != 1)
                .filter(req -> req[1] < 1 || req[1] > 11)
                .collect(Collectors.toList())
                .size() > 0)
        {
            throw new JsonException("invalid direction or start floor!");
        }

        if(workload.getRequests()
                .stream()
                .filter(req -> allUsers.contains(req[0]))
                .collect(Collectors.toList())
                .size() > 0)
        {
            throw new JsonException("user already in elevator!");
        }
    }

    private void prepare(Workload workload) {
        //goto part
        List<int[]> gotos = workload.getGotos();
        Map<Integer, Integer> gotoMap = new HashMap<>();
        gotos.forEach(aGoto -> gotoMap.put(aGoto[0], aGoto[1]));

        for(Elevator e : elevators) {
            Set<User> users = e.getCurrentUsers();
            users.stream()
                    .filter(user -> users.size() > 0 && gotoMap.containsKey(user.getUserId()))
                    .collect(Collectors.toList())
                    .forEach(user -> user.setDestFloor(gotoMap.get(user.getUserId())));

            if(users.size() > 0) {  //set goal floor for elevators
                if(e.getDirection() == 1) {
                    int goal = 2;
                    for(User u : users) {
                        goal = Math.max(goal, u.getDestFloor());
                    }
                    e.setGoalFloor(goal);
                }
                else if(e.getDirection() == 0) {
                    int goal = 10;
                    for(User u : users) {
                        goal = Math.min(goal, u.getDestFloor());
                    }
                    e.setGoalFloor(goal);
                }
            }
        }

        //request part
        List<int[]> requests = workload.getRequests();
        List<User> comers = new ArrayList<>();
        requests.forEach(req -> comers.add(new User(req[0], req[1], req[2], req[2]==1?req[1]+1:req[1]-1))); //default dest floor by +-1
        comers.stream()
                .filter(comer -> comer.getDirection() == 1)
                .collect(Collectors.toList())
                .forEach(comer -> waitUpLists[comer.getStartFloor()].add(comer));
        comers.stream()
                .filter(comer -> comer.getDirection() == 0)
                .collect(Collectors.toList())
                .forEach(comer -> waitDownLists[comer.getStartFloor()].add(comer));

        findClosestElevator();
    }

    private void work() {
        for(Elevator e : elevators) {
            if(e.getGoalFloor() != -1) {
                e.moveNext();
                unload(e);
                pickup(e);
            }
        }
    }

    //set goal floor for each elevator every tick, consider both time and power cost
    private void findClosestElevator() {
        for(int i=1; i<=11; i++) {
            if(isFloorEmpty(i))continue;

            int minDistance = 11, minPower = 6*11;

            Elevator closestElevator = null;

            for(Elevator e : elevators) {
                int distance, power;
                if(!e.isMove() || (e.getDirection() == 1 && i >= e.getCurrentFloor())
                        || (e.getDirection() == 0 && i <= e.getCurrentFloor())) {
                    distance = Math.abs(e.getCurrentFloor() - i);
                    power = e.getCurrentFloor()>i ? 4*(e.getGoalFloor()-i) : 6*(i-e.getGoalFloor());
                }
                else {
                    distance = Math.abs(e.getCurrentFloor()-e.getGoalFloor()) + Math.abs(e.getGoalFloor()-i);
                    power = e.getCurrentFloor()>i ? (6*(e.getGoalFloor()-e.getCurrentFloor())+4*(e.getGoalFloor()-i))
                            : (4*(e.getCurrentFloor()-e.getGoalFloor())+6*(i-e.getGoalFloor()));
                }

                if(distance < minDistance || (distance == minDistance && power < minPower)) {
                    minDistance = distance;
                    minPower = power;
                    closestElevator = e;
                }
            }

            if(closestElevator != null) {
                if(!closestElevator.isMove()) {
                    closestElevator.setGoalFloor(i);
                    closestElevator.setDirection(closestElevator.getCurrentFloor()>i? 0 : 1);
                }
                else {
                    if((closestElevator.getDirection() == 1 && closestElevator.getGoalFloor() < i)
                            || (closestElevator.getDirection() == 0 && closestElevator.getGoalFloor() > i)) {
                        closestElevator.setGoalFloor(i);
                    }
                }
            }
        }
    }

    private void unload(Elevator e) {
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

    private void pickup(Elevator e) {
        if(isFloorEmpty(e.getCurrentFloor())) return; // empty floor, no person to pickup

        if(e.getDirection() == 1 || e.getCurrentFloor() == 1) {
            while(e.getCurrentUsers().size() < 20) {
                if(waitUpLists[e.getCurrentFloor()].size() > 0) {
                    User out = (User)waitUpLists[e.getCurrentFloor()].remove();
                    e.getCurrentUsers().add(out);
                    e.getCurrentUserIds().add(out.getUserId());
                    e.setDirection(1);
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
                    e.setDirection(0);
                }
                else
                    break;
            }
        }
        else if(e.getDirection() == -1) {  //elevator is idle initially. no person inside right now.
            if(waitUpLists[e.getCurrentFloor()].size() > 0) {
                while(e.getCurrentUsers().size() < 20) {
                    if(waitUpLists[e.getCurrentFloor()].size() > 0) {
                        User out = (User)waitUpLists[e.getCurrentFloor()].remove();
                        e.getCurrentUsers().add(out);
                        e.getCurrentUserIds().add(out.getUserId());
                        e.setDirection(1);
                    }
                    else
                        break;
                }
            }
            else if(waitDownLists[e.getCurrentFloor()].size() > 0) {
                while(e.getCurrentUsers().size() < 20) {
                    if(waitDownLists[e.getCurrentFloor()].size() > 0) {
                        User out = (User)waitDownLists[e.getCurrentFloor()].remove();
                        e.getCurrentUsers().add(out);
                        e.getCurrentUserIds().add(out.getUserId());
                        e.setDirection(0);
                    }
                    else
                        break;
                }
            }
            e.setMove(false);
        }
        if(e.getCurrentUserIds().size() > 0)e.setMove(true);
    }

    private boolean isFloorEmpty(int floor) {    //check if there is anyone waiting on this floor.
        return waitUpLists[floor].size() + waitDownLists[floor].size() == 0;
    }

}
