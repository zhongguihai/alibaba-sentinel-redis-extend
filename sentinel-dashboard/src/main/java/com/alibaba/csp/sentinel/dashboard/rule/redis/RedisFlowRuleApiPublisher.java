package com.alibaba.csp.sentinel.dashboard.rule.redis;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
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
@Component("redisFlowRuleApiPublisher")
public class RedisFlowRuleApiPublisher implements DynamicRulePublisher<List<FlowRuleEntity>> {

    private static final String FLOW_RULE_CHANNEL = "sentinel_rule_flow_channel";

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
    public void publish(String app, List<FlowRuleEntity> rules) throws Exception {
        if (StringUtil.isBlank(app)) {
            return;
        }
        if (rules == null) {
            rules = new ArrayList<>();
        }
        String s = JSON.toJSONString(rules);
        List<FlowRule> flowRuleList = rules.stream().map(FlowRuleEntity::toRule).collect(Collectors.toList());
        String simple = JSON.toJSONString(flowRuleList);

        stringRedisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            public List<Object> execute(RedisOperations operations) {
                operations.multi();
                operations.opsForValue().set(RuleConstants.SENTINEL_FLOW_KEY + app, s);
//                operations.opsForValue().set(RuleConstants.SENTINEL_FLOW_RULE_SIMP_KEY + app, simple);
                // 通知订阅者，限流规则更新了
                operations.convertAndSend(FLOW_RULE_CHANNEL, s);
                return operations.exec();
            }
        });
    }
}
