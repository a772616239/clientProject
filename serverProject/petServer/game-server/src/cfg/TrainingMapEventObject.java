package cfg;
import model.base.baseConfigObject;
public class  TrainingMapEventObject implements baseConfigObject{



private int id;

private int[][] reward;

private int[] events;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setReward(int[][] reward) {

this.reward = reward;

}

public int[][] getReward() {

return this.reward;

}


public void setEvents(int[] events) {

this.events = events;

}

public int[] getEvents() {

return this.events;

}




}
