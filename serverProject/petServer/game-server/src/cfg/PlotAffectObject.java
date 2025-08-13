package cfg;
import model.base.baseConfigObject;
public class  PlotAffectObject implements baseConfigObject{



private int id;

private int plotid;

private int newnodeid;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setPlotid(int plotid) {

this.plotid = plotid;

}

public int getPlotid() {

return this.plotid;

}


public void setNewnodeid(int newnodeid) {

this.newnodeid = newnodeid;

}

public int getNewnodeid() {

return this.newnodeid;

}




}
