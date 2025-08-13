package cfg;
import model.base.baseConfigObject;
public class  RankConfigObject implements baseConfigObject{



private int rankid;

private int sort;

private int[] rankreward_range;

private int[] rankreward_target;

private int rankrefreshtime;




public void setRankid(int rankid) {

this.rankid = rankid;

}

public int getRankid() {

return this.rankid;

}


public void setSort(int sort) {

this.sort = sort;

}

public int getSort() {

return this.sort;

}


public void setRankreward_range(int[] rankreward_range) {

this.rankreward_range = rankreward_range;

}

public int[] getRankreward_range() {

return this.rankreward_range;

}


public void setRankreward_target(int[] rankreward_target) {

this.rankreward_target = rankreward_target;

}

public int[] getRankreward_target() {

return this.rankreward_target;

}


public void setRankrefreshtime(int rankrefreshtime) {

this.rankrefreshtime = rankrefreshtime;

}

public int getRankrefreshtime() {

return this.rankrefreshtime;

}




}
