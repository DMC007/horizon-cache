<p align="center">
    <h3 align="center">XXL-CACHE</h3>
    <p align="center">
        HORIZON-CACHE is a simple multi-level cache framework.
        <br/>
        <img src="https://img.shields.io/badge/Java%20CI-passing-brightgreen?logo=github" >
    </p>    
</p>

## Introduction
HORIZON-CACHE (the name prefix is derived from Sony's Horizon series of games) is a simple and easy-to-use multi-level caching framework. By default, it employs an efficient combination of local and distributed caching using Redis and Caffeine, supporting capabilities such as "multi-level caching, consistency guarantee, TTL, Category isolation, and anti-penetration". Characterized by "high performance, high scalability, flexibility, and ease of use", it is an easy-to-use, high-performance multi-level caching solution.

HORIZON-CACHE（名称前缀来源索尼地平线系列游戏） 是一个简单易用的多级缓存框架，默认采用Redis+Caffeine的方式高效组合本地缓存和分布式缓存，支持“多级缓存、一致性保障、TTL、Category隔离、防穿透”等能力；具有“高性能、高扩展、灵活易用”等特性，是一个易用，高性能多级缓存解决方案。

## Get started
```xml
<dependency>
    <groupId>org.horizon</groupId>
    <artifactId>horizon-cache</artifactId>
    <version>${最新稳定版}</version>
</dependency>
```
配置说明
```properties
# horizon-cache
## L1缓存（jvm本地）提供者，默认 caffeine
horizon.cache.l1.provider=caffeine
## L1缓存最大容量，默认10000（配置逻辑参考CacheManager）
horizon.cache.l1.maxSize=-1
## L1缓存过期时间，单位秒，默认10min；
horizon.cache.l1.expireAfterWrite=-1
## L2缓存（分布式）提供者，默认 redis
horizon.cache.l2.provider=redis
## L2缓存序列化方式，默认 java（后续开放json方式）
horizon.cache.l2.serializer=java
## L2缓存节点配置，多个节点用逗号分隔；例如 “127.0.0.1:6379”、“127.0.0.1:6379,127.0.0.1:6380”
horizon.cache.l2.nodes=127.0.0.1:6379
## L2缓存用户名配置
horizon.cache.l2.user=
## L2缓存密码配置
horizon.cache.l2.password=
## L2缓存数据库设置，默认0
horizon.cache.l2.database=0
```

## Features
- 1、简单易用: 接入方便，能快速上手使用；
- 2、多级缓存：默认采用Redis+Caffeine的方式高效组合本地缓存和分布式缓存，支持2级缓存；
- 3、高扩展：框架进行模块化抽象设计，本地缓存、分布式缓存以及序列化方案均支持自定义扩展；
- 4、高性能：多级缓存模型：L1(本地)+L2(远程)，对高热数据的查询减少网络通信IO，最大化提升性能；
- 5、一致性保障：采用广播模式（Redis的 Pub/Sub机制）以及客户端主动过期，保障各节点2级缓存的一致性；
- 6、TTL：支持设置TTL，支持缓存数据主动过期及清理；
- 7、Category隔离：支持自定义缓存Category分类，隔离不同类型的缓存数据；
- 8、缓存风险治理：对于常见缓存问题，如缓存穿透，底层进行针对性设计进行风险防护；
- 9、透明接入：屏蔽底层实现细节，降低业务开发和学习成本；
- 10、多序列化协议支持：组件化抽象Serializer，可灵活扩展更多序列化协议；


## Copyright and License
This product is open source and free, and will continue to provide free community technical support. Individual or enterprise users are free to access and use.

- Licensed under the Apache License, Version 2.0.
- Copyright (c) 2015-present, xuxueli.

产品开源免费，并且将持续提供免费的社区技术支持。个人或企业内部可自由的接入和使用。

