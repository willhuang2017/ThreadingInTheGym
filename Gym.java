import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.HashMap;
import java.util.Collections;

public class Gym {
  private final int totalGymMembers;
  private Map<MachineType,Integer> avaliableMachines;
  private int min = Integer.MAX_VALUE;

  public Gym(int totalGymMembers, Map<MachineType,Integer> avaliableMachines){
    this.totalGymMembers = totalGymMembers;
    this.avaliableMachines = avaliableMachines;
  }

  public void openForTheDay(){
    List<Thread> gymMembersRoutines;
    gymMembersRoutines = IntStream.rangeClosed(1, this.totalGymMembers).mapToObj((id) -> {
      Member member = new Member(id);
      return new Thread(() -> {
        try {
          synchronized(this) {
            while(min <= 0) {
              wait();
            }
            for(MachineType key : avaliableMachines.keySet()){
              avaliableMachines.put(key,avaliableMachines.get(key) - 1);
            }
            min = Collections.min(avaliableMachines.values());
          }
          member.performRoutine();
          synchronized(this) {
            for(MachineType key : avaliableMachines.keySet()){
              avaliableMachines.put(key,avaliableMachines.get(key) + 1);
            }
            min = Collections.min(avaliableMachines.values());
            notifyAll();
          }
          
        } catch(Exception e){
          System.out.println(e);
        }
      });
    }).collect(Collectors.toList());
    Thread supervisor = this.createSupervisor(gymMembersRoutines);
    gymMembersRoutines.forEach((t) -> t.start());
    supervisor.start();
  }

  public Thread createSupervisor(List<Thread> threads){
    Thread supervisor = new Thread(() -> {
      while(true){
        List<String> runningThreads = threads.stream().filter((t) -> t.isAlive()).map((t) -> t.getName()).collect(Collectors.toList());
        System.out.println(Thread.currentThread().getName() + ": " + runningThreads.size() + " people are still working out " + runningThreads + "\n");
        if(runningThreads.isEmpty()){
          break;
        }
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e){
          System.out.println(e);
        }
      }
      System.out.println(Thread.currentThread().getName() + ": " + "All members have finished their workout");
    });
    supervisor.setName("Gym Staff");
    return supervisor;
  }

  public static void main(String[] args){
    Gym gym = new Gym(16, new HashMap<>() {
      {
      put(MachineType.LEGPRESSMACHINE, 5);
      put(MachineType.BARBELL, 5);
      put(MachineType.SQUATMACHINE, 5);
      put(MachineType.LEGEXTENSIONMACHINE, 5);
      put(MachineType.LEGCURLMACHINE, 5);
      put(MachineType.LATPULLDOWNMACHINE, 5);
      put(MachineType.CABLECROSSOVERMACHINE, 5);
      } 
    });
    gym.openForTheDay();
  }
}
