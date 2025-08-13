package server.handler.redpoint;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.warpServer.WarpServerManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_RedPoint;
import protocol.ServerTransfer.GS_BS_RedPoint;
import protocol.ServerTransfer.ServerTypeEnum;

@MsgId(msgId = MsgIdEnum.GS_BS_RedPoint_VALUE)
public class RedPointNoticeHandler extends AbstractHandler<GS_BS_RedPoint> {
    @Override
    protected GS_BS_RedPoint parse(byte[] bytes) throws Exception {
        return GS_BS_RedPoint.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_BS_RedPoint req, int i) {
    	BS_GS_RedPoint.Builder builder = BS_GS_RedPoint.newBuilder();
    	builder.setPlayerId(req.getPlayerId());
    	builder.setType(req.getType());
    	builder.setState(req.getState());
    	int svrIndex = req.getSvrIndex();
    	if (svrIndex <= 0) {
    	    svrIndex = WarpServerManager.getInstance().getSeverIndexByIp(req.getIp());
        }
    	WarpServerManager.getInstance().sendMsgToServer(ServerTypeEnum.STE_GameServer_VALUE, svrIndex, MsgIdEnum.BS_GS_RedPoint_VALUE, builder);
    }

}
