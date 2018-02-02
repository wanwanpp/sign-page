package com.site.controller;

import com.alibaba.fastjson.JSON;
import com.site.model.sign.Response;
import com.site.model.sign.SignRecords;
import com.site.repository.MemberRepo;
import com.site.repository.SignRecordsRepo;
import com.site.utils.DateUtil;
import com.site.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@RequestMapping("/sign")
public class SignCrol {

    @Autowired
    private MemberRepo memberRepo;

    @Autowired
    private SignRecordsRepo signRecordsRepo;

    private final Jedis jedis = RedisUtil.getJedis();
    /**
     * 超时时间6个小时
     */
    private final static int TIMEOUT = 6 * 60 * 60 * 1000;

    //    获取签到页面
    @RequestMapping("")
    public String showpage(HttpServletRequest request) {
        //设置会话超时时间为1天
        request.getSession().setMaxInactiveInterval(24 * 60 * 60);
        return "signWork";
    }

    //获取签到信息以填充签到页 （姓名和是否已签到）
    @RequestMapping("/name")
    @ResponseBody
    public List<Object[]> getNamesAndIfStart() {
        //使用HQL进行某个表的多字段查询获取的格式为[[name,isstart],[name,isstart]]
        List<Object[]> members = memberRepo.findNamesAndIsstart();
        return members;
    }

    private final static String SIGN_SUCCESS = "签到成功";
    private final static String SIGN_FAIL = "签到失败";

    //签到动作
    @RequestMapping(value = "/start")
    @ResponseBody
    public Response sendStart(@RequestBody String name) {
        String msg;
//        if (jedis.exists(name)) {
//            msg = name + "已经签到";
//            log.info(msg);
//            return new Response(3, msg);
//        }
        SignRecords signRecords = new SignRecords(name);
//        签到成功,redis  set成功就返回OK。
        if ("OK".equals(saveToRedis(signRecords))) {
            memberRepo.setIsStart(name);
            msg = name + SIGN_SUCCESS;
            log.info(msg);
            return new Response(1, msg);
        }
//        签到失败
        else {
            msg = name + SIGN_FAIL;
            log.error(msg);
            return new Response(0, msg);
        }
    }

    private final static String SIGN_OUT_SUCCESS = "签退成功";
    private final static String SIGN_OUT_FAIL = "签退失败";
    private final static String SIGN_TIMEOUT = "签到超过6小时，签到无效";

    //签退
    @RequestMapping(value = "/end")
    @ResponseBody
    public Response sendend(@RequestBody String name) {

        //根据名字找到对应的最后一条记录。
        SignRecords signRecords = getFromRedis(name);
        Timestamp leaveTimeStamp = new Timestamp(System.currentTimeMillis());
        String msg;
        Long totalTime = leaveTimeStamp.getTime() - signRecords.getComeTime().getTime();
        //超过六个小时此签到记录就失效
        if (totalTime > TIMEOUT) {
            memberRepo.setIsEnd(name);
            jedis.del(name);
            msg = name + SIGN_TIMEOUT;
            log.error(msg);
            return new Response(2, msg);
        } else {
            String strTotal = String.valueOf(DateUtil.formatdate(totalTime));
            signRecords.setLeaveTime(leaveTimeStamp);
            signRecords.setStrTime(strTotal);
            signRecords.setTotalMill(totalTime);
            if (signRecordsRepo.save(signRecords) != null) {
                memberRepo.setIsEnd(name);
                jedis.del(name);
                msg = name + SIGN_OUT_SUCCESS;
                log.info(msg);
                return new Response(1, msg);
            } else {
                msg = name + SIGN_OUT_FAIL;
                log.error(msg);
                return new Response(0, msg);
            }
        }
    }

    @RequestMapping("/getWarn")
    @ResponseBody
    public Object getWarn() {
        List<Map<String, Object>> warnRecords = new LinkedList<>();
        long now = System.currentTimeMillis();
        List<String> names = memberRepo.findNamesStart();
        //每次查看是否有超时记录时，先删除旧数据。
        jedis.del("warnRecords");
        for (String name : names) {
            Timestamp cometime = getFromRedis(name).getComeTime();
            if (cometime != null) {
//            如果大于四小时就放入warnMap中进行提示
                if (now - cometime.getTime() > 4 * 60 * 60 * 1000) {
                    Map<String, Object> map = new HashMap();
                    map.put("name", name);
                    map.put("time", DateUtil.formatdate(now - cometime.getTime()));
                    log.info("超过四小时的记录: " + name + " : " + DateUtil.formatdate(now - cometime.getTime()));
                    warnRecords.add(map);
                }
            }
        }
        //有超时记录，就缓存到redis。
        if (warnRecords.size() > 0) {
            String jsonString = JSON.toJSONString(warnRecords);
            System.out.println(jsonString);
            jedis.set("warnRecords", jsonString);
            return warnRecords;
        } else {
            return "";
        }
    }

    @RequestMapping(value = "/onceEnd", produces = "application/text")
    @ResponseBody
    public String onceEnd() {
        String warnRecords = jedis.get("warnRecords");
        List<HashMap> hashMaps = JSON.parseArray(warnRecords, HashMap.class);
        //超过六小时被删除的
        StringBuffer deletedInfo = new StringBuffer("");

        if (hashMaps.size() > 0) {
            //遍历警告列表，超过六个小时的就删除，没有的就签退后重新签到。
            for (Map<String, Object> map : hashMaps) {
                String name = (String) map.get("name");
                SignRecords signRecords = getFromRedis(name);
                Timestamp leaveTimeStamp = new Timestamp(System.currentTimeMillis());
                Long totalTime = leaveTimeStamp.getTime() - signRecords.getComeTime().getTime();
                //超时的就删除
                if (totalTime > TIMEOUT) {
                    memberRepo.setIsEnd(name);
                    deletedInfo.append(name + ",");
                }
                //没超时的，就签退后重新签到。
                else {
                    String str_total = String.valueOf(DateUtil.formatdate(totalTime));
                    signRecords.setLeaveTime(leaveTimeStamp);
                    signRecords.setTotalMill(totalTime);
                    signRecords.setStrTime(str_total);
                    if (signRecordsRepo.save(signRecords) != null) {
                        memberRepo.setIsEnd(name);
                    }
                    SignRecords newSignRecord = new SignRecords(name);
                    if ("OK".equals(saveToRedis(newSignRecord))) {
                        memberRepo.setIsStart(name);
                    }
                }
            }
        }
        jedis.del("warnRecords");
        if (!"".equals(deletedInfo.toString())) {
            String string = deletedInfo.deleteCharAt(deletedInfo.length() - 1).append("超过六小时，此次签到无效").toString();
            log.error(string);
            return string;
        }
        return "一键重签成功";
    }

    private String saveToRedis(SignRecords records) {
//        return jedis.set(records.getName(), JSON.toJSONString(records));
        return RedisUtil.setcache(records.getName(), JSON.toJSONString(records));
    }

    private SignRecords getFromRedis(String name) {
        return JSON.parseObject(jedis.get(name), SignRecords.class);
    }
}
