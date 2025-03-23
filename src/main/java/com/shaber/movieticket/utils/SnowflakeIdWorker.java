package com.shaber.movieticket.utils;

import java.util.concurrent.TimeUnit;

/**
 * 分布式ID生成器（雪花算法实现）
 * 结构：符号位(1位) + 时间戳(41位) + 数据中心ID(5位) + 机器ID(5位) + 序列号(12位)
 */
public class SnowflakeIdWorker {
    // 起始时间戳（2020-01-01 00:00:00）
    private final static long START_TIMESTAMP = 1577808000000L;

    // 各部分的位数
    private final static long SEQUENCE_BIT = 12;   // 序列号位数
    private final static long MACHINE_BIT = 5;     // 机器标识位数
    private final static long DATACENTER_BIT = 5;  // 数据中心位数

    // 最大值计算
    private final static long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);
    private final static long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);
    private final static long MAX_DATACENTER_NUM = ~(-1L << DATACENTER_BIT);

    // 各部分左移量
    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private final static long TIMESTAMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

    private final long datacenterId;  // 数据中心ID
    private final long machineId;     // 机器ID
    private long sequence = 0L;       // 序列号
    private long lastTimestamp = -1L; // 上次时间戳

    /**
     * 构造函数
     * @param datacenterId 数据中心ID (0~31)
     * @param machineId    机器ID (0~31)
     */
    public SnowflakeIdWorker(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException(
                    "Datacenter ID can't be greater than " + MAX_DATACENTER_NUM + " or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException(
                    "Machine ID can't be greater than " + MAX_MACHINE_NUM + " or less than 0");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    /**
     * 生成下一个ID
     */
    public synchronized long nextId() {
        long currentTimestamp = timeGen();

        if (currentTimestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("Clock moved backwards. Refusing to generate id for %d milliseconds",
                            lastTimestamp - currentTimestamp));
        }

        if (currentTimestamp == lastTimestamp) {
            // 相同毫秒内序列号递增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                // 当前毫秒序列号已用完，等待下一毫秒
                currentTimestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒序列号重置
            sequence = 0L;
        }

        lastTimestamp = currentTimestamp;

        return ((currentTimestamp - START_TIMESTAMP) << TIMESTAMP_LEFT)
                | (datacenterId << DATACENTER_LEFT)
                | (machineId << MACHINE_LEFT)
                | sequence;
    }

    /**
     * 阻塞到下一毫秒
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 获取当前时间戳
     */
    private long timeGen() {
        return System.currentTimeMillis();
    }
}
