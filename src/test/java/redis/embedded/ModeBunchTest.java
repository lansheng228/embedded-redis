package redis.embedded;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.concurrent.NotThreadSafe;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;
import redis.embedded.common.CommonConstant;
import redis.embedded.util.JedisUtil;

// 哨兵模式
@Slf4j
@NotThreadSafe
public class ModeBunchTest extends BaseTest {
  private int sentinelPort;
  private String sentinelHost;
  private String masterName;
  private String masterName1;
  private String masterName2;
  private String masterName3;

  @BeforeEach
  public void setUp() {
    super.setUp();
    masterName = RandomStringUtils.randomAlphabetic(5, 10);
    masterName1 = RandomStringUtils.randomAlphabetic(5, 10);
    masterName2 = RandomStringUtils.randomAlphabetic(5, 10);
    masterName3 = RandomStringUtils.randomAlphabetic(5, 10);
    sentinelHost = CommonConstant.DEFAULT_REDIS_HOST;
    sentinelPort = RandomUtils.nextInt(10000, 60000);
  }

  @Test
  public void testSimpleOperationsAfterRunWithSingleMasterNoSlave() {
    List<Integer> sentinelPorts = Collections.singletonList(sentinelPort);

    RedisBunch redisBunch =
        RedisBunch.builder()
            .sentinelCount(1)
            .sentinelPorts(sentinelPorts)
            .replicationGroup(masterName, 0)
            .build();
    redisBunch.start();

    JedisSentinelPool sentinelPool =
        new JedisSentinelPool(
            masterName,
            Sets.newHashSet(sentinelHost + CommonConstant.SEPARATOR_COLON + sentinelPort));
    Jedis sentinelJedis = sentinelPool.getResource();

    writeSuccess(sentinelJedis);
    readSuccess(sentinelJedis);

    sentinelPool.close();
    redisBunch.stop();
  }

  @Test
  public void testSimpleOperationsAfterRunWithSingleMasterOneSlave() {
    List<Integer> sentinelPorts = Collections.singletonList(sentinelPort);

    RedisBunch redisBunch =
        RedisBunch.builder()
            .sentinelCount(1)
            .sentinelPorts(sentinelPorts)
            .replicationGroup(masterName, 1)
            .build();
    redisBunch.start();

    JedisSentinelPool sentinelPool =
        new JedisSentinelPool(
            masterName,
            Sets.newHashSet(sentinelHost + CommonConstant.SEPARATOR_COLON + sentinelPort));
    Jedis sentinelJedis = sentinelPool.getResource();

    writeSuccess(sentinelJedis);
    readSuccess(sentinelJedis);

    sentinelPool.close();
    redisBunch.stop();
  }

  @Test
  public void testSimpleOperationsAfterRunWithSingleMasterMultipleSlaves() {
    List<Integer> sentinelPorts = Collections.singletonList(sentinelPort);

    RedisBunch redisBunch =
        RedisBunch.builder()
            .sentinelCount(1)
            .sentinelPorts(sentinelPorts)
            .replicationGroup(masterName, 2)
            .build();
    redisBunch.start();

    JedisSentinelPool sentinelPool =
        new JedisSentinelPool(
            masterName,
            Sets.newHashSet(sentinelHost + CommonConstant.SEPARATOR_COLON + sentinelPort));
    Jedis sentinelJedis = sentinelPool.getResource();

    writeSuccess(sentinelJedis);
    readSuccess(sentinelJedis);

    sentinelPool.close();
    redisBunch.stop();
  }

  @Test
  public void testSimpleOperationsAfterRunWithTwoSentinelsSingleMasterMultipleSlaves() {
    List<Integer> sentinelPorts = Arrays.asList(sentinelPort, sentinelPort + 1);

    RedisBunch redisBunch =
        RedisBunch.builder()
            .sentinelCount(2)
            .sentinelPorts(sentinelPorts)
            .replicationGroup(masterName, 2)
            .build();
    redisBunch.start();

    JedisSentinelPool sentinelPool =
        new JedisSentinelPool(
            masterName,
            Sets.newHashSet(
                sentinelHost + CommonConstant.SEPARATOR_COLON + sentinelPort,
                sentinelHost + CommonConstant.SEPARATOR_COLON + (sentinelPort + 1)));
    Jedis sentinelJedis = sentinelPool.getResource();

    writeSuccess(sentinelJedis);
    readSuccess(sentinelJedis);

    sentinelPool.close();
    redisBunch.stop();
  }

