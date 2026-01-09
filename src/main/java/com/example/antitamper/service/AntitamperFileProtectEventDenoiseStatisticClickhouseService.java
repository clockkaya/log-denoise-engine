package com.sama.antitamper.service;

import com.core4ct.base.nokey.NkBaseService;
import com.core4ct.support.Pagination;
import com.sama.api.antitamper.bean.AntiTamperDenoiseRuleDO;
import com.sama.api.antitamper.bean.AntitamperFileProtectEventDenoiseStatisticClickhouseDO;
import com.sama.api.antitamper.bean.DenoiseEventDTO;
import com.sama.api.antitamper.bean.ForkDenoiseRuleDO;

import java.util.Date;
import java.util.List;

/**
 * @author: huxh
 * @description:
 * @datetime: 2025/3/19 10:26
 */
public interface AntitamperFileProtectEventDenoiseStatisticClickhouseService extends NkBaseService<AntitamperFileProtectEventDenoiseStatisticClickhouseDO> {

    /**
     * 批量插入
     *
     * @param toInsertList  目标列表
     */
    void batchInsert(List<AntitamperFileProtectEventDenoiseStatisticClickhouseDO> toInsertList);

    /**
     * 执行降噪统计的定时任务方法
     * 该方法用于根据设定的间隔时间cronInterval，对所有开启的降噪规则进行统计处理
     * 它首先获取所有开启的规则，然后对每个规则检查是否到达了统计周期
     * 如果到达统计周期，则查询相应周期内的事件，并将统计结果插入数据库
     *
     * @param cronInterval 定时任务的执行间隔时间，单位为分钟
     */
    void persistentDenoiseStatisticsCron(Integer cronInterval);

    /**
     * 根据降噪规则动态生成SQL并查询数据，其中 [lastDot, currentDot) 组成一个完整的统计时间窗口
     *
     * @param ruleDO        降噪规则
     * @param lastDot       统计时间段的起始值
     * @param currentDot    统计时间段的结束值
     * @param standingTime  当前的真实时间
     * @return              查询到的数据列表
     */
    List<AntitamperFileProtectEventDenoiseStatisticClickhouseDO> completeDynamicSqlAndQuery(AntiTamperDenoiseRuleDO ruleDO, Date lastDot, Date currentDot, Date standingTime);

    /**
     * 执行降噪清理的定时任务方法
     * 物理删除3个月前的表内数据
     */
    void cleanupCron();

    /**
     * 查询降噪事件列表
     *
     * @param clickhouseDO 查询条件
     * @return 降噪事件列表
     */
    Pagination<DenoiseEventDTO> queryDenoiseEventList(
            AntitamperFileProtectEventDenoiseStatisticClickhouseDO clickhouseDO, Integer current, Integer size);

    /**
     * 查询降噪事件详情
     *
     * @param uid 事件id
     * @return 详情
     */
    AntitamperFileProtectEventDenoiseStatisticClickhouseDO queryDenoiseEventDetail(String uid);

    /**
     * 更新降噪事件
     *
     * @param clickhouseDO 更新内容
     */
    void updateDenoiseEvent(AntitamperFileProtectEventDenoiseStatisticClickhouseDO clickhouseDO);

    /**
     * 查询日志累计匹配条数
     *
     * @param tenantOrgCode 租户code
     * @param ruleId        规则id
     * @return 条数
     */
    Integer countDenoiseLog(String tenantOrgCode, Long ruleId);
}
