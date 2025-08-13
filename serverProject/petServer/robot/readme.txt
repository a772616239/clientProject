1.因为目前每个项目组通讯底层不一致，需要自己补全client类，实现一个可以连接服务器的client。以及添加protobuf相关的协议包等

2.Robot类里面有个heart()方法，需要在里面发送和服务器的心跳消息。默认为6秒一次。可在RobotManager里面根据情况修改

3.PlayerData为robot所需要的缓存数据集合类

4.机器人每次操作检测的间隔为500毫秒。可在RobotManager里面根据情况修改

5.robot.properties文件为机器人基础配置里面有详细解释

6.index.xlsx为机器人逻辑配置表。其中index为执行步骤序号（无需配置从1递增拉表即可），method为步骤对应的执行方法需要

7.设置一个执行方法需要根据功能模块创建对应包（为了结构清晰）,包中handler包为接受服务器返回消息的消息类，handler需要自己注册（类似服务器的handler），
  Manager类为逻辑执行类，需要加上@contorller类注解，manager里面的方法为需要录入到index.xlsx里面method的方法需要加@Index(value=步骤序号)注解。
  ps:参考示例item包

8.robot中dealResult意思  1为当步骤结束(自动执行下个配置表步骤)   0当前步骤执行失败（机器人流程有误，需要人工检查机器人失败原因） -1正在执行某一步骤

9.robotAI会自己根据index.xlsx顺序执行逻辑步骤。 
   一个完整的机器人步骤流程为：1）.robotAI中设置dealResult为-1
                             2）.执行该步骤对应的index.xlsx里面的步骤方法（该方法为7中manager的方法，必定是发消息给服务器交互）
                             3）.如果不需要等待服务器返回消息，直接在manger方法中判断步骤是否成功，设置dealResult为0或者1.（需要程序自己写设置逻辑）
                             4).如果有服务器返回消息,在handler中根据返回数据处理逻辑。 如果该步骤还未结束，继续和服务器交互，如果结束根据返回设置dealResult为0或者1
                             5）.robotAI根据dealResult值继续往后处理。当前默认为最后一步无限循环执行。（最后一步一般配置为打怪什么的操作，保持机器人一直在跑）