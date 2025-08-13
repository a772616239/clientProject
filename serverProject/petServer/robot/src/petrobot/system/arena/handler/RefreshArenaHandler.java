package petrobot.system.arena.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.util.SyncExecuteFunction;
import protocol.Arena.ArenaOpponent;
import protocol.Arena.SC_ClaimArenaInfo.Builder;
import protocol.Arena.SC_RefreshArena;
import protocol.MessageId.MsgIdEnum;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author huhan
 * @date 2020/05/28
 */
@MsgId(msgId = MsgIdEnum.SC_RefreshArena_VALUE)
public class RefreshArenaHandler extends AbstractHandler<SC_RefreshArena> {
    @Override
    protected SC_RefreshArena parse(byte[] bytes) throws Exception {
        return SC_RefreshArena.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_RefreshArena req, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, robot -> {
            Builder arenaInfo = robot.getData().getArenaInfo();
            arenaInfo.setScore(req.getNewScore());

            List<ArenaOpponent> newOpponentInfoList = arenaInfo.getOpponnentInfoList().stream().filter(e ->
                    e != null && !req.getDefeatPlayerIdxList().contains(e.getSimpleInfo().getPlayerIdx())).collect(Collectors.toList());

            arenaInfo.clearOpponnentInfo().addAllOpponnentInfo(newOpponentInfoList);

        });
    }
}
