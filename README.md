# ElevatorController
elevator system design

main idea:
1)create waiting queues on each floor, including up and down two queues. once a request comes in, 
  we wrap requests as users and push them into corresponding queues according to direction and start floor.
  
2)find closest elevator every tick for requests on each floor, consider both time and power cost, 
  remember up and down power cost differs, after that set goal floor and direction for the closest elevator:
  - if target_floor>current_goal_floor and direction is up, we update current_goal_floor with this greater value.
  - if target_floor<current_goal_floor and direction is down, we update current_goal_floor with this smaller value.
  - if targer_floor<current_goal_floor and direction is up, skip this elevator.
  - if target_floor>current_goal_floor and direction is down, skip this elevator.
  
3)elevator will unload and pick up persons on each floor per request, if the elevator is running up currently,
  it will pick up people waiting up only, and vice verse. Once the elevator reaches the goal floor, it resets all flags, 
  such as direction, move, goal floor, and keeps waiting on new call over there.
