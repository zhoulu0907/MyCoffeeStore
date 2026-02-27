package com.mycoffeestore.service.coffee;

import com.mycoffeestore.dto.coffee.CoffeeRecommendationRequestDTO;
import com.mycoffeestore.vo.coffee.CoffeeRecommendationVO;

import java.util.List;

/**
 * 咖啡推荐服务接口
 *
 * @author Backend Developer
 * @since 2024-02-27
 */
public interface CoffeeRecommendationService {

    /**
     * 根据用户角色和偏好推荐咖啡
     *
     * @param request 推荐请求
     * @return 推荐列表
     */
    List<CoffeeRecommendationVO> recommend(CoffeeRecommendationRequestDTO request);
}
