package com.sichuan.poker.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信相关工具类
 */
@Component
public class WxUtils {

    @Value("${wx.appid}")
    private String appId;

    @Value("${wx.secret}")
    private String secret;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 获取微信OpenID
     */
    public String getOpenId(String code) {
        try {
            String url = String.format(
                    "https://api.weixin.qq.com/sns/jscode2session?" +
                    "appid=%s" +
                    "&secret=%s" +
                    "&js_code=%s" +
                    "&grant_type=authorization_code",
                    appId, secret, code);

            String result = restTemplate.getForObject(url, String.class);
            Map<String, Object> response = objectMapper.readValue(result, HashMap.class);

            if (response.containsKey("openid")) {
                return (String) response.get("openid");
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检验签名
     */
    public boolean checkSignature(String signature, String timestamp, String nonce, String token) {
        // 1. 将token、timestamp、nonce三个参数进行字典序排序
        String[] arr = new String[]{token, timestamp, nonce};
        Arrays.sort(arr);

        // 2. 将三个参数字符串拼接成一个字符串进行sha1加密
        StringBuilder content = new StringBuilder();
        for (String s : arr) {
            content.append(s);
        }
        String encrypt = SHA1(content.toString());

        // 3. 将sha1加密后的字符串可与signature对比
        return encrypt != null && encrypt.equals(signature);
    }

    /**
     * SHA1加密
     */
    private String SHA1(String decript) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(decript.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String shaHex = Integer.toHexString(aMessageDigest & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return null;
        }
    }
}