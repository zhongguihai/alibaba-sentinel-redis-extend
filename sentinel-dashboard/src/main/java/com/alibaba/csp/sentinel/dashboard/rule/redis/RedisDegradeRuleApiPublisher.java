package com.alibaba.csp.sentinel.dashboard.rule.redis;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 流控Publisher
 */
@Component("redisDegradeRuleApiPublisher")
public class RedisDegradeRuleApiPublisher implements DynamicRulePublisher<List<DegradeRuleEntity>> {


    @Resource
    public StringRedisTemplate stringRedisTemplate;

    /**
     * 更新规则，并通知客户端刷新规则
     *
     * @param app   app name
     * @param rules list of rules to push
     * @throws Exception
     */
    @Override
    public void publish(String app, List<DegradeRuleEntity> rules) throws Exception {
        if (StringUtil.isBlank(app)) {
            return;
        }
        if (rules == null) {
            rules = new ArrayList<>();
        }
        String s = JSON.toJSONString(rules);
        List<DegradeRule> degradeRules = rules.stream().map(DegradeRuleEntity::toRule).collect(Collectors.toList());

        stringRedisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            public List<Object> execute(RedisOperations operations) {
                operations.multi();
                operations.opsForValue().set(RuleConstants.SENTINEL_DEGRADE_KEY + app, s);
//                operations.opsForValue().set(RuleConstants.SENTINEL_FLOW_RULE_SIMP_KEY + app, simple);
                // 通知订阅者，降级规则更新了
                operations.convertAndSend(RuleConstants.DEGRADE_RULE_CHANNEL, s);
                return operations.exec();
            }
        });
    }
}
