package server.handler.chat.entity;

import java.util.ArrayList;
import java.util.List;
import protocol.Chat;

public class SystemChatCreator {

    private final List<Chat.SystemChatEntity> chatEntities = new ArrayList();

    public SystemChatCreator addPlayerIdx(String playerIdx) {
        return addParamEntity(buildChatEntity(Chat.SystemEntityType.SET_PlayerId, playerIdx));
    }

    public SystemChatCreator addPlayerName(String playerName) {
        return addParamEntity(buildChatEntity(Chat.SystemEntityType.SET_PlayerName, playerName));
    }

    public SystemChatCreator addPetRarity(int petRarity) {
        return addParamEntity(buildChatEntity(Chat.SystemEntityType.SET_PetRarity, String.valueOf(petRarity)));
    }

    public SystemChatCreator addPetCfgId(int petCfgId) {
        return addParamEntity(buildChatEntity(Chat.SystemEntityType.SET_PetId, String.valueOf(petCfgId)));
    }

    private SystemChatCreator addParamEntity(Chat.SystemChatEntity entity) {
        chatEntities.add(entity);
        return this;
    }


    /**
     * 增加指定类型参数
     * @param paramsType
     * @param params
     * @return
     */
    public SystemChatCreator addParamByType(Chat.SystemEntityType paramsType, String params) {
        chatEntities.add(buildChatEntity(paramsType, params));
        return this;
    }

    /**
     * 加入客户端直接展示的参数
     * (宠物id,玩家id...品质这些需要走其他方法加入参数)
     * @param params
     * @return
     */
    public SystemChatCreator addDirectShowParams(String... params) {
        for (String param : params) {
            addParamEntity(buildChatEntity(Chat.SystemEntityType.SET_Null, param));
        }
        return this;
    }


    public Chat.SystemChatEntity buildChatEntity(Chat.SystemEntityType type, String value) {
        return Chat.SystemChatEntity.newBuilder().setParams(value).setType(type).build();
    }

    public List<Chat.SystemChatEntity> createParamsList() {
        return chatEntities;
    }

}
