package cfg;
import model.base.baseConfigObject;
public class  NewbeeTagObject implements baseConfigObject{



private int id;

private String comment;

private boolean joinappsflyer;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setComment(String comment) {

this.comment = comment;

}

public String getComment() {

return this.comment;

}


public void setJoinappsflyer(boolean joinappsflyer) {

this.joinappsflyer = joinappsflyer;

}

public boolean getJoinappsflyer() {

return this.joinappsflyer;

}




}
