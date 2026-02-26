package com.mycoffeestore.service.impl.coffee;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycoffeestore.common.result.PageResult;
import com.mycoffeestore.entity.Coffee;
import com.mycoffeestore.exception.BusinessException;
import com.mycoffeestore.mapper.CoffeeMapper;
import com.mycoffeestore.service.coffee.CoffeeService;
import com.mycoffeestore.vo.coffee.CoffeeCategoryVO;
import com.mycoffeestore.vo.coffee.CoffeeDetailVO;
import com.mycoffeestore.vo.coffee.CoffeeListItemVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 咖啡产品服务实现类
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoffeeServiceImpl implements CoffeeService {

    private final CoffeeMapper coffeeMapper;
    private final ObjectMapper objectMapper;

    @Override
    public PageResult<CoffeeListItemVO> list(String category, Integer page, Integer size) {
        // 构建查询条件
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(Coffee::getStatus, 1) // 只查询上架的咖啡
                .eq(Coffee::getIsDeleted, 0)
                .orderBy(Coffee::getSortOrder, false)
                .orderBy(Coffee::getCreateTime, false);

        if (category != null && !category.isEmpty()) {
            queryWrapper.eq(Coffee::getCategory, category);
        }

        // 分页查询
        Page<Coffee> pageResult = coffeeMapper.paginate(page, size, queryWrapper);

        // 转换为VO
        List<CoffeeListItemVO> voList = pageResult.getRecords().stream()
                .map(this::convertToListItemVO)
                .collect(Collectors.toList());

        return PageResult.<CoffeeListItemVO>builder()
                .total(pageResult.getTotalRow())
                .page(page)
                .size(size)
                .list(voList)
                .build();
    }

    @Override
    public CoffeeDetailVO detail(Long coffeeId) {
        Coffee coffee = coffeeMapper.selectOneById(coffeeId);
        if (coffee == null) {
            throw new BusinessException(2001, "咖啡不存在");
        }

        return convertToDetailVO(coffee);
    }

    @Override
    public List<CoffeeCategoryVO> categories() {
        // 查询所有分类并统计数量
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .eq(Coffee::getStatus, 1)
                .eq(Coffee::getIsDeleted, 0);

        List<Coffee> coffees = coffeeMapper.selectListByQuery(queryWrapper);

        // 按分类统计
        return coffees.stream()
                .collect(Collectors.groupingBy(Coffee::getCategory,
                        Collectors.collectingAndThen(Collectors.toList(), list -> {
                            String category = list.get(0).getCategory();
                            String categoryName = list.get(0).getCategoryName();
                            return CoffeeCategoryVO.builder()
                                    .code(category)
                                    .name(categoryName)
                                    .count(list.size())
                                    .build();
                        })))
                .values()
                .stream()
                .sorted((a, b) -> a.getCode().compareTo(b.getCode()))
                .collect(Collectors.toList());
    }

    /**
     * 转换为列表项VO
     */
    private CoffeeListItemVO convertToListItemVO(Coffee coffee) {
        CoffeeListItemVO vo = new CoffeeListItemVO();
        BeanUtils.copyProperties(coffee, vo);
        vo.setCoffeeId(coffee.getId());
        return vo;
    }

    /**
     * 转换为详情VO
     */
    private CoffeeDetailVO convertToDetailVO(Coffee coffee) {
        CoffeeDetailVO vo = new CoffeeDetailVO();
        BeanUtils.copyProperties(coffee, vo);
        vo.setCoffeeId(coffee.getId());

        // 解析图片列表JSON
        if (coffee.getImages() != null && !coffee.getImages().isEmpty()) {
            try {
                List<String> imageList = objectMapper.readValue(coffee.getImages(),
                        new TypeReference<List<String>>() {});
                vo.setImages(imageList);
            } catch (Exception e) {
                log.error("解析图片列表失败: {}", coffee.getImages(), e);
                vo.setImages(new ArrayList<>());
            }
        } else {
            vo.setImages(new ArrayList<>());
        }

        return vo;
    }
}
