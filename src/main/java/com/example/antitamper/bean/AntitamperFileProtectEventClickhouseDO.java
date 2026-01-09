package com.sama.api.antitamper.bean;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.List;

/**
 * @author: huxh
 * @description:
 * @datetime: 2025/1/10 9:44
 */
@TableName("antitamper_file_protect_event")
public class AntitamperFileProtectEventClickhouseDO extends ClickhouseBaseModel {

    private static final long serialVersionUID = 5786291167482108482L;

    /**
     * 唯一id
     */
    @TableId(type = IdType.NONE)
    @JSONField(name = "event_id")
    private String eventId;

    /**
     * 租户组织id
     */
    @JSONField(name = "tenant_org_code")
    private String tenantOrgCode;

    /**
     * 资源池id
     */
    @JSONField(name = "pool_id")
    private Long poolId;

    /**
     * 资源池code
     */
    @JSONField(name = "pool_org_code")
    private String poolOrgCode;

    /**
     * 虚机id
     */
    @JSONField(name = "vm_id")
    private Long vmId;

    /**
     * 虚机ip
     */
    @JSONField(name = "vm_ip")
    private String vmIp;

    /**
     * 虚机名称
     */
    @JSONField(name = "vm_name")
    private String vmName;

    /**
     * 资产id
     */
    @JSONField(name = "asset_id")
    private Long assetId;

    /**
     * 查询开始时间
     */
    @TableField(exist = false)
    private Date startTime;

    /**
     * 查询结束时间
     */
    @TableField(exist = false)
    private Date endTime;

    /**
     * 代理id列表(用于查询日志)
     */
    @TableField(exist = false)
    private List<String> agentIds;

    /**
     * 时间类型（0：日，1：周，2：月）
     */
    @TableField(exist = false)
    private Integer timeType;

    /**
     * 是否为对内
     */
    @TableField(exist = false)
    private Boolean isInternal;

    /**
     * 资产id列表（查询用）
     */
    @TableField(exist = false)
    private List<Long> assetIdList;

    public AntitamperFileProtectEventClickhouseDO() {
    }

    public AntitamperFileProtectEventClickhouseDO(String tenantOrgCode, Date startTime, Date endTime) {
        this.tenantOrgCode = tenantOrgCode;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public AntitamperFileProtectEventClickhouseDO(String tenantOrgCode, Integer interceptStatus, Date startTime, Date endTime) {
        this.tenantOrgCode = tenantOrgCode;
        this.interceptStatus = interceptStatus;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public AntitamperFileProtectEventClickhouseDO(String tenantOrgCode, Integer interceptStatus, Long assetId, Date startTime, Date endTime) {
        this.tenantOrgCode = tenantOrgCode;
        this.interceptStatus = interceptStatus;
        this.assetId = assetId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * 告警时间
     */
    @JSONField(name = "alarm_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date alarmTime;

    /**
     * 文件对象（路径与文件名）
     */
    @JSONField(name = "file_path")
    private String filePath;

    /**
     * （拆分的）路径
     */
    @JSONField(name = "split_path")
    private String splitPath;

    /**
     * （拆分的）文件名
     */
    @JSONField(name = "split_file")
    private String splitFile;

    /**
     * 告警内容
     */
    @JSONField(name = "alarm_msg")
    private String alarmMsg;

    /**
     * 安恒：审计级、低风险、高风险、已失陷
     * 绿盟：0：一般、1：低危、2：中危、3：高危
     * 告警等级( 0:审计级，1:低风险，2:高风险，3:已失陷，4:一般，5:低危，6:中危，7:高危)
     */
    @JSONField(name = "level_desc")
    private Integer levelDesc;

    /**
     * 操作类型（创建文件，删除文件...）
     */
    @JSONField(name = "operation_type")
    private String operationType;

    /**
     * 安恒拦截状态：拦截并记录 / 仅记录 / 已拦截 / 已放行
     * 绿盟拦截状态：0.已拦截 1.未拦截
     */
    @JSONField(name = "deal_status")
    private String dealStatus;

    /**
     * 篡改进程
     */
    @JSONField(name = "process")
    private String process;

    /**
     * 防护类型，1: 监控，2: 防护
     */
    @JSONField(name = "type")
    private Integer type;

    /**
     * 资产名称
     */
    @JSONField(name = "asset_name")
    private String assetName;

    /**
     * 服务器名称
     */
    @JSONField(name = "host_name")
    private String hostName;

    /**
     * agent代理id
     */
    @JSONField(name = "agent_id")
    private String agentId;

    /**
     * 操作系统类型（1为windows系统，2为linux系统）
     */
    @JSONField(name = "os_type")
    private Integer osType;

    /**
     * 主机ip地址（通信ip）
     */
    @JSONField(name = "host_ip")
    private String hostIp;

    /**
     * 业务域
     */
    @JSONField(name = "biz_domain")
    private String bizDomain;

    /**
     * 拦截状态，0：已拦截，1：未拦截
     */
    @JSONField(name = "intercept_status")
    private Integer interceptStatus;

    /**
     * 攻击源ip
     */
    @JSONField(name = "attack_ip")
    private String attackIp;

    /**
     * 内外ip
     */
    @JSONField(name = "ipv4")
    private String ipv4;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getTenantOrgCode() {
        return tenantOrgCode;
    }

    public void setTenantOrgCode(String tenantOrgCode) {
        this.tenantOrgCode = tenantOrgCode;
    }

    public Long getPoolId() {
        return poolId;
    }

    public void setPoolId(Long poolId) {
        this.poolId = poolId;
    }

    public String getPoolOrgCode() {
        return poolOrgCode;
    }

    public void setPoolOrgCode(String poolOrgCode) {
        this.poolOrgCode = poolOrgCode;
    }

    public Long getVmId() {
        return vmId;
    }

    public void setVmId(Long vmId) {
        this.vmId = vmId;
    }

    public String getVmIp() {
        return vmIp;
    }

    public void setVmIp(String vmIp) {
        this.vmIp = vmIp;
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public Date getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(Date alarmTime) {
        this.alarmTime = alarmTime;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getSplitPath() {
        return splitPath;
    }

    public void setSplitPath(String splitPath) {
        this.splitPath = splitPath;
    }

    public String getSplitFile() {
        return splitFile;
    }

    public void setSplitFile(String splitFile) {
        this.splitFile = splitFile;
    }

    public String getAlarmMsg() {
        return alarmMsg;
    }

    public void setAlarmMsg(String alarmMsg) {
        this.alarmMsg = alarmMsg;
    }

    public Integer getLevelDesc() {
        return levelDesc;
    }

    public void setLevelDesc(Integer levelDesc) {
        this.levelDesc = levelDesc;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getDealStatus() {
        return dealStatus;
    }

    public void setDealStatus(String dealStatus) {
        this.dealStatus = dealStatus;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public Integer getOsType() {
        return osType;
    }

    public void setOsType(Integer osType) {
        this.osType = osType;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public String getBizDomain() {
        return bizDomain;
    }

    public void setBizDomain(String bizDomain) {
        this.bizDomain = bizDomain;
    }

    public Integer getInterceptStatus() {
        return interceptStatus;
    }

    public void setInterceptStatus(Integer interceptStatus) {
        this.interceptStatus = interceptStatus;
    }

    public String getAttackIp() {
        return attackIp;
    }

    public void setAttackIp(String attackIp) {
        this.attackIp = attackIp;
    }

    public String getIpv4() {
        return ipv4;
    }

    public void setIpv4(String ipv4) {
        this.ipv4 = ipv4;
    }

    @Override
    public String toString() {
        return "AntitamperFileProtectEventClickhouseDO{" +
                "eventId='" + eventId + '\'' +
                ", tenantOrgCode='" + tenantOrgCode + '\'' +
                ", poolId=" + poolId +
                ", poolOrgCode='" + poolOrgCode + '\'' +
                ", vmId=" + vmId +
                ", vmIp='" + vmIp + '\'' +
                ", vmName='" + vmName + '\'' +
                ", assetId=" + assetId +
                ", alarmTime=" + alarmTime +
                ", filePath='" + filePath + '\'' +
                ", splitPath='" + splitPath + '\'' +
                ", splitFile='" + splitFile + '\'' +
                ", alarmMsg='" + alarmMsg + '\'' +
                ", levelDesc=" + levelDesc +
                ", operationType='" + operationType + '\'' +
                ", dealStatus='" + dealStatus + '\'' +
                ", process='" + process + '\'' +
                ", type=" + type +
                ", assetName='" + assetName + '\'' +
                ", hostName='" + hostName + '\'' +
                ", agentId='" + agentId + '\'' +
                ", osType=" + osType +
                ", hostIp='" + hostIp + '\'' +
                ", bizDomain='" + bizDomain + '\'' +
                ", interceptStatus=" + interceptStatus +
                ", attackIp='" + attackIp + '\'' +
                ", ipv4='" + ipv4 + '\'' +
                "} " + super.toString();
    }

    public Integer getTimeType() {
        return timeType;
    }

    public void setTimeType(Integer timeType) {
        this.timeType = timeType;
    }

    public Boolean getInternal() {
        return isInternal;
    }

    public void setInternal(Boolean internal) {
        isInternal = internal;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public List<String> getAgentIds() {
        return agentIds;
    }

    public void setAgentIds(List<String> agentIds) {
        this.agentIds = agentIds;
    }

    public List<Long> getAssetIdList() {
        return assetIdList;
    }

    public void setAssetIdList(List<Long> assetIdList) {
        this.assetIdList = assetIdList;
    }
}
