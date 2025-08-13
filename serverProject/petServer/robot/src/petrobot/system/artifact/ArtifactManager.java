package petrobot.system.artifact;

import cfg.PetRuneProperties;
import cfg.PetRunePropertiesObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import petrobot.robot.Robot;
import petrobot.robot.anotation.Controller;
import petrobot.robot.anotation.Index;
import petrobot.robotConst.DealResultConst;
import petrobot.robotConst.IndexConst;
import petrobot.util.LogUtil;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.CS_PetRuneBagInit;
import protocol.PetMessage.CS_PetRuneEquip;
import protocol.PetMessage.CS_PetRuneLvlUp;
import protocol.PetMessage.CS_PetRuneUnEquip;
import protocol.PetMessage.Pet;
import protocol.PetMessage.Rune;
import protocol.PlayerInfo;

/**
 * @author xiao_FL
 * @date 2019/12/17
 */
@Controller
public class ArtifactManager {

    private static final int upLv = 2;

    private static final int upStar = 1;

    @Index(IndexConst.Artifact_Star_Up)
    public void petRuneBagInit(Robot robot) {
        List<PlayerInfo.Artifact> artifactList = robot.getData().getBaseInfo().getArtifactList();
        PlayerInfo.CS_ArtifactUp.Builder msg = PlayerInfo.CS_ArtifactUp.newBuilder();
        if (artifactList.size() <= 1) {
            msg.setArtifactId(3);
        } else {
            msg.setArtifactId(1);
        }
        msg.setType(upStar);

        robot.getClient().send(MsgIdEnum.CS_ArtifactUp_VALUE, msg);
    }

    @Index(IndexConst.Artifact_Lv_Up)
    public void petRuneUnEquip(Robot robot) {
        List<PlayerInfo.Artifact> artifactList = robot.getData().getBaseInfo().getArtifactList();
        PlayerInfo.CS_ArtifactUp.Builder msg = PlayerInfo.CS_ArtifactUp.newBuilder();
        PlayerInfo.Artifact artifact = artifactList.get(RandomUtils.nextInt(artifactList.size()));
        msg.setArtifactId(artifact.getArtifactId());
        msg.setType(upLv);

        robot.getClient().send(MsgIdEnum.CS_ArtifactUp_VALUE, msg);
    }

}