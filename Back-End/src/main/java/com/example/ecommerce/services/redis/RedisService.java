package com.example.ecommerce.services.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveToken(String userEmail,String token, long jwtExpiration) {
        redisTemplate.opsForValue().set("access:"+userEmail, token,jwtExpiration, TimeUnit.MILLISECONDS);
    }
    public void saveRefreshToken(String userEmail, String rToken, long refreshExpirationTime) {
        redisTemplate.opsForValue().set("refresh:"+userEmail, rToken,refreshExpirationTime, TimeUnit.MILLISECONDS);
    }
    public String getTokenByUser(String userEmail) {
        return redisTemplate.opsForValue().get("access:"+userEmail);
    }
    public String getRefreshTokenByUser(String userEmail) {
        return redisTemplate.opsForValue().get("refresh:"+userEmail);
    }
    public void invalidateToken(String userEmail) {
        redisTemplate.delete("access:"+userEmail);
    }
    public void invalidateRefreshToken(String userEmail) {
        redisTemplate.delete("refresh:"+userEmail);
    }
    public boolean hasToken(String userEmail) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("access:"+userEmail));
    }
    public boolean hasRefreshToken(String userEmail) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("refresh:"+userEmail));
    }
}