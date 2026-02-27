package com.mycoffeestore.service.impl.coffee;

import com.mycoffeestore.dto.coffee.CoffeeRecommendationRequestDTO;
import com.mycoffeestore.entity.Coffee;
import com.mycoffeestore.mapper.CoffeeMapper;
import com.mycoffeestore.service.coffee.CoffeeRecommendationService;
import com.mycoffeestore.vo.coffee.CoffeeRecommendationVO;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 咖啡推荐服务实现类
 *
 * @author Backend Developer
 * @since 2024-02-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoffeeRecommendationServiceImpl implements CoffeeRecommendationService {

    private final CoffeeMapper coffeeMapper;

    /**
     * 角色到咖啡分类的映射
     */
    private static final Map<String, List<String>> ROLE_CATEGORY_MAP = Map.of(
            "beginner", List.of("espresso"),
            "energy", List.of("espresso", "cold"),
            "drip", List.of("brew")
    );

    /**
     * 角色到咖啡关键词的映射
     */
    private static final Map<String, List<String>> ROLE_KEYWORD_MAP = Map.of(
            "beginner", List.of("拿铁", "卡布奇诺", "摩卡", "焦糖"),
            "energy", List.of("美式", "浓缩", "冷萃", "冰"),
            "drip", List.of("耶加", "哥斯达", "肯尼亚", "手冲")
    );

    /**
     * 推荐理由模板
     */
    private static final Map<String, String> REASON_TEMPLATES = Map.of(
            "beginner", "适合咖啡初学者，口感温和不苦涩",
            "energy", "精选高咖啡因豆种，提神醒脑效果好",
            "drip", "精品产区豆，展现咖啡原本风味"
    );

    @Override
    public List<CoffeeRecommendationVO> recommend(CoffeeRecommendationRequestDTO request) {
        log.info("开始咖啡推荐，角色: {}, 偏好: {}", request.getRoles(), request.getPreference());

        // 1. 根据角色筛选咖啡
        List<Coffee> candidates = findCandidatesByRoles(request.getRoles());

        if (candidates.isEmpty()) {
            // 如果没有匹配的咖啡，返回所有上架的咖啡
            candidates = getAllAvailableCoffees();
        }

        // 2. 根据偏好进一步筛选（如果有偏好描述）
        if (request.getPreference() != null && !request.getPreference().trim().isEmpty()) {
            candidates = filterByPreference(candidates, request.getPreference());
        }

        // 3. 随机选择最多3款咖啡
        List<Coffee> selected = randomlySelect(candidates, 3);

        // 4. 生成推荐结果
        List<CoffeeRecommendationVO> recommendations = new ArrayList<>();
        Set<String> usedRoles = new HashSet<>(request.getRoles());

        for (int i = 0; i < selected.size(); i++) {
            Coffee coffee = selected.get(i);
            String primaryRole = findPrimaryRoleForCoffee(coffee, usedRoles);
            String reason = generateReason(coffee, primaryRole, request.getPreference());
            int rating = calculateRating(coffee, primaryRole);

            recommendations.add(CoffeeRecommendationVO.builder()
                    .coffeeId(coffee.getId())
                    .name(coffee.getName())
                    .description(coffee.getDescription())
                    .imageUrl(coffee.getImageUrl())
                    .price(coffee.getPrice())
                    .reason(reason)
                    .rating(rating)
                    .build());
        }

        log.info("推荐完成，共推荐 {} 款咖啡", recommendations.size());
        return recommendations;
    }

    /**
     * 根据角色查找候选咖啡
     */
    private List<Coffee> findCandidatesByRoles(List<String> roles) {
        Set<String> targetCategories = new HashSet<>();
        Set<String> targetKeywords = new HashSet<>();

        for (String role : roles) {
            List<String> categories = ROLE_CATEGORY_MAP.get(role);
            List<String> keywords = ROLE_KEYWORD_MAP.get(role);

            if (categories != null) {
                targetCategories.addAll(categories);
            }
            if (keywords != null) {
                targetKeywords.addAll(keywords);
            }
        }

        // 按分类查询
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(Coffee::getStatus, 1)
                .eq(Coffee::getIsDeleted, 0)
                .orderBy(Coffee::getSales, false)
                .orderBy(Coffee::getSortOrder, false);

        if (!targetCategories.isEmpty()) {
            queryWrapper.in(Coffee::getCategory, targetCategories);
        }

        List<Coffee> results = coffeeMapper.selectListByQuery(queryWrapper);

        // 如果有指定关键词，进一步过滤
        if (!targetKeywords.isEmpty()) {
            results = results.stream()
                    .filter(coffee -> targetKeywords.stream()
                            .anyMatch(keyword -> coffee.getName().contains(keyword)))
                    .collect(Collectors.toList());
        }

        return results;
    }

    /**
     * 根据偏好过滤咖啡
     */
    private List<Coffee> filterByPreference(List<Coffee> coffees, String preference) {
        String pref = preference.toLowerCase();

        // 简单的关键词匹配逻辑
        if (pref.contains("酸") || pref.contains("苦")) {
            // 用户怕酸怕苦，优先推荐奶基咖啡
            return coffees.stream()
                    .filter(c -> c.getName().contains("拿铁") ||
                                 c.getName().contains("卡布奇诺") ||
                                 c.getName().contains("摩卡") ||
                                 c.getName().contains("焦糖"))
                    .collect(Collectors.toList());
        } else if (pref.contains("浓") || pref.contains("烈")) {
            // 用户喜欢浓郁，推荐美式和浓缩
            return coffees.stream()
                    .filter(c -> c.getName().contains("美式") ||
                                 c.getName().contains("浓缩"))
                    .collect(Collectors.toList());
        } else if (pref.contains("甜")) {
            // 用户喜欢甜，推荐带糖的咖啡
            return coffees.stream()
                    .filter(c -> c.getName().contains("焦糖") ||
                                 c.getName().contains("摩卡") ||
                                 c.getName().contains("玛奇朵"))
                    .collect(Collectors.toList());
        }

        return coffees;
    }

    /**
     * 随机选择指定数量的咖啡
     */
    private List<Coffee> randomlySelect(List<Coffee> coffees, int count) {
        if (coffees.size() <= count) {
            return new ArrayList<>(coffees);
        }

        List<Coffee> copy = new ArrayList<>(coffees);
        Collections.shuffle(copy, ThreadLocalRandom.current());
        return copy.subList(0, count);
    }

    /**
     * 查找咖啡对应的主要角色
     */
    private String findPrimaryRoleForCoffee(Coffee coffee, Set<String> roles) {
        String name = coffee.getName();

        for (String role : roles) {
            List<String> keywords = ROLE_KEYWORD_MAP.get(role);
            if (keywords != null) {
                for (String keyword : keywords) {
                    if (name.contains(keyword)) {
                        return role;
                    }
                }
            }
        }

        // 默认返回第一个角色
        return roles.iterator().next();
    }

    /**
     * 生成推荐理由
     */
    private String generateReason(Coffee coffee, String role, String preference) {
        StringBuilder reason = new StringBuilder();

        String baseReason = REASON_TEMPLATES.getOrDefault(role, "为您精心推荐");
        reason.append(baseReason);

        // 根据偏好添加额外说明
        if (preference != null && !preference.trim().isEmpty()) {
            if (preference.contains("酸") || preference.contains("苦")) {
                reason.append("，奶香中和了咖啡的苦涩");
            } else if (preference.contains("浓") || preference.contains("烈")) {
                reason.append("，口感浓郁醇厚");
            } else if (preference.contains("甜")) {
                reason.append("，甜美可口");
            }
        }

        reason.append("。").append(coffee.getDescription());
        return reason.toString();
    }

    /**
     * 计算推荐评分（1-5）
     */
    private int calculateRating(Coffee coffee, String role) {
        // 基础分数
        int baseScore = 4;

        // 根据销量加分
        if (coffee.getSales() > 1000) {
            baseScore++;
        }

        // 根据角色匹配度加分
        String name = coffee.getName();
        List<String> keywords = ROLE_KEYWORD_MAP.get(role);
        if (keywords != null && keywords.stream().anyMatch(name::contains)) {
            baseScore = Math.min(5, baseScore + 1);
        }

        return Math.min(5, Math.max(1, baseScore));
    }

    /**
     * 获取所有可用的咖啡
     */
    private List<Coffee> getAllAvailableCoffees() {
        return coffeeMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq(Coffee::getStatus, 1)
                        .eq(Coffee::getIsDeleted, 0)
                        .orderBy(Coffee::getSales, false)
        );
    }
}
