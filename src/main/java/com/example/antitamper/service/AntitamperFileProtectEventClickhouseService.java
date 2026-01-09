package com.sama.antitamper.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.core4ct.base.nokey.NkBaseService;
import com.sama.antitamper.utils.data.bean.writeEs.WriteAntitamperBean;
import com.sama.api.antitamper.bean.AntitamperFileProtectEventClickhouseDO;
import com.core4ct.support.Pagination;
import com.sama.api.antitamper.bean.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author: huxh
 * @description:
 * @datetime: 2025/1/10 9:46
 */
public interface AntitamperFileProtectEventClickhouseService extends NkBaseService<AntitamperFileProtectEventClickhouseDO> {

    /**
     * 批量插入
     *
     * @param list  List<NetworkFlowlogEventClickhouseDO>
     */
    void batchInsert(List<AntitamperFileProtectEventClickhouseDO> list);

    /**
     * 批量（转换）迁移
     *
     * @param rawList   原始 WriteAntitamperBean 列表
     */
    void batchMigrate(List<WriteAntitamperBean> rawList);

    /**
     * 拓展wrapper查询的返回类型
     *
     * @param wrapper
     * @return  List<Map<String, Object>>
     */
    List<Map<String, Object>> queryObjectFromDB(Wrapper<AntitamperFileProtectEventClickhouseDO> wrapper);
    

    /**
     * 查询文件监控日志
     *
     * @param eventDO 查询条件
     * @param current 页码
     * @param size    条数
     * @return 结果
     */
    Pagination<AntitamperFileProtectEventClickhouseDO> getFileEventPage(AntitamperFileProtectEventClickhouseDO eventDO, Integer current, Integer size);

    /**
     * 根据时间查询事件数量
     *
     * @param eventDO 条件
     * @return 事件数量
     */
    Integer getEventCount(AntitamperFileProtectEventClickhouseDO eventDO);


    /**
     * 查询拦截状态-数量
     */

    Map<Integer, Integer> queryInterceptCount(String orgPrefix, Date startTime, Date endTime, Long assetId);
    /**
     * 获取最近告警时间
     *
     * @param tenantOrgCode 租户id
     * @param assetId       资产名称
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @return 最近告警时间
     */
    Date getLastAlarmTime(String tenantOrgCode, Long assetId, Date startTime, Date endTime);


    /**
     * 获取拦截日志趋势
     *
     * @param tenantOrgCode 租户id
     * @param timeType      时间类型（日/月/周）
     * @param endTime       结束时间
     * @param startTime     开始时间
     * @return BaseDataModel
     */
    BaseDataModel getEventTrend(String tenantOrgCode, Integer timeType, Date endTime, Date startTime) throws Exception;



    /**
     * 获取资产日志数统计结果
     *
     * @param tenantOrgCode 租户
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @param type          防护类型
     * @return
     */
    List<AssetLogNumTop> getAssetLogNum(String tenantOrgCode, Date startTime, Date endTime, Integer type);

    /**
     * 获取进程日志数统计结果
     *
     * @param tenantOrgCode 租户
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @param type          防护类型
     * @return
     */
    List<ProcessLogNumTop> getProcessLogNum(String tenantOrgCode, Date startTime, Date endTime, Integer type);

    /**
     * 获取攻击源ip日志数统计结果
     *
     * @param tenantOrgCode 租户
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @return 数据
     */

    List<AttackIpNumTop> getAttackIpLogNum(String tenantOrgCode, Date startTime, Date endTime);
    /**
     * 根据事件ID查询数据列表
     *
     * @param eventIds 事件ID列表
     * @return AntitamperFileProtectEventClickhouseDO列表
     */
    List<AntitamperFileProtectEventClickhouseDO> queryList(List<String> eventIds);
    
}
