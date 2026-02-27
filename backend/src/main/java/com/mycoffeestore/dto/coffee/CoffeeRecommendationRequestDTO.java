package com.mycoffeestore.dto.coffee;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 咖啡推荐请求DTO
 *
 * @author Backend Developer
 * @since 2024-02-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "咖啡推荐请求")
public class CoffeeRecommendationRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户角色列表：beginner-初学者，energy-提神需求，drip-手冲爱好者
     */
    @Schema(description = "用户角色列表：beginner-初学者，energy-提神需求，drip-手冲爱好者",
            example = "[\"beginner\"]")
    @NotEmpty(message = "角色列表不能为空")
    @Size(min = 1, max = 3, message = "角色数量必须在1-3个之间")
    private List<String> roles;

    /**
     * 用户口味描述
     */
    @Schema(description = "用户口味描述", example = "不要太酸")
    @Size(max = 200, message = "口味描述长度不能超过200字符")
    private String preference;
}
