package com.atguigu.gmall1213.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall1213.common.constant.RedisConst;
import com.atguigu.gmall1213.common.result.Result;
import com.atguigu.gmall1213.common.util.IpUtil;
import com.atguigu.gmall1213.model.user.UserInfo;
import com.atguigu.gmall1213.user.service.UserService;
import jdk.nashorn.internal.parser.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/api/user/passport")
public class PassportController {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;

    //登录的控制器  url  是谁 以及提交的方式是什么
    @PostMapping("login")
    public Result login(@RequestBody UserInfo userInfo, HttpServletRequest request) {

        UserInfo info = userService.login(userInfo);

        if (null != info) {
            String token = UUID.randomUUID().toString();

            HashMap<String, Object> map = new HashMap<>();
            map.put("token", token);
            //还需要   登录成功 需要显示用户昵称
            map.put("nickName", info.getNickName());
            //如果登录成功 需要将用户信息储存缓存 只需要userId 就可以了
            //此时 登录的用户ip地址放入缓存
            //声明一个对象
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userId", info.getId().toString());
            jsonObject.put("ip", IpUtil.getIpAddress(request));
            //将数据放入缓存
            //定义key
            String userKey = RedisConst.USER_LOGIN_KEY_PREFIX + token;
            redisTemplate.opsForValue().set(userKey, jsonObject.toJSONString(), RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);

            //将map 返回
            return Result.ok(map);

        } else {
            //将数据放入OK方法中

            return Result.fail().message("用户名密码不匹配");
        }


    }

    @GetMapping("logout")
    public Result logout(HttpServletRequest request){

        //删除缓存中的 数据 userKey = user：login
        //token 跟用户缓存key 有直接关系 登录的时候 放入cookie
        String token = request.getHeader("token");
        String userKey =    RedisConst.USER_LOGIN_KEY_PREFIX + token;
        redisTemplate.delete(userKey);

        return Result.ok();
    }

}
