package petrobot.system.thewar;

import java.util.Random;
import lombok.Getter;
import lombok.Setter;
import petrobot.robot.Robot;
import protocol.TheWarDB.WarPlayerDB;
import protocol.TheWarDefine.Position;

@Getter
@Setter
public class RobotTheWarData {
    protected Robot owner;
    protected String warRoomIdx;

    protected int camp;
    protected int jobTile;

    protected WarPlayerDB.Builder playerData;

    public RobotTheWarData(Robot robot) {
        this.owner = robot;
        this.playerData = WarPlayerDB.newBuilder();
    }

    public Position getAvailableAttackPos() {
       int randPosIndex = new Random().nextInt(getPlayerData().getOwnedGridPosCount());
       return playerData.getOwnedGridPos(randPosIndex);
    }
}
