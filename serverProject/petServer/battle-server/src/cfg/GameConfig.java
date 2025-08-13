/*CREATED BY TOOL*/

package cfg;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;

@annationInit(value ="GameConfig", methodname = "initConfig")
public class GameConfig extends baseConfig<GameConfigObject>{


private static GameConfig instance = null;

public static GameConfig getInstance() {

if (instance == null)
instance = new GameConfig();
return instance;

}


public static Map<Integer, GameConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (GameConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"GameConfig");

for(Map e:ret)
{
put(e);
}

}

public static GameConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, GameConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setMaxmailboxcapacity(MapHelper.getInt(e, "maxMailBoxCapacity"));

config.setDailymissioncompletecount(MapHelper.getInt(e, "dailyMissionCompleteCount"));

config.setDefaultskillcfgid(MapHelper.getInt(e, "defaultSkillCfgId"));

config.setDefaultaritifact(MapHelper.getIntArray(e, "defaultAritifact"));

config.setAutofreerarity(MapHelper.getInt(e, "autoFreeRarity"));

config.setChallengeplayerfightmake(MapHelper.getInt(e, "challengePlayerFightMake"));

config.setBuyteamcost(MapHelper.getInts(e, "buyTeamCost"));

config.setDisplayerpetlimit(MapHelper.getInt(e, "disPlayerPetLimit"));

config.setDefaultlv(MapHelper.getInt(e, "defaultLv"));

config.setDefaultavatarid(MapHelper.getInt(e, "defaultAvatarId"));

config.setRenameexpend(MapHelper.getInt(e, "renameExpend"));

config.setRenamelimittime(MapHelper.getInt(e, "renameLimitTime"));

config.setMistboxrewadmaxmarqueesize(MapHelper.getInt(e, "mistBoxRewadMaxMarqueeSize"));

config.setPetmissionrefreshconsume(MapHelper.getInt(e, "PetMissionRefreshConsume"));

config.setPetmissionfinishconsume(MapHelper.getInt(e, "PetMissionFinishConsume"));

config.setPetmissionmaxnumber(MapHelper.getInt(e, "PetMissionMaxNumber"));

config.setPetmissionaddconsume(MapHelper.getInts(e, "PetMissionAddConsume"));

config.setPetmissionrefreshvars(MapHelper.getInts(e, "petMissionRefreshVars"));

config.setEndlessspirerankingcount(MapHelper.getInt(e, "endlessSpireRankingCount"));

config.setEndlessspirerefreashinteval(MapHelper.getInt(e, "endlessSpireRefreashInteval"));

config.setApplylimit(MapHelper.getInt(e, "applyLimit"));

config.setEachrecommandcount(MapHelper.getInt(e, "eachRecommandCount"));

config.setSendfriendpointreawrd(MapHelper.getInts(e, "sendFriendPointReawrd"));

config.setMistattincrease(MapHelper.getInt(e, "MistAttIncrease"));

config.setResettime(MapHelper.getInt(e, "resetTime"));

config.setMainlineonhookrefreash(MapHelper.getInt(e, "mainLineOnHookRefreash"));

config.setMainlinerandominterval(MapHelper.getInt(e, "mainLineRandomInterval"));

config.setMainlinemaxonhooktime(MapHelper.getInt(e, "mainLineMaxOnHookTIme"));

config.setMainlinerankingcount(MapHelper.getInt(e, "mainLineRankingCount"));

config.setMainlinerankinginterval(MapHelper.getInt(e, "mainLineRankingInterval"));

config.setRecentpassedcount(MapHelper.getInt(e, "recentPassedCount"));

config.setDropmainlineinterval(MapHelper.getInt(e, "dropMainLineInterval"));

config.setPetrunemaxlvl(MapHelper.getInt(e, "PetRuneMaxLvl"));

config.setPetiintensifyrunemaxllvl(MapHelper.getInt(e, "PetIIntensifyRuneMaxlLvl"));

config.setRuneexpaddproportion(MapHelper.getInt(e, "RuneExpAddProportion"));

config.setPetbaginit(MapHelper.getInt(e, "petBagInit"));

config.setPetrunebaginit(MapHelper.getInt(e, "petRuneBagInit"));

config.setMineexploitscroll(MapHelper.getInt(e, "mineExploitScroll"));

