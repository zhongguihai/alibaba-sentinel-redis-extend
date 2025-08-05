package com.alibaba.csp.sentinel.dashboard.rule.redis;

/**
 * @Description: RuleConstants
 * @Author: zhong.guihai
 * @Date: 2025-07-25 10:46
 **/
public class RuleConstants {

    // ======= 限流相关的key ========
    public static final String SENTINEL_FLOW_KEY = "ids:sentinel:flow:";

    public static final String SENTINEL_FLOW_RULE_SIMP_KEY = "ids:sentinel:flowSimp:";

    // ====== 授权相关的key ========
    public static final String SENTINEL_AUTH_KEY = "ids:sentinel:auth:";

    public static final String SENTINEL_AUTH_RULE_SIMP_KEY = "ids:sentinel:authSimp:";

    // ====== 系统规则相关的key ========
    public static final String SENTINEL_SYSTEM_KEY = "ids:sentinel:system:";

    public static final String SENTINEL_SYSTEM_RULE_SIMP_KEY = "ids:sentinel:systemSimp:";

    // ====== 熔断规则相关的key ========
    public static final String SENTINEL_DEGRADE_KEY = "ids:sentinel:degrade:";

    public static final String SENTINEL_DEGRADE_RULE_SIMP_KEY = "ids:sentinel:degradeSimp:";

    public static final String DEGRADE_RULE_CHANNEL = "sentinel_rule_degrade_channel";

}

