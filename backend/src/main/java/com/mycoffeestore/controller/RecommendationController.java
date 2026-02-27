package com.mycoffeestore.controller;

import com.mycoffeestore.common.result.Result;
import com.mycoffeestore.dto.coffee.CoffeeRecommendationRequestDTO;
import com.mycoffeestore.dto.coffee.CoffeeRecommendationResponseDTO;
import com.mycoffeestore.service.coffee.CoffeeRecommendationService;
import com.mycoffeestore.vo.coffee.CoffeeRecommendationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 咖啡推荐控制器
 *
 * @author Backend Developer
 * @since 2024-02-27
 */
@Slf4j
@RestController
@RequestMapping("/v1/recommendation")
@RequiredArgsConstructor
@Tag(name = "咖啡推荐", description = "咖啡推荐接口")
public class RecommendationController {

    private final CoffeeRecommendationService recommendationService;

    @PostMapping
    @Operation(summary = "获取咖啡推荐", description = "根据用户角色和偏好推荐咖啡")
    public Result<CoffeeRecommendationResponseDTO> recommend(
            @RequestBody @Valid CoffeeRecommendationRequestDTO request) {

        log.info("收到咖啡推荐请求，角色: {}, 偏好: {}", request.getRoles(), request.getPreference());

        List<CoffeeRecommendationVO> recommendations = recommendationService.recommend(request);

        String message = generateMessage(request, recommendations.size());

        CoffeeRecommendationResponseDTO response = CoffeeRecommendationResponseDTO.builder()
                .recommendations(recommendations)
                .message(message)
                .build();

        return Result.success(message, response);
    }

    /**
     * 生成响应消息
     */
    private String generateMessage(CoffeeRecommendationRequestDTO request, int count) {
        if (count == 0) {
            return "暂时没有找到匹配的咖啡";
        }

        StringBuilder message = new StringBuilder("根据您的偏好");
        if (request.getPreference() != null && !request.getPreference().trim().isEmpty()) {
            message.append("（").append(request.getPreference()).append("）");
        }
        message.append("，为您推荐").append(count).append("款咖啡");
        return message.toString();
    }
}