config.setPettransferconsume(MapHelper.getIntArray(e, "petTransferConsume"));

config.setChooseminerewardtime(MapHelper.getInt(e, "ChooseMineRewardTime"));

config.setWaitminebattletime(MapHelper.getInt(e, "WaitMineBattleTime"));

config.setMineexploitscrollprice(MapHelper.getInts(e, "MineExploitScrollPrice"));

config.setMinelimitlevel(MapHelper.getInt(e, "MineLimitLevel"));

config.setMinegiftposnum(MapHelper.getInt(e, "MineGiftPosNum"));

config.setFriendhelppetusetime(MapHelper.getInt(e, "FriendHelpPetUseTime"));

config.setApplyfriendhelpinterval(MapHelper.getInt(e, "ApplyFriendHelpInterval"));

config.setNewbeepet(MapHelper.getInts(e, "newBeePet"));

config.setNewbeerewards(MapHelper.getIntArray(e, "newBeeRewards"));

config.setNewbeechoicepet(MapHelper.getInts(e, "newBeeChoicePet"));

config.setNewbeeonhookrewards(MapHelper.getIntArray(e, "newBeeOnHookRewards"));

config.setNewbeedrawcard(MapHelper.getInt(e, "newBeeDrawCard"));

config.setFightshowparameter(MapHelper.getInt(e, "fightShowParameter"));

config.setNewbeefightmakeid(MapHelper.getInts(e, "newbeeFightMakeId"));

config.setMsgmaxnum(MapHelper.getInt(e, "MsgMaxNum"));

config.setBondlevel(MapHelper.getInts(e, "BondLevel"));

config.setBosscalcx(MapHelper.getInt(e, "BossCalcX"));

config.setBosscalcn(MapHelper.getInt(e, "BossCalcN"));

config.setRebornpetlv(MapHelper.getInt(e, "rebornPetLv"));

config.setRebornpet(MapHelper.getInts(e, "rebornPet"));

config.setMistnewbiereward(MapHelper.getIntArray(e, "MistNewBieReward"));

config.setCumupaymaxstep(MapHelper.getInt(e, "CumuPayMaxStep"));

config.setRuneexpexchangerate(MapHelper.getIntArray(e, "runeExpExchangeRate"));

config.setMistentryticketprice(MapHelper.getInts(e, "MistEntryTicketPrice"));

config.setAdvancedfeatsprice(MapHelper.getInts(e, "advancedFeatsPrice"));

config.setFeatspricerechargeproductid(MapHelper.getInts(e, "featspricerechargeproductid"));

config.setBravechallengerebornprice(MapHelper.getInts(e, "braveChallengeRebornPrice"));

config.setFeatesrestdays(MapHelper.getInt(e, "featesRestDays"));

config.setRunquality(MapHelper.getInts(e, "runQuality"));

config.setRunmarqueeid(MapHelper.getInt(e, "runMarqueeId"));

config.setAdvancedwishingwellprice(MapHelper.getInts(e, "advancedWishingWellPrice"));

config.setActivityrankingdissize(MapHelper.getInt(e, "activityRankingDisSize"));

config.setGrowthfundprice(MapHelper.getInts(e, "growthFundPrice"));

config.setPrice_dianquan(MapHelper.getInt(e, "Price_Dianquan"));

config.setPrice_diamond(MapHelper.getInt(e, "Price_Diamond"));

config.setArenaunlockperson(MapHelper.getInt(e, "arenaUnlockPerson"));

config.setArenaskipviplv(MapHelper.getInt(e, "ArenaSkipVipLv"));

config.setBattlespeedupunlocknode(MapHelper.getInt(e, "BattleSpeedUpUnlockNode"));

config.setMaxonekeycomposepetcount(MapHelper.getInt(e, "MaxOneKeyComposePetCount"));

config.setDefaultskill(MapHelper.getIntArray(e, "defaultSkill"));

config.setDailytimelimitgiftnum(MapHelper.getInt(e, "dailyTimeLimitGiftNum"));

config.setVoidstoneunlocklvl(MapHelper.getInt(e, "voidStoneUnlockLvl"));

config.setGoldexdefaultnode(MapHelper.getInt(e, "goldExDefaultNode"));

config.setPatrolsweepconsume(MapHelper.getInts(e, "patrolSweepConsume"));

config.setPatrolgoogsnum(MapHelper.getInt(e, "patrolGoogsNum"));

