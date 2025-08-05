package com.alibaba.csp.sentinel.dashboard.rule.redis;

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
@Component("redisFlowRuleApiProvider")
public class RedisFlowRuleApiProvider implements DynamicRuleProvider<List<FlowRuleEntity>> {


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
    public List<FlowRuleEntity> getRules(String appName) throws Exception {
        if (StringUtil.isBlank(appName)) {
            return new ArrayList<>();
        }
        Object json = stringRedisTemplate.opsForValue().get(RuleConstants.SENTINEL_FLOW_KEY + appName);
        if (json != null) {
            List<FlowRuleEntity> flowRuleEntities = JSON.parseArray(String.valueOf(json), FlowRuleEntity.class);
            return flowRuleEntities;
        }
        return new ArrayList<>();
    }
}
