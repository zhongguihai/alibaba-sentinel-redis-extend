package com.alibaba.csp.sentinel.dashboard.rule.redis;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 流控Provider
 */
@Component("redisDegradeRuleApiProvider")
public class RedisDegradeRuleApiProvider implements DynamicRuleProvider<List<DegradeRuleEntity>> {


    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 从redis中读取规则
     *
     * @param appName
     * @return
     * @throws Exception
     */
    @Override
    public List<DegradeRuleEntity> getRules(String appName) throws Exception {
        if (StringUtil.isBlank(appName)) {
            return new ArrayList<>();
        }
        Object json = stringRedisTemplate.opsForValue().get(RuleConstants.SENTINEL_DEGRADE_KEY + appName);
        if (json != null) {
            List<DegradeRuleEntity> degradeRuleEntities = JSON.parseArray(String.valueOf(json), DegradeRuleEntity.class);
            return degradeRuleEntities;
        }
        return new ArrayList<>();
    }
}
