package model.farmmine;

import cfg.*;
import common.GameConst;
import common.GlobalData;
import common.SyncExecuteFunction;
import common.tick.Tickable;
import datatool.StringHelper;
import db.entity.BaseEntity;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.farmmine.bean.*;
import model.farmmine.dbCache.farmmineCache;
import model.farmmine.entity.farmmineEntity;
import model.farmmine.util.FarmMineUtil;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import platform.logs.ReasonManager;
import protocol.Common;
import protocol.FarmMine.*;
import protocol.FarmMineDB;
import protocol.MessageId;
import protocol.RetCodeId;
import util.GameUtil;
import util.LogUtil;
import util.RandomUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 矿区农场管理器
 */
public class FarmMineManager implements Tickable {

    private static class LazyHolder {
        private static final FarmMineManager INSTANCE = new FarmMineManager();
    }

    private FarmMineManager() {
    }

    public static FarmMineManager getInstance() {
        return FarmMineManager.LazyHolder.INSTANCE;
    }

    // 存储活动公共数据
    private farmmineEntity pubEntity;
    // 公共数据
    private FarmMineDB.FarmMineZeroDB.Builder pubDB = FarmMineDB.FarmMineZeroDB.newBuilder();
    // 当前活动时间段
    private FarmMineTimeLoop currCycleTime = new FarmMineTimeLoop();
    // 是否是收获阶段
    private boolean isHarvest = false;
    /**
     * 缓存玩家偷取次数
     */
    private Map<String, Integer> playerStealMap = new ConcurrentHashMap<>();
    private boolean playerStealMapIsSave = false;
    /**
     * 缓存玩家竞价数据
     */
    private Map<String, FarmMinePlayerOffer> playerOfferMap = new ConcurrentHashMap<>();
    /**
     * 下一次刷帧时间,该系统不需要太高的精度，暂定3秒一次
     */
    private long nextOnTickTime = 0;

    /**
     * @return
     * 数据初始化
     */
    public boolean init() {
        // 初始化时间数据
        FarmMineConfigObject fmcfg = getFMCfg();
        if (null == fmcfg) {
            return false;
        }
        initCFGTime();
        if (null == currCycleTime) {
            LogUtil.error("矿场农场活动初始化生成活动时间数据异常");
            return false;
        }
        // 初始化DB数据
        initDBData();
        return true;
    }

    /**
     * 初始化DB数据
     */
    private void initDBData() {
        // 读取公共数据配置初始化历史价格等公共数据
        pubEntity = farmmineCache.getByIdx(FarmMineUtil.KEY_PUB);
        if (null == pubEntity) {
            pubEntity = createFarmmineEntity(FarmMineUtil.KEY_PUB);
        }
        if (null != pubEntity.getZerodata()) {
            try {
                pubDB = FarmMineDB.FarmMineZeroDB.parseFrom(pubEntity.getZerodata()).toBuilder();
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
        // 检查是否需要刷新数据
        checkResetMineDBdataAndRef();
    }

    /**
     * 检查是否需要刷新数据
     */
    private void checkResetMineDBdataAndRef() {
        // 判断是否需要初始化数据
        if (pubEntity.getJointime() != currCycleTime.getJointime()) {
            // 数据初始化
            resetMineData();
            // 不是同一周需要初始化数据重置当前竞猜信息
            pubEntity.setJointime(currCycleTime.getJointime());
            pubDB.setCurrLoop(0);
            pubDB.clearHisLoop();
            pubDB.clearPlayerSteal();
            Map<Integer, FarmMineDB.FarmMineHisDB> temp = new HashMap<>();
            for (Map.Entry<Integer, FarmMineDB.FarmMineHisDB> ent : pubDB.getHisDataMap().entrySet()) {
                FarmMineDB.FarmMineHisDB.Builder tempOne = ent.getValue().toBuilder();
                tempOne.setLastPriceNum(tempOne.getCurrPriceNum());
                tempOne.setLastPriceVue(tempOne.getCurrPriceVue());
                tempOne.setCurrPriceNum(0);
                tempOne.setCurrPriceVue(0);
                temp.put(ent.getKey(), tempOne.build());
            }
            pubDB.clearHisData();
            pubDB.putAllHisData(temp);
            farmmineCache.getInstance().clearPlayerKeyMap();
        } else {
            // 启动服务器不再收获阶段，则处理竞价数据
            FarmMineDB.FarmMineOfferTimeDB currData = pubDB.getHisLoopMap().get(pubDB.getCurrLoop());
            if (null != currData && currData.getState() == FarmMineUtil.STATE_OFFERPRICE) {
                for (int cid : currData.getMineIdsList()) {
                    farmmineEntity fment = farmmineCache.getByIdx(String.valueOf(cid));
                    if (null != fment) {
                        for (Map.Entry<String, Integer> tempEnt : fment.getAuctionInfoDB().getOfferPriceMap().entrySet()) {
                            FarmMinePlayerOffer podata = playerOfferMap.computeIfAbsent(tempEnt.getKey(), k-> new FarmMinePlayerOffer());
                            podata.setPlayerIdx(tempEnt.getKey());
                            podata.getOfferInfo().put(cid, tempEnt.getValue());
                            podata.getOfferTime().put(cid, fment.getAuctionInfoDB().getOfferTimeOrDefault(tempEnt.getKey(), System.currentTimeMillis()));
                        }
                        for (String curr : fment.getAuctionInfoDB().getFirstIdList()) {
                            if (playerOfferMap.containsKey(curr)) {
                                playerOfferMap.get(curr).setFirstIdx(cid);
                            }
                        }
                        fment.getAuctionInfoDB().getOfferPriceMap();
                    }
                }
                farmmineCache.getInstance().getAllNotPub();
            }
        }
        savePubDB();
    }

    /**
     * 初始化活动时间周期数据
     */
    private void initCFGTime() {
        String timeStr = getFMCfg().getParm();
        FarmMineTimeLoop timeLoop = new FarmMineTimeLoop();
        timeLoop.setStartTime(getWeekStartTimeSatFirstMs());
        timeLoop.setEndTime(getWeekEndTimeSatFirst());
        timeLoop.setStartAuctionTime(timeLoop.getStartTime());
        timeLoop.setEndAuctionTime(getWeekMonStartTimeSatFirst());
        timeLoop.setStartGiveTime(timeLoop.getEndAuctionTime());
        timeLoop.setEndTime(timeLoop.getEndTime());
        handleAuctionTime(timeLoop, timeStr);
        this.currCycleTime = timeLoop;
    }

    /**
     * 保存一次公共数据
     */
    private void savePubDB() {
        if (null != pubDB) {
            SyncExecuteFunction.executeConsumer(pubEntity, entityTemp -> {
                pubDB.clearPlayerSteal();
                pubDB.putAllPlayerSteal(playerStealMap);
                pubEntity.setZerodata(pubDB.build().toByteArray());
            });
        }
    }

    /**
     * @param idx
     * @return
     * 数据初始化，保证字段赋值
     */
    private farmmineEntity createFarmmineEntity(String idx) {
        farmmineEntity fment = new farmmineEntity();
        fment.setIdx(idx);
        fment.setJointime(0);
        fment.setExtids("");
        fment.setBaseidx(0);
        fment.setAuctionstart(0);
        fment.setAuctionend(0);
        fment.setOccplayerid("");
        fment.setPetid(0);
        fment.setPrice(0);
        fment.setTitleid(0);
        return fment;
    }

    /**
     * 重置矿地信息(活动周期重新开始,所有除历史数据都重置)
     */
    private void resetMineData() {
        playerOfferMap.clear();
        playerStealMap.clear();
        int totalMineNum = getFMCfg().getMainmax();
        int total = 0;
        for (FarmMineObject fm : FarmMine._ix_id.values()) {
            if (fm.getId() <= 0) {
                continue;
            }
            total += fm.getWight();
        }
        Map<Integer, Integer> mineNums = new HashMap<>();
        int totalMineNumTemp = totalMineNum;
        for (FarmMineObject fm : FarmMine._ix_id.values()) {
            if (fm.getId() <= 0) {
                continue;
            }
            int num = Math.round(fm.getWight()*1F/total * totalMineNum);
            num = num == 0 ? 1 : num;
            if (num <= totalMineNumTemp) {
                mineNums.put(fm.getId(), num);
            } else {
                if (totalMineNumTemp > 0) {
                    mineNums.put(fm.getId(), totalMineNumTemp);
                }
            }
            totalMineNumTemp -= num;
            if (totalMineNumTemp <= 0) {
                break;
            }
        }
        List<Integer> baseIdList = new ArrayList<>();
        for (Map.Entry<Integer, Integer> ent : mineNums.entrySet()) {
            for (int j=0;j<ent.getValue();j++) {
                baseIdList.add(ent.getKey());
            }
        }
        if (baseIdList.size() > totalMineNum) {
            baseIdList = baseIdList.subList(0, totalMineNum);
        }
        Collections.shuffle(baseIdList);
        int mineIdx = 1;
        for (int baseId : baseIdList) {
            FarmMineObject fm = FarmMine._ix_id.get(baseId);
            farmmineEntity fment = farmmineCache.getByIdx(String.valueOf(mineIdx));
            if (null == fment) {
                fment = createFarmmineEntity(String.valueOf(mineIdx));
            }
            farmmineEntity temp = randomMineAwardType(fm);
            fment.setBaseidx(temp.getBaseidx());
            fment.setExtids(temp.getExtids());
            fment.setPetid(temp.getPetid());
            fment.setOccplayerid("");
            fment.setAuctionInfoDB(FarmMineDB.FarmMineOfferDB.newBuilder());
            fment.setPlayerdataDB(FarmMineDB.FarmMinePDB.newBuilder());
            fment.setJointime(currCycleTime.getJointime());
            fment.setAuctionstart(0);
            fment.setAuctionend(0);
            fment.setPrice(0);
            // 数据更新入库
            farmmineCache.getInstance().flush(fment);
            mineIdx++;
        }
    }

    /**
     * @param fm
     * @return
     * 根据类型随机产出，初始化数据
     */
    private farmmineEntity randomMineAwardType(FarmMineObject fm) {
        farmmineEntity fment = createFarmmineEntity(String.valueOf(0));
        // 随机基础产出
        FarmMineAwardObject fmaoRandom = randomById(fm.getBaseaward());
        if (null != fmaoRandom) {
            fment.setBaseidx(fmaoRandom.getId());
        }
        // 随机概率产出
        String extStr = "";
        for (int i=0; i<fm.getExtnum(); i++) {
            FarmMineAwardObject fmaoExt = randomById(fm.getExtaward());
            if (null != fmaoExt) {
                extStr = extStr + fmaoExt.getId() + ",";
            }
        }
        fment.setExtids(extStr);
        // 随机宠物
        FarmMineAwardObject fmaoPet = randomById(fm.getPetadd());
        if (null != fmaoPet) {
            fment.setPetid(fmaoPet.getId());
        }
        return fment;
    }

    /**
     * @param rule
     * 处理活动竞价时间
     */
    private void handleAuctionTime(FarmMineTimeLoop timeLoop, String rule) {
        rule = rule.substring(1, rule.length() - 1);// *][*][w4][20:30-21:00
        String[] t = rule.split("\\]\\[");// *,*,w4,20:30-21:00
        if (t.length != 4) {
            LogUtil.printStackTrace(new IllegalStateException());
            return;
        }
        // 默认每年每月都开启，暂时不处理逻辑，后期需要在添加
        // 处理周
        String weekStr = t[2];
        List<Integer> li = new LinkedList<Integer>();
        li.add(0);
        li.add(1);
//        if (weekStr.startsWith("w") || weekStr.startsWith("W")) {
//            weekStr = weekStr.substring(1, weekStr.length());
//            List<Integer> temp = toListInt(weekStr);
//            if (temp.size() > 0) {
//                li.addAll(temp);
//            }
//        }
        // 处理时间
        Map<Long, FarmMineAuctionTime> auctionTime = new TreeMap<>();
        for (String str : t[3].split(",")) {
            if (str.indexOf("-") != -1) {
                try {
                    String[] seg = str.split("-");
                    String[] start = seg[0].split(":");
                    String[] end = seg[1].split(":");


                    for (int weekDay : li) {
                        Calendar calendar = getWeekStartTimeSatFirst();
                        calendar.set(Calendar.DAY_OF_WEEK, weekDay);
                        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(start[0]));
                        calendar.set(Calendar.MINUTE, Integer.parseInt(start[1]));
                        calendar.set(Calendar.SECOND, Integer.parseInt(start[2]));
                        calendar.set(Calendar.MILLISECOND, 0);

                        FarmMineAuctionTime fmat = new FarmMineAuctionTime();
                        fmat.setStartTime(calendar.getTimeInMillis());

                        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(end[0]));
                        calendar.set(Calendar.MINUTE, Integer.parseInt(end[1]));
                        calendar.set(Calendar.SECOND, Integer.parseInt(end[2]));
                        long endOfferTime = calendar.getTimeInMillis() - 1800000L;
                        fmat.setEndTimeAndOfferTime(calendar.getTimeInMillis(), endOfferTime);
                        auctionTime.put(fmat.getStartTime(), fmat);
                    }
                } catch (Exception e) {
                    LogUtil.printStackTrace(new IllegalStateException());
                    return;
                }
            }
        }
        Map<Integer, FarmMineAuctionTime> auctionTimeRes = new TreeMap<>();
        int loop = 1;
        for (FarmMineAuctionTime ent : auctionTime.values()) {
            ent.setLoop(loop);
            auctionTimeRes.put(loop, ent);
            loop+=1;
        }
        timeLoop.setAuctionTime(auctionTimeRes);
    }

