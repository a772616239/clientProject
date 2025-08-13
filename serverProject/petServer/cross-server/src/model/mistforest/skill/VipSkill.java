package model.mistforest.skill;

import cfg.MistPlayerSkillCfg;
import cfg.MistPlayerSkillCfgObject;
import cfg.MistWorldMapConfig;
import cfg.MistWorldMapConfigObject;
import common.GlobalData;
import common.GlobalTick;
import java.util.HashMap;
import lombok.Getter;
import model.mistforest.MistConst;
import model.mistforest.MistConst.MistSkillTiming;
import model.mistforest.MistConst.MistTriggerParamType;
import model.mistforest.MistConst.MistVipSkillType;
import model.mistforest.mistobj.MistFighter;
import model.mistplayer.entity.MistPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.MistRetCode;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistVipSkillData;
import protocol.MistForest.SC_UseMistVipSkill;
import protocol.ServerTransfer.CS_GS_UpdateVipSkillData;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

@Getter
public class VipSkill {
    protected int vipSkillId;
    protected MistFighter owner;

    protected int stackCount;
    protected int maxStackCount;
    protected long coolDown;
    protected long refreshCoolDownTimestamp;

    public VipSkill(MistFighter fighter, MistPlayerSkillCfgObject cfg) {
        this.owner = fighter;
        this.vipSkillId = cfg.getId();
        this.coolDown = cfg.getCooldown() * TimeUtil.MS_IN_A_S;
        this.maxStackCount = cfg.getMaxstack();
        this.stackCount = this.maxStackCount;
    }

    public void clear() {
        this.vipSkillId = 0;
        this.stackCount = 0;
        this.maxStackCount = 0;
        this.coolDown = 0;
        this.owner = null;
    }

    public int getSkillType() {
        MistPlayerSkillCfgObject cfg = MistPlayerSkillCfg.getById(vipSkillId);
        if (cfg == null) {
            return 0;
        }
        return cfg.getType();
    }

    public void initByOffSkillData(MistVipSkillData skillData) {
        long curTime = GlobalTick.getInstance().getCurrentTime();
        if (skillData.getSkillStack() >= maxStackCount) {
            stackCount = maxStackCount;
            refreshCoolDownTimestamp = 0;
        } else if (skillData.getExpireTimestamp() <= 0) {
            this.stackCount = skillData.getSkillStack();
            refreshCoolDownTimestamp = curTime + coolDown; // 记录的时间戳为0，则为异常情况，再走一次cd
        } else if (curTime < skillData.getExpireTimestamp()) {
            stackCount = skillData.getSkillStack();
            refreshCoolDownTimestamp = skillData.getExpireTimestamp();
        } else {
            int revertCount = (int) ((curTime - skillData.getExpireTimestamp()) / coolDown); // 额外恢复的技能层数
            stackCount = skillData.getSkillStack() + revertCount + 1;
            if (stackCount >= maxStackCount) {
                refreshCoolDownTimestamp = 0;
            } else {
                refreshCoolDownTimestamp = curTime + coolDown - curTime % skillData.getExpireTimestamp();
            }
        }
        if (stackCount > maxStackCount) {
            stackCount = maxStackCount;
            refreshCoolDownTimestamp = 0;
        } else if (stackCount < 0) {
            stackCount = 0;
            refreshCoolDownTimestamp = curTime + coolDown;
        }
    }

    public MistRetCode useSkill(String param) {
        MistRetCode retCode = triggerSkillEffect(param);
        if (retCode == MistRetCode.MRC_Success) {
            boolean needUpdateCd = true;
            if (getSkillType() == MistVipSkillType.RebornInSituPlace) {
                int intParam = Integer.valueOf(param);
                if (intParam == 0) {
                    needUpdateCd = false;
                }
            }
            if (needUpdateCd) {
                stackCount = Math.max(0, stackCount - 1);
                if (refreshCoolDownTimestamp == 0) {
                    refreshCoolDownTimestamp = GlobalTick.getInstance().getCurrentTime() + coolDown;
                }
            }
            updateSkillData();
        }
        return retCode;
    }