config.setTreasuregreedconfig(MapHelper.getIntArray(e, "treasureGreedConfig"));

config.setResopeningtreasure(MapHelper.getInts(e, "ResOpeningTreasure"));

config.setFightwaittimeout(MapHelper.getInt(e, "FightWaitTimeOut"));

config.setBuyvipexpgetitem(MapHelper.getInts(e, "BuyVipExpGetItem"));

config.setBuyvipexpcost(MapHelper.getInts(e, "BuyVipExpCost"));

config.setVipexpdailybuylimit(MapHelper.getInt(e, "vipExpDailyBuyLimit"));

config.setWishwellsendmailtime(MapHelper.getInt(e, "wishWellSendMailTime"));

config.setWishwellneedlv(MapHelper.getInt(e, "wishWellNeedLv"));

config.setMistunlockpvplevel(MapHelper.getInt(e, "mistUnlockPvpLevel"));

config.setMistforcepvpstarttime(MapHelper.getIntArray(e, "mistForcePvpStartTime"));

config.setPetgembaginit(MapHelper.getInt(e, "petGemBagInit"));

config.setMistmarqueerewardtype(MapHelper.getIntArray(e, "MistMarqueeRewardType"));

config.setMistboxrarerewardmarqueeid(MapHelper.getInt(e, "MistBoxRareRewardMarqueeId"));

config.setMistpkchangeinterval(MapHelper.getInt(e, "mistPkChangeInterval"));

config.setMistrefiningstonespercent(MapHelper.getInt(e, "mistRefiningStonesPercent"));

config.setMistkillplayerupdateinterval(MapHelper.getInt(e, "mistKillPlayerUpdateInterval"));

config.setMistpvemonsterbuff(MapHelper.getIntArray(e, "mistPveMonsterBuff"));

config.setBosstowersweepneedstar(MapHelper.getInt(e, "bossTowerSweepNeedStar"));

config.setBattlecooldown(MapHelper.getInt(e, "battleCoolDown"));

config.setBlindboxreardweight(MapHelper.getInts(e, "blindBoxReardWeight"));

config.setBlindboxreardnum(MapHelper.getInts(e, "blindBoxReardNum"));

config.setRichmanrandomdicecost(MapHelper.getInts(e, "richManRandomDiceCost"));

config.setRichmanoptionaldicecost(MapHelper.getInts(e, "richManOptionalDiceCost"));

config.setRichmanbigrewardmarquee(MapHelper.getInt(e, "richManBigRewardMarquee"));

config.setFightplaybackversion(MapHelper.getStr(e, "FightPlaybackVersion"));

config.setPatrolfailrewardrate(MapHelper.getInt(e, "patrolFailRewardRate"));

config.setRichmandailyitem(MapHelper.getInts(e, "richManDailyItem"));

config.setMistmazeseasonmission(MapHelper.getInts(e, "MistMazeSeasonMission"));

config.setMissionlimit(MapHelper.getInts(e, "MissionLimit"));

config.setInscriptionopenlv(MapHelper.getInts(e, "inscriptionOpenLv"));

config.setPopupdailylimit(MapHelper.getInt(e, "popupDailyLimit"));

config.setMaxghostrecordcount(MapHelper.getInt(e, "MaxGhostRecordCount"));

config.setResmaxrecycledays(MapHelper.getInt(e, "resMaxRecycleDays"));

config.setResrecyclebaserewardrate(MapHelper.getInt(e, "resRecycleBaseRewardRate"));

config.setEvolveneedrarity(MapHelper.getInts(e, "evolveNeedRarity"));

config.setBuymiststaminaconsume(MapHelper.getInts(e, "BuyMistStaminaConsume"));

config.setBuymiststaminanum(MapHelper.getInt(e, "BuyMistStaminaNum"));

config.setBuymiststaminamaxtimes(MapHelper.getInt(e, "BuyMistStaminaMaxTimes"));

config.setInitmiststamina(MapHelper.getInt(e, "InitMistStamina"));

config.setMiststaminamaxnum(MapHelper.getInt(e, "MistStaminaMaxNum"));

config.setMistrecoverstamina(MapHelper.getInt(e, "MistRecoverStamina"));

config.setMistsummonnum(MapHelper.getInt(e, "MistSummonNum"));

