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

package org.opengoofy.index12306.biz.ticketservice.toolkit;

import org.opengoofy.index12306.biz.ticketservice.dto.domain.RouteDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 站点计算工具
 * 公众号：马丁玩编程，回复：加群，添加马哥微信（备注：12306）获取项目资料
 */
public final class StationCalculateUtil {

    /**
     * 计算出发站和终点站中间的站点（包含出发站和终点站）
     *
     * @param stations     所有站点数据
     * @param startStation 出发站
     * @param endStation   终点站
     * @return 出发站和终点站中间的站点（包含出发站和终点站）
     */
    public static List<RouteDTO> throughStation(List<String> stations, String startStation, String endStation) {
        List<RouteDTO> routesToDeduct = new ArrayList<>();
        int startIndex = stations.indexOf(startStation);
        int endIndex = stations.indexOf(endStation);
        if (startIndex < 0 || endIndex < 0 || startIndex >= endIndex) {
            return routesToDeduct;
        }
        for (int i = startIndex; i < endIndex; i++) {
            for (int j = i + 1; j <= endIndex; j++) {
                String currentStation = stations.get(i);
                String nextStation = stations.get(j);
                RouteDTO routeDTO = new RouteDTO(currentStation, nextStation);
                routesToDeduct.add(routeDTO);
            }
        }
        return routesToDeduct;
    }

    /**
     * 计算出发站和终点站需要扣减余票的站点（包含出发站和终点站）
     *
     * @param stations     列车途经的所有站点列表（按顺序排列）
     * @param startStation 出发站
     * @param endStation   终点站
     * @return 出发站和终点站需要扣减余票的站点（包含出发站和终点站）
     */
    public static List<RouteDTO> takeoutStation(List<String> stations, String startStation, String endStation) {
        List<RouteDTO> takeoutStationList = new ArrayList<>();
        int startIndex = stations.indexOf(startStation);
        int endIndex = stations.indexOf(endStation);
        // 检查出发站和终点站是否有效
        if (startIndex == -1 || endIndex == -1 || startIndex >= endIndex) {
            return takeoutStationList;
        }
        // 如果出发站不是第一个站点（startIndex != 0），需要处理出发站之前的所有站点。
        if (startIndex != 0) {
            // 对每一个出发站之前的站点 ，生成它与 出发站之后所有站点 的组合。
            /*
             *
             * 假设站点列表：[A, B, C, D, E]，出发站是 C（startIndex=2）。
             * 生成的组合： A→D, A→E, B→D, B→E。
             */
            for (int i = 0; i < startIndex; i++) {
                for (int j = 1; j < stations.size() - startIndex; j++) {
                    takeoutStationList.add(new RouteDTO(stations.get(i), stations.get(startIndex + j)));
                }
            }
        }
        // 处理出发站到终点站的区间
        /*
         *生成的组合： C->D,C->E,D->E
         */
        for (int i = startIndex; i <= endIndex; i++) {
            for (int j = i + 1; j < stations.size() && i < endIndex; j++) {
                takeoutStationList.add(new RouteDTO(stations.get(i), stations.get(j)));
            }
        }
        return takeoutStationList;
    }

    public static void main(String[] args) {
        List<String> stations = Arrays.asList("北京南", "济南西", "南京南", "杭州东", "宁波");
        String startStation = "北京南";
        String endStation = "南京南";
        StationCalculateUtil.takeoutStation(stations, startStation, endStation).forEach(System.out::println);
    }
}
