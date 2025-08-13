package cfg;
import model.base.baseConfigObject;
public class  NoviceCreditObject implements baseConfigObject{



private int points;

private int[] award;




public void setPoints(int points) {

this.points = points;

}

public int getPoints() {

return this.points;

}


public void setAward(int[] award) {

this.award = award;

}

public int[] getAward() {

return this.award;

}




}
