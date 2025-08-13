package server.handler.player;

import protocol.Common.EnumFunction;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import common.AbstractBaseHandler;
import common.HttpRequestUtil;
import common.HttpRequestUtil.PlatFormRetCode;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.ArrayList;
import java.util.List;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerInfo.CS_ClaimCDK;
import protocol.PlayerInfo.SC_ClaimCDK;
import protocol.PlayerInfo.SC_ClaimCDK.Builder;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimCDK_VALUE)
public class ClaimCDKHandler extends AbstractBaseHandler<CS_ClaimCDK> {
    @Override
    protected CS_ClaimCDK parse(byte[] bytes) throws Exception {
        return CS_ClaimCDK.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimCDK req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        Builder resultBuilder = SC_ClaimCDK.newBuilder();
        JSONObject jsonObject = HttpRequestUtil.checkActiveCode(playerIdx, req.getCDKStr());
        LogUtil.debug("Receive CDK code = " + req.getCDKStr() + ", from playerIdx:" + playerIdx
                + " query result = " + jsonObject);
        if (jsonObject == null || !jsonObject.containsKey("retCode")) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimCDK_VALUE, resultBuilder);
            return;
        }

        Integer retCode = jsonObject.getInteger("retCode");
        RetCodeEnum retCodeEnum;
        if (retCode == PlatFormRetCode.SUCCESS) {
            List<Reward> rewardList = parseToRewards(jsonObject);
            if (CollectionUtils.isEmpty(rewardList)) {
                LogUtil.error("ClaimCDKHandler.execute, cdk result is success but can not parse rewards");
                retCodeEnum = RetCodeEnum.RCE_UnknownError;
            } else {
                Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_CDK);
                RewardManager.getInstance().doRewardByList(playerIdx, rewardList, reason, true);
                retCodeEnum = RetCodeEnum.RCE_Success;
            }
        } else if (retCode == PlatFormRetCode.ActiveCode_LoseEfficacy) {
            retCodeEnum = RetCodeEnum.RCE_ActiveCode_LoseEfficacy;
        } else if (retCode == PlatFormRetCode.ActiveCode_Used) {
            retCodeEnum = RetCodeEnum.RCE_ActiveCode_Used;
        } else if (retCode == PlatFormRetCode.ActiveCode_Error) {
            retCodeEnum = RetCodeEnum.RCE_ActiveCode_Error;
        } else if (retCode == PlatFormRetCode.ActiveCode_UpperLimit) {
            retCodeEnum = RetCodeEnum.RCE_ActiveCode_UpperLimit;
        } else if (retCode == PlatFormRetCode.ActiveCode_UseSameTypeCode) {
            retCodeEnum = RetCodeEnum.RCE_ActiveCode_UseSameTypeCode;
        } else {
            retCodeEnum = RetCodeEnum.RCE_UnknownError;
        }
        resultBuilder.setRetCode(GameUtil.buildRetCode(retCodeEnum));
        gsChn.send(MsgIdEnum.SC_ClaimCDK_VALUE, resultBuilder);
    }

    private List<Reward> parseToRewards(JSONObject obj) {
        if (obj == null || !obj.containsKey("itemInfo")) {
            return null;
        }

        JSONArray itemInfo = obj.getJSONArray("itemInfo");
        List<Reward> result = new ArrayList<>();
        for (Object itemObj : itemInfo) {
            if (!(itemObj instanceof JSONObject)) {
                continue;
            }

            JSONObject itemJsonObj = (JSONObject) itemObj;
            if (itemJsonObj.containsKey("itemType")
                    && itemJsonObj.containsKey("itemId")
                    && itemJsonObj.containsKey("count")) {
                Reward reward = RewardUtil.parseReward(itemJsonObj.getInteger("itemType"),
                        itemJsonObj.getInteger("itemId"),
                        itemJsonObj.getInteger("count"));
                if (reward != null) {
                    result.add(reward);
                }
            }
        }
        return result;
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }


 }
