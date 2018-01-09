package com.project.app.util;

import java.io.UnsupportedEncodingException;

import com.project.app.AppConfig;
import com.project.app.bean.User;
import com.project.server.storage.db.UserInfo;

import engine.java.util.KeyExpiryMap;
import engine.java.util.Util;
import engine.java.util.secure.CryptoUtil;
import engine.java.util.secure.HexUtil;

/**
 * 凭证管理器
 */
public class TokenManager {
    
    /** 过期时间 **/
    private static final long expiryTime = 7 * 24 * 60 * 60 * 1000;     // 7天
    
    private static final KeyExpiryMap<String, Long> userMap
    = new KeyExpiryMap<String, Long>();                                 // 用户查询表[Token-uid]
    
    /**
     * 生成32位Token串（解码后为16个字节数据用于Socket握手）
     */
    public static String generateToken(UserInfo userInfo, String deviceID)
            throws UnsupportedEncodingException {
        if (AppConfig.IS_TESTING) return backdoor(userInfo, deviceID);
        return _generateToken(userInfo, deviceID);
    }
    
    private static String _generateToken(UserInfo userInfo, String deviceID)
            throws UnsupportedEncodingException {
        // 用户名+设备号保证唯一性，加盐保证随机性
        int salt = Util.getRandom(0, Integer.MAX_VALUE);
        String str = userInfo.username + deviceID + salt;
        // 加密后16个字节
        byte[] data = CryptoUtil.md5(str.getBytes());
        // 编码为32位字符串
        String token = HexUtil.encode(data);
        
        userMap.put(token, userInfo.getUid(), expiryTime);
        User user = UserManager.saveUser(token, userInfo, deviceID);
        if (user != null)
        {
            user.invalidate();
            userMap.remove(user.token);
        }
        
        return token;
    }
    
    /**
     * Token认证
     */
    public static User authenticate(String token) {
        Long uid = userMap.get(token);
        if (uid != null)
        {
            User user = UserManager.getUser(uid);
            if (user == null)
            {
                userMap.remove(token);
            }
            
            return user;
        }
        
        return null;
    }
    
    /**
     * 一个测试后门
     */
    private static String backdoor(UserInfo userInfo, String deviceID)
            throws UnsupportedEncodingException {
        String token = null;
        if ("18311287987".equals(userInfo.username))
        {
            User user = UserManager.getUser(userInfo.getUid());
            if (user != null)
            {
                token = user.token;
            }
            else
            {
                token = "03BC46A4D28DEF08FBC81466C2BBC6B4";
                
                userMap.put(token, userInfo.getUid(), expiryTime);
                UserManager.saveUser(token, userInfo, deviceID);
            }
        }
        else
        {
            token = _generateToken(userInfo, deviceID);
        }
        
        return token;
    }
}