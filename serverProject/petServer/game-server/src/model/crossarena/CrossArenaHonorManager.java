package model.crossarena;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.math.NumberUtils;

import com.alibaba.fastjson.JSON;

import cfg.CrossArenaCfg;
import cfg.CrossArenaCfgObject;
import cfg.CrossArenaHonor;
import cfg.CrossArenaHonorObject;
import common.GameConst;
import common.GlobalData;
import common.JedisUtil;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import model.crossarena.bean.CrossArenaTopHis;
import model.crossarena.bean.CrossArenaTopHisSub;
import model.crossarena.entity.playercrossarenaEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import platform.logs.ReasonManager;
import protocol.Common;
import protocol.CrossArena;
import protocol.MessageId;
import protocol.RetCodeId;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

public class CrossArenaHonorManager {

    private static CrossArenaHonorManager instance;

    public static CrossArenaHonorManager getInstance() {
        if (instance == null) {
            synchronized (CrossArenaHonorManager.class) {
                if (instance == null) {
                    instance = new CrossArenaHonorManager();
                }
            }
        }
        return instance;
    }

    private CrossArenaHonorManager() {
    }

    public void honorVueByKeyAdd(String playerIdx, int key, int vue) {
        honorVueByKey(playerIdx, key, vue, CrossArenaUtil.DbChangeAdd);
    }

    public void honorVueByKeyRep(String playerIdx, int key, int vue) {
        honorVueByKey(playerIdx, key, vue, CrossArenaUtil.DbChangeRep);
    }

    public void honorVueByKeyRepMax(String playerIdx, int key, int vue) {
        honorVueByKey(playerIdx, key, vue, CrossArenaUtil.DbChangeRepMax);
    }

    /**
     * @param playerIdx
     * @param key
     * 第一次事件
     */
    public void honorVueFirst(String playerIdx, int key) {
        playerEntity ple = playerCache.getByIdx(playerIdx);
        // 竞猜成功次数累加
        if (null == ple) {
            return;
        }
        playercrossarenaEntity pe = CrossArenaManager.getInstance().getPlayerEntity(playerIdx);
        if (pe.getHonorMsg().containsHisTime(key)) {
            return;
        }
        SyncExecuteFunction.executeConsumer(pe, p -> {
            p.getHonorMsg().putHisTime(key, GlobalTick.getInstance().getCurrentTime());
        });
    }

    /**
     * @param playerIdx
     * @param key
     * @param vue
     * @param oper
     * 增加成就累计
     */
    public void honorVueByKey(String playerIdx, int key, int vue, int oper) {
        playerEntity ple = playerCache.getByIdx(playerIdx);
        // 竞猜成功次数累加
        if (null == ple) {
            return;
        }
        playercrossarenaEntity pe = CrossArenaManager.getInstance().getPlayerEntity(playerIdx);
        SyncExecuteFunction.executeConsumer(pe, p -> {
            if (oper == CrossArenaUtil.DbChangeRep) {
                p.getHonorMsg().putDbs(key, vue);
            } else if (oper == CrossArenaUtil.DbChangeRepMax) {
                if (p.getHonorMsg().getDbsOrDefault(key, 0) < vue) {
                    p.getHonorMsg().putDbs(key, vue);
                }
            } else {
                int guessNum = p.getHonorMsg().getDbsOrDefault(key, 0) + vue;
                p.getHonorMsg().putDbs(key, guessNum);
            }
        });
        checkSucc(pe);
    }

    /**
     * @param pe
     */
    public void checkSucc(playercrossarenaEntity pe) {
    	CrossArena.SC_CrossArenaHonorRef.Builder msgf = CrossArena.SC_CrossArenaHonorRef.newBuilder();
        for (CrossArenaHonorObject cahoCfg : CrossArenaHonor._ix_id.values()) {
        	if (cahoCfg.getBigtype() == 4 || cahoCfg.getId() == 0) {
                continue;
            }
            if (pe.getHonorMsg().getFlishsList().contains(cahoCfg.getId())) {
                continue;
            }
            int num = pe.getHonorMsg().getDbsOrDefault(cahoCfg.getMissiontype(), 0);
            CrossArena.CrossArenaHonor.Builder msg = CrossArena.CrossArenaHonor.newBuilder();
            msg.setId(cahoCfg.getId());
            if (num >= cahoCfg.getParm()) {
                msg.setState(1);
                msg.setCurr(cahoCfg.getParm());
                msgf.addInfo(msg);
            }
        }
        GlobalData.getInstance().sendMsg(pe.getIdx(), MessageId.MsgIdEnum.SC_CrossArenaHonorRef_VALUE, msgf);
    }

