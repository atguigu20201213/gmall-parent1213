package com.atguigu.gmall1213.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall1213.common.result.Result;
import com.atguigu.gmall1213.common.result.ResultCodeEnum;
import com.atguigu.gmall1213.common.util.IpUtil;
import org.omg.CORBA.IRObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class AuthGlobalFilter implements GlobalFilter {
    @Autowired
    private RedisTemplate redisTemplate;

        //路径匹配工具类
    //private AntPathMatcher antPathMatcher;
    private AntPathMatcher antPathMatcher = new AntPathMatcher();
    @Value("${authUrls.url}")
    private String authUrlsUrl;

    //过滤器   mono
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        //获取用户在浏览器输入的访问路径 url
        //获取到请求对象
        ServerHttpRequest request = exchange.getRequest();
        //通过请求对象 获取url
        String path = request.getURI().getPath();
        //判断用户发起的请求是否有 inner 说明是内部接口，不允许在浏览器直接访问
        //做一个路径匹配工作
        if (antPathMatcher.match("/**/inner/**", path)) {
            //给提示信息 没有权限访问
            //获取响应对象
            ServerHttpResponse response = exchange.getResponse();
            //out 方法提示信息
            return out(response, ResultCodeEnum.PERMISSION);
        }
        //想获取用户登录信息，用户登录成功之后，我们储存一个userId 在缓存
        //如果缓存有userId  说明登录了
        //缓存    key  = user:login:token
        //token  zai cooki   header
        String userId = getUserId(request);
        //获取临时id
        String userTempId = getUserTempId(request);
        //判断防止token被盗用
        if ("-1".equals(userId)) {
            //获取响应对象
            ServerHttpResponse response = exchange.getResponse();
            //out 方法提示信息
            return out(response, ResultCodeEnum.PERMISSION);
        }

        //用户登录认证
        if (antPathMatcher.match("/api/**/auth/**", path)) {
            //此时访问 url 包含此路径 用户必须登录
            if (StringUtils.isEmpty(userId)) {
                //获取响应对象
                ServerHttpResponse response = exchange.getResponse();
                //out 方法提示信息
                return out(response, ResultCodeEnum.LOGIN_AUTH);
            }

        }
        //验证用户访问 web——all 中是否带黑名单的控制器
            for (String authUrl : authUrlsUrl.split(",")) {
                //用户访问是否包含上述的内容
                if (path.indexOf(authUrl) != -1 && StringUtils.isEmpty(userId)) {
                    //获取响应对象
                    ServerHttpResponse response = exchange.getResponse();
                    //返回状态码  重定向 获取请求资源
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    //访问登录页面
                    response.getHeaders().set(HttpHeaders.LOCATION, "http://www.gmall.com/login.html?originUrl=" + request.getURI());
                    //设置返回
                    return response.setComplete();
                }

            }
        if (!StringUtils.isEmpty(userId) || !StringUtils.isEmpty(userTempId)) {
            if (!StringUtils.isEmpty(userId)) {
                //将用户id 储存在请求头中
                request.mutate().header("userId", userId).build();
                //固定写法
               // return chain.filter(exchange.mutate().request(request).build());
            }
            if (!StringUtils.isEmpty(userTempId)) {
                //将用户id 储存在请求头中
                request.mutate().header("userTempId", userTempId).build();
			}
                //固定写法
                return chain.filter(exchange.mutate().request(request).build());
            
        }
            //用户访问任何一个微服务 ，必须走网官  从网关获取id


        return chain.filter(exchange);
    }

    private String getUserId(ServerHttpRequest request) {
        //用户ID储存在缓存  header    如何储存的
        //关键是  token    cookie  header
        String token = "";
        //从header 中获取
        List<String> list = request.getHeaders().get("token");
        if (null != list) {
            //集合中的数据是如何存储的    集合中只有一个数据  key是同一个
            token=list.get(0);
        } else {
            //从cookie 中获取
            MultiValueMap<String, HttpCookie> cookies = request.getCookies();
//            List<HttpCookie> token1 = cookies.get("token");
//            token1.get(0);
            HttpCookie cookie = cookies.getFirst("token");
            if (null != cookie) {
                //因为token 要经过url  进行传送
                token = URLDecoder.decode(cookie.getValue());
            }

        }
        if (!StringUtils.isEmpty(token)) {
            String userKey = "user:login:" + token;
            //从缓存获取数据
            String userJson = (String) redisTemplate.opsForValue().get(userKey);
            //   转换userJson   因为数据中有userId  ip
            JSONObject jsonObject = JSONObject.parseObject(userJson);
            //获取ip   缓存中的ip
            String ip = jsonObject.getString("ip");
            //获取当前ip
            String curIp = IpUtil.getGatwayIpAddress(request);
            //检验token 是否被盗用
            if (ip.equals(curIp)) {
                return jsonObject.getString("userId");
            } else {
                return "-1";
            }
        }

        return null;
    }
    /**
     * 获取当前用户临时用户id
     * @param request
     * @return
     */
    private String getUserTempId(ServerHttpRequest request) {
        String userTempId = "";
        List<String> tokenList = request.getHeaders().get("userTempId");
        if(null  != tokenList) {
            userTempId = tokenList.get(0);
        } else {
            MultiValueMap<String, HttpCookie> cookieMultiValueMap =  request.getCookies();
            HttpCookie cookie = cookieMultiValueMap.getFirst("userTempId");
            if (null!=cookie){
                userTempId = URLDecoder.decode(cookie.getValue());
            }
        }
        return userTempId;
    }

    //提示信息方法
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
            //提示信息
        Result<Object> result = Result.build(null, resultCodeEnum);
        //设置字符集  字节数组
        byte[] bytes = JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer wrap = response.bufferFactory().wrap(bytes);
        //给用户显示 提示 显示到页面
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        return response.writeWith(Mono.just(wrap));
    }

}
