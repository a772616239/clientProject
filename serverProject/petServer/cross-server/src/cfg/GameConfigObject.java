package cfg;
import model.base.baseConfigObject;
public class  GameConfigObject implements baseConfigObject{



private int id;

private int maxmailboxcapacity;

private int dailymissioncompletecount;

private int defaultskillcfgid;

private int[][] defaultaritifact;

private int autofreerarity;

private int challengeplayerfightmake;

private int[] buyteamcost;

private int displayerpetlimit;

private int defaultlv;

private int defaultavatarid;

private int renameexpend;

private int renamelimittime;

private int mistboxrewadmaxmarqueesize;

private int petmissionrefreshconsume;

private int petmissionfinishconsume;

private int petmissionmaxnumber;

private int[] petmissionaddconsume;

private int[] petmissionrefreshvars;

private int endlessspirerankingcount;

private int endlessspirerefreashinteval;

private int applylimit;

private int eachrecommandcount;

private int[] sendfriendpointreawrd;

private int mistattincrease;

private int resettime;

private int mainlineonhookrefreash;

private int mainlinerandominterval;

private int mainlinemaxonhooktime;

private int mainlinerankingcount;

private int mainlinerankinginterval;

private int recentpassedcount;

private int dropmainlineinterval;

private int petrunemaxlvl;

private int petiintensifyrunemaxllvl;

private int runeexpaddproportion;

private int petbaginit;

private int petrunebaginit;

private int mineexploitscroll;

private int[][] pettransferconsume;

private int chooseminerewardtime;

private int waitminebattletime;

private int[] mineexploitscrollprice;

private int minelimitlevel;

private int minegiftposnum;

private int friendhelppetusetime;

private int applyfriendhelpinterval;

private int[] newbeepet;

private int[][] newbeerewards;

private int[] newbeechoicepet;

private int[][] newbeeonhookrewards;

private int newbeedrawcard;

private int fightshowparameter;

private int[] newbeefightmakeid;

private int msgmaxnum;

private int[] bondlevel;

private int bosscalcx;

private int bosscalcn;

private int rebornpetlv;

private int[] rebornpet;

private int[][] mistnewbiereward;

private int cumupaymaxstep;

private int[][] runeexpexchangerate;

private int[] mistentryticketprice;

private int[] advancedfeatsprice;

private int[] featspricerechargeproductid;

private int[] bravechallengerebornprice;

private int featesrestdays;

private int[] runquality;

private int runmarqueeid;

private int[] advancedwishingwellprice;

private int activityrankingdissize;

private int[] growthfundprice;

private int price_dianquan;

private int price_diamond;

private int arenaunlockperson;

private int arenaskipviplv;

private int battlespeedupunlocknode;

private int maxonekeycomposepetcount;

private int[][] defaultskill;

private int dailytimelimitgiftnum;

private int voidstoneunlocklvl;

private int goldexdefaultnode;

private int[] patrolsweepconsume;

private int patrolgoogsnum;

private int[][] treasuregreedconfig;

private int[] resopeningtreasure;

private int fightwaittimeout;

private int[] buyvipexpgetitem;

private int[] buyvipexpcost;

private int vipexpdailybuylimit;

private int wishwellsendmailtime;

private int wishwellneedlv;

private int mistunlockpvplevel;

private int[][] mistforcepvpstarttime;

private int petgembaginit;

private int[][] mistmarqueerewardtype;

private int mistboxrarerewardmarqueeid;

private int mistpkchangeinterval;

private int mistrefiningstonespercent;

private int mistkillplayerupdateinterval;

private int[][] mistpvemonsterbuff;

private int bosstowersweepneedstar;

private int battlecooldown;

private int[] blindboxreardweight;

private int[] blindboxreardnum;

private int[] richmanrandomdicecost;

private int[] richmanoptionaldicecost;

private int richmanbigrewardmarquee;

private String fightplaybackversion;

private int patrolfailrewardrate;

private int[] richmandailyitem;

private int[] mistmazeseasonmission;

private int[] missionlimit;

private int[] inscriptionopenlv;

private int popupdailylimit;

private int maxghostrecordcount;

private int resmaxrecycledays;

private int resrecyclebaserewardrate;

private int[] evolveneedrarity;

private int[] buymiststaminaconsume;

private int buymiststaminanum;

private int buymiststaminamaxtimes;

private int initmiststamina;

private int miststaminamaxnum;

private int mistrecoverstamina;

private int mistsummonnum;

private int mistrecoverstaminainterval;

private int coupteamminlv;

private int coupteampetlvdif;

private int bestpetcoretype;

private int[] trainfresh;

private int[] trainconsume;

private int trainconsumebase;

private int trainfree;

private int trainhelpcount;

private int maxbarragesize;

private int trainbufflimit;

private int magictime;

private int magicbosstime;

private int[] magicbuff;

private int magicrecordsize;

private int magicranksize;

private int offerreward_fightper;

private int offerreward_eachpage;

private int offerreward_getper;

private int offerreward_person;

private int[] bravechallengereset;

private int[] featidlist;

private int[] bosstower_buyitem;

private int patrol_join;

private int patrol_sweep;

private int crossarenascorelimit;

private int[] bosstower_buy;

private int roll_frag2pet_grade;

private int endlessfeatsdays;

private int patrolfeatsdays;

private int offerreward_daylimit;

private int[] offer_levellimit;

private int[] mistmazeitems;

private int magicthron_pvplist;

private int magicthron_p1;

private int magicthron_param2;

private int magicthron_rankall;

private int magicthron_rankone;

private int offerreward_open;

private int crossarenapvp_open;

private int[] cross_weekbos_luck;

private int[][] initmistcarrypack;

private int dailyelitemonsterrewradtimes;

private int rarityresetminrarity;

private int afterrarityresetrarity;

private int worldlvrank;

private int maxtrainuseitemnum;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setMaxmailboxcapacity(int maxmailboxcapacity) {

this.maxmailboxcapacity = maxmailboxcapacity;

}

public int getMaxmailboxcapacity() {

return this.maxmailboxcapacity;

}


public void setDailymissioncompletecount(int dailymissioncompletecount) {

this.dailymissioncompletecount = dailymissioncompletecount;

}

public int getDailymissioncompletecount() {

return this.dailymissioncompletecount;

}


public void setDefaultskillcfgid(int defaultskillcfgid) {

this.defaultskillcfgid = defaultskillcfgid;

}

public int getDefaultskillcfgid() {

return this.defaultskillcfgid;

}


public void setDefaultaritifact(int[][] defaultaritifact) {

this.defaultaritifact = defaultaritifact;

}

public int[][] getDefaultaritifact() {

return this.defaultaritifact;

}


public void setAutofreerarity(int autofreerarity) {

this.autofreerarity = autofreerarity;

}

public int getAutofreerarity() {

return this.autofreerarity;

}


public void setChallengeplayerfightmake(int challengeplayerfightmake) {

this.challengeplayerfightmake = challengeplayerfightmake;

}

public int getChallengeplayerfightmake() {

return this.challengeplayerfightmake;

}


public void setBuyteamcost(int[] buyteamcost) {

this.buyteamcost = buyteamcost;

}

public int[] getBuyteamcost() {

return this.buyteamcost;

}


public void setDisplayerpetlimit(int displayerpetlimit) {

this.displayerpetlimit = displayerpetlimit;

}

public int getDisplayerpetlimit() {

return this.displayerpetlimit;

}


public void setDefaultlv(int defaultlv) {

this.defaultlv = defaultlv;

}

public int getDefaultlv() {

return this.defaultlv;

}


public void setDefaultavatarid(int defaultavatarid) {

this.defaultavatarid = defaultavatarid;

}

public int getDefaultavatarid() {

return this.defaultavatarid;

}


public void setRenameexpend(int renameexpend) {

this.renameexpend = renameexpend;

}

public int getRenameexpend() {

return this.renameexpend;

}


public void setRenamelimittime(int renamelimittime) {

this.renamelimittime = renamelimittime;

}

public int getRenamelimittime() {

return this.renamelimittime;

}


public void setMistboxrewadmaxmarqueesize(int mistboxrewadmaxmarqueesize) {

this.mistboxrewadmaxmarqueesize = mistboxrewadmaxmarqueesize;

}

public int getMistboxrewadmaxmarqueesize() {

return this.mistboxrewadmaxmarqueesize;

}


public void setPetmissionrefreshconsume(int petmissionrefreshconsume) {

this.petmissionrefreshconsume = petmissionrefreshconsume;

}

public int getPetmissionrefreshconsume() {

return this.petmissionrefreshconsume;

}


public void setPetmissionfinishconsume(int petmissionfinishconsume) {

this.petmissionfinishconsume = petmissionfinishconsume;

}

public int getPetmissionfinishconsume() {

return this.petmissionfinishconsume;

}


public void setPetmissionmaxnumber(int petmissionmaxnumber) {

this.petmissionmaxnumber = petmissionmaxnumber;

}

public int getPetmissionmaxnumber() {

return this.petmissionmaxnumber;

}


public void setPetmissionaddconsume(int[] petmissionaddconsume) {

this.petmissionaddconsume = petmissionaddconsume;

}

public int[] getPetmissionaddconsume() {

return this.petmissionaddconsume;

}


public void setPetmissionrefreshvars(int[] petmissionrefreshvars) {

this.petmissionrefreshvars = petmissionrefreshvars;

}

public int[] getPetmissionrefreshvars() {

return this.petmissionrefreshvars;

}


public void setEndlessspirerankingcount(int endlessspirerankingcount) {

this.endlessspirerankingcount = endlessspirerankingcount;

}

public int getEndlessspirerankingcount() {

return this.endlessspirerankingcount;

}


public void setEndlessspirerefreashinteval(int endlessspirerefreashinteval) {

this.endlessspirerefreashinteval = endlessspirerefreashinteval;

}

public int getEndlessspirerefreashinteval() {

return this.endlessspirerefreashinteval;

}


public void setApplylimit(int applylimit) {

this.applylimit = applylimit;

}

public int getApplylimit() {

return this.applylimit;

}


public void setEachrecommandcount(int eachrecommandcount) {

this.eachrecommandcount = eachrecommandcount;

}

public int getEachrecommandcount() {

return this.eachrecommandcount;

}


public void setSendfriendpointreawrd(int[] sendfriendpointreawrd) {

this.sendfriendpointreawrd = sendfriendpointreawrd;

}

public int[] getSendfriendpointreawrd() {

return this.sendfriendpointreawrd;

}


public void setMistattincrease(int mistattincrease) {

this.mistattincrease = mistattincrease;

}

public int getMistattincrease() {

return this.mistattincrease;

}


public void setResettime(int resettime) {

this.resettime = resettime;

}

public int getResettime() {

return this.resettime;

}


public void setMainlineonhookrefreash(int mainlineonhookrefreash) {

this.mainlineonhookrefreash = mainlineonhookrefreash;

}

public int getMainlineonhookrefreash() {

return this.mainlineonhookrefreash;

}


public void setMainlinerandominterval(int mainlinerandominterval) {

this.mainlinerandominterval = mainlinerandominterval;

}

public int getMainlinerandominterval() {

return this.mainlinerandominterval;

}


public void setMainlinemaxonhooktime(int mainlinemaxonhooktime) {

this.mainlinemaxonhooktime = mainlinemaxonhooktime;

}

public int getMainlinemaxonhooktime() {

return this.mainlinemaxonhooktime;

}


public void setMainlinerankingcount(int mainlinerankingcount) {

this.mainlinerankingcount = mainlinerankingcount;

}

public int getMainlinerankingcount() {

return this.mainlinerankingcount;

}


public void setMainlinerankinginterval(int mainlinerankinginterval) {

this.mainlinerankinginterval = mainlinerankinginterval;

}

public int getMainlinerankinginterval() {

return this.mainlinerankinginterval;

}


public void setRecentpassedcount(int recentpassedcount) {

this.recentpassedcount = recentpassedcount;

}

public int getRecentpassedcount() {

return this.recentpassedcount;

}


public void setDropmainlineinterval(int dropmainlineinterval) {

this.dropmainlineinterval = dropmainlineinterval;

}

public int getDropmainlineinterval() {

return this.dropmainlineinterval;

}


public void setPetrunemaxlvl(int petrunemaxlvl) {

this.petrunemaxlvl = petrunemaxlvl;

}

public int getPetrunemaxlvl() {

return this.petrunemaxlvl;

}


public void setPetiintensifyrunemaxllvl(int petiintensifyrunemaxllvl) {

this.petiintensifyrunemaxllvl = petiintensifyrunemaxllvl;

}

public int getPetiintensifyrunemaxllvl() {

return this.petiintensifyrunemaxllvl;

}


public void setRuneexpaddproportion(int runeexpaddproportion) {

this.runeexpaddproportion = runeexpaddproportion;

}

public int getRuneexpaddproportion() {

return this.runeexpaddproportion;

}


public void setPetbaginit(int petbaginit) {

this.petbaginit = petbaginit;

}

public int getPetbaginit() {

return this.petbaginit;

}


public void setPetrunebaginit(int petrunebaginit) {

this.petrunebaginit = petrunebaginit;

}

public int getPetrunebaginit() {

return this.petrunebaginit;

}


public void setMineexploitscroll(int mineexploitscroll) {

this.mineexploitscroll = mineexploitscroll;

}

public int getMineexploitscroll() {

return this.mineexploitscroll;

}


public void setPettransferconsume(int[][] pettransferconsume) {

this.pettransferconsume = pettransferconsume;

}

public int[][] getPettransferconsume() {

return this.pettransferconsume;

}


public void setChooseminerewardtime(int chooseminerewardtime) {

this.chooseminerewardtime = chooseminerewardtime;

}

public int getChooseminerewardtime() {

return this.chooseminerewardtime;

}


public void setWaitminebattletime(int waitminebattletime) {

this.waitminebattletime = waitminebattletime;

}

public int getWaitminebattletime() {

return this.waitminebattletime;

}


public void setMineexploitscrollprice(int[] mineexploitscrollprice) {

this.mineexploitscrollprice = mineexploitscrollprice;

}

public int[] getMineexploitscrollprice() {

return this.mineexploitscrollprice;

}


public void setMinelimitlevel(int minelimitlevel) {

this.minelimitlevel = minelimitlevel;

}

public int getMinelimitlevel() {

return this.minelimitlevel;

}


public void setMinegiftposnum(int minegiftposnum) {

this.minegiftposnum = minegiftposnum;

}

public int getMinegiftposnum() {

return this.minegiftposnum;

}


public void setFriendhelppetusetime(int friendhelppetusetime) {

this.friendhelppetusetime = friendhelppetusetime;

}

public int getFriendhelppetusetime() {

return this.friendhelppetusetime;

}


public void setApplyfriendhelpinterval(int applyfriendhelpinterval) {

this.applyfriendhelpinterval = applyfriendhelpinterval;

}

public int getApplyfriendhelpinterval() {

return this.applyfriendhelpinterval;

}


public void setNewbeepet(int[] newbeepet) {

this.newbeepet = newbeepet;

}

public int[] getNewbeepet() {

return this.newbeepet;

}


public void setNewbeerewards(int[][] newbeerewards) {

this.newbeerewards = newbeerewards;

}

public int[][] getNewbeerewards() {

return this.newbeerewards;

}


public void setNewbeechoicepet(int[] newbeechoicepet) {

this.newbeechoicepet = newbeechoicepet;

}

public int[] getNewbeechoicepet() {

return this.newbeechoicepet;

}


public void setNewbeeonhookrewards(int[][] newbeeonhookrewards) {

this.newbeeonhookrewards = newbeeonhookrewards;

}

public int[][] getNewbeeonhookrewards() {

return this.newbeeonhookrewards;

}


public void setNewbeedrawcard(int newbeedrawcard) {

this.newbeedrawcard = newbeedrawcard;

}

public int getNewbeedrawcard() {

return this.newbeedrawcard;

}


public void setFightshowparameter(int fightshowparameter) {

this.fightshowparameter = fightshowparameter;

}

public int getFightshowparameter() {

return this.fightshowparameter;

}


public void setNewbeefightmakeid(int[] newbeefightmakeid) {

this.newbeefightmakeid = newbeefightmakeid;

}

public int[] getNewbeefightmakeid() {

return this.newbeefightmakeid;

}


public void setMsgmaxnum(int msgmaxnum) {

this.msgmaxnum = msgmaxnum;

}

public int getMsgmaxnum() {

return this.msgmaxnum;

}


public void setBondlevel(int[] bondlevel) {

this.bondlevel = bondlevel;

}

public int[] getBondlevel() {

return this.bondlevel;

}


public void setBosscalcx(int bosscalcx) {

this.bosscalcx = bosscalcx;

}

public int getBosscalcx() {

return this.bosscalcx;

}


public void setBosscalcn(int bosscalcn) {

this.bosscalcn = bosscalcn;

}

public int getBosscalcn() {

return this.bosscalcn;

}


public void setRebornpetlv(int rebornpetlv) {

this.rebornpetlv = rebornpetlv;

}

public int getRebornpetlv() {

return this.rebornpetlv;

}


public void setRebornpet(int[] rebornpet) {

this.rebornpet = rebornpet;

}

public int[] getRebornpet() {

return this.rebornpet;

}


public void setMistnewbiereward(int[][] mistnewbiereward) {

this.mistnewbiereward = mistnewbiereward;

}

public int[][] getMistnewbiereward() {

return this.mistnewbiereward;

}


public void setCumupaymaxstep(int cumupaymaxstep) {

this.cumupaymaxstep = cumupaymaxstep;

}

public int getCumupaymaxstep() {

return this.cumupaymaxstep;

}


public void setRuneexpexchangerate(int[][] runeexpexchangerate) {

this.runeexpexchangerate = runeexpexchangerate;

}

public int[][] getRuneexpexchangerate() {

return this.runeexpexchangerate;

}


public void setMistentryticketprice(int[] mistentryticketprice) {

this.mistentryticketprice = mistentryticketprice;

}

public int[] getMistentryticketprice() {

return this.mistentryticketprice;

}


public void setAdvancedfeatsprice(int[] advancedfeatsprice) {

this.advancedfeatsprice = advancedfeatsprice;

}

public int[] getAdvancedfeatsprice() {

return this.advancedfeatsprice;

}


public void setFeatspricerechargeproductid(int[] featspricerechargeproductid) {

this.featspricerechargeproductid = featspricerechargeproductid;

}

public int[] getFeatspricerechargeproductid() {

return this.featspricerechargeproductid;

}


public void setBravechallengerebornprice(int[] bravechallengerebornprice) {

this.bravechallengerebornprice = bravechallengerebornprice;

}

public int[] getBravechallengerebornprice() {

return this.bravechallengerebornprice;

}


public void setFeatesrestdays(int featesrestdays) {

this.featesrestdays = featesrestdays;

}

public int getFeatesrestdays() {

return this.featesrestdays;

}


public void setRunquality(int[] runquality) {

this.runquality = runquality;

}

public int[] getRunquality() {

return this.runquality;

}


public void setRunmarqueeid(int runmarqueeid) {

this.runmarqueeid = runmarqueeid;

}

public int getRunmarqueeid() {

return this.runmarqueeid;

}


public void setAdvancedwishingwellprice(int[] advancedwishingwellprice) {

this.advancedwishingwellprice = advancedwishingwellprice;

}

public int[] getAdvancedwishingwellprice() {

return this.advancedwishingwellprice;

}


public void setActivityrankingdissize(int activityrankingdissize) {

this.activityrankingdissize = activityrankingdissize;

}

public int getActivityrankingdissize() {

return this.activityrankingdissize;

}


public void setGrowthfundprice(int[] growthfundprice) {

this.growthfundprice = growthfundprice;

}

public int[] getGrowthfundprice() {

return this.growthfundprice;

}


public void setPrice_dianquan(int price_dianquan) {

this.price_dianquan = price_dianquan;

}

public int getPrice_dianquan() {

return this.price_dianquan;

}


public void setPrice_diamond(int price_diamond) {

this.price_diamond = price_diamond;

}

public int getPrice_diamond() {

return this.price_diamond;

}


public void setArenaunlockperson(int arenaunlockperson) {

this.arenaunlockperson = arenaunlockperson;

}

public int getArenaunlockperson() {

return this.arenaunlockperson;

}


public void setArenaskipviplv(int arenaskipviplv) {

this.arenaskipviplv = arenaskipviplv;

}

public int getArenaskipviplv() {

return this.arenaskipviplv;

}


public void setBattlespeedupunlocknode(int battlespeedupunlocknode) {

this.battlespeedupunlocknode = battlespeedupunlocknode;

}

public int getBattlespeedupunlocknode() {

return this.battlespeedupunlocknode;

}


public void setMaxonekeycomposepetcount(int maxonekeycomposepetcount) {

this.maxonekeycomposepetcount = maxonekeycomposepetcount;

}

public int getMaxonekeycomposepetcount() {

return this.maxonekeycomposepetcount;

}


public void setDefaultskill(int[][] defaultskill) {

this.defaultskill = defaultskill;

}

public int[][] getDefaultskill() {

return this.defaultskill;

}


public void setDailytimelimitgiftnum(int dailytimelimitgiftnum) {

this.dailytimelimitgiftnum = dailytimelimitgiftnum;

}

public int getDailytimelimitgiftnum() {

return this.dailytimelimitgiftnum;

}


public void setVoidstoneunlocklvl(int voidstoneunlocklvl) {

this.voidstoneunlocklvl = voidstoneunlocklvl;

}

public int getVoidstoneunlocklvl() {

return this.voidstoneunlocklvl;

}


public void setGoldexdefaultnode(int goldexdefaultnode) {

this.goldexdefaultnode = goldexdefaultnode;

}

public int getGoldexdefaultnode() {

return this.goldexdefaultnode;

}


public void setPatrolsweepconsume(int[] patrolsweepconsume) {

this.patrolsweepconsume = patrolsweepconsume;

}

public int[] getPatrolsweepconsume() {

return this.patrolsweepconsume;

}


public void setPatrolgoogsnum(int patrolgoogsnum) {

this.patrolgoogsnum = patrolgoogsnum;

}

public int getPatrolgoogsnum() {

return this.patrolgoogsnum;

}


public void setTreasuregreedconfig(int[][] treasuregreedconfig) {

this.treasuregreedconfig = treasuregreedconfig;

}

public int[][] getTreasuregreedconfig() {

return this.treasuregreedconfig;

}


public void setResopeningtreasure(int[] resopeningtreasure) {

this.resopeningtreasure = resopeningtreasure;

}

public int[] getResopeningtreasure() {

return this.resopeningtreasure;

}


public void setFightwaittimeout(int fightwaittimeout) {

this.fightwaittimeout = fightwaittimeout;

}

public int getFightwaittimeout() {

return this.fightwaittimeout;

}


public void setBuyvipexpgetitem(int[] buyvipexpgetitem) {

this.buyvipexpgetitem = buyvipexpgetitem;

}

public int[] getBuyvipexpgetitem() {

return this.buyvipexpgetitem;

}


public void setBuyvipexpcost(int[] buyvipexpcost) {

this.buyvipexpcost = buyvipexpcost;

}

public int[] getBuyvipexpcost() {

return this.buyvipexpcost;

}


public void setVipexpdailybuylimit(int vipexpdailybuylimit) {

this.vipexpdailybuylimit = vipexpdailybuylimit;

}

public int getVipexpdailybuylimit() {

return this.vipexpdailybuylimit;

}


public void setWishwellsendmailtime(int wishwellsendmailtime) {

this.wishwellsendmailtime = wishwellsendmailtime;

}

public int getWishwellsendmailtime() {

return this.wishwellsendmailtime;

}


public void setWishwellneedlv(int wishwellneedlv) {

this.wishwellneedlv = wishwellneedlv;

}

public int getWishwellneedlv() {

return this.wishwellneedlv;

}


public void setMistunlockpvplevel(int mistunlockpvplevel) {

this.mistunlockpvplevel = mistunlockpvplevel;

}

public int getMistunlockpvplevel() {

return this.mistunlockpvplevel;

}


public void setMistforcepvpstarttime(int[][] mistforcepvpstarttime) {

this.mistforcepvpstarttime = mistforcepvpstarttime;

}

public int[][] getMistforcepvpstarttime() {

return this.mistforcepvpstarttime;

}


public void setPetgembaginit(int petgembaginit) {

this.petgembaginit = petgembaginit;

}

public int getPetgembaginit() {

return this.petgembaginit;

}


public void setMistmarqueerewardtype(int[][] mistmarqueerewardtype) {

this.mistmarqueerewardtype = mistmarqueerewardtype;

}

public int[][] getMistmarqueerewardtype() {

return this.mistmarqueerewardtype;

}


public void setMistboxrarerewardmarqueeid(int mistboxrarerewardmarqueeid) {

this.mistboxrarerewardmarqueeid = mistboxrarerewardmarqueeid;

}

public int getMistboxrarerewardmarqueeid() {

return this.mistboxrarerewardmarqueeid;

}


public void setMistpkchangeinterval(int mistpkchangeinterval) {

this.mistpkchangeinterval = mistpkchangeinterval;

}

public int getMistpkchangeinterval() {

return this.mistpkchangeinterval;

}


public void setMistrefiningstonespercent(int mistrefiningstonespercent) {

this.mistrefiningstonespercent = mistrefiningstonespercent;

}

public int getMistrefiningstonespercent() {

return this.mistrefiningstonespercent;

}


public void setMistkillplayerupdateinterval(int mistkillplayerupdateinterval) {

this.mistkillplayerupdateinterval = mistkillplayerupdateinterval;

}

public int getMistkillplayerupdateinterval() {

return this.mistkillplayerupdateinterval;

}


public void setMistpvemonsterbuff(int[][] mistpvemonsterbuff) {

this.mistpvemonsterbuff = mistpvemonsterbuff;

}

public int[][] getMistpvemonsterbuff() {

return this.mistpvemonsterbuff;

}


public void setBosstowersweepneedstar(int bosstowersweepneedstar) {

this.bosstowersweepneedstar = bosstowersweepneedstar;

}

public int getBosstowersweepneedstar() {

return this.bosstowersweepneedstar;

}


public void setBattlecooldown(int battlecooldown) {

this.battlecooldown = battlecooldown;

}

public int getBattlecooldown() {

return this.battlecooldown;

}


public void setBlindboxreardweight(int[] blindboxreardweight) {

this.blindboxreardweight = blindboxreardweight;

}

public int[] getBlindboxreardweight() {

return this.blindboxreardweight;

}


public void setBlindboxreardnum(int[] blindboxreardnum) {

this.blindboxreardnum = blindboxreardnum;

}

public int[] getBlindboxreardnum() {

return this.blindboxreardnum;

}


public void setRichmanrandomdicecost(int[] richmanrandomdicecost) {

this.richmanrandomdicecost = richmanrandomdicecost;

}

public int[] getRichmanrandomdicecost() {

return this.richmanrandomdicecost;

}


public void setRichmanoptionaldicecost(int[] richmanoptionaldicecost) {

this.richmanoptionaldicecost = richmanoptionaldicecost;

}

public int[] getRichmanoptionaldicecost() {

return this.richmanoptionaldicecost;

}


public void setRichmanbigrewardmarquee(int richmanbigrewardmarquee) {

this.richmanbigrewardmarquee = richmanbigrewardmarquee;

}

public int getRichmanbigrewardmarquee() {

return this.richmanbigrewardmarquee;

}


public void setFightplaybackversion(String fightplaybackversion) {

this.fightplaybackversion = fightplaybackversion;

}

public String getFightplaybackversion() {

return this.fightplaybackversion;

}


public void setPatrolfailrewardrate(int patrolfailrewardrate) {

this.patrolfailrewardrate = patrolfailrewardrate;

}

public int getPatrolfailrewardrate() {

return this.patrolfailrewardrate;

}


public void setRichmandailyitem(int[] richmandailyitem) {

this.richmandailyitem = richmandailyitem;

}

public int[] getRichmandailyitem() {

return this.richmandailyitem;

}


public void setMistmazeseasonmission(int[] mistmazeseasonmission) {

this.mistmazeseasonmission = mistmazeseasonmission;

}

public int[] getMistmazeseasonmission() {

return this.mistmazeseasonmission;

}


public void setMissionlimit(int[] missionlimit) {

this.missionlimit = missionlimit;

}

public int[] getMissionlimit() {

return this.missionlimit;

}


public void setInscriptionopenlv(int[] inscriptionopenlv) {

this.inscriptionopenlv = inscriptionopenlv;

}

public int[] getInscriptionopenlv() {

return this.inscriptionopenlv;

}


public void setPopupdailylimit(int popupdailylimit) {

this.popupdailylimit = popupdailylimit;

}

public int getPopupdailylimit() {

return this.popupdailylimit;

}


public void setMaxghostrecordcount(int maxghostrecordcount) {

this.maxghostrecordcount = maxghostrecordcount;

}

public int getMaxghostrecordcount() {

return this.maxghostrecordcount;

}


public void setResmaxrecycledays(int resmaxrecycledays) {

this.resmaxrecycledays = resmaxrecycledays;

}

public int getResmaxrecycledays() {

return this.resmaxrecycledays;

}


public void setResrecyclebaserewardrate(int resrecyclebaserewardrate) {

this.resrecyclebaserewardrate = resrecyclebaserewardrate;

}

public int getResrecyclebaserewardrate() {

return this.resrecyclebaserewardrate;

}


public void setEvolveneedrarity(int[] evolveneedrarity) {

this.evolveneedrarity = evolveneedrarity;

}

public int[] getEvolveneedrarity() {

return this.evolveneedrarity;

}


public void setBuymiststaminaconsume(int[] buymiststaminaconsume) {

this.buymiststaminaconsume = buymiststaminaconsume;

}

public int[] getBuymiststaminaconsume() {

return this.buymiststaminaconsume;

}


public void setBuymiststaminanum(int buymiststaminanum) {

this.buymiststaminanum = buymiststaminanum;

}

public int getBuymiststaminanum() {

return this.buymiststaminanum;

}


public void setBuymiststaminamaxtimes(int buymiststaminamaxtimes) {

this.buymiststaminamaxtimes = buymiststaminamaxtimes;

}

public int getBuymiststaminamaxtimes() {

return this.buymiststaminamaxtimes;

}


public void setInitmiststamina(int initmiststamina) {

this.initmiststamina = initmiststamina;

}

public int getInitmiststamina() {

return this.initmiststamina;

}


public void setMiststaminamaxnum(int miststaminamaxnum) {

this.miststaminamaxnum = miststaminamaxnum;

}

public int getMiststaminamaxnum() {

return this.miststaminamaxnum;

}


public void setMistrecoverstamina(int mistrecoverstamina) {

this.mistrecoverstamina = mistrecoverstamina;

}

public int getMistrecoverstamina() {

return this.mistrecoverstamina;

}


public void setMistsummonnum(int mistsummonnum) {

this.mistsummonnum = mistsummonnum;

}

public int getMistsummonnum() {

return this.mistsummonnum;

}


public void setMistrecoverstaminainterval(int mistrecoverstaminainterval) {

this.mistrecoverstaminainterval = mistrecoverstaminainterval;

}

public int getMistrecoverstaminainterval() {

return this.mistrecoverstaminainterval;

}


public void setCoupteamminlv(int coupteamminlv) {

this.coupteamminlv = coupteamminlv;

}

public int getCoupteamminlv() {

return this.coupteamminlv;

}


public void setCoupteampetlvdif(int coupteampetlvdif) {

this.coupteampetlvdif = coupteampetlvdif;

}

public int getCoupteampetlvdif() {

return this.coupteampetlvdif;

}


public void setBestpetcoretype(int bestpetcoretype) {

this.bestpetcoretype = bestpetcoretype;

}

public int getBestpetcoretype() {

return this.bestpetcoretype;

}


public void setTrainfresh(int[] trainfresh) {

this.trainfresh = trainfresh;

}

public int[] getTrainfresh() {

return this.trainfresh;

}


public void setTrainconsume(int[] trainconsume) {

this.trainconsume = trainconsume;

}

public int[] getTrainconsume() {

return this.trainconsume;

}


public void setTrainconsumebase(int trainconsumebase) {

this.trainconsumebase = trainconsumebase;

}

public int getTrainconsumebase() {

return this.trainconsumebase;

}


public void setTrainfree(int trainfree) {

this.trainfree = trainfree;

}

public int getTrainfree() {

return this.trainfree;

}


public void setTrainhelpcount(int trainhelpcount) {

this.trainhelpcount = trainhelpcount;

}

public int getTrainhelpcount() {

return this.trainhelpcount;

}


public void setMaxbarragesize(int maxbarragesize) {

this.maxbarragesize = maxbarragesize;

}

public int getMaxbarragesize() {

return this.maxbarragesize;

}


public void setTrainbufflimit(int trainbufflimit) {

this.trainbufflimit = trainbufflimit;

}

public int getTrainbufflimit() {

return this.trainbufflimit;

}


public void setMagictime(int magictime) {

this.magictime = magictime;

}

public int getMagictime() {

return this.magictime;

}


public void setMagicbosstime(int magicbosstime) {

this.magicbosstime = magicbosstime;

}

public int getMagicbosstime() {

return this.magicbosstime;

}


public void setMagicbuff(int[] magicbuff) {

this.magicbuff = magicbuff;

}

public int[] getMagicbuff() {

return this.magicbuff;

}


public void setMagicrecordsize(int magicrecordsize) {

this.magicrecordsize = magicrecordsize;

}

public int getMagicrecordsize() {

return this.magicrecordsize;

}


public void setMagicranksize(int magicranksize) {

this.magicranksize = magicranksize;

}

public int getMagicranksize() {

return this.magicranksize;

}


public void setOfferreward_fightper(int offerreward_fightper) {

this.offerreward_fightper = offerreward_fightper;

}

public int getOfferreward_fightper() {

return this.offerreward_fightper;

}


public void setOfferreward_eachpage(int offerreward_eachpage) {

this.offerreward_eachpage = offerreward_eachpage;

}

public int getOfferreward_eachpage() {

return this.offerreward_eachpage;

}


public void setOfferreward_getper(int offerreward_getper) {

this.offerreward_getper = offerreward_getper;

}

public int getOfferreward_getper() {

return this.offerreward_getper;

}


public void setOfferreward_person(int offerreward_person) {

this.offerreward_person = offerreward_person;

}

public int getOfferreward_person() {

return this.offerreward_person;

}


public void setBravechallengereset(int[] bravechallengereset) {

this.bravechallengereset = bravechallengereset;

}

public int[] getBravechallengereset() {

return this.bravechallengereset;

}


public void setFeatidlist(int[] featidlist) {

this.featidlist = featidlist;

}

public int[] getFeatidlist() {

return this.featidlist;

}


public void setBosstower_buyitem(int[] bosstower_buyitem) {

this.bosstower_buyitem = bosstower_buyitem;

}

public int[] getBosstower_buyitem() {

return this.bosstower_buyitem;

}


public void setPatrol_join(int patrol_join) {

this.patrol_join = patrol_join;

}

public int getPatrol_join() {

return this.patrol_join;

}


public void setPatrol_sweep(int patrol_sweep) {

this.patrol_sweep = patrol_sweep;

}

public int getPatrol_sweep() {

return this.patrol_sweep;

}


public void setCrossarenascorelimit(int crossarenascorelimit) {

this.crossarenascorelimit = crossarenascorelimit;

}

public int getCrossarenascorelimit() {

return this.crossarenascorelimit;

}


public void setBosstower_buy(int[] bosstower_buy) {

this.bosstower_buy = bosstower_buy;

}

public int[] getBosstower_buy() {

return this.bosstower_buy;

}


public void setRoll_frag2pet_grade(int roll_frag2pet_grade) {

this.roll_frag2pet_grade = roll_frag2pet_grade;

}

public int getRoll_frag2pet_grade() {

return this.roll_frag2pet_grade;

}


public void setEndlessfeatsdays(int endlessfeatsdays) {

this.endlessfeatsdays = endlessfeatsdays;

}

public int getEndlessfeatsdays() {

return this.endlessfeatsdays;

}


public void setPatrolfeatsdays(int patrolfeatsdays) {

this.patrolfeatsdays = patrolfeatsdays;

}

public int getPatrolfeatsdays() {

return this.patrolfeatsdays;

}


public void setOfferreward_daylimit(int offerreward_daylimit) {

this.offerreward_daylimit = offerreward_daylimit;

}

public int getOfferreward_daylimit() {

return this.offerreward_daylimit;

}


public void setOffer_levellimit(int[] offer_levellimit) {

this.offer_levellimit = offer_levellimit;

}

public int[] getOffer_levellimit() {

return this.offer_levellimit;

}


public void setMistmazeitems(int[] mistmazeitems) {

this.mistmazeitems = mistmazeitems;

}

public int[] getMistmazeitems() {

return this.mistmazeitems;

}


public void setMagicthron_pvplist(int magicthron_pvplist) {

this.magicthron_pvplist = magicthron_pvplist;

}

public int getMagicthron_pvplist() {

return this.magicthron_pvplist;

}


public void setMagicthron_p1(int magicthron_p1) {

this.magicthron_p1 = magicthron_p1;

}

public int getMagicthron_p1() {

return this.magicthron_p1;

}


public void setMagicthron_param2(int magicthron_param2) {

this.magicthron_param2 = magicthron_param2;

}

public int getMagicthron_param2() {

return this.magicthron_param2;

}


public void setMagicthron_rankall(int magicthron_rankall) {

this.magicthron_rankall = magicthron_rankall;

}

public int getMagicthron_rankall() {

return this.magicthron_rankall;

}


public void setMagicthron_rankone(int magicthron_rankone) {

this.magicthron_rankone = magicthron_rankone;

}

public int getMagicthron_rankone() {

return this.magicthron_rankone;

}


public void setOfferreward_open(int offerreward_open) {

this.offerreward_open = offerreward_open;

}

public int getOfferreward_open() {

return this.offerreward_open;

}


public void setCrossarenapvp_open(int crossarenapvp_open) {

this.crossarenapvp_open = crossarenapvp_open;

}

public int getCrossarenapvp_open() {

return this.crossarenapvp_open;

}


public void setCross_weekbos_luck(int[] cross_weekbos_luck) {

this.cross_weekbos_luck = cross_weekbos_luck;

}

public int[] getCross_weekbos_luck() {

return this.cross_weekbos_luck;

}


public void setInitmistcarrypack(int[][] initmistcarrypack) {

this.initmistcarrypack = initmistcarrypack;

}

public int[][] getInitmistcarrypack() {

return this.initmistcarrypack;

}


public void setDailyelitemonsterrewradtimes(int dailyelitemonsterrewradtimes) {

this.dailyelitemonsterrewradtimes = dailyelitemonsterrewradtimes;

}

public int getDailyelitemonsterrewradtimes() {

return this.dailyelitemonsterrewradtimes;

}


public void setRarityresetminrarity(int rarityresetminrarity) {

this.rarityresetminrarity = rarityresetminrarity;

}

public int getRarityresetminrarity() {

return this.rarityresetminrarity;

}


public void setAfterrarityresetrarity(int afterrarityresetrarity) {

this.afterrarityresetrarity = afterrarityresetrarity;

}

public int getAfterrarityresetrarity() {

return this.afterrarityresetrarity;

}


public void setWorldlvrank(int worldlvrank) {

this.worldlvrank = worldlvrank;

}

public int getWorldlvrank() {

return this.worldlvrank;

}


public void setMaxtrainuseitemnum(int maxtrainuseitemnum) {

this.maxtrainuseitemnum = maxtrainuseitemnum;

}

public int getMaxtrainuseitemnum() {

return this.maxtrainuseitemnum;

}




}