  @Test
  public void testSimpleOperationsAfterRunWithTwoPredefinedSentinelsSingleMasterMultipleSlaves() {
    List<Integer> sentinelPorts = Arrays.asList(sentinelPort, sentinelPort + 1);

    RedisBunch redisBunch =
        RedisBunch.builder().sentinelPorts(sentinelPorts).replicationGroup(masterName, 2).build();
    redisBunch.start();
    Set<String> sentinelJedisHosts = JedisUtil.portsToJedisHosts(sentinelPorts);

    JedisSentinelPool sentinelPool = new JedisSentinelPool(masterName, sentinelJedisHosts);
    Jedis sentinelJedis = sentinelPool.getResource();

    writeSuccess(sentinelJedis);
    readSuccess(sentinelJedis);

    sentinelPool.close();
    redisBunch.stop();
  }

  @Test
  public void testSimpleOperationsAfterRunWithThreeSentinelsThreeMastersOneSlavePerMaster() {
    List<Integer> sentinelPorts = Arrays.asList(sentinelPort, sentinelPort + 1, sentinelPort + 2);

    RedisBunch redisBunch =
        RedisBunch.builder()
            .sentinelCount(3)
            .sentinelPorts(sentinelPorts)
            .quorumSize(2)
            .replicationGroup(masterName1, 1)
            .replicationGroup(masterName2, 1)
            .replicationGroup(masterName3, 1)
            .build();
    redisBunch.start();

    JedisSentinelPool sentinelPool1 =
        new JedisSentinelPool(
            masterName1,
            Sets.newHashSet(
                sentinelHost + CommonConstant.SEPARATOR_COLON + sentinelPort,
                sentinelHost + CommonConstant.SEPARATOR_COLON + (sentinelPort + 1),
                sentinelHost + CommonConstant.SEPARATOR_COLON + (sentinelPort + 2)));

    JedisSentinelPool sentinelPool2 =
        new JedisSentinelPool(
            masterName2,
            Sets.newHashSet(
                sentinelHost + CommonConstant.SEPARATOR_COLON + sentinelPort,
                sentinelHost + CommonConstant.SEPARATOR_COLON + (sentinelPort + 1),
                sentinelHost + CommonConstant.SEPARATOR_COLON + (sentinelPort + 2)));

    JedisSentinelPool sentinelPool3 =
        new JedisSentinelPool(
            masterName3,
            Sets.newHashSet(
                sentinelHost + CommonConstant.SEPARATOR_COLON + sentinelPort,
                sentinelHost + CommonConstant.SEPARATOR_COLON + (sentinelPort + 1),
                sentinelHost + CommonConstant.SEPARATOR_COLON + (sentinelPort + 2)));

    Jedis sentinelJedis1 = sentinelPool1.getResource();
    Jedis sentinelJedis2 = sentinelPool2.getResource();
    Jedis sentinelJedis3 = sentinelPool3.getResource();

    writeSuccess(sentinelJedis1);
    readSuccess(sentinelJedis1);
    writeSuccess(sentinelJedis2);
    readSuccess(sentinelJedis2);
    writeSuccess(sentinelJedis3);
    readSuccess(sentinelJedis3);

    sentinelPool1.close();
    sentinelPool2.close();
    sentinelPool3.close();
    redisBunch.stop();
  }

  @Test
  public void
      testSimpleOperationsAfterRunWithThreeSentinelsThreeMastersOneSlavePerMasterEphemeral() {
    List<Integer> sentinelPorts = Arrays.asList(sentinelPort, sentinelPort + 1, sentinelPort + 2);

    RedisBunch redisBunch =
        RedisBunch.builder()
            .ephemeral()
            .sentinelCount(3)
            .sentinelPorts(sentinelPorts)
            .quorumSize(2)
            .replicationGroup(masterName1, 1)
            .replicationGroup(masterName2, 1)
            .replicationGroup(masterName3, 1)
            .build();
    redisBunch.start();
    Set<String> sentinelJedisHosts = JedisUtil.sentinelJedisHosts(redisBunch);

    JedisSentinelPool sentinelPool1 = new JedisSentinelPool(masterName1, sentinelJedisHosts);

    JedisSentinelPool sentinelPool2 = new JedisSentinelPool(masterName2, sentinelJedisHosts);

    JedisSentinelPool sentinelPool3 = new JedisSentinelPool(masterName3, sentinelJedisHosts);

    Jedis sentinelJedis1 = sentinelPool1.getResource();
    Jedis sentinelJedis2 = sentinelPool2.getResource();
    Jedis sentinelJedis3 = sentinelPool3.getResource();

    writeSuccess(sentinelJedis1);
    readSuccess(sentinelJedis1);
    writeSuccess(sentinelJedis2);
    readSuccess(sentinelJedis2);
    writeSuccess(sentinelJedis3);
    readSuccess(sentinelJedis3);

    sentinelPool1.close();
    sentinelPool2.close();
    sentinelPool3.close();
    redisBunch.stop();
  }
}
