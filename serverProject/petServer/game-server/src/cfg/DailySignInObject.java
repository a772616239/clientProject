package cfg;
import model.base.baseConfigObject;
public class  DailySignInObject implements baseConfigObject{



private int cumusignindays;

private int[][] signinreward;

private int templateid;




public void setCumusignindays(int cumusignindays) {

this.cumusignindays = cumusignindays;

}

public int getCumusignindays() {

return this.cumusignindays;

}


public void setSigninreward(int[][] signinreward) {

this.signinreward = signinreward;

}

public int[][] getSigninreward() {

return this.signinreward;

}


public void setTemplateid(int templateid) {

this.templateid = templateid;

}

public int getTemplateid() {

return this.templateid;

}




}
