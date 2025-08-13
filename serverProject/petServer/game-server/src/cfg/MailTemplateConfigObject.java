package cfg;
import model.base.baseConfigObject;
public class  MailTemplateConfigObject implements baseConfigObject{



private int templateid;

private int mailtype;

private int sender;

private int title_tipids;

private int body_tipsid;

private int[][] attachment;

private int expiretime;




public void setTemplateid(int templateid) {

this.templateid = templateid;

}

public int getTemplateid() {

return this.templateid;

}


public void setMailtype(int mailtype) {

this.mailtype = mailtype;

}

public int getMailtype() {

return this.mailtype;

}


public void setSender(int sender) {

this.sender = sender;

}

public int getSender() {

return this.sender;

}


public void setTitle_tipids(int title_tipids) {

this.title_tipids = title_tipids;

}

public int getTitle_tipids() {

return this.title_tipids;

}


public void setBody_tipsid(int body_tipsid) {

this.body_tipsid = body_tipsid;

}

public int getBody_tipsid() {

return this.body_tipsid;

}


public void setAttachment(int[][] attachment) {

this.attachment = attachment;

}

public int[][] getAttachment() {

return this.attachment;

}


public void setExpiretime(int expiretime) {

this.expiretime = expiretime;

}

public int getExpiretime() {

return this.expiretime;

}




}
