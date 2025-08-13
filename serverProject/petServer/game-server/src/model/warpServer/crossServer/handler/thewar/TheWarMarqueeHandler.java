package model.warpServer.crossServer.handler.thewar;

import cfg.MarqueeTemplate;
import cfg.MarqueeTemplateObject;
import cfg.ServerStringRes;
import cfg.TheWarGroupConfig;
import cfg.TheWarGroupConfigObject;
import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import java.util.List;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import org.springframework.util.CollectionUtils;
import protocol.Common.LanguageEnum;
import protocol.Common.SC_Marquee;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_BroadcastMarquee;
import util.ArrayUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_GS_BroadcastMarquee_VALUE)
public class TheWarMarqueeHandler extends AbstractHandler<CS_GS_BroadcastMarquee> {
    @Override
    protected CS_GS_BroadcastMarquee parse(byte[] bytes) throws Exception {
        return CS_GS_BroadcastMarquee.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_BroadcastMarquee ret, int i) {
        if (CollectionUtils.isEmpty(ret.getPlayerIdListList())) {
            return;
        }
        MarqueeTemplateObject templateCfg = MarqueeTemplate.getById(ret.getMarqueeId());
        if (templateCfg == null) {
            LogUtil.error("TheWar marquee template not found, id = " + ret.getMarqueeId());
            return;
        }

        playerEntity player;
        for (String playerIdx : ret.getPlayerIdListList()) {
            player = playerCache.getByIdx(playerIdx);
            if (player == null || !player.isOnline()) {
                continue;
            }
            String[] realParamList = parseWarMarqueParams(ret.getMarqueeId(), player.getLanguage(), ret.getParamsList());
            String content = ServerStringRes.getContentByLanguage(templateCfg.getContent(), player.getLanguage(), realParamList);
            SC_Marquee.Builder builder = SC_Marquee.newBuilder()
                    .setCycleCount(templateCfg.getRolltimes())
                    .setInfo(content)
                    .addAllScenesValue(ArrayUtil.intArrayToList(templateCfg.getScene()))
                    .setPriorityValue(templateCfg.getPriority())
                    .setDuration(templateCfg.getDuration());
            GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_Marquee_VALUE, builder);
        }
    }

    protected String parseCampParam(int camp, LanguageEnum language) {
        TheWarGroupConfigObject cfg = TheWarGroupConfig.getByGroupid(camp);
        if (cfg == null) {
            return "";
        }
        return ServerStringRes.getContentByLanguage(cfg.getServergroupname(), language);
    }

    protected String[] parseWarMarqueParams(int marqueeId, LanguageEnum languageEnum, List<String> params) {
        if (CollectionUtils.isEmpty(params)) {
            return null;
        }
        String[] realParamList = new String[params.size()];
        for (int i = 0; i < params.size(); i++) {
            if ((marqueeId == 25 || marqueeId == 26) // 远征阵营
                    && (i == 0 || i == 2)) {
                realParamList[i] = (parseCampParam(Integer.parseInt(params.get(i)), languageEnum));
            } else {
                realParamList[i]= params.get(i);
            }
        }
        return realParamList;
    }
}
