package petrobot.system.petrune.handler;

import cfg.PetRuneProperties;
import cfg.PetRunePropertiesObject;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import org.apache.commons.collections4.CollectionUtils;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.LogUtil;
import petrobot.util.SyncExecuteFunction;
import protocol.GM.CS_GM;
import protocol.GM.CS_GM.Builder;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.Rune;
import protocol.PetMessage.SC_PetRuneBagInit;

/**
 * @author xiao_FL
 * @date 2019/12/20
 */
@MsgId(msgId = MsgIdEnum.SC_PetRuneBagInit_VALUE)
public class PetRuneBagInitHandler extends AbstractHandler<SC_PetRuneBagInit> {

    @Override
    protected SC_PetRuneBagInit parse(byte[] bytes) throws Exception {
        return SC_PetRuneBagInit.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, SC_PetRuneBagInit scPetRuneBagInit, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gameServerTcpChannel.channel);
        if (robot == null || scPetRuneBagInit.getPageNum() > 1 || robot.getData().getPetRuneList().size() > 20) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robot, r -> {
            robot.getData().getPetRuneList().clear();
            if (CollectionUtils.isNotEmpty(scPetRuneBagInit.getRuneList())) {
                for (Rune rune : scPetRuneBagInit.getRuneList()) {
                    PetRunePropertiesObject thisRune = PetRuneProperties.getByRuneid(rune.getRuneBookId());
                    if (thisRune == null) {
                        LogUtil.error("SC_PetRuneBagInitHandler cant`t find PetRuneProperties by rune book id:" + rune.getRuneBookId());
                        continue;
                    }
                    robot.getData().getPetRuneList().add(rune);
                }

            }
            robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        });
        if (scPetRuneBagInit.getTotalPage() <= 1 && robot.getData().getPetRuneList().size() < 10) {
            Builder builder = CS_GM.newBuilder();
            builder.setStr("petRune");
            robot.getClient().send(MsgIdEnum.CS_GM_VALUE, builder);
        }

    }

    public int getRuneRarity(int bookId) {
        PetRunePropertiesObject thisRune = PetRuneProperties.getByRuneid(bookId);
        if (thisRune == null) {
            LogUtil.error("getRuneRarity() cant`t find PetRuneProperties by rune book id:" + bookId);
            return 0;
        }
        return thisRune.getRunerarity();
    }
}