config.setMistrecoverstaminainterval(MapHelper.getInt(e, "MistRecoverStaminaInterval"));

config.setCoupteamminlv(MapHelper.getInt(e, "coupTeamMinLv"));

config.setCoupteampetlvdif(MapHelper.getInt(e, "coupTeamPetLvDif"));

config.setBestpetcoretype(MapHelper.getInt(e, "bestPetCoreType"));

config.setTrainfresh(MapHelper.getInts(e, "trainFresh"));

config.setTrainconsume(MapHelper.getInts(e, "trainConsume"));

config.setTrainconsumebase(MapHelper.getInt(e, "trainConsumeBase"));

config.setTrainfree(MapHelper.getInt(e, "trainFree"));

config.setTrainhelpcount(MapHelper.getInt(e, "trainHelpCount"));

config.setMaxbarragesize(MapHelper.getInt(e, "maxBarrageSize"));

config.setTrainbufflimit(MapHelper.getInt(e, "trainbufflimit"));

config.setMagictime(MapHelper.getInt(e, "magicTime"));

config.setMagicbosstime(MapHelper.getInt(e, "magicBossTime"));

config.setMagicbuff(MapHelper.getInts(e, "magicBuff"));

config.setMagicrecordsize(MapHelper.getInt(e, "magicRecordSize"));

config.setMagicranksize(MapHelper.getInt(e, "magicRankSize"));

config.setOfferreward_fightper(MapHelper.getInt(e, "offerreward_fightper"));

config.setOfferreward_eachpage(MapHelper.getInt(e, "offerreward_eachpage"));

config.setOfferreward_getper(MapHelper.getInt(e, "offerreward_getper"));

config.setOfferreward_person(MapHelper.getInt(e, "offerreward_person"));

config.setBravechallengereset(MapHelper.getInts(e, "braveChallengeReset"));

config.setFeatidlist(MapHelper.getInts(e, "FeatIdList"));

config.setBosstower_buyitem(MapHelper.getInts(e, "bosstower_buyitem"));

config.setPatrol_join(MapHelper.getInt(e, "patrol_join"));

config.setPatrol_sweep(MapHelper.getInt(e, "patrol_sweep"));

config.setCrossarenascorelimit(MapHelper.getInt(e, "CrossArenaScoreLimit"));

config.setBosstower_buy(MapHelper.getInts(e, "bosstower_buy"));

config.setRoll_frag2pet_grade(MapHelper.getInt(e, "roll_frag2pet_grade"));

config.setEndlessfeatsdays(MapHelper.getInt(e, "endlessFeatsDays"));

config.setPatrolfeatsdays(MapHelper.getInt(e, "patrolFeatsDays"));

config.setOfferreward_daylimit(MapHelper.getInt(e, "offerreward_daylimit"));

config.setOffer_levellimit(MapHelper.getInts(e, "offer_levellimit"));

config.setMistmazeitems(MapHelper.getInts(e, "MistMazeItems"));

config.setMagicthron_pvplist(MapHelper.getInt(e, "magicthron_pvplist"));

config.setMagicthron_p1(MapHelper.getInt(e, "magicthron_p1"));

config.setMagicthron_param2(MapHelper.getInt(e, "magicthron_param2"));

config.setMagicthron_rankall(MapHelper.getInt(e, "magicthron_rankall"));

config.setMagicthron_rankone(MapHelper.getInt(e, "magicthron_rankone"));

config.setOfferreward_open(MapHelper.getInt(e, "offerreward_open"));

config.setCrossarenapvp_open(MapHelper.getInt(e, "crossarenapvp_open"));

config.setCross_weekbos_luck(MapHelper.getInts(e, "cross_weekbos_luck"));

config.setInitmistcarrypack(MapHelper.getIntArray(e, "initMistCarryPack"));

config.setDailyelitemonsterrewradtimes(MapHelper.getInt(e, "dailyEliteMonsterRewradTimes"));

config.setRarityresetminrarity(MapHelper.getInt(e, "rarityResetMinRarity"));

config.setAfterrarityresetrarity(MapHelper.getInt(e, "afterrarityResetRarity"));

config.setWorldlvrank(MapHelper.getInt(e, "worldLvRank"));

config.setMaxtrainuseitemnum(MapHelper.getInt(e, "maxTrainUseItemNum"));


_ix_id.put(config.getId(),config);



}
}
