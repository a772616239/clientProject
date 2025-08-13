package cfg;
import model.base.baseConfigObject;
public class  ArenaRobotConfigObject implements baseConfigObject{



private int id;

private int dan;

private int startscore;

private int endscore;

private int needcount;

private String name;

private int[][] petcount;

private int[] petlvrange;

private int[] petlvrange2;

private int[] petlvrange3;

private int[] petwakeuprange;

private int[][] exproperty;

private int namestr;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setDan(int dan) {

this.dan = dan;

}

public int getDan() {

return this.dan;

}


public void setStartscore(int startscore) {

this.startscore = startscore;

}

public int getStartscore() {

return this.startscore;

}


public void setEndscore(int endscore) {

this.endscore = endscore;

}

public int getEndscore() {

return this.endscore;

}


public void setNeedcount(int needcount) {

this.needcount = needcount;

}

public int getNeedcount() {

return this.needcount;

}


public void setName(String name) {

this.name = name;

}

public String getName() {

return this.name;

}


public void setPetcount(int[][] petcount) {

this.petcount = petcount;

}

public int[][] getPetcount() {

return this.petcount;

}


public void setPetlvrange(int[] petlvrange) {

this.petlvrange = petlvrange;

}

public int[] getPetlvrange() {

return this.petlvrange;

}


public void setPetlvrange2(int[] petlvrange2) {

this.petlvrange2 = petlvrange2;

}

public int[] getPetlvrange2() {

return this.petlvrange2;

}


public void setPetlvrange3(int[] petlvrange3) {

this.petlvrange3 = petlvrange3;

}

public int[] getPetlvrange3() {

return this.petlvrange3;

}


public void setPetwakeuprange(int[] petwakeuprange) {

this.petwakeuprange = petwakeuprange;

}

public int[] getPetwakeuprange() {

return this.petwakeuprange;

}


public void setExproperty(int[][] exproperty) {

this.exproperty = exproperty;

}

public int[][] getExproperty() {

return this.exproperty;

}


public void setNamestr(int namestr) {

this.namestr = namestr;

}

public int getNamestr() {

return this.namestr;

}




}
