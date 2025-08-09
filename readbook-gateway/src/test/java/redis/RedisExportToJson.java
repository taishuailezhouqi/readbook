package redis;

import redis.clients.jedis.Jedis;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RedisExportToJson {

    public static void main(String[] args) {
        // 连接本地Redis服务器
        try (Jedis jedis = new Jedis("localhost", 6379)) {

            // 获取所有key（注意：生产环境建议使用SCAN避免阻塞）
            Set<String> keys = jedis.keys("*");

            Map<String, String> data = new HashMap<>();
            for (String key : keys) {
                String type = jedis.type(key);
                if ("string".equals(type)) {
                    String value = jedis.get(key);
                    data.put(key, value);
                }
                // 如果需要处理Hash、List、Set等类型，可以在这里扩展
            }

            // 将数据写成JSON文件
            ObjectMapper mapper = new ObjectMapper();
            File outFile = new File("redis_data.json");
            mapper.writerWithDefaultPrettyPrinter().writeValue(outFile, data);

            System.out.println("Redis 数据已导出到文件：" + outFile.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("写入JSON文件失败：" + e.getMessage());
        }
    }
}
