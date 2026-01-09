package com.sama.antitamper.mapper.clickhouse;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.core4ct.base.nokey.NkBaseMapper;
import com.sama.api.antitamper.bean.*;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author: huxh
 * @description:
 * @datetime: 2025/1/10 9:49
 */
@DS("clickhouse")
public interface AntitamperFileProtectEventClickhouseMapper extends NkBaseMapper<AntitamperFileProtectEventClickhouseDO> {

    List<AntitamperFileProtectEventClickhouseDO> selectAll();

    void batchInsert(@Param("list") List<AntitamperFileProtectEventClickhouseDO> list);

    /**
     * 查询日志
     */
    List<AntitamperFileProtectEventClickhouseDO> selectFileEventList(@Param("cm") AntitamperFileProtectEventClickhouseDO eventDO);

    /**
     * 导出日志
     */
    List<AntitamperFileProtectEventClickhouseDO> pagingQuery(@Param("cm") AntitamperFileProtectEventClickhouseDO dto);

    /**
     * 查询事件数量
     *
     * @param eventDO 条件
     * @return 事件数
     */
    Integer queryEventCount(@Param("cm") AntitamperFileProtectEventClickhouseDO eventDO);

    /**
     * 查询事件拦截状态对应的数量
     */
    @MapKey("intercept_status")
    Map<Integer, Object> queryInterceptCount(@Param("orgPrefix") String orgPrefix,
                                             @Param("startTime") Date startTime,
                                             @Param("endTime") Date endTime,
                                             @Param("assetId") Long assetId);

    /**
     * 查询最近告警时间
     *
     * @param eventDO 条件：租户id，资产名称，开始时间，结束时间
     * @return 最近告警时间
     */
    Date queryLastAlarmTime(@Param("cm") AntitamperFileProtectEventClickhouseDO eventDO);

    /**
     * 查询查询事件的趋势
     *
     * @param eventDO 条件
     * @return 趋势
     */
    List<AntitamperEventTrendDTO> queryEventTrend(@Param("cm") AntitamperFileProtectEventClickhouseDO eventDO);


    /**
     * 删除一个月以上的数据
     */
    void deleteFileEventOverAMonth();

    /**
     * 查询资产日志统计top
     *
     * @param eventDO 条件参数：租户code前缀、开始时间、结束时间、防护类型
     * @param top     top级别
     * @return top数据
     */
    List<AssetLogNumTop> queryAssetLogTop(@Param("cm") AntitamperFileProtectEventClickhouseDO eventDO, @Param("top") Integer top);

    /**
     * 查询进程日志统计top
     *
     * @param eventDO 条件参数：租户code前缀、开始时间、结束时间、防护类型
     * @param top     top级别
     * @return top数据
     */
    List<ProcessLogNumTop> queryProcessLogTop(@Param("cm") AntitamperFileProtectEventClickhouseDO eventDO, @Param("top") Integer top);

    /**
     * 查询攻击源ip日志统计top
     *
     * @param orgPrefix 租户前缀
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param top       top级别
     * @return top数据
     */
    List<AttackIpNumTop> queryAttackIpLogTop(@Param("orgPrefix") String orgPrefix,
                                             @Param("startTime") Date startTime,
                                             @Param("endTime") Date endTime,
                                             @Param("top") Integer top);



    /**
     * 根据时间范围查询资产清单
     *
     * @param orgPrefix 租户前缀
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 资产清单
     */
    List<String> queryAssetIpNameList(@Param("orgPrefix") String orgPrefix,
                                      @Param("startTime") Date startTime,
                                      @Param("endTime") Date endTime);

    /**
     * 查询风险等级分布
     *
     * @param orgPrefix 租户前缀
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 风险等级分布
     */
    List<RiskLevelNum> queryRiskLevelNum(@Param("orgPrefix") String orgPrefix,
                                         @Param("startTime") Date startTime,
                                         @Param("endTime") Date endTime);

    /**
     * 查询监控/防护分布
     *
     * @param orgPrefix 租户前缀
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 监控/防护分布
     */
    List<TypeNum> queryTypeNum(@Param("orgPrefix") String orgPrefix,
                               @Param("startTime") Date startTime,
                               @Param("endTime") Date endTime);

    /**
     * 查询攻击源ip列表
     *
     * @param orgPrefix 租户前缀
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 资产清单
     */
    List<String> queryAttackIpList(@Param("orgPrefix") String orgPrefix,
                                   @Param("startTime") Date startTime,
                                   @Param("endTime") Date endTime);

    /**
     * 查询进程列表top10
     *
     * @param orgPrefix 租户前缀
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 进程列表
     */
    List<String> queryProcessTopTen(@Param("orgPrefix") String orgPrefix,
                                    @Param("startTime") Date startTime,
                                    @Param("endTime") Date endTime);

    void truncateTable();

}
