package com.alibaba.csp.sentinel.datasource.redis;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.redis.config.RedisConnectionConfig;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.SlotHash;
import io.lettuce.core.cluster.api.sync.NodeSelection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

/**
 * Redis redisCluster mode test cases for {@link RedisDataSource}.
 *
 * @author liqiangz
 */
@Ignore(value = "Before run this test, you need to set up your Redis Cluster.")
public class ClusterModeRedisDataSourceTest {

    private final String host = "localhost";

    private final int redisSentinelPort = 7000;
    private final RedisClusterClient client = RedisClusterClient.create(RedisURI.Builder.redis(host, redisSentinelPort).build());
    private final String ruleKey = "sentinel.rules.flow.ruleKey";
    private final String channel = "sentinel.rules.flow.channel";

    @Before
    public void initData() {
        Converter<String, List<FlowRule>> flowConfigParser = buildFlowConfigParser();
        RedisConnectionConfig config = RedisConnectionConfig.builder().withRedisCluster(host, redisSentinelPort).build();
        initRedisRuleData();
        ReadableDataSource<String, List<FlowRule>> redisDataSource = new RedisDataSource<>(config, ruleKey, channel, flowConfigParser);
        FlowRuleManager.register2Property(redisDataSource.getProperty());
    }

    @Test
    public void testConnectToSentinelAndPubMsgSuccess() {
        int maxQueueingTimeMs = new Random().nextInt();
        String flowRulesJson = "[{\"resource\":\"test\", \"limitApp\":\"default\", \"grade\":1, \"count\":\"0.0\", \"strategy\":0, " + "\"refResource\":null, " + "\"controlBehavior\":0, \"warmUpPeriodSec\":10, \"maxQueueingTimeMs\":" + maxQueueingTimeMs + ", \"controller\":null}]";
        RedisAdvancedClusterCommands<String, String> subCommands = client.connect().sync();
        int slot = SlotHash.getSlot(ruleKey);
        NodeSelection<String, String> nodes = subCommands.nodes((n) -> n.hasSlot(slot));
        RedisCommands<String, String> commands = nodes.commands(0);
        commands.multi();
        commands.set(ruleKey, flowRulesJson);
        commands.publish(channel, flowRulesJson);
        commands.exec();

        await().timeout(2, TimeUnit.SECONDS).until(new Callable<List<FlowRule>>() {
            @Override
            public List<FlowRule> call() throws Exception {
                return FlowRuleManager.getRules();
            }
        }, Matchers.hasSize(1));

        List<FlowRule> rules = FlowRuleManager.getRules();
        Assert.assertEquals(rules.get(0).getMaxQueueingTimeMs(), maxQueueingTimeMs);
        String value = subCommands.get(ruleKey);
        List<FlowRule> flowRulesValuesInRedis = buildFlowConfigParser().convert(value);
        Assert.assertEquals(flowRulesValuesInRedis.size(), 1);
        Assert.assertEquals(flowRulesValuesInRedis.get(0).getMaxQueueingTimeMs(), maxQueueingTimeMs);
    }

    @After
    public void clearResource() {
        RedisAdvancedClusterCommands<String, String> stringRedisCommands = client.connect().sync();
        stringRedisCommands.del(ruleKey);
        client.shutdown();
    }

    private Converter<String, List<FlowRule>> buildFlowConfigParser() {
        return source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
        });
    }

    private void initRedisRuleData() {
        String flowRulesJson = "[{\"resource\":\"test\", \"limitApp\":\"default\", \"grade\":1, \"count\":\"0.0\", \"strategy\":0, " + "\"refResource\":null, " + "\"controlBehavior\":0, \"warmUpPeriodSec\":10, \"maxQueueingTimeMs\":500, \"controller\":null}]";
        RedisAdvancedClusterCommands<String, String> stringRedisCommands = client.connect().sync();
        String ok = stringRedisCommands.set(ruleKey, flowRulesJson);
        Assert.assertEquals("OK", ok);
    }
}