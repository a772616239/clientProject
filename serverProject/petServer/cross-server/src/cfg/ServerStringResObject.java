package cfg;
import model.base.baseConfigObject;

public class ServerStringResObject implements baseConfigObject{



private int id;

private String content_cn;

private String content_tw;

private String content_en;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setContent_cn(String content_cn) {

this.content_cn = content_cn;

}

public String getContent_cn() {

return this.content_cn;

}


public void setContent_tw(String content_tw) {

this.content_tw = content_tw;

}

public String getContent_tw() {

return this.content_tw;

}


public void setContent_en(String content_en) {

this.content_en = content_en;

}

public String getContent_en() {

return this.content_en;

}




}
