package server.handler.pet.rune;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.petrune.dbCache.petruneCache;
import protocol.Common.EnumFunction;
import protocol.PetMessage.CS_PetRuneQuery;
import protocol.PetMessage.Rune;
import protocol.PetMessage.SC_PetRuneQuery;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

import java.util.List;

import static protocol.MessageId.MsgIdEnum.CS_PetRuneQuery_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_PetRuneQuery_VALUE;

/**
 * 处理客户端查询宠物装备的符文请求
 *
 * @author xiao_FL
 * @date 2019/6/17
 */
@MsgId(msgId = CS_PetRuneQuery_VALUE)
public class PetRuneQueryHandler extends AbstractBaseHandler<CS_PetRuneQuery> {

    @Override
    protected CS_PetRuneQuery parse(byte[] bytes) throws Exception {
        return CS_PetRuneQuery.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetRuneQuery req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());

        SC_PetRuneQuery.Builder resultBuilder = SC_PetRuneQuery.newBuilder();
        List<Rune> queryResult = petruneCache.getInstance().getPetRune(playerId, req.getPetId());
        if (queryResult != null) {
            resultBuilder.addAllRune(queryResult);
        }
        resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(SC_PetRuneQuery_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(protocol.MessageId.MsgIdEnum.SC_PetRuneQuery_VALUE, SC_PetRuneQuery.newBuilder().setResult(retCode));
    }


}
