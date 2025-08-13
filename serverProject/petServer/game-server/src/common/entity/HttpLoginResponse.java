package common.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * 平台返回结果实体
 *
 * @author xiao_FL
 * @date 2019/7/3
 */

@Setter
@Getter
public class HttpLoginResponse extends HttpCommonResponse {
    private String userId;
    private String token;
    private String realName;
    private String phone;
    private boolean certification;
    private String idCard;
}