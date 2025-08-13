package cfg;
import model.base.baseConfigObject;
public class  ArenaLeagueObject implements baseConfigObject{



private int id;

private int startdan;

private int enddan;

private int[][] canuseteamnumandpetcount;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setStartdan(int startdan) {

this.startdan = startdan;

}

public int getStartdan() {

return this.startdan;

}


public void setEnddan(int enddan) {

this.enddan = enddan;

}

public int getEnddan() {

return this.enddan;

}


public void setCanuseteamnumandpetcount(int[][] canuseteamnumandpetcount) {

this.canuseteamnumandpetcount = canuseteamnumandpetcount;

}

public int[][] getCanuseteamnumandpetcount() {

return this.canuseteamnumandpetcount;

}




}
