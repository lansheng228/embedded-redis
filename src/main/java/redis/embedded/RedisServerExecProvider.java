package redis.embedded;

import lombok.extern.slf4j.Slf4j;
import redis.embedded.common.CommonConstant;
import redis.embedded.util.OSArchitecture;

@Slf4j
public class RedisServerExecProvider extends RedisExecProvider {

  public static RedisServerExecProvider defaultProvider() {
    return new RedisServerExecProvider();
  }

  private RedisServerExecProvider() {
    initExecutables();
  }

  @Override
  protected void initExecutables() {
    executables.put(OSArchitecture.UNIX_X86, CommonConstant.REDIS_SERVER_EXEC_UNIX_X86);
    executables.put(OSArchitecture.UNIX_AMD64, CommonConstant.REDIS_SERVER_EXEC_UNIX_AMD64);

    executables.put(OSArchitecture.MAC_OSX_X86, CommonConstant.REDIS_SERVER_EXEC_MAC_OSX);
    executables.put(OSArchitecture.MAC_OSX_AMD64, CommonConstant.REDIS_SERVER_EXEC_MAC_OSX);
  }
}
