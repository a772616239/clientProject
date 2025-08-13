package cfg;
import model.base.baseConfigObject;
public class  ReportConfigObject implements baseConfigObject{



private int id;

private int[] autodealtype;

private int dealneedtimes;

private int eachdaycanreporttimes;

private int autodealbandays;

private int bancommenttips;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setAutodealtype(int[] autodealtype) {

this.autodealtype = autodealtype;

}

public int[] getAutodealtype() {

return this.autodealtype;

}


public void setDealneedtimes(int dealneedtimes) {

this.dealneedtimes = dealneedtimes;

}

public int getDealneedtimes() {

return this.dealneedtimes;

}


public void setEachdaycanreporttimes(int eachdaycanreporttimes) {

this.eachdaycanreporttimes = eachdaycanreporttimes;

}

public int getEachdaycanreporttimes() {

return this.eachdaycanreporttimes;

}


public void setAutodealbandays(int autodealbandays) {

this.autodealbandays = autodealbandays;

}

public int getAutodealbandays() {

return this.autodealbandays;

}


public void setBancommenttips(int bancommenttips) {

this.bancommenttips = bancommenttips;

}

public int getBancommenttips() {

return this.bancommenttips;

}




}
