package com.vinayms.razorpayclone.common.services;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.Optional;


@RequiredArgsConstructor
@Component
public class RedisService {

    private final RedisTemplate<String,Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void set(String key,Object value,int expireTimeInSeconds){
        redisTemplate.opsForValue().set(key,value, Duration.ofSeconds(expireTimeInSeconds));
    }

    public <T> Optional<T> get(String key, Class<T> type) {
        Object value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(objectMapper.convertValue(value, type));
    }


    public void delete(String key){
        redisTemplate.delete(key);
    }
}

