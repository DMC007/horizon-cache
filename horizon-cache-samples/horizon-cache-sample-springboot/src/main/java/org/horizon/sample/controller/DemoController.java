package org.horizon.sample.controller;

import lombok.extern.slf4j.Slf4j;
import org.horizon.HorizonCacheHelper;
import org.springframework.web.bind.annotation.*;

/**
 * @author: DMC007
 * @date: 2026-02-11 22:24
 * @description: 简单使用案例
 */
@Slf4j
@RestController
@RequestMapping("/demo")
public class DemoController {

    /**
     * 指定缓存category和过期时间
     */
    private HorizonCacheHelper.HorizonCache personCache = HorizonCacheHelper.getCache("person", 30 * 1000);

    @GetMapping("/find")
    @ResponseBody
    public String find() {
        String key = "person001";
        //按照 L1到L2 顺序依次读取缓存，如果L1存在缓存则返回，否则读取L2缓存并同步L1
        String value = personCache.get(key);
        return "key: " + key + ";value: " + value;
    }

    @PutMapping("/update")
    @ResponseBody
    public String update(@RequestParam String value) {
        String key = "person001";
        //按照 L1到L2 顺序依次写缓存，同时借助内部广播机制更新全局L1节点缓存
        personCache.set(key, value);
        return "update ok";
    }

    @DeleteMapping("/delete")
    @ResponseBody
    public String delete() {
        String key = "person001";
        //按照 L1到L2 顺序依次删缓存，同时借助内部广播机制更新全局L1节点缓存
        personCache.delete(key);
        return "delete ok";
    }
}
