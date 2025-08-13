package server.handler.battle;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.battle.AbstractBattleController;
import model.battle.BattleManager;
import protocol.Battle.CS_EnterNextBattle;
import protocol.Battle.SC_EnterFight;
import protocol.Battle.SC_EnterNextBattle;
import protocol.Battle.SC_EnterNextBattle.Builder;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020/05/18
 */
@MsgId(msgId = MsgIdEnum.CS_EnterNextBattle_VALUE)
public class EnterNextBattleHandler extends AbstractBaseHandler<CS_EnterNextBattle> {
    @Override
    protected CS_EnterNextBattle parse(byte[] bytes) throws Exception {
        return CS_EnterNextBattle.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_EnterNextBattle req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        Builder resultBuilder = SC_EnterNextBattle.newBuilder();

        AbstractBattleController controller = BattleManager.getInstance().getController(playerIdx);
        if (controller == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Battle_NotInBattle));
            gsChn.send(MsgIdEnum.SC_EnterNextBattle_VALUE, resultBuilder);
            return;
        }
        RetCodeEnum ret = controller.enterNextBattle();
        resultBuilder.setRetCode(GameUtil.buildRetCode(ret));
        gsChn.send(MsgIdEnum.SC_EnterNextBattle_VALUE, resultBuilder);

        if (ret == RetCodeEnum.RCE_Success) {
            SC_EnterFight.Builder builder = controller.buildEnterBattleBuilder();
            controller.setPveEnterFightData(builder);
            gsChn.send(MsgIdEnum.SC_EnterFight_VALUE, builder);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Battle;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        doAction(gsChn, codeNum);
    }
}