    protected void updateSkillData() {
        MistPlayer player = owner.getOwnerPlayerInSameRoom();
        if (player != null) {
            SC_UseMistVipSkill.Builder builder = SC_UseMistVipSkill.newBuilder();
            builder.setRetCode(MistRetCode.MRC_Success);
            builder.setSkillId(vipSkillId);
            builder.setSkillStack(stackCount);
            builder.setExpireTimestamp(refreshCoolDownTimestamp);
            player.sendMsgToServer(MsgIdEnum.SC_UseMistVipSkill_VALUE, builder);

            CS_GS_UpdateVipSkillData.Builder transBuilder = CS_GS_UpdateVipSkillData.newBuilder();
            transBuilder.setPlayerIdx(player.getIdx());
            MistVipSkillData.Builder skillBuilder = MistVipSkillData.newBuilder();
            skillBuilder.setSkillId(vipSkillId);
            skillBuilder.setSkillStack(stackCount);
            skillBuilder.setExpireTimestamp(refreshCoolDownTimestamp);
            transBuilder.addVipSkillData(skillBuilder);
            GlobalData.getInstance().sendMsgToServer(player.getServerIndex(), MsgIdEnum.CS_GS_UpdateVipSkillData_VALUE, transBuilder);
        }
    }

    protected MistRetCode triggerSkillEffect(String param) {
        int skillType = getSkillType();
        try {
            switch (skillType) {
                case MistVipSkillType.Transport: {
                    if (stackCount <= 0) {
                        return MistRetCode.MRC_SkillCoolDown; // 冷却中
                    }
                    int intParam = Integer.valueOf(param);
                    MistWorldMapConfigObject mapCfg = MistWorldMapConfig.getByMapid(owner.getRoom().getLevel());
                    if (mapCfg == null) {
                        return MistRetCode.MRC_ErrorParam;
                    }
                    int[][] posList = mapCfg.getTeleporterlist();
                    if (posList == null || posList.length < 0) {
                        return MistRetCode.MRC_NotFoundConfig;
                    }
                    if (intParam < 0 || intParam >= posList.length) {
                        return MistRetCode.MRC_ErrorParam;
                    }
                    if (posList[intParam] == null || posList[intParam].length < 2) {
                        return MistRetCode.MRC_ErrorParam;
                    }
                    HashMap<Integer, Long> paramMap = new HashMap<>();
                    paramMap.put(MistTriggerParamType.VipSkillType, (long) MistVipSkillType.Transport);
                    long posData = GameUtil.mergeIntToLong(posList[intParam][0], posList[intParam][1]);
                    paramMap.put(MistTriggerParamType.TranPosData, posData);
                    owner.getSkillMachine().triggerPassiveSkills(MistSkillTiming.UseVipSkill, owner, paramMap);
                    return MistRetCode.MRC_Success;
                }
                case MistVipSkillType.SearchTreasure: {
                    if (stackCount <= 0) {
                        return MistRetCode.MRC_SkillCoolDown; // 冷却中
                    }
                    HashMap<Integer, Long> paramMap = new HashMap<>();
                    paramMap.put(MistTriggerParamType.VipSkillType, (long) MistVipSkillType.SearchTreasure);
                    owner.getSkillMachine().triggerPassiveSkills(MistSkillTiming.UseVipSkill, owner, paramMap);
                    return MistRetCode.MRC_Success;
                }
                case MistVipSkillType.RebornInSituPlace: {
                    Long intParam = Long.valueOf(param);
                    if (intParam > 0 && stackCount <= 0) {
                        return MistRetCode.MRC_SkillCoolDown; // 冷却中
                    }
                    if (owner.getAttribute(MistUnitPropTypeEnum.MUPT_WaitingBossState_VALUE) > 0) {
                        return MistRetCode.MRC_CannotUseSkill;
                    }
                    if (intParam > 0) {
                        owner.getBufMachine().interruptBuffById(MistConst.MistVipRebornBuffId);
                    } else {
                        owner.getBufMachine().removeBuff(MistConst.MistVipRebornBuffId);
                    }
                    return MistRetCode.MRC_Success;
                }
                default:
                    break;
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return MistRetCode.MRC_ErrorParam;
        }
        return MistRetCode.MRC_NotFoundConfig;
    }

    public MistVipSkillData buildVipSkillData() {
        MistVipSkillData.Builder builder = MistVipSkillData.newBuilder();
        builder.setSkillId(vipSkillId).setSkillStack(stackCount).setExpireTimestamp(refreshCoolDownTimestamp);
        return builder.build();
    }

    public void onTick(long curTime) {
        if (stackCount >= maxStackCount) {
            return;
        }
        if (refreshCoolDownTimestamp > curTime) {
            return;
        }
        ++stackCount;
        if (stackCount < maxStackCount) {
            refreshCoolDownTimestamp = curTime + coolDown;
        } else {
            refreshCoolDownTimestamp = 0;
        }
        updateSkillData();
    }
}
