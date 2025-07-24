/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengoofy.index12306.biz.orderservice.service.orderid;

/**
 * 全局唯一订单号生成器
 * 公众号：马丁玩编程，回复：加群，添加马哥微信（备注：12306）获取项目资料
 */
public class DistributedIdGenerator {

    private static final long EPOCH = 1609459200000L;  // 2021-01-01 00:00:00 UTC
    private static final int NODE_BITS = 5;  // 节点ID占用的位数
    private static final int SEQUENCE_BITS = 7;  // 序列号占用的位数

    private final long nodeID;  // 节点ID
    private long lastTimestamp = -1L;  // 上次生成ID的时间戳
    private long sequence = 0L;  // 序列号

    /**
     * 生成的64位ID由三部分组成：
     * 时间戳部分：高位部分，从自定义纪元(EPOCH)开始计算的时间差
     * 节点ID部分：中间部分，标识生成ID的节点
     * 序列号部分：低位部分，同一毫秒内的序列号
     * 具体位分配：
     * 时间戳：64 - (NODE_BITS + SEQUENCE_BITS) = 64 - (5 + 7) = 52位
     * 节点ID：5位
     * 序列号：7位
     */
    public DistributedIdGenerator(long nodeID) {
        this.nodeID = nodeID;
    }

    public synchronized long generateId() {
        // 1. 获取当前时间戳（相对于EPOCH）
        long timestamp = System.currentTimeMillis() - EPOCH;
        // 2. 检查时钟回拨
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate ID.");
        }
        // 3. 处理同一毫秒内的ID生成
        if (timestamp == lastTimestamp) {
            // 增加序列号（使用位运算确保不超过最大值）
            sequence = (sequence + 1) & ((1 << SEQUENCE_BITS) - 1);
            // 如果序列号溢出，等待下一毫秒
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒则重置序列号
            sequence = 0L;
        }
        // 4. 更新最后时间戳
        lastTimestamp = timestamp;
        // 5. 组合各部分生成最终ID
        return (timestamp << (NODE_BITS + SEQUENCE_BITS)) | (nodeID << SEQUENCE_BITS) | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis() - EPOCH;
        // 自旋等待直到下一毫秒
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis() - EPOCH;
        }
        return timestamp;
    }
}
