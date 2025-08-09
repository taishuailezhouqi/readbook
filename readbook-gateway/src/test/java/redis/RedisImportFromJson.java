package redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class RedisImportFromJson {

    public static void main(String[] args) {
        try (Jedis jedis = new Jedis("localhost", 6379)) {

            ObjectMapper mapper = new ObjectMapper();
            // 读取导出的 JSON 文件
            File inFile = new File("redis_data.json");
            Map<String, String> data = mapper.readValue(inFile, Map.class);

            // 导入到 Redis
            for (Map.Entry<String, String> entry : data.entrySet()) {
                jedis.set(entry.getKey(), entry.getValue());
            }

            System.out.println("Redis 数据已成功导入！");

        } catch (IOException e) {
            System.err.println("读取JSON文件失败：" + e.getMessage());
        }
    }
}