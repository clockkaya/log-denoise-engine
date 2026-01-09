package com.sama.api.antitamper.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.core4ct.base.nokey.NkBaseModel;

import java.util.Date;
import java.util.List;

/**
 * @author: huxh
 * @description:
 * @datetime: 2025/3/26 9:14
 */
@TableName("event_denoise_statistic")
public class AntitamperFileProtectEventDenoiseStatisticClickhouseDO extends NkBaseModel {

    private static final long serialVersionUID = -6733401782752092840L;

    /**
     * 唯一id
     */
    @TableId(type = IdType.NONE)
    private String uid;

    /**
     * 统计窗口的起始值
     */
    private Date statisticTime;

    /**
     * 关联的规则id
     */
    private Long ruleId;

    /**
     * 组织（租户）code
     */
    private String tenantOrgCode;

    /**
     * 分组实际值，以 | 表示递进
     */
    @TableField(value = "groupPivot")
    private String groupPivot;

    /**
     * 统计窗口内的日志匹配计数
     */
    private Long matchedCount;

    /**
     * 统计窗口内的最早发生时间
     */
    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date earliestTime;

    /**
     * 统计窗口内的最晚发生时间
     */
    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date latestTime;

    /**
     * 关联的日志uuid（展平）
     */
    private String flattenedEventId;

    /**
     * 资产id（复合）
     */
    private String compositedAssetId;

    /**
     * 文件对象（复合）
     */
    private String compositedFilePath;

    /**
     * 操作类型（复合）
     */
    private String compositedOperationType;

    /**
     * 拦截状态（复合）
     */
    private String compositedInterceptStatus;

    /**
     * 告警等级（复合）
     */
    private String compositedLevelDesc;

    /**
     * 拆分的路径（复合）
     */
    private String compositedSplitPath;

    /**
     * 拆分的文件名（复合）
     */
    private String compositedSplitFile;

    /**
     * 篡改进程（复合）
     */
    private String compositedProcess;

    /**
     * 防护类型（复合）
     */
    private String compositedType;

    /**
     * 服务器名称（复合）
     */
    private String compositedHostName;

    /**
     * 操作系统类型（复合）
     */
    private String compositedOsType;

    /**
     * 业务域（复合）
     */
    private String compositedBizDomain;

    /**
     * 发生源ip（复合）
     */
    private String compositedAttackIp;

    /**
     * 处理状态:0-未处理,1-已处理
     */
    private Integer status;

    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 降噪事件等级，0：一般，1：低危，2：中危，3：高危
     */
    @TableField(exist = false)
    private Integer denoiseEventLevel;

    /**
     * 原始日志告警等级，用于降噪事件等级查询
     */
    @TableField(exist = false)
    private String originalLevel1;

    /**
     * 原始日志告警等级，用于降噪事件等级查询
     */
    @TableField(exist = false)
    private String originalLevel2;

    /**
     * 降噪规则id列表，用于降噪事件等级查询
     */
    @TableField(exist = false)
    private List<Long> ruleIdList;

    /**
     * 未设置降噪等级的规则id列表
     */
    @TableField(exist = false)
    private List<Long> unSetRuleIds;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Date getStatisticTime() {
        return statisticTime;
    }

    public void setStatisticTime(Date statisticTime) {
        this.statisticTime = statisticTime;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public String getTenantOrgCode() {
        return tenantOrgCode;
    }

    public void setTenantOrgCode(String tenantOrgCode) {
        this.tenantOrgCode = tenantOrgCode;
    }

    public String getGroupPivot() {
        return groupPivot;
    }

    public void setGroupPivot(String groupPivot) {
        this.groupPivot = groupPivot;
    }

    public Long getMatchedCount() {
        return matchedCount;
    }

    public void setMatchedCount(Long matchedCount) {
        this.matchedCount = matchedCount;
    }

    public Date getEarliestTime() {
        return earliestTime;
    }

    public void setEarliestTime(Date earliestTime) {
        this.earliestTime = earliestTime;
    }

    public Date getLatestTime() {
        return latestTime;
    }

    public void setLatestTime(Date latestTime) {
        this.latestTime = latestTime;
    }

    public String getFlattenedEventId() {
        return flattenedEventId;
    }

    public void setFlattenedEventId(String flattenedEventId) {
        this.flattenedEventId = flattenedEventId;
    }

    public String getCompositedAssetId() {
        return compositedAssetId;
    }

    public void setCompositedAssetId(String compositedAssetId) {
        this.compositedAssetId = compositedAssetId;
    }

    public String getCompositedFilePath() {
        return compositedFilePath;
    }

    public void setCompositedFilePath(String compositedFilePath) {
        this.compositedFilePath = compositedFilePath;
    }

    public String getCompositedOperationType() {
        return compositedOperationType;
    }

    public void setCompositedOperationType(String compositedOperationType) {
        this.compositedOperationType = compositedOperationType;
    }

    public String getCompositedInterceptStatus() {
        return compositedInterceptStatus;
    }

    public void setCompositedInterceptStatus(String compositedInterceptStatus) {
        this.compositedInterceptStatus = compositedInterceptStatus;
    }

    public String getCompositedLevelDesc() {
        return compositedLevelDesc;
    }

    public void setCompositedLevelDesc(String compositedLevelDesc) {
        this.compositedLevelDesc = compositedLevelDesc;
    }

    public String getCompositedSplitPath() {
        return compositedSplitPath;
    }

    public void setCompositedSplitPath(String compositedSplitPath) {
        this.compositedSplitPath = compositedSplitPath;
    }

    public String getCompositedSplitFile() {
        return compositedSplitFile;
    }

    public void setCompositedSplitFile(String compositedSplitFile) {
        this.compositedSplitFile = compositedSplitFile;
    }

    public String getCompositedProcess() {
        return compositedProcess;
    }

    public void setCompositedProcess(String compositedProcess) {
        this.compositedProcess = compositedProcess;
    }

    public String getCompositedType() {
        return compositedType;
    }

    public void setCompositedType(String compositedType) {
        this.compositedType = compositedType;
    }

    public String getCompositedHostName() {
        return compositedHostName;
    }

    public void setCompositedHostName(String compositedHostName) {
        this.compositedHostName = compositedHostName;
    }

    public String getCompositedOsType() {
        return compositedOsType;
    }

    public void setCompositedOsType(String compositedOsType) {
        this.compositedOsType = compositedOsType;
    }

    public String getCompositedBizDomain() {
        return compositedBizDomain;
    }

    public void setCompositedBizDomain(String compositedBizDomain) {
        this.compositedBizDomain = compositedBizDomain;
    }

    public String getCompositedAttackIp() {
        return compositedAttackIp;
    }

    public void setCompositedAttackIp(String compositedAttackIp) {
        this.compositedAttackIp = compositedAttackIp;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getDenoiseEventLevel() {
        return denoiseEventLevel;
    }

    public void setDenoiseEventLevel(Integer denoiseEventLevel) {
        this.denoiseEventLevel = denoiseEventLevel;
    }

    public String getOriginalLevel1() {
        return originalLevel1;
    }

    public void setOriginalLevel1(String originalLevel1) {
        this.originalLevel1 = originalLevel1;
    }

    public String getOriginalLevel2() {
        return originalLevel2;
    }

    public void setOriginalLevel2(String originalLevel2) {
        this.originalLevel2 = originalLevel2;
    }

    public List<Long> getRuleIdList() {
        return ruleIdList;
    }

    public void setRuleIdList(List<Long> ruleIdList) {
        this.ruleIdList = ruleIdList;
    }

    public List<Long> getUnSetRuleIds() {
        return unSetRuleIds;
    }

    public void setUnSetRuleIds(List<Long> unSetRuleIds) {
        this.unSetRuleIds = unSetRuleIds;
    }

    @Override
    public String toString() {
        return "AntitamperFileProtectEventDenoiseStatisticClickhouseDO{" +
                "uid='" + uid + '\'' +
                ", statisticTime=" + statisticTime +
                ", ruleId=" + ruleId +
                ", tenantOrgCode='" + tenantOrgCode + '\'' +
                ", groupPivot='" + groupPivot + '\'' +
                ", matchedCount=" + matchedCount +
                ", earliestTime=" + earliestTime +
                ", latestTime=" + latestTime +
                ", flattenedEventId='" + flattenedEventId + '\'' +
                ", compositedAssetId='" + compositedAssetId + '\'' +
                ", compositedFilePath='" + compositedFilePath + '\'' +
                ", compositedOperationType='" + compositedOperationType + '\'' +
                ", compositedInterceptStatus='" + compositedInterceptStatus + '\'' +
                ", compositedLevelDesc='" + compositedLevelDesc + '\'' +
                ", compositedSplitPath='" + compositedSplitPath + '\'' +
                ", compositedSplitFile='" + compositedSplitFile + '\'' +
                ", compositedProcess='" + compositedProcess + '\'' +
                ", compositedType='" + compositedType + '\'' +
                ", compositedHostName='" + compositedHostName + '\'' +
                ", compositedOsType='" + compositedOsType + '\'' +
                ", compositedBizDomain='" + compositedBizDomain + '\'' +
                ", compositedAttackIp='" + compositedAttackIp + '\'' +
                ", status=" + status +
                ", updateTime=" + updateTime +
                ", denoiseEventLevel=" + denoiseEventLevel +
                ", originalLevel1='" + originalLevel1 + '\'' +
                ", originalLevel2='" + originalLevel2 + '\'' +
                ", ruleIdList=" + ruleIdList +
                ", unSetRuleIds=" + unSetRuleIds +
                "} " + super.toString();
    }
}
