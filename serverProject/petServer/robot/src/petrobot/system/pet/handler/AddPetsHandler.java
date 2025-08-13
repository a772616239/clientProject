package petrobot.system.pet.handler;

import cfg.PetBaseProperties;
import cfg.PetBasePropertiesObject;
import com.google.protobuf.ProtocolStringList;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.util.LogUtil;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.Pet;
import protocol.PetMessage.SC_AddPets;
import protocol.PetMessage.SC_PetBagInit.Builder;

import java.util.List;

/**
 * @Description
 * @Author hanx
 * @Date2020/10/23 0023 17:29
 **/
@MsgId(msgId = MsgIdEnum.SC_AddPets_VALUE)
public class AddPetsHandler extends AbstractHandler<SC_AddPets> {
    @Override
    protected SC_AddPets parse(byte[] bytes) throws Exception {
        return SC_AddPets.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_AddPets result, int ii) {
        List<Integer> bookIdList = result.getBookIdList();
        ProtocolStringList petIdList = result.getPetIdList();


        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }

        if (bookIdList.size() != petIdList.size() || bookIdList.size() == 0) {
            LogUtil.error("SC_AddPets data error,bookIdList: " + bookIdList + "petIdList: " + petIdList);
        }
        SyncExecuteFunction.executeConsumer(robot, r -> {
            //背包信息为空直接返回 因为背包初始化会初始全量信息
            if (robot.getData().getPetBag() == null) {
                return;
            }
            Builder petBag = robot.getData().getPetBag().toBuilder();
            int bookId;
            String petId;
            for (int i = 0; i < petIdList.size(); i++) {
                bookId = bookIdList.get(i);
                petId = petIdList.get(i);
                Pet.Builder petBuilder = getPetBuilder(petId, bookId);
                petBag.addPet(petBuilder);
            }
            robot.getData().setPetBag(petBag.build());
        });
    }


    public Pet.Builder getPetBuilder(String petId, int petBookId) {

        PetBasePropertiesObject config = PetBaseProperties.getByPetid(petBookId);
        if (config == null) {
            LogUtil.error("error in PetServiceImpl,method getPetEntity():pet cfg is null" + "\n");
            return null;
        }
        // 配置表属性部分数据已*1000，注意

        Pet.Builder result = Pet.newBuilder();
        // 基础属性
        result.setId(petId);
        result.setPetBookId(petBookId);
        result.setPetLvl(1);
        result.setPetRarity(config.getStartrarity());
        result.setPetAliveStatus(1);
        return result;
    }
}
