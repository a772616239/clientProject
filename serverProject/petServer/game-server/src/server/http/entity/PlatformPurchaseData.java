package server.http.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import lombok.ToString;

/**
 * @Description 平台购买消息
 * @Author hanx
 * @Date2020/6/19 0019 14:47
 **/
@Getter
@Setter
@ToString
public class PlatformPurchaseData {
    private String userId;
    private String orderNo;
    private int serverIndex;
    private BigDecimal payPrice;
    private String productCode;
    private String productName;
    private int payType;
    private String clientId;
    private String channel;
    private String platform;

    public String getProductString() {
        return "productCode:" + productCode + ", productName:" + productName + ", payPrice:" + payPrice;
    }
}