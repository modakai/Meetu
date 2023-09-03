package com.sakura.meetu.config.aspect;

import cn.hutool.core.date.SystemClock;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sakura.meetu.config.annotation.AutoLog;
import com.sakura.meetu.entity.Log;
import com.sakura.meetu.entity.User;
import com.sakura.meetu.exception.ServiceException;
import com.sakura.meetu.service.ILogService;
import com.sakura.meetu.utils.IpUtils;
import com.sakura.meetu.utils.SessionUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * 日志切面
 *
 * @author sakura
 * @date 2023/9/3 13:13:08 周日
 */
@Component
@Aspect
@Slf4j
public class LogAspect {

    private final ILogService logService;

    public LogAspect(ILogService logService) {
        this.logService = logService;
    }

    @Around("@annotation(autoLog)")
    public Object autoLog(ProceedingJoinPoint joinPoint, AutoLog autoLog) throws Throwable {
        long beginTime = SystemClock.now();
        //执行方法
        Object result = joinPoint.proceed();
        //执行时长(毫秒)
        long time = SystemClock.now() - beginTime;

        // 操作
        HttpServletRequest request = getRequest();
        // 请求的url
        String url = request.getRequestURL().toString();
        if (url.contains("/api/file/download/file")) {
            return result;
        }
        // 操作人
        String username;
        User user = null;
        try {
            user = SessionUtils.getUser();
        } catch (ServiceException e) {
            log.info("当前没有用户登入");
        }
        if (user != null) {
            username = user.getUsername();
        } else {
            username = "";
        }

        ThreadUtil.execute(() -> {
            saveLog(joinPoint, autoLog, time, result, url, username);
        });
        return result;
    }

    private void saveLog(ProceedingJoinPoint joinPoint, AutoLog autoLog,
                         long time, Object result, String url, String username) {
        String name = autoLog.value();
        // 请求参数
        String params = "";
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            Object arg0 = args[0];
            if (arg0 instanceof MultipartFile) {
                params = "{params: '文件流'}";
            } else {
                // 拼接json 格式参数
                MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
                String[] parameterNames = methodSignature.getParameterNames();
                params = spellMethodParam(args, parameterNames);
            }
        }
        String returnValue = JSONUtil.toJsonStr(result);
        // ip 和地址
        Dict ipAndCity = IpUtils.getIPAndCity();
        String ip = ipAndCity.getStr("ip");
        String city = ipAndCity.getStr("city");

        // 保存到数据库
        Log log = Log.builder().name(name).params(params).output(returnValue).url(url).ip(ip)
                .address(city).duration((int) time).username(username).build();
        logService.save(log);
    }

    private HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    /**
     * 拼接字符串
     *
     * @param argsMethod   传递的参数
     * @param methodParams 方法中的参数名
     * @return 拼接好的字符串
     */
    private String spellMethodParam(Object[] argsMethod, String[] methodParams) {
        JSONObject jsonObject = new JSONObject();
        for (int i = 0; i < methodParams.length; i++) {
            String methodParam = methodParams[i];
            Object o = argsMethod[i];
            jsonObject.put(methodParam, o);
        }
        return jsonObject.toString();
    }


}
