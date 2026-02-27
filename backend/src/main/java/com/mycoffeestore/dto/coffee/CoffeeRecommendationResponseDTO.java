package com.mycoffeestore.dto.coffee;

import com.mycoffeestore.vo.coffee.CoffeeRecommendationVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 咖啡推荐响应DTO
 *
 * @author Backend Developer
 * @since 2024-02-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "咖啡推荐响应")
public class CoffeeRecommendationResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 推荐列表
     */
    @Schema(description = "推荐列表")
    private List<CoffeeRecommendationVO> recommendations;

    /**
     * 推荐消息
     */
    @Schema(description = "推荐消息", example = "根据您的偏好，为您推荐以下咖啡")
    private String message;
}
