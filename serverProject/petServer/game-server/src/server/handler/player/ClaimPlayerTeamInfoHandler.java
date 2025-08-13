package server.handler.player;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.ArrayList;
import java.util.List;
import model.pet.dbCache.petCache;
import model.team.dbCache.teamCache;
import model.team.entity.Team;
import model.team.entity.teamEntity;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Battle.BattlePetData;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerInfo.CS_ClaimPlayerTeamInfo;
import protocol.PlayerInfo.PlayerTeamInfo;
import protocol.PlayerInfo.SC_ClaimPlayerTeamInfo;
import protocol.PlayerInfo.SC_ClaimPlayerTeamInfo.Builder;
import protocol.PrepareWar.TeamNumEnum;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2021/3/3
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimPlayerTeamInfo_VALUE)
public class ClaimPlayerTeamInfoHandler extends AbstractBaseHandler<CS_ClaimPlayerTeamInfo> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.NullFuntion;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        Builder builder = SC_ClaimPlayerTeamInfo.newBuilder()
                .setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_ClaimPlayerTeamInfo_VALUE, builder);
    }

    @Override
    protected CS_ClaimPlayerTeamInfo parse(byte[] bytes) throws Exception {
        return CS_ClaimPlayerTeamInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimPlayerTeamInfo req, int i) {
        teamEntity entity = teamCache.getInstance().getTeamEntityByPlayerId(req.getPlayerIdx());

        SC_ClaimPlayerTeamInfo.Builder resultBuilder = SC_ClaimPlayerTeamInfo.newBuilder();
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimPlayerTeamInfo_VALUE, resultBuilder);
            return;
        }

        TeamNumEnum nowUsedTeamNum = entity.getNowUsedTeamNum(TeamTypeEnum.TTE_Common);
        Team dbTeam = entity.getDBTeam(nowUsedTeamNum);
        if (dbTeam == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimPlayerTeamInfo_VALUE, resultBuilder);
            return;
        }
        List<String> petIdxList = new ArrayList<>(dbTeam.getLinkPetMap().values());
        List<BattlePetData> battlePetData
                = petCache.getInstance().getPetBattleData(req.getPlayerIdx(), petIdxList, BattleSubTypeEnum.BSTE_ChallengePlayer);

        PlayerTeamInfo.Builder teamInfo = PlayerTeamInfo.newBuilder();
        teamInfo.setTeamNum(nowUsedTeamNum.getNumber());
        if (CollectionUtils.isNotEmpty(battlePetData)) {
            teamInfo.addAllPets(battlePetData);
        }
        teamInfo.addAllSkills(dbTeam.getLinkSkillMap().values());


        resultBuilder.setTeamInfo(teamInfo);
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_ClaimPlayerTeamInfo_VALUE, resultBuilder);
    }
}