    public void sendHonor(String playerIdx) {
        //重新计算
        CrossArenaManager.getInstance().quitCrossArenaLJTime(playerIdx,false);
        CrossArena.SC_CrossArenaHonor.Builder msgf = CrossArena.SC_CrossArenaHonor.newBuilder();
        msgf.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        playercrossarenaEntity pe = CrossArenaManager.getInstance().getPlayerEntity(playerIdx);
        for (CrossArenaHonorObject cahoCfg : CrossArenaHonor._ix_id.values()) {
            if (cahoCfg.getBigtype() == 4 || cahoCfg.getId() == 0) {
                continue;
            }
            CrossArena.CrossArenaHonor.Builder msg = CrossArena.CrossArenaHonor.newBuilder();
            msg.setId(cahoCfg.getId());
            if (pe.getHonorMsg().getFlishsList().contains(cahoCfg.getId())) {
                msg.setState(2);
                msg.setCurr(cahoCfg.getParm());
                msgf.addInfos(msg);
            } else {
                int num = getHonorValue(pe, cahoCfg);
                if (cahoCfg.getParm() > num) {
                    msg.setState(0);
                    msg.setCurr(num);
                    msgf.addInfos(msg);
                } else {
                    msg.setState(1);
                    msg.setCurr(cahoCfg.getParm());
                    msgf.addInfos(msg);
                }
            }
        }
        GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaHonor_VALUE, msgf);
    }

    private int getHonorValue(playercrossarenaEntity pe, CrossArenaHonorObject cahoCfg) {
        if (cahoCfg.getMissiontype()==CrossArenaUtil.HR_LT_TIME){
            return (int) (pe.getDataMsg().getLeijiTime()/ TimeUtil.MS_IN_A_HOUR);
        }
        return pe.getHonorMsg().getDbsOrDefault(cahoCfg.getMissiontype(), 0);
    }

    public void getAward(String playerIdx, List<Integer> ids) {
        CrossArena.SC_CrossArenaHonorJL.Builder msg = CrossArena.SC_CrossArenaHonorJL.newBuilder();
        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaHonorJL_VALUE, msg);
        playercrossarenaEntity pe = CrossArenaManager.getInstance().getPlayerEntity(playerIdx);
        boolean isChange = false;
        List<Common.Reward> rewards = new ArrayList<>();
        for (int id : ids) {
            CrossArenaHonorObject cahoCfg = CrossArenaHonor.getById(id);
            if (pe.getHonorMsg().getFlishsList().contains(cahoCfg.getId())) {
                continue;
            }
            int num = getHonorValue(pe, cahoCfg);
            if (cahoCfg.getParm() > num) {
                continue;
            }
            SyncExecuteFunction.executeConsumer(pe, p -> {
                p.getHonorMsg().addFlishs(cahoCfg.getId());
            });
            isChange = true;
            List<Common.Reward> rewardst = RewardUtil.parseRewardIntArrayToRewardList(cahoCfg.getAward());
            if (null != rewardst) {
                rewards.addAll(rewardst);
            }
        }
        if (isChange) {
            ReasonManager.Reason reason = ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_CrossAreanHonor);
            RewardManager.getInstance().doRewardByList(playerIdx, rewards, reason, true);
            sendHonor(playerIdx);
        }
    }

    public void sendHonorHis(String playerIdx) {
        CrossArena.SC_CrossArenaHonorHis.Builder msg = CrossArena.SC_CrossArenaHonorHis.newBuilder();
        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        playercrossarenaEntity pe = CrossArenaManager.getInstance().getPlayerEntity(playerIdx);
        Map<Integer, Long> hisTimeMap = pe.getHonorMsg().getHisTimeMap();
        for (CrossArenaHonorObject cahoCfg : CrossArenaHonor._ix_id.values()) {
        	if (hisTimeMap.containsKey(cahoCfg.getMissiontype())) {
        		msg.addId(cahoCfg.getId());
        		msg.addFlishtime(hisTimeMap.get(cahoCfg.getMissiontype()));
        	}
        }
        msg.addFlishtime(System.currentTimeMillis() + 5000000L);
        GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaHonorHis_VALUE, msg);
    }

    public void getNote(String playerIdx, CrossArena.CrossArenaNoteType type, int parm) {
        CrossArena.SC_CrossArenaNote.Builder msg = CrossArena.SC_CrossArenaNote.newBuilder();
        if (type == CrossArena.CrossArenaNoteType.CONT_WIN) {
            msg = sendContWin(playerIdx);
        } else if (type == CrossArena.CrossArenaNoteType.INS_PASS) {
            msg = sendINSPASS(playerIdx, parm);
        } else if (type == CrossArena.CrossArenaNoteType.TOP_FIRST) {
            msg = sendTop(playerIdx);
        }
        playercrossarenaEntity pe = CrossArenaManager.getInstance().getPlayerEntity(playerIdx);
        for (int ent : pe.getDataMsg().getNoteAwardList()) {
            msg.addWorship(CrossArena.CrossArenaNoteType.forNumber(ent));
        }
        GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaNote_VALUE, msg);
    }

    public CrossArena.SC_CrossArenaNote.Builder sendContWin(String playerIdx) {
        CrossArena.SC_CrossArenaNote.Builder msg = CrossArena.SC_CrossArenaNote.newBuilder();
        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        msg.setType(CrossArena.CrossArenaNoteType.CONT_WIN);
        try {
            Map<String, String> jedisstr = JedisUtil.jedis.hgetAll(GameConst.RedisKey.CrossArenaNoteCotMap);
            if (null != jedisstr) {
                for (Map.Entry<String, String> ent : jedisstr.entrySet()) {
                    CrossArena.CrossArenaNote.Builder msg2 = CrossArena.CrossArenaNote.newBuilder();
                    msg2.setYear(NumberUtils.toInt(ent.getKey())/1000);
                    msg2.setName(ent.getValue());
                    msg.addInfos(msg2);
                }
            }
        } catch (Exception e) {
            LogUtil.error("" + e.getMessage());
        }
        return msg;
    }

    public CrossArena.SC_CrossArenaNote.Builder sendINSPASS(String playerIdx, int parm) {
        CrossArena.SC_CrossArenaNote.Builder msg = CrossArena.SC_CrossArenaNote.newBuilder();
        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        msg.setType(CrossArena.CrossArenaNoteType.INS_PASS);
        try {
            Map<String, String> jedisstr = JedisUtil.jedis.hgetAll(GameConst.RedisKey.CrossArenaNoteInsMap + parm);
            if (null != jedisstr) {
                for (Map.Entry<String, String> ent : jedisstr.entrySet()) {
                    CrossArena.CrossArenaNote.Builder msg2 = CrossArena.CrossArenaNote.newBuilder();
                    msg2.setYear(NumberUtils.toInt(ent.getKey())/1000);
                    msg2.setName(ent.getValue());
                    msg.addInfos(msg2);
                }
            }
        } catch (Exception e) {
            LogUtil.error("" + e.getMessage());
        }
        return msg;
    }

    public CrossArena.SC_CrossArenaNote.Builder sendTop(String playerIdx) {
        CrossArena.SC_CrossArenaNote.Builder msg = CrossArena.SC_CrossArenaNote.newBuilder();
        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        msg.setType(CrossArena.CrossArenaNoteType.TOP_FIRST);
        try {
            Map<String, String> jedisstr = JedisUtil.jedis.hgetAll(GameConst.RedisKey.CrossArenaNoteTopMap);
            if (null != jedisstr) {
                Map<Integer, String> zhmap = new TreeMap<>();
                for (Map.Entry<String, String> ent : jedisstr.entrySet()) {
                    zhmap.put(NumberUtils.toInt(ent.getKey()), ent.getValue());
                }
                int max = zhmap.size();
                int s = 0;
                if (max > 12) {
                    s = max-12;
                }
                int i = 0;
                for (Map.Entry<Integer, String> ent : zhmap.entrySet()) {
                    if (i < s) {
                        continue;
                    }
                    i++;
                    CrossArena.CrossArenaNote.Builder msg2 = CrossArena.CrossArenaNote.newBuilder();
                    msg2.setYear(ent.getKey());
                    // 解析特殊数据
                    CrossArenaTopHis json = JSON.parseObject(ent.getValue(), CrossArenaTopHis.class);
                    for (CrossArenaTopHisSub ent1 : json.getHis().values()) {
                        CrossArena.CrossArenaNoteSub.Builder msg3 = CrossArena.CrossArenaNoteSub.newBuilder();
                        msg3.setYear(ent1.getGroup());
                        msg3.setNameFirst(ent1.getName());
                        msg3.setNameOther(ent1.getOtherName());
                        msg2.addOtherInfo(msg3);
                    }
                    msg.addInfos(msg2);
                }
            }
        } catch (Exception e) {
            LogUtil.error("" + e.getMessage());
        }
        return msg;
    }

    public void getNoteAward(String playerIdx, CrossArena.CrossArenaNoteType type) {
        CrossArena.SC_CrossArenaNoteWorship.Builder msg = CrossArena.SC_CrossArenaNoteWorship.newBuilder();
        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        msg.setType(type);
        playercrossarenaEntity pe = CrossArenaManager.getInstance().getPlayerEntity(playerIdx);
        if (pe.getDataMsg().getNoteAwardList().contains(type.getNumber())) {
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaNoteWorship_VALUE, msg);
            return;
        }
        CrossArenaCfgObject ecfg = CrossArenaCfg.getById(GameConst.CONFIG_ID);
        if (null == ecfg) {
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaNoteWorship_VALUE, msg);
            return;
        }
        if (type.getNumber() < 1 || type.getNumber() > ecfg.getWorship().length) {
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaNoteWorship_VALUE, msg);
            return;
        }
        SyncExecuteFunction.executeConsumer(pe, p -> {
            p.getDataMsg().addNoteAward(type.getNumber());
            int awardid = ecfg.getWorship()[type.getNumber()-1];
            List<Common.Reward> rewardst = RewardUtil.getRewardsByRewardId(awardid);
            ReasonManager.Reason reason = ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_CROSSARENA_WORSHIP);
            RewardManager.getInstance().doRewardByList(playerIdx, rewardst, reason, true);
        });
        GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaNoteWorship_VALUE, msg);
    }

    public void clearMobaiGM(String playerIdx) {
    	playercrossarenaEntity pe = CrossArenaManager.getInstance().getPlayerEntity(playerIdx);
    	SyncExecuteFunction.executeConsumer(pe, p -> {
    		p.getDataMsg().clearNoteAward();
    	});
    }
    
}
