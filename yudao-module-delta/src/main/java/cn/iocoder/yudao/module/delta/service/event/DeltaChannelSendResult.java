package cn.iocoder.yudao.module.delta.service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 渠道发送结果 DTO
 *
 * @author Delta-Vanguard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaChannelSendResult {

    /** 是否成功 */
    private boolean success;

    /** 发送状态：SUCCESS / FAILED / SKIPPED / DEAD */
    private String status;

    /** 微信返回码 */
    private String responseCode;

    /** 微信返回摘要 */
    private String responseMessage;

    /** 错误摘要 */
    private String errorMessage;

    /** 是否需要重试 */
    private boolean retryable;

    public static DeltaChannelSendResult success(String responseCode, String responseMessage) {
        return DeltaChannelSendResult.builder()
                .success(true)
                .status("SUCCESS")
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
    }

    public static DeltaChannelSendResult failed(String errorMessage, boolean retryable) {
        return DeltaChannelSendResult.builder()
                .success(false)
                .status(retryable ? "FAILED" : "DEAD")
                .errorMessage(errorMessage)
                .retryable(retryable)
                .build();
    }

    public static DeltaChannelSendResult skipped(String errorMessage) {
        return DeltaChannelSendResult.builder()
                .success(false)
                .status("SKIPPED")
                .errorMessage(errorMessage)
                .retryable(false)
                .build();
    }
}