    @Override
    public void onTick() {
        if (currCycleTime.getAuctionTime().isEmpty()) {
            return;
        }
        long currTime = System.currentTimeMillis();
        if (currTime < nextOnTickTime) {
            return;
        }
        nextOnTickTime = currTime + 3000L;
        checkChangeCycleTime();
        if (currCycleTime.isAuction(currTime)) {
            if (isHarvest) {
                isHarvest = false;
                refState();
            }
            for (FarmMineAuctionTime ent : currCycleTime.getAuctionTime().values()) {
                ent.backState(currTime);
                try {
                    if (ent.isNeedOffer()) {
                        ent.stateGive();
                        openOfferStage(ent);
                    } else if (ent.isNeedView()) {
                        ent.stateGive();
                        openViewStage(ent);
                    } else if (ent.isNeedClose()) {
                        ent.stateGive();
                        closeStage(ent);
                    }
                } catch (CloneNotSupportedException e) {
                    LogUtil.printStackTrace(e);
                }
            }
        } else if (currCycleTime.isGive(currTime)) {
            if (!isHarvest) {
                isHarvest = true;
                refState();
            }
        }
    }

    /**
     * 检查可以偷取的列表
     */
    public void checkCanStealsInfo() {

    }

    /**
     * 检查是否刷新周期数据
     */
    public void checkChangeCycleTime() {
        long startTime = getWeekStartTimeSatFirstMs();
        if (currCycleTime.isNeedCheck() && currCycleTime.getStartTime() != startTime) {
            // 活动周期结束刷新新一轮数据
            initCFGTime();
            checkResetMineDBdataAndRef();
        }
    }

    /**
     * 阶段切换刷新数据
     */
    public void refState() {
        SC_FarmMineRefState.Builder msg = SC_FarmMineRefState.newBuilder();
        msg.setState(isHarvest ? 2 : 1);
        GlobalData.getInstance().sendMsgToAllOnlinePlayer(MessageId.MsgIdEnum.SC_FarmMineRefState, msg);
    }

    /**
     * @param ent
     * @throws CloneNotSupportedException
     * 开启一轮竞价活动
     */
    public void openOfferStage(FarmMineAuctionTime ent) throws CloneNotSupportedException {
        if (pubDB.containsHisLoop(ent.getLoop())) {
            return;
        }
        // 查询需要
        List<Integer> ids = new ArrayList<>();
        Map<String, BaseEntity> dbdata = farmmineCache.getInstance().getAllNotPub();
        for (BaseEntity fm : dbdata.values()) {
            farmmineEntity fe = (farmmineEntity) fm;
            if (fe.getAuctionstart() < fe.getJointime()) {
                ids.add(Integer.valueOf(fe.getIdx()));
            }
        }
        int hopeLoopMax = currCycleTime.getAuctionTime().size();
        int loopMineNumMax = getFMCfg().getMainmax() / hopeLoopMax;
        // 判断下剩余的数量是否太多
        int sy = ids.size() - ((hopeLoopMax - ent.getLoop() + 1) * loopMineNumMax);
        if (sy > 0) {
            loopMineNumMax = loopMineNumMax + sy/(hopeLoopMax - ent.getLoop() + 1);
        }
        Collections.shuffle(ids);
        // 更新数据
        List<Integer> needIds = new ArrayList<>();
        if (ids.size() < loopMineNumMax) {
            needIds.addAll(ids);
        } else {
            needIds.addAll(ids.subList(0, loopMineNumMax));
        }
        FarmMineDB.FarmMineOfferTimeDB newData = createOfferTime(ent, needIds);
        pubDB.setCurrLoop(ent.getLoop());
        pubDB.putHisLoop(ent.getLoop(), newData);
        for (int idx : needIds) {
            farmmineEntity fe = farmmineCache.getByIdx(String.valueOf(idx));
            if (null != fe) {
                fe.setAuctionstart(ent.getStartTime());
                fe.setAuctionend(ent.getEndTimeoffer());
                fe.setAuctionInfoDB(FarmMineDB.FarmMineOfferDB.newBuilder());
                farmmineCache.getInstance().flush(fe);
            }
        }
        // 新一轮的竞价开始，清空竞价数据
        playerOfferMap.clear();
        savePubDB();
        refAuction();
    }

    private FarmMineDB.FarmMineOfferTimeDB createOfferTime(FarmMineAuctionTime ent, List<Integer> ids) {
        FarmMineDB.FarmMineOfferTimeDB.Builder msg = FarmMineDB.FarmMineOfferTimeDB.newBuilder();
        msg.setLoop(ent.getLoop());
        msg.setStartTime(ent.getStartTime());
        msg.setStartTimeView(ent.getStartTimeView());
        msg.setState(ent.getState());
        msg.setEndTime(ent.getEndTime());
        msg.setEndTimeView(ent.getEndTimeView());
        msg.setEndTimeoffer(ent.getEndTimeoffer());
        msg.addAllMineIds(ids);
        return msg.build();
    }

    /**
     * @param ent
     * 开启展示阶段，直接竞拍阶段
     */
    public void openViewStage(FarmMineAuctionTime ent) {
        if (!pubDB.containsHisLoop(ent.getLoop())) {
            LogUtil.error("----------------------矿区农场活动开启展示时，没有查询到伦茨数据----------------------" + ent.getLoop());
            return;
        }
        // 处理竞拍结算
        FarmMineDB.FarmMineOfferTimeDB tempPubDB = pubDB.getHisLoopMap().get(ent.getLoop());
        List<Integer> handleIds = tempPubDB.getMineIdsList();
        pubDB.putHisLoop(ent.getLoop(), tempPubDB.toBuilder().setState(ent.getState()).build());
        // 处理竞拍结算
        Map<String, farmmineEntity> tempMap = new ConcurrentHashMap<>();
        for (int idxId : handleIds) {
            farmmineEntity fe = farmmineCache.getByIdx(String.valueOf(idxId));
            if (null == fe) {
                continue;
            }
            tempMap.put(fe.getIdx(), fe);
        }
        // 将每一个矿地的竞拍信息进行归纳排序
        Map<String, List<FarmMineOfferBean>> allOffer = new HashMap<>();
        for (farmmineEntity feEnt : tempMap.values()) {
            List<FarmMineOfferBean> mineOfferList = new LinkedList<>();
            for (Map.Entry<String, Integer> offerInfo : feEnt.getAuctionInfoDB().getOfferPriceMap().entrySet()) {
                FarmMineOfferBean fmob = new FarmMineOfferBean();
                fmob.setIdx(Integer.valueOf(feEnt.getIdx()));
                fmob.setBaseId(feEnt.getBaseidx());
                fmob.setPlayerIdx(offerInfo.getKey());
                fmob.setPrice(offerInfo.getValue());
                fmob.setOfferNum(feEnt.getAuctionInfoDB().getOfferPriceCount());
                fmob.setTime(feEnt.getAuctionInfoDB().getOfferTimeOrDefault(offerInfo.getKey(), System.currentTimeMillis()));
                if (feEnt.getAuctionInfoDB().getFirstIdList().contains(offerInfo.getKey())) {
                    fmob.setFirst(true);
                }
                mineOfferList.add(fmob);
            }
            // 对出价人排序,出价高，出价时间靠前的拍前面
            Collections.sort(mineOfferList, new Comparator<FarmMineOfferBean>() {
                public int compare(FarmMineOfferBean arg0, FarmMineOfferBean arg1) {
                    if (arg0.getPrice() > arg1.getPrice()) {
                        return -1;
                    } else if (arg0.getPrice() == arg1.getPrice()) {
                        if (arg0.getTime() < arg1.getTime()) {
                            return -1;
                        } else {
                            return 1;
                        }
                    } else {
                        return 1;
                    }
                }
            });
            allOffer.put(feEnt.getIdx(), mineOfferList);
        }
        // 全部统计后，开始分配矿地
        Map<String, Integer> choosePlayers = new HashMap<>();
        Map<String, FarmMineOfferBean> offerResult = new HashMap<>();
        // 分配
        choosePlayer(offerResult, choosePlayers, allOffer);

        // 计算称号
        Map<Integer, Integer> titles = chooseTitle(offerResult);
        pubDB.clearTitle();
        pubDB.putAllTitle(titles);
        // 计算历史数据
        Map<Integer, List<Integer>> hisData = new HashMap<>();
        for (Map.Entry<String, FarmMineOfferBean> result : offerResult.entrySet()) {
            if (result.getValue().getPrice() <= 0) {
                continue;
            }
            hisData.computeIfAbsent(result.getValue().getBaseId(), k->new ArrayList<Integer>()).add(result.getValue().getPrice());
        }
        for (Map.Entry<Integer, List<Integer>> his : hisData.entrySet()) {
            FarmMineDB.FarmMineHisDB.Builder tempHisDB = pubDB.getHisDataMap().getOrDefault(his.getKey(), FarmMineDB.FarmMineHisDB.newBuilder().setTypeId(his.getKey()).build()).toBuilder();
            // 本次平均值=
            int total = 0;
            for (int t : his.getValue()) {
                total += t;
            }
            double avgh = Math.round((tempHisDB.getHisPriceVue() * 1D * tempHisDB.getHisPriceNum() + total)/(tempHisDB.getHisPriceNum() + his.getValue().size()));
            tempHisDB.setHisPriceNum(tempHisDB.getHisPriceNum() + his.getValue().size());
            tempHisDB.setHisPriceVue(new Double(avgh).intValue());
            double avgl = Math.round((tempHisDB.getCurrPriceVue() * 1D * tempHisDB.getCurrPriceNum() + total)/(tempHisDB.getCurrPriceNum() + his.getValue().size()));
            tempHisDB.setCurrPriceNum(tempHisDB.getCurrPriceNum() + his.getValue().size());
            tempHisDB.setCurrPriceVue(new Double(avgl).intValue());
            pubDB.putHisData(his.getKey(), tempHisDB.build());
        }
        savePubDB();

        // 处理结果到数据库保存
        for (Map.Entry<String, FarmMineOfferBean> result : offerResult.entrySet()) {
            FarmMineOfferBean tempValue = result.getValue();
            farmmineEntity fe = farmmineCache.getByIdx(result.getKey());
            if (null == fe) {
                continue;
            }
            if (tempValue.getIdx() == 0) {
                continue;
            }
            // 判断玩家是否有足够消耗
            ReasonManager.Reason reason = ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_FARMMINE_OFFER);
            Common.Consume consumet = ConsumeUtil.parseConsume(getFMCfg().getOfferpricitem()[0], getFMCfg().getOfferpricitem()[1], result.getValue().getPrice());
            if (!ConsumeManager.getInstance().consumeMaterial(tempValue.getPlayerIdx(), consumet, reason)) {
                continue;
            }
            // 保存公共数据，保存玩家数据
            fe.setOccplayerid(result.getValue().getPlayerIdx());
            fe.setPrice(result.getValue().getPrice());
            fe.getPlayerdataDB().setIdx(Integer.valueOf(result.getKey()));
            fe.getPlayerdataDB().setLastHarvestTime(currCycleTime.getStartGiveTime());
            fe.getPlayerdataDB().setLastHarvestTimeXY(currCycleTime.getStartGiveTime());
            FarmMineAwardObject fmaoCfg = FarmMineAward.getById(fe.getPetid());
            if (null != fmaoCfg) {
                petEntity cache = petCache.getInstance().getEntityByPlayer(result.getValue().getPlayerIdx());
                if (cache != null && cache.getPetOwnedCount(fmaoCfg.getPetid()) > 0) {
                    fe.getPlayerdataDB().setIsAddPet(1);
                }
            }
            // 结算完成清空竞价数据
            fe.setAuctionInfoDB(FarmMineDB.FarmMineOfferDB.newBuilder());
            farmmineCache.getInstance().flush(fe);
        }
        refAuction();
    }

    private Map<Integer, Integer> chooseTitle(Map<String, FarmMineOfferBean> offerResult) {
        Map<Integer, Integer> back = new HashMap<>();
        int baseIdPriceMax1 = 0;
        int baseIdPriceMin1 = Integer.MAX_VALUE;
        int baseIdPeopleMax1 = 0;
        int baseIdInsMax1 = 0;
        // 先选出最高的
        Map<Integer, Integer> basePeople = new HashMap<>();
        for (Map.Entry<String, FarmMineOfferBean> result : offerResult.entrySet()) {
            if (result.getValue().getPrice() <= 0) {
                continue;
            }
            int pnum = basePeople.getOrDefault(result.getValue().getBaseId(), 0) + result.getValue().getOfferNum();
            basePeople.put(result.getValue().getBaseId(), pnum);
            if (result.getValue().getPrice() > baseIdPriceMax1) {
                baseIdPriceMax1 = result.getValue().getPrice();
                back.put(getFMCfg().getTitle()[0], result.getValue().getBaseId());
            }
        }
        // 选出最低的
        for (Map.Entry<String, FarmMineOfferBean> result : offerResult.entrySet()) {
            if (result.getValue().getPrice() <= 0) {
                continue;
            }
            if (result.getValue().getPrice() < baseIdPriceMin1) {
                if (!back.containsValue(result.getValue().getBaseId())) {
                    baseIdPriceMin1 = result.getValue().getPrice();
                    back.put(getFMCfg().getTitle()[1], result.getValue().getBaseId());
                }
            }
        }
        // 选出差价最大的
        for (Map.Entry<String, FarmMineOfferBean> result : offerResult.entrySet()) {
            if (result.getValue().getPrice() <= 0) {
                continue;
            }
            int last = pubDB.getHisDataMap().getOrDefault(result.getValue().getBaseId(), FarmMineDB.FarmMineHisDB.newBuilder().build()).getLastPriceVue();
            if (result.getValue().getPrice() - last > baseIdInsMax1) {
                if (!back.containsValue(result.getValue().getBaseId())) {
                    baseIdInsMax1 = result.getValue().getPrice();
                    back.put(getFMCfg().getTitle()[2], result.getValue().getBaseId());
                }
            }
        }
        // 选出人最多的
        for (Map.Entry<Integer, Integer> ent : basePeople.entrySet()) {
            if (ent.getValue() > baseIdPeopleMax1) {
                baseIdPeopleMax1 = ent.getValue();
                if (!back.containsValue(ent.getValue())) {
                    back.put(getFMCfg().getTitle()[2], ent.getKey());
                }
            }
        }
        Map<Integer, Integer> backtemp = new HashMap<>();
        for (Map.Entry<Integer, Integer> ent : back.entrySet()) {
            backtemp.put(ent.getValue(), ent.getKey());
        }
        return backtemp;
    }

    /**
     * @param offerResult
     * @param choosePlayers
     * @param needChoose
     * 竞价后判断竞标结果
     */
    private void choosePlayer(Map<String, FarmMineOfferBean> offerResult, Map<String, Integer> choosePlayers, Map<String, List<FarmMineOfferBean>> needChoose) {
        // 同一个玩家的竞价数据
        Map<String, List<FarmMineOfferBean>> repeatPlayer = new HashMap<>();
        // 第一遍拿出每轮的第一名来竞拍
        for (Map.Entry<String, List<FarmMineOfferBean>> sortData : needChoose.entrySet()) {
            // 该矿是否已经有结果
            if (offerResult.containsKey(sortData.getKey())) {
                continue;
            }
            // 获取该矿地第一个未获得矿地的玩家
            FarmMineOfferBean choose = null;
            for (FarmMineOfferBean need : sortData.getValue()) {
                if (!choosePlayers.containsKey(need.getPlayerIdx())) {
                    choose = need;
                    break;
                }
            }
            if (null != choose) {
                // 将每一个第一名数据分组存储
                List<FarmMineOfferBean> playerFirst = repeatPlayer.computeIfAbsent(choose.getPlayerIdx(), k -> new ArrayList<FarmMineOfferBean>());
                playerFirst.add(choose);
            } else {
                // 流拍,直接将结果存入最终结果集合
                offerResult.put(sortData.getKey(), new FarmMineOfferBean());
            }
        }
        // 第二次根据选出的第一名，筛除重复玩家,选出数据
        for (Map.Entry<String, List<FarmMineOfferBean>> startChoose : repeatPlayer.entrySet()) {
            FarmMineOfferBean choose = startChoose.getValue().get(0);
            if (startChoose.getValue().size() > 1) {
                // 处理同个玩家多个第一的问题
                for (FarmMineOfferBean fmob : startChoose.getValue()) {
                    if (fmob.isFirst()) {
                        // 是否标记优先级
                        choose = fmob;
                        break;
                    } else {
                        if (choose.getPrice() < fmob.getPrice()) {
                            // 不是优先级则给予出价最高的
                            choose = fmob;
                        } else if (choose.getPrice() == fmob.getPrice()) {
                            // 出价相同则，随机处理
                            if (RandomUtil.getRandomValue(0, 100) < 50) {
                                choose = fmob;
                            }
                        }
                    }
                }
            }
            choosePlayers.put(choose.getPlayerIdx(), choose.getIdx());
            offerResult.put(String.valueOf(choose.getIdx()), choose);
        }
        // 筛选一次过后，没有每个都选完则，继续此流程
        if (offerResult.size() != needChoose.size()) {
            choosePlayer(offerResult, choosePlayers, needChoose);
        }
    }

    /**
     * @param ent
     * 竞拍结束
     */
    public void closeStage(FarmMineAuctionTime ent) {
        if (!pubDB.containsHisLoop(ent.getLoop())) {
            LogUtil.error("----------------------矿区农场活动竞拍关闭时，没有查询到伦茨数据----------------------" + ent.getLoop());
            return;
        }
        // 处理竞拍结算
        FarmMineDB.FarmMineOfferTimeDB tempPubDB = pubDB.getHisLoopMap().get(ent.getLoop());
        pubDB.putHisLoop(ent.getLoop(), tempPubDB.toBuilder().setState(ent.getState()).build());
        savePubDB();
    }

    /**
     * @return
     * 获取可以偷取的列表
     */
    public List<Integer> getStealsList(String playerIdx) {
        List<Integer> temp = new ArrayList<>();
        if (!isHarvest) {
            return temp;
        }
        long ins = getFMCfg().getStealstimecan() * FarmMineUtil.HOURTOSECOND;
        long nowTime = System.currentTimeMillis();
        for (BaseEntity ent : farmmineCache.getInstance().getAllNotPub().values()) {
            farmmineEntity entity = (farmmineEntity) ent;
            if (entity.getPlayerdataDB().getLastHarvestTime() <= 0) {
                continue;
            }
            if (Objects.equals(entity.getOccplayerid(), playerIdx)) {
                continue;
            }
            if (nowTime - entity.getPlayerdataDB().getLastHarvestTime() > ins) {
                temp.add(Integer.valueOf(entity.getIdx()));
            }
        }
        return temp;
    }

    public void sendMyInfo(String playerIdx) {
        SC_FarmMineMyInfo.Builder msg = SC_FarmMineMyInfo.newBuilder();
        farmmineEntity entity = farmmineCache.getInstance().getByPlayerIdx(playerIdx);
        msg.setMyMineInfo(createMyMineInfo(playerIdx, entity));
        GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineMyInfo_VALUE, msg);
    }

    public void sendMyInfo(String playerIdx, farmmineEntity entity) {
        SC_FarmMineMyInfo.Builder msg = SC_FarmMineMyInfo.newBuilder();
        msg.setMyMineInfo(createMyMineInfo(playerIdx, entity));
        GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineMyInfo_VALUE, msg);
    }

    /**
     * @param playerIdx
     * 推送我的竞拍信息
     */
    public void sendMyAuction(String playerIdx) {
        SC_FarmMineMyAuction.Builder msg = SC_FarmMineMyAuction.newBuilder();
        FarmMinePlayerOffer fmpo = playerOfferMap.get(playerIdx);
        if (null != fmpo) {
            msg.setInfo(createMyAuction(fmpo));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineMyAuction_VALUE, msg);
        }
    }

    /**
     * @param playerIdx
     * 下发主面板信息
     */
    public void sendMainPanel(String playerIdx) {
        SC_FarmMineMainPanel.Builder msg = SC_FarmMineMainPanel.newBuilder();
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success);
        if (farmmineCache.getInstance().hasData()) {
            // 没有数据，活动没开启
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineMainPanel_VALUE, msg);
            return;
        }
        msg.setState(isHarvest ? 2 : 1);
        msg.setMineInfo(createAllMineInfo());
        msg.addAllSteals(getStealsList(playerIdx));// 可以被偷取的列表
        farmmineEntity entity = farmmineCache.getInstance().getByPlayerIdx(playerIdx);
        msg.setMyMineInfo(createMyMineInfo(playerIdx, entity));
        if (!isHarvest) {
            FarmMinePlayerOffer fmpo = playerOfferMap.get(playerIdx);
            if (null != fmpo) {
                msg.setMyAuction(createMyAuction(fmpo));
            } else {
                msg.setMyAuction(FarmMineMyAuction.newBuilder());
            }
            msg.setAuction(createAuction());
        }
        msg.setResult(retCode);
        GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineMainPanel_VALUE, msg);
    }

    /**
     * 刷新竞价信息
     */
    public void refAuction() {
        SC_FarmMineAuction.Builder msg = SC_FarmMineAuction.newBuilder();
        msg.setInfo(createAuction());
        GlobalData.getInstance().sendMsgToAllOnlinePlayer(MessageId.MsgIdEnum.SC_FarmMineAuction, msg);
    }

    /**
     * @param ids
     * 推送可偷取列表
     */
    public void sendSteals(String playerIdx) {
        SC_FarmMineSteals.Builder msg = SC_FarmMineSteals.newBuilder();
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success);
        msg.setResult(retCode);
        msg.addAllSteals(getStealsList(playerIdx));
        GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineSteals_VALUE, msg);
    }

    /**
     * @param playerIdx
     * @param idx
     * 刷新单个矿地信息
     */
    public void refMineOneInfo(String playerIdx, int idx) {
        SC_FarmMineRefOneInfo.Builder msg = SC_FarmMineRefOneInfo.newBuilder();
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success);
        msg.setResult(retCode);
        farmmineEntity entity = farmmineCache.getByIdx(String.valueOf(idx));
        if (null == entity) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineRefOneInfo_VALUE, msg);
            return;
        }
        // 收获阶段下发累计的固定奖励
        if (isHarvest && Objects.equals(entity.getOccplayerid(), playerIdx)) {
            FarmMineDrop fmd = computeFruit(entity, true);
            msg.setReward(fmd.getBase());
        }
        msg.setInfo(createOneMineInfo(entity));
        GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineRefOneInfo_VALUE, msg);
    }

    /**
     * @param playerIdx
     * @param consumeList
     * 使用锄头加成
     */
    public void useItem(String playerIdx, List<Common.Consume> consumeList) {
        SC_FarmMineUseItem.Builder msg = SC_FarmMineUseItem.newBuilder();
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success);
        msg.setResult(retCode);
        if (!isHarvest) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineUseItem_VALUE, msg);
            return;
        }
        farmmineEntity entity = farmmineCache.getInstance().getByPlayerIdx(playerIdx);
        if (!checkIsMyMine(entity)) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_FARMMINE_NOTMINE);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineUseItem_VALUE, msg);
            return;
        }
        // 根据矿地查找数据
        FarmMineConfigObject fmccfg = getFMCfg();
        SyncExecuteFunction.executeConsumer(entity, entityTemp -> {
            // 检查道具
            int allAddTime = 0;
            for (Common.Consume ent : consumeList) {
                if (ent.getCount() < 1) {
                    continue;
                }
                for (int[] cfgent : fmccfg.getSpeedadd()) {
                    if (ent.getRewardType().getNumber() == cfgent[0] || ent.getId() == cfgent[1]) {
                        allAddTime += cfgent[3];
                        break;
                    }
                }
            }
            if (allAddTime <= 0) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
                msg.setResult(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineUseItem_VALUE, msg);
                return;
            }
            // 合并历史加成数据检查失效长度
            long stratTime = System.currentTimeMillis();
            long endTime = allAddTime * FarmMineUtil.HOURTOSECOND;
            if (System.currentTimeMillis() > entityTemp.getPlayerdataDB().getAddspeedEndTime()) {
                endTime = System.currentTimeMillis() + endTime;
            } else {
                stratTime = entityTemp.getPlayerdataDB().getAddspeedStartTime();
                endTime = entityTemp.getPlayerdataDB().getAddspeedEndTime() + endTime;
            }
            if (endTime > currCycleTime.getEndGiveTime()) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_FARMMINE_USEITEMTIMEMAX);
                msg.setResult(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineUseItem_VALUE, msg);
                return;
            }
            // 判断消耗
            ReasonManager.Reason reason = ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_FARMMINE_USEITEM);
            if (!ConsumeManager.getInstance().consumeMaterialByList(playerIdx, consumeList, reason)) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_Player_CurrencysNotEnought);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineUseItem_VALUE, msg);
                return;
            }
            entityTemp.getPlayerdataDB().setAddspeedStartTime(stratTime);
            entityTemp.getPlayerdataDB().setAddspeedEndTime(endTime);
            sendMyInfo(playerIdx, entityTemp);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineUseItem_VALUE, msg);
        });
    }

    /**
     * @param playerIdx
     * @param idx
     * 收获一次
     */
    public void harvest(String playerIdx, int idx) {
        SC_FarmMineHarvest.Builder msg = SC_FarmMineHarvest.newBuilder();
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success);
        msg.setResult(retCode);
        if (!isHarvest) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineHarvest_VALUE, msg);
            return;
        }
        farmmineEntity entity = farmmineCache.getInstance().getByPlayerIdx(playerIdx);
        if (!checkIsMyMine(entity) || Integer.valueOf(entity.getIdx()) != idx) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_FARMMINE_NOTMINE);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineHarvest_VALUE, msg);
            return;
        }
        // 间隔N分钟领一次奖励
        long insHarvsetTime = getFMCfg().getHarvestinstime() * 60000L;
        if (System.currentTimeMillis() - entity.getPlayerdataDB().getLastHarvestTime() < insHarvsetTime) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_FARMMINE_HARVESTQK);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineHarvest_VALUE, msg);
            return;
        }
        SyncExecuteFunction.executeConsumer(entity, cacheTemp -> {
            FarmMineDrop fmd = computeFruit(cacheTemp, false);
            if (fmd.getBase().getCount() > 0) {
                // 有效收获
                List<Common.Reward> gailv = new ArrayList<>();
                gailv.add(fmd.getBase());
                if (fmd.isGailv()) {
                    gailv.addAll(fmd.getGailv());
                    cacheTemp.getPlayerdataDB().setLastHarvestTimeXY(fmd.getGailvTime());
                }
                cacheTemp.getPlayerdataDB().clearStealInfoLast();
                cacheTemp.getPlayerdataDB().setMyTotalTime(cacheTemp.getPlayerdataDB().getMyTotalTime() + fmd.getLeijiTime());
                cacheTemp.getPlayerdataDB().setLastHarvestTime(fmd.getBaseTime());
                cacheTemp.getPlayerdataDB().setLastHarvestTimeXY(fmd.getGailvTime());
                ReasonManager.Reason reason = ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_FARMMINE_FRUIT);
                RewardManager.getInstance().doRewardByList(playerIdx, gailv, reason, true);
                msg.addAllReward(gailv);
                sendMyInfo(playerIdx, cacheTemp);
            }
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineHarvest_VALUE, msg);
        });
    }

    /**
     * @param entity
     * 计算产量
     */
    public FarmMineDrop computeFruit(farmmineEntity entity, boolean isSteal) {
        FarmMineDrop all = null;
        long currTime = System.currentTimeMillis();
        long lastTime = entity.getPlayerdataDB().getLastHarvestTime();
        // 获取基础产出物
        FarmMineAwardObject fmaoCfg = FarmMineAward.getById(entity.getBaseidx());
        if (null == fmaoCfg) {
            return all;
        }
        all = new FarmMineDrop();
        // 最大累计奖励时间
        long insTimeMax = getFMCfg().getHarvesttimemax() * FarmMineUtil.HOURTOSECOND;
        // 间隔N分钟领一次奖励
        long insHarvsetTime = getFMCfg().getHarvestinstime() * 60000L;
        // 计算本次产出时间
        long insTime = currTime - lastTime;
        // 基础奖励计数次数
        long baseNum = 0;
        if (insTime > insTimeMax) {
            baseNum = insTimeMax / insHarvsetTime;
        } else {
            baseNum = insTime / insHarvsetTime;
        }
        // 计算基础产出 = 每小时产量/一小时产N次*N次
        float base = fmaoCfg.getRewardvue() *1F / 60F * getFMCfg().getHarvestinstime() * baseNum;
        float addPet = 0F;
        // 计算宠物加成
        if (entity.getPlayerdataDB().getIsAddPet() > 0) {
            FarmMineAwardObject fmaoCfgPet = FarmMineAward.getById(entity.getPetid());
            if (null != fmaoCfgPet) {
                float ratePet = fmaoCfgPet.getPetadd() / 10000F;
                addPet = base * ratePet;
            }
        }
        // 计算道具加成
        float addItem = 0F;
        if (lastTime < entity.getPlayerdataDB().getAddspeedEndTime()) {
            // 计算本次道具产出时间
            long endTimeItem = currTime > entity.getPlayerdataDB().getAddspeedEndTime() ? entity.getPlayerdataDB().getAddspeedEndTime() : currTime;
            long insTimeItem = endTimeItem - lastTime;
            long addBaseNum = insTimeItem / insHarvsetTime;
            // 计算基础产出 = 每小时产量/一小时产N次*N次
            float addBaseItem = fmaoCfg.getRewardvue() *1F / 60F * getFMCfg().getHarvestinstime() * baseNum;
            float rateItem = getFMCfg().getSpeedaddvue() / 100F;
            addItem = addBaseItem * rateItem;
        }
        // 减去被偷的
        float steal = 0F;
        for (int vue : entity.getPlayerdataDB().getStealInfoLastMap().values()) {
            steal += vue;
        }
        float totalbase = base + addPet + addItem - steal;
        double totalbaseLong = Math.ceil(totalbase);
        Common.Reward.Builder rewardBase = Common.Reward.newBuilder();
        rewardBase.setRewardType(Common.RewardTypeEnum.forNumber(fmaoCfg.getReward()[0]));
        rewardBase.setId(fmaoCfg.getReward()[1]);
        rewardBase.setCount((int) Math.round(totalbaseLong));
        all.setBase(rewardBase.build());
        if (insTime > insTimeMax) {
            all.setBaseTime(currTime);
        } else {
            all.setBaseTime(lastTime + baseNum*insHarvsetTime);
        }
        all.setLeijiTime(baseNum*insHarvsetTime);
        if (isSteal) {
            return all;
        }
        // 计算概率掉落
        long lastTimeXY = entity.getPlayerdataDB().getLastHarvestTimeXY();
        long insTimeXY = currTime - lastTimeXY;
        if (insTimeXY > insTimeMax) {
            insTimeXY = insTimeMax;
        }
        // 概率掉落
        long suijiNUM = insTimeXY/insHarvsetTime;
        if (suijiNUM < 1) {
            return all;
        }
        all.setGailv(true);
        all.setGailvTime(lastTimeXY + suijiNUM*insHarvsetTime);
        for (int glent : toListInt(entity.getExtids())) {
            FarmMineAwardObject fmaoCfgGL = FarmMineAward.getById(glent);
            if (null != fmaoCfgGL) {
                float hourNumGL = 60F/getFMCfg().getHarvestinstime();
                float gailv = fmaoCfgGL.getRewardvue() / hourNumGL;
                int gailvInt = (int) (gailv * 100);
                int count = 0;
                for (long i=0; i<suijiNUM; i++) {
                    int ramdom = RandomUtil.getRandomValue(0, 100);
                    if (ramdom < gailvInt) {
                        count+=1;
                    }
                }
                if (count > 0) {
                    Common.Reward rewardBaseGL = RewardUtil.parseReward(fmaoCfgGL.getReward()[0], fmaoCfgGL.getReward()[1], count);
                    all.getGailv().add(rewardBaseGL);
                }
            }
        }
        return all;
    }

    /**
     * @param playerIdx
     * @param idx
     * @param consume
     * 出价
     */
    public void offerPrice(String playerIdx, int idx, Common.Consume consume) {
        SC_FarmMineOfferPrice.Builder msg = SC_FarmMineOfferPrice.newBuilder();
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success);
        msg.setResult(retCode);
        if (isHarvest) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineOfferPrice_VALUE, msg);
            return;
        }
        // 根据矿地查找数据
        farmmineEntity entity = farmmineCache.getByIdx(String.valueOf(idx));
        if (null == entity || !StringHelper.isNull(entity.getOccplayerid())) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineOfferPrice_VALUE, msg);
            return;
        }
        farmmineEntity entityMy = farmmineCache.getInstance().getByPlayerIdx(playerIdx);
        if (null != entityMy) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_FARMMINE_HAVEMINE);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineOfferPrice_VALUE, msg);
            return;
        }
        if (entity.getJointime() != currCycleTime.getStartTime()) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineOfferPrice_VALUE, msg);
            return;
        }
        FarmMineDB.FarmMineOfferTimeDB isOpen = null;
        for (FarmMineDB.FarmMineOfferTimeDB ent : pubDB.getHisLoopMap().values()) {
            if (ent.getMineIdsList().contains(idx)) {
                isOpen = ent;
                break;
            }
        }
        if (null == isOpen || isOpen.getState() != FarmMineUtil.STATE_OFFERPRICE) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineOfferPrice_VALUE, msg);
            return;
        }
        // 根据矿地查找数据
        if (null == entity || entity.getAuctionstart() != isOpen.getStartTime()) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineOfferPrice_VALUE, msg);
            return;
        }
        if (getFMCfg().getOfferpricitem()[1] != consume.getId()) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineOfferPrice_VALUE, msg);
            return;
        }
        int loop = isOpen.getLoop();
        SyncExecuteFunction.executeConsumer(entity, entityTemp -> {
            // 判断玩家出价个数
            FarmMinePlayerOffer fmpo = playerOfferMap.computeIfAbsent(playerIdx, k -> new FarmMinePlayerOffer());
            if (fmpo.getOfferInfo().size() >= getFMCfg().getPricenum()) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_FARMMINE_OFFERNUMMAX);
                msg.setResult(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineOfferPrice_VALUE, msg);
                return;
            }
            if (entityTemp.getAuctionInfoDB().containsOfferPrice(playerIdx)) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_FARMMINE_OFFERREQ);
                msg.setResult(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineOfferPrice_VALUE, msg);
                return;
            }
            // 判断竞价消耗是否足够
            if (!ConsumeManager.getInstance().materialIsEnough(playerIdx, consume)) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_Player_CurrencysNotEnought);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineOfferPrice_VALUE, msg);
                return;
            }
            // 判断出价消耗
            ReasonManager.Reason reason = ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_FARMMINE_OFFER);
            Common.Consume consumet = ConsumeUtil.parseConsume(getFMCfg().getOfferpriceconsume());
            if (!ConsumeManager.getInstance().consumeMaterial(playerIdx, consumet, reason)) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_Player_CurrencysNotEnought);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineOfferPrice_VALUE, msg);
                return;
            }
            // 判断通过写数据，因为玩家的数据只有这个地方写，没有移除操作，所以不做所处理
            // 同步公共池数据
            entityTemp.getAuctionInfoDB().putOfferPrice(playerIdx, consume.getCount());
            entityTemp.getAuctionInfoDB().putOfferTime(playerIdx, System.currentTimeMillis());
            // 处理玩家数据
            fmpo.getOfferInfo().put(idx, consume.getCount());
            fmpo.getOfferTime().put(idx, System.currentTimeMillis());
            // 推送我的竞拍信息
            sendMyAuction(playerIdx);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineOfferPrice_VALUE, msg);
        });
    }

    /**
     * @param playerIdx
     * @param insTime
     * 获取累计奖励
     */
    public void getInsAward(String playerIdx, int insTime) {
        SC_FarmMineInsAward.Builder msg = SC_FarmMineInsAward.newBuilder();
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success);
        msg.setResult(retCode);
        if (!isHarvest) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineInsAward_VALUE, msg);
            return;
        }
        // 根据矿地查找数据
        farmmineEntity entity = farmmineCache.getInstance().getByPlayerIdx(playerIdx);
        // 不是自己的矿地
        if (!checkIsMyMine(entity)) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_FARMMINE_NOTMINE);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineInsAward_VALUE, msg);
            return;
        }
        if (entity.getPlayerdataDB().getInsSwardList().contains(insTime)) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_Mist_HasClaimedReward);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineInsAward_VALUE, msg);
            return;
        }
        int[] award = null;
        for (int[] cfgent : getFMCfg().getExtawardtime()) {
            if (cfgent[3] == insTime) {
                award = cfgent;
                break;
            }
        }
        if (null == award) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineInsAward_VALUE, msg);
            return;
        }
        if (entity.getPlayerdataDB().getMyTotalTime() < insTime * FarmMineUtil.HOURTOSECOND) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_FARMMINE_LJTIMENOT);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineInsAward_VALUE, msg);
            return;
        }
        int[] finalAward = award;
        SyncExecuteFunction.executeConsumer(entity, cacheTemp -> {
            cacheTemp.getPlayerdataDB().addInsSward(insTime);
            Common.Reward rewardBuilder = RewardUtil.parseReward(finalAward[0], finalAward[1], finalAward[2]);
            ReasonManager.Reason reason = ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_FARMMINE_INS);
            RewardManager.getInstance().doReward(playerIdx, rewardBuilder, reason, true);
            sendMyInfo(playerIdx, cacheTemp);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineInsAward_VALUE, msg);
        });
    }

    /**
     * @param playerIdx
     * @param idx
     * 偷取
     */
    public void stealAward(String playerIdx, int idx) {
        SC_FarmMineSteal.Builder msg = SC_FarmMineSteal.newBuilder();
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success);
        msg.setResult(retCode);
        if (!isHarvest) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineSteal_VALUE, msg);
            return;
        }
        farmmineEntity entity = farmmineCache.getByIdx(String.valueOf(idx));
        if (null == entity || StringHelper.isNull(entity.getOccplayerid())) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineSteal_VALUE, msg);
            return;
        }
        if (Objects.equals(playerIdx, entity.getOccplayerid())) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineSteal_VALUE, msg);
            return;
        }
        if (playerStealMap.getOrDefault(playerIdx, 0) >= getFMCfg().getStealsnum()) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_FARMMINE_StealMAX);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineSteal_VALUE, msg);
            return;
        }
        SyncExecuteFunction.executeConsumer(entity, cacheTemp -> {
            // 多长时间没有收获
            long insHarvest = System.currentTimeMillis() - entity.getPlayerdataDB().getLastHarvestTime();
            if (insHarvest < getFMCfg().getStealstimecan() * FarmMineUtil.HOURTOSECOND) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_FARMMINE_StealNOT);
                msg.setResult(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineSteal_VALUE, msg);
                return;
            }
            if (entity.getPlayerdataDB().containsStealInfoLast(playerIdx)) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_FARMMINE_StealREPIR);
                msg.setResult(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineSteal_VALUE, msg);
                return;
            }
            if (cacheTemp.getPlayerdataDB().getBestealCount() >= getFMCfg().getBestealsnum()) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_FARMMINE_BEStealMAX);
                msg.setResult(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineSteal_VALUE, msg);
                return;
            }
            FarmMineDrop fmd = computeFruit(cacheTemp, true);
            if (fmd.getBase().getCount() <= 0) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_FARMMINE_StealNOT);
                msg.setResult(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineSteal_VALUE, msg);
                return;
            }
            Common.Reward.Builder otherAward = fmd.getBase().toBuilder();
            int vue = (int) (getFMCfg().getStealsvue() * 1F * 60000L / fmd.getLeijiTime() * otherAward.getCount());
            otherAward.setCount(vue);
            ReasonManager.Reason reason = ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_FARMMINE_STEAL);
            RewardManager.getInstance().doReward(playerIdx, otherAward.build(), reason, true);
            cacheTemp.getPlayerdataDB().setBestealCount(cacheTemp.getPlayerdataDB().getBestealCount()+1);
            cacheTemp.getPlayerdataDB().putStealInfoLast(playerIdx, vue);
            int totalVue = cacheTemp.getPlayerdataDB().getStealInfoMap().getOrDefault(playerIdx, 0) + vue;
            cacheTemp.getPlayerdataDB().putStealInfo(playerIdx, totalVue);
            playerStealMap.put(playerIdx, playerStealMap.getOrDefault(playerIdx, 0) + 1);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineSteal_VALUE, msg);
            sendMyInfo(playerIdx);
        });
        savePubDB();
    }

    /**
     * @param playerIdx
     * @param idx
     * 加价
     */
    public void offerPriceAdd(String playerIdx, int idx) {
        SC_FarmMineOfferPriceAdd.Builder msg = SC_FarmMineOfferPriceAdd.newBuilder();
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success);
        msg.setResult(retCode);
        if (isHarvest) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineOfferPriceAdd_VALUE, msg);
            return;
        }
        // 根据矿地查找数据
        farmmineEntity entity = farmmineCache.getByIdx(String.valueOf(idx));
        if (null == entity ) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineOfferPriceAdd_VALUE, msg);
            return;
        }
        FarmMineDB.FarmMineOfferTimeDB isOpen = null;
        for (FarmMineDB.FarmMineOfferTimeDB ent : pubDB.getHisLoopMap().values()) {
            if (ent.getMineIdsList().contains(idx)) {
                isOpen = ent;
                break;
            }
        }
        if (null == isOpen || isOpen.getState() != FarmMineUtil.STATE_OFFERPRICE) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineOfferPriceAdd_VALUE, msg);
            return;
        }
        if (entity.getAuctionstart() != isOpen.getStartTime()) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineOfferPriceAdd_VALUE, msg);
            return;
        }
        int loop= isOpen.getLoop();
        SyncExecuteFunction.executeConsumer(entity, entityTemp -> {
            if (!entityTemp.getAuctionInfoDB().containsOfferPrice(playerIdx)) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_FARMMINE_OFFERPEL);
                msg.setResult(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineOfferPriceAdd_VALUE, msg);
                return;
            }
            float rate = (getFMCfg().getAddpricepre() / 100F + 1) * entityTemp.getAuctionInfoDB().getOfferPriceMap().get(playerIdx);
            int lastprice = Math.round(rate);
            // 判断竞价消耗是否足够
            Common.Consume consume = ConsumeUtil.parseConsume(getFMCfg().getOfferpricitem()[0], getFMCfg().getOfferpricitem()[1], lastprice);
            if (!ConsumeManager.getInstance().materialIsEnough(playerIdx, consume)) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_Player_CurrencysNotEnought);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineOfferPriceAdd_VALUE, msg);
                return;
            }
            // 判断通过写数据，因为玩家的数据只有这个地方写，没有移除操作，所以不做所处理
            // 同步公共池数据
            entityTemp.getAuctionInfoDB().putOfferPrice(playerIdx, lastprice);
            entityTemp.getAuctionInfoDB().putOfferTime(playerIdx, System.currentTimeMillis());
            // 处理同步玩家数据
            FarmMinePlayerOffer fmpo = playerOfferMap.computeIfAbsent(playerIdx, k -> new FarmMinePlayerOffer());
            // 处理玩家数据
            fmpo.getOfferInfo().put(idx, lastprice);
            fmpo.getOfferTime().put(idx, System.currentTimeMillis());
            // 推送我的竞拍信息
            sendMyAuction(playerIdx);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineOfferPriceAdd_VALUE, msg);
        });
    }

    /**
     * @param playerIdx
     * @param idx
     * 标记优先级
     */
    public void markMine(String playerIdx, int idx) {
        SC_FarmMineMark.Builder msg = SC_FarmMineMark.newBuilder();
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success);
        msg.setResult(retCode);
        if (isHarvest) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineMark_VALUE, msg);
            return;
        }
        // 根据矿地查找数据
        farmmineEntity entity = farmmineCache.getByIdx(String.valueOf(idx));
        if (null == entity ) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineMark_VALUE, msg);
            return;
        }
        FarmMineDB.FarmMineOfferTimeDB isOpen = null;
        for (FarmMineDB.FarmMineOfferTimeDB ent : pubDB.getHisLoopMap().values()) {
            if (ent.getMineIdsList().contains(idx)) {
                isOpen = ent;
                break;
            }
        }
        if (null == isOpen || isOpen.getState() != FarmMineUtil.STATE_OFFERPRICE) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineMark_VALUE, msg);
            return;
        }
        if (entity.getAuctionstart() != isOpen.getStartTime()) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineMark_VALUE, msg);
            return;
        }
        if (!entity.getAuctionInfoDB().containsOfferPrice(playerIdx)) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_FARMMINE_OFFERPEL);
            msg.setResult(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineMark_VALUE, msg);
            return;
        }
        FarmMinePlayerOffer fmpo = playerOfferMap.computeIfAbsent(playerIdx, k -> new FarmMinePlayerOffer());
        if (fmpo.getFirstIdx() == idx) {
            farmmineEntity entity1 = farmmineCache.getByIdx(String.valueOf(fmpo.getFirstIdx()));
            if (null != entity1 ) {
                SyncExecuteFunction.executeConsumer(entity1, cacheTemp -> {
                    // 新的添加，老的移除
                    List<String> tempList = new ArrayList<>();
                    tempList.addAll(cacheTemp.getAuctionInfoDB().getFirstIdList());
                    tempList.remove(playerIdx);
                    cacheTemp.getAuctionInfoDB().clearFirstId();
                    cacheTemp.getAuctionInfoDB().addAllFirstId(tempList);
                    fmpo.setFirstIdx(0);
                    sendMyAuction(playerIdx);
                    GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineMark_VALUE, msg);
                });
            }
        } else {
            if (fmpo.getFirstIdx() > 0) {
                farmmineEntity entity1 = farmmineCache.getByIdx(String.valueOf(fmpo.getFirstIdx()));
                if (null != entity1 ) {
                    SyncExecuteFunction.executeConsumer(entity1, cacheTemp -> {
                        // 新的添加，老的移除
                        List<String> tempList = new ArrayList<>();
                        tempList.addAll(cacheTemp.getAuctionInfoDB().getFirstIdList());
                        tempList.remove(playerIdx);
                        cacheTemp.getAuctionInfoDB().clearFirstId();
                        cacheTemp.getAuctionInfoDB().addAllFirstId(tempList);
                    });
                }
            }
            // 第一个设置，直接添加
            SyncExecuteFunction.executeConsumer(entity, cacheTemp -> {
                // 新的添加，老的移除
                cacheTemp.getAuctionInfoDB().addFirstId(playerIdx);
                fmpo.setFirstIdx(idx);
                sendMyAuction(playerIdx);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_FarmMineMark_VALUE, msg);
            });
        }
    }

    public FarmMineAllMine createAllMineInfo() {
        FarmMineAllMine.Builder msg = FarmMineAllMine.newBuilder();
        for (BaseEntity ent : farmmineCache.getInstance().getAllNotPub().values()) {
            farmmineEntity entity = (farmmineEntity) ent;
            msg.addInfos(createOneMineInfo(entity));
        }
        return msg.build();
    }

    public FarmMineOneInfo.Builder createOneMineInfo(farmmineEntity entity) {
        FarmMineOneInfo.Builder msg = FarmMineOneInfo.newBuilder();
        msg.setIdx(Integer.valueOf(entity.getIdx()));
        msg.setPlayerIdx(entity.getOccplayerid());
        playerEntity player = playerCache.getByIdx(entity.getOccplayerid());
        if (null != player) {
            msg.setPlayerName(player.getName());
            msg.setTitleIdPlayer(player.getCurEquipNewTitleId());
            msg.setAvatar(player.getAvatar());
        }
        msg.setBaseProduceId(entity.getBaseidx());
        msg.addAllExtProduceId(toListInt(entity.getExtids()));
        msg.setAddPet(entity.getPetid());
        msg.setTitleId(pubDB.getTitleOrDefault(entity.getBaseidx(), 0));
        FarmMineDB.FarmMineHisDB ent = pubDB.getHisDataMap().get(entity.getBaseidx());
        if (null != ent) {
            msg.setLastPrice(ent.getLastPriceVue());
            msg.setAveragePrice(ent.getHisPriceVue());
        } else {
            msg.setLastPrice(100);
            msg.setAveragePrice(100);
        }
        if (!isHarvest) {
            msg.setOfferPriceNum(entity.getAuctionInfoDB().getOfferPriceCount());
        }
        msg.setGetPrice(entity.getPrice());
        if (entity.getAuctionstart() > entity.getJointime() && System.currentTimeMillis() > entity.getAuctionend()) {
            if (StringHelper.isNull(entity.getOccplayerid())) {
                msg.setIsDrain(1);
            }
        }
        return msg;
    }

    public FarmMineAuction.Builder createAuction() {
        FarmMineAuction.Builder msg = FarmMineAuction.newBuilder();
        FarmMineDB.FarmMineOfferTimeDB ent = pubDB.getHisLoopMap().get(pubDB.getCurrLoop());
        if (null != ent) {
            if (ent.getState() == FarmMineUtil.STATE_NOT_OPEN) {
                FarmMineAuctionTime next = currCycleTime.getAuctionTime().get(pubDB.getCurrLoop()+1);
                if (null != next) {
                    msg.setNextTime(next.getStartTime());
                }
                return msg;
            }
            msg.setLoop(ent.getLoop());
            msg.addAllIdxs(ent.getMineIdsList());
            msg.setNextTime(ent.getEndTime());
            if (ent.getState() == FarmMineUtil.STATE_OFFERPRICE) {
                msg.setState(0);
                msg.setStateStartTime(ent.getStartTime());
                msg.setStateEndTime(ent.getStartTimeView());
            } else {
                msg.setState(1);
                msg.setStateStartTime(ent.getStartTimeView());
                msg.setStateEndTime(ent.getEndTimeView());
            }
        } else {
            FarmMineAuctionTime next = currCycleTime.getAuctionTime().get(1);
            if (null != next) {
                msg.setNextTime(next.getStartTime());
            }
        }
        return msg;
    }

    public FarmMineMyAuction.Builder createMyAuction(FarmMinePlayerOffer fmpo) {
        FarmMineMyAuction.Builder msg = FarmMineMyAuction.newBuilder();
        for (Map.Entry<Integer, Integer> ent : fmpo.getOfferInfo().entrySet()) {
            msg.addIdx(ent.getKey());
            msg.addPrice(ent.getValue());
        }
        msg.setFirstIdx(fmpo.getFirstIdx());
        return msg;
    }

    public FarmMineMyInfo.Builder createMyMineInfo(String playerIdx, farmmineEntity entity) {
        FarmMineMyInfo.Builder msg = FarmMineMyInfo.newBuilder();
        if (null != entity && Objects.equals(entity.getOccplayerid(), playerIdx)) {
            msg.setIdx(Integer.valueOf(entity.getIdx()));
            msg.setMyTotalTime(entity.getPlayerdataDB().getMyTotalTime());
            msg.setIsAddPet(entity.getPlayerdataDB().getIsAddPet());
            msg.setAddspeedStartTime(entity.getPlayerdataDB().getAddspeedStartTime());
            msg.setAddspeedEndTime(entity.getPlayerdataDB().getAddspeedEndTime());
            msg.setLastHarvestTime(entity.getPlayerdataDB().getLastHarvestTime());
            msg.addAllReward(entity.getPlayerdataDB().getRewardList());
            msg.addAllInsSward(entity.getPlayerdataDB().getInsSwardList());
        }
        msg.setStealCountSurplus(getFMCfg().getStealsnum()-playerStealMap.getOrDefault(playerIdx, 0));
        return msg;
    }

    /**
     * @param entity
     * @return
     * 检查本期活动是否有我的矿区
     */
    public boolean checkIsMyMine(farmmineEntity entity) {
        if (null == entity || entity.getPlayerdataDB().getIdx() == 0) {
            return false;
        }
        if (entity.getJointime() != currCycleTime.getStartTime()) {
            return false;
        }
        if (StringHelper.isNull(entity.getOccplayerid())) {
            return false;
        }
        return true;
    }

    public FarmMineConfigObject getFMCfg() {
        return FarmMineConfig._ix_id.get(GameConst.CONFIG_ID);
    }

    public static List<Integer> toListInt(String str) {
        List<Integer> result = new ArrayList<Integer>();
        if (StringUtils.isBlank(str)) {
            return result;
        }
        String[] pairs = StringUtils.split(str, ",");
        if (pairs == null) {
            return result;
        }
        for (String str1 : pairs) {
            int i = NumberUtils.toInt(str1);
            if (!result.contains(i)) {
                result.add(i);
            }
        }
        return result;
    }

    /**
     * @return
     * 获取一周的开始时间，周六为第一天的周
     */
    public Calendar getWeekStartTimeSatFirst() {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.SATURDAY);
        calendar.set(Calendar.DAY_OF_WEEK, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 100);
        return calendar;
    }

    /**
     * @return
     * 获取当前时间活动周的第一天作为基准时间
     */
    public long getWeekStartTimeSatFirstMs() {
        return getWeekStartTimeSatFirst().getTimeInMillis();
    }

    /**
     * @return
     * 获取活动周周一开始的时间
     */
    public long getWeekMonStartTimeSatFirst() {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.SATURDAY);
        calendar.set(Calendar.DAY_OF_WEEK, 2);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * @return
     * 获取当前时间活动周的最后一天作为基准时间
     */
    public long getWeekEndTimeSatFirst() {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.SATURDAY);
        calendar.set(Calendar.DAY_OF_WEEK, 6);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 900);
        return calendar.getTimeInMillis();
    }

    /**
     * 判断时间 是不是本年的同一周
     */
    public boolean isThatSameWeek(long time, long time2) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTimeInMillis(time);
        c2.setTimeInMillis(time2);
        c1.setFirstDayOfWeek(Calendar.SATURDAY);
        c2.setFirstDayOfWeek(Calendar.SATURDAY);
        if (c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR)) {
            return false;
        }
        int week1 = c1.get(Calendar.WEEK_OF_YEAR);
        int week2 = c2.get(Calendar.WEEK_OF_YEAR);
        return week1 == week2;
    }

    public FarmMineAwardObject randomById(int[] ids) {
        // 随机基础产出
        int total = 0;
        List<FarmMineAwardObject> temp = new LinkedList<>();
        for (int awardId : ids) {
            FarmMineAwardObject fmao = FarmMineAward.getById(awardId);
            if (null != fmao) {
                total += fmao.getWeight();
                temp.add(fmao);
            }
        }
        int rom = RandomUtil.getRandomValue(0, total);
        int add = 0;
        for (FarmMineAwardObject fmao : temp) {
            add += fmao.getWeight();
            if (rom < add) {
                return fmao;
            }
        }
        return null;
    }

    /**
     * gm命令改活动周期为2小时一次，方便测试
     */
    public void gmChangeHourCycle(int loop, int auctime) {
        loop = loop < 1 ? 1 : loop;
        loop = loop > 6 ? 6 : loop;
        auctime = auctime < 1 ? 1 : auctime;
        auctime = auctime > 30 ? 30 : auctime;
        FarmMineTimeLoop timeLoop = new FarmMineTimeLoop();
        long currTime = System.currentTimeMillis();
        long endTime = currTime + FarmMineUtil.HOURTOSECOND * 100;
        timeLoop.setStartTime(currTime);
        timeLoop.setEndTime(endTime);
        timeLoop.setEndGiveTime(endTime);
        timeLoop.setStartAuctionTime(timeLoop.getStartTime());
        Map<Integer, FarmMineAuctionTime> auctionTime = new TreeMap<>();
        long startAuctionTime = currTime + 30000L;
        for (int i=1; i<=loop; i++) {
            FarmMineAuctionTime fmat = new FarmMineAuctionTime();
            fmat.setLoop(i);
            fmat.setStartTime(startAuctionTime);
            long endAuctionTime = startAuctionTime + auctime * 60000L;
            long endAuctionViewTime = endAuctionTime + 60000L;
            fmat.setEndTimeoffer(endAuctionTime);
            fmat.setStartTimeView(endAuctionTime);
            fmat.setEndTimeView(endAuctionViewTime);
            fmat.setEndTime(endAuctionViewTime);
            auctionTime.put(i, fmat);
            startAuctionTime = endAuctionViewTime;
        }
        timeLoop.setEndAuctionTime(startAuctionTime);
        timeLoop.setStartGiveTime(startAuctionTime);
        timeLoop.setAuctionTime(auctionTime);
        timeLoop.setNeedCheck(false);
        this.currCycleTime = timeLoop;
        checkResetMineDBdataAndRef();
    }

    /**
     * @param playerIdx
     * @param n
     * 修改我的收获时间
     */
    public void gmAddGiveTime(String playerIdx, int n) {
        farmmineEntity entity = farmmineCache.getInstance().getByPlayerIdx(playerIdx);
        if (!checkIsMyMine(entity)) {
            return;
        }
        SyncExecuteFunction.executeConsumer(entity, cacheTemp -> {
            long tarTime = cacheTemp.getPlayerdataDB().getLastHarvestTime() - n * FarmMineUtil.HOURTOSECOND;
            tarTime = tarTime < 0 ? 0 : tarTime;
            cacheTemp.getPlayerdataDB().setLastHarvestTime(tarTime);
            cacheTemp.getPlayerdataDB().setLastHarvestTimeXY(tarTime);
            sendMainPanel(playerIdx);
        });
    }

    /**
     * 随机拉取玩家分配矿地
     */
    public void gmAddRandomPlayerMine(int n) {
        if (!isHarvest) {
            return;
        }
        List<String> pids = new ArrayList<>();
        List<String> pidsAll = new ArrayList<>();
        pidsAll.addAll(playerCache.getInstance().getAll().keySet());
        Collections.shuffle(pidsAll);
        int i = 0;
        for (BaseEntity ent : farmmineCache.getInstance().getAllNotPub().values()) {
            farmmineEntity entity = (farmmineEntity) ent;
            if (!StringHelper.isNull(entity.getOccplayerid())) {
                pids.add(entity.getOccplayerid());
                continue;
            }
            String pid = "";
            for (int j=i; j< pidsAll.size(); j++) {
                pid = pidsAll.get(j);
                i++;
                if (!pids.contains(pid)) {
                    break;
                }
            }
            if ("".equals(pid)) {
                break;
            }
            pids.add(pid);
            entity.setOccplayerid(pid);
            entity.setPrice(999);
            entity.getPlayerdataDB().setIdx(Integer.valueOf(entity.getIdx()));
            entity.getPlayerdataDB().setLastHarvestTime(System.currentTimeMillis()-36000000L);
            entity.getPlayerdataDB().setLastHarvestTimeXY(System.currentTimeMillis()-36000000L);
            if (RandomUtil.getRandomValue(0, 100) < 50) {
                entity.getPlayerdataDB().setIsAddPet(1);
            }
            // 结算完成清空竞价数据
            entity.setAuctionInfoDB(FarmMineDB.FarmMineOfferDB.newBuilder());
            farmmineCache.getInstance().flush(entity);
            if (i > n) {
                break;
            }
        }
    }

}
