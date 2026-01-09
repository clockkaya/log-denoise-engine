package com.sama.antitamper.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.core4ct.base.nokey.impl.NkBaseServiceImpl;
import com.core4ct.support.Pagination;
import com.core4ct.utils.DataUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sama.antitamper.constant.Constants;
import com.core4ct.utils.StringUtils;
import com.sama.antitamper.mapper.clickhouse.AntitamperFileProtectEventClickhouseMapper;
import com.sama.antitamper.service.AntitamperFileProtectEventClickhouseService;
import com.sama.antitamper.support.DataProcessCacheManager;
import com.sama.antitamper.utils.DateUtil;
import com.sama.antitamper.utils.StringToDateConverter;
import com.sama.antitamper.utils.data.bean.writeEs.WriteAntitamperBean;
import com.sama.api.antitamper.bean.*;
import com.sama.api.business.AssetDubboService;
import com.sama.api.business.object.AssetDO;
import com.sama.api.pool.object.DO.VMDO;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.convention.NameTokenizers;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import jakarta.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
/**
 * @author: huxh
 * @description:
 * @datetime: 2025/1/10 9:47
 */
@Service
public class AntitamperFileProtectEventClickhouseServiceImpl
        extends NkBaseServiceImpl<AntitamperFileProtectEventClickhouseDO, AntitamperFileProtectEventClickhouseMapper>
        implements AntitamperFileProtectEventClickhouseService {

    private final static Logger logger = LogManager.getLogger(AntitamperFileProtectEventClickhouseService.class);

    private final ModelMapper migrationMapper;

    @Resource(name = "assetNameCache")
    private Cache<Long, String> assetNameCache;

    @DubboReference
    private AssetDubboService assetDubboService;

    @Resource
    DataProcessCacheManager dataProcessCacheManager;

    public AntitamperFileProtectEventClickhouseServiceImpl() {
        this.migrationMapper = new ModelMapper();
        this.migrationMapper.getConfiguration()
                .setSourceNameTokenizer(NameTokenizers.UNDERSCORE)
                .setDestinationNameTokenizer(NameTokenizers.CAMEL_CASE)
                .setMatchingStrategy(MatchingStrategies.STANDARD)
                .setAmbiguityIgnored(true);

        // 添加自定义日期转换器
        this.migrationMapper.createTypeMap(String.class, Date.class).setConverter(new StringToDateConverter("yyyy-MM-dd HH:mm:ss"));

        // logic from MySQLUtils.writeMySQL(List<WriteAntitamperBean> list)
        this.migrationMapper.addMappings(new PropertyMap<WriteAntitamperBean, AntitamperFileProtectEventClickhouseDO>() {
            @Override
            protected void configure() {
                map().setEventId(source.getKafka_id());
                map().setAlarmMsg(source.getDesc());
                map().setOperationType(source.getOperation());
                map().setDealStatus(source.getResult());
                map().setAgentId(source.getUid());
                // （包含条件判断）先跳过 type 字段的自动映射
                skip(destination.getType());
            }
        });
    }

    @Override
    public void batchInsert(List<AntitamperFileProtectEventClickhouseDO> list) {
        this.mapper.batchInsert(list);
    }

    @Override
    public void batchMigrate(List<WriteAntitamperBean> rawList) {
        List<AntitamperFileProtectEventClickhouseDO> targetList = new ArrayList<>();
        rawList.forEach(flowLogBean -> {
            AntitamperFileProtectEventClickhouseDO fileProtectClickhouseDO = migrationMapper.map(flowLogBean, AntitamperFileProtectEventClickhouseDO.class);

            // 处理 type 字段
            if (DataUtils.isNotEmpty(flowLogBean.protective_type)){
                fileProtectClickhouseDO.setType(Integer.valueOf(flowLogBean.protective_type));
            }

            // 处理 filePath 字段
            String filePath = fileProtectClickhouseDO.getFilePath();
            if (DataUtils.isNotEmpty(filePath)){
                String splitPath;
                String splitFile  = "";
                if (filePath.contains("->") && DataUtils.isNotEmpty(fileProtectClickhouseDO.getInterceptStatus())){
                    // 1 包含 "->" 的情况
                    if (0 == fileProtectClickhouseDO.getInterceptStatus()){
                        // 1.1 拦截状态为 0：已拦截，则取 "->" 前的字符串
                        filePath = StringUtils.substringBefore(filePath, "->").trim();
                    } else if (1 == fileProtectClickhouseDO.getInterceptStatus()){
                        // 1.2 拦截状态为 1：未拦截，则取 "->" 后的字符串
                        filePath = StringUtils.substringAfter(filePath, "->").trim();
                    }
                }
                // 2 （并继续）拆分处理
                int lastDotIndex = filePath.lastIndexOf('.');
                int lastSeparatorIndex = Math.max(filePath.lastIndexOf('\\'), filePath.lastIndexOf('/'));
                if (lastDotIndex > lastSeparatorIndex) {
                    // 2.1 存在文件名，则拆分
                    splitPath = filePath.substring(0, lastSeparatorIndex + 1);
                    splitFile = filePath.substring(lastSeparatorIndex + 1);
                } else {
                    // 2.2 不存在文件名，则保留
                    splitPath = filePath;
                }
                fileProtectClickhouseDO.setSplitPath(splitPath);
                fileProtectClickhouseDO.setSplitFile(splitFile);
            }

            // 补充虚机信息
            VMDO vmDO = dataProcessCacheManager.getVmIdAndCache().get(flowLogBean.getVm_id());
            if (DataUtils.isNotEmpty(vmDO)){
                fileProtectClickhouseDO.setVmId(vmDO.getId());
                fileProtectClickhouseDO.setVmIp(vmDO.getControlIp());
                fileProtectClickhouseDO.setVmName(vmDO.getVmName());
            }

            fileProtectClickhouseDO.setCreateTime(new Date());
            fileProtectClickhouseDO.setUpdateTime(new Date());
            targetList.add(fileProtectClickhouseDO);
        });
        logger.info("【迁移|文件防护】 List<WriteAntitamperBean> -----> List<AntitamperFileProtectEventClickhouseDO>\n{}\n-----> {}",
                JSON.toJSONString(rawList), JSON.toJSONString(targetList));
        batchInsert(targetList);
    }


    @Override
    public Pagination<AntitamperFileProtectEventClickhouseDO> getFileEventPage(AntitamperFileProtectEventClickhouseDO eventDO, Integer current, Integer size) {
        String tenantOrgCode = eventDO.getTenantOrgCode();
        Assert.notNull(tenantOrgCode, "租户id为空！");

        Page<AntitamperFileProtectEventClickhouseDO> page = PageHelper.startPage(current, size);
        List<AntitamperFileProtectEventClickhouseDO> protectEventDOS = this.mapper.selectFileEventList(eventDO);

        // 查资产名称
        protectEventDOS.forEach(element -> {
            String assetName = assetNameCache.get(element.getAssetId(), key -> {
                AssetDO detail = assetDubboService.detail(key);
                return DataUtils.isEmpty(detail) ? "" : detail.getAssetName();
            });
            element.setAssetName(assetName);
        });

        Pagination<AntitamperFileProtectEventClickhouseDO> pagination = new Pagination<>(current, size);
        pagination.setRecords(protectEventDOS);
        long total = page.getTotal() < 100000? page.getTotal():100000;
        pagination.setTotal(total);
        return pagination;
    }

    @Override
    public Integer getEventCount(AntitamperFileProtectEventClickhouseDO eventDO) {
        return this.mapper.queryEventCount(eventDO);
    }

    @Override
    public Map<Integer, Integer> queryInterceptCount(String orgPrefix, Date startTime, Date endTime, Long assetId) {
        Map<Integer, Object> map = mapper.queryInterceptCount(orgPrefix, startTime, endTime, assetId);
        Map<Integer, Integer> result = new HashMap<Integer, Integer>() {{
            put(Constants.BLOCKED_STATUS, 0);
            put(Constants.UNBLOCKED_STATUS, 0);
        }};
        if (DataUtils.isNotEmpty(map.get(Constants.BLOCKED_STATUS))) {
            HashMap hashMap = (HashMap) map.get(Constants.BLOCKED_STATUS);
            result.put(Constants.BLOCKED_STATUS, Integer.valueOf(hashMap.get("num").toString()));
        }
        if (DataUtils.isNotEmpty(map.get(Constants.UNBLOCKED_STATUS))) {
            HashMap hashMap = (HashMap) map.get(Constants.UNBLOCKED_STATUS);
            result.put(Constants.UNBLOCKED_STATUS, Integer.valueOf(hashMap.get("num").toString()));

        }
        return result;
    }

    @Override
    public Date getLastAlarmTime(String tenantOrgCode, Long assetId, Date startTime, Date endTime) {
        AntitamperFileProtectEventClickhouseDO eventDO = new AntitamperFileProtectEventClickhouseDO();
        eventDO.setTenantOrgCode(tenantOrgCode);
        eventDO.setAssetId(assetId);
        eventDO.setStartTime(startTime);
        eventDO.setEndTime(endTime);
        return this.mapper.queryLastAlarmTime(eventDO);
    }

    /**
     * 处理趋势数据
     */
    private void processTrendData(List<AntitamperEventTrendDTO> trendList, List<String> xList, List<Integer> yData, int queryType) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Calendar calendar = Calendar.getInstance();
        Map<String, Integer> trendMap = trendList.stream().collect(
                Collectors.toMap(AntitamperEventTrendDTO::getTimeXaxis, AntitamperEventTrendDTO::getCountYaxis));

        for (String xaxis : xList) {
            int sum = 0;
            if (queryType == 1) {          // 按小时统计
                sum = trendMap.getOrDefault(xaxis, 0);
            } else if (queryType == 2) {   // 按天统计
                sum = trendMap.getOrDefault(xaxis, 0);
            } else if (queryType == 3) {  // 按6小时统计
                calendar.setTime(sdf.parse(xaxis));
                calendar.add(Calendar.HOUR, 6);
                long head = sdf.parse(xaxis).getTime();
                long tail = calendar.getTime().getTime();
                for (AntitamperEventTrendDTO trendDTO : trendList) {
                    long time = sdf.parse(trendDTO.getTimeXaxis()).getTime();
                    if (time >= head && time < tail) {
                        sum += trendDTO.getCountYaxis();
                    }
                }
            }
            yData.add(sum);
        }
    }



    @Override
    public BaseDataModel getEventTrend(String tenantOrgCode, Integer timeType, Date endTime, Date startTime) throws Exception {
        AntitamperFileProtectEventClickhouseDO eventDO = new AntitamperFileProtectEventClickhouseDO();
        eventDO.setTenantOrgCode(tenantOrgCode);
        eventDO.setEndTime(endTime);
        eventDO.setStartTime(startTime);
        long diff = endTime.getTime() - startTime.getTime();

        BaseDataModel baseDataModel = new BaseDataModel();

        int timeTypeValue;
        List<String> xList;
        int queryType;
        if ((diff - 86400000L) <= 0) { // T<=24h
            timeTypeValue = 3; // 3——按小时统计
            queryType = 1;     // 每小时一个点
            xList = DateUtil.getTimeXaxis(queryType, startTime, endTime);
        } else if ((diff - 604800000L) <= 0) { // 1d<T<=7d
            timeTypeValue = 3; // 3——按小时统计
            queryType = 3;     // 每6小时一个点
            xList = DateUtil.getTimeXaxis(queryType, startTime, endTime);
        } else { // 7d<T
            timeTypeValue = 5; // 5——按天统计
            queryType = 2;     // 每天一个点
            xList = DateUtil.getTimeXaxis(queryType, startTime, endTime);
        }

        eventDO.setTimeType(timeTypeValue);
        List<AntitamperEventTrendDTO> tempEventTrend = this.mapper.queryEventTrend(eventDO);
        Map<Integer, List<AntitamperEventTrendDTO>> typeEventTrendMap = tempEventTrend.stream()
                .collect(Collectors.groupingBy(AntitamperEventTrendDTO::getType));

        baseDataModel.setxAxis(xList);
        List<BaseDataModel.Data> yAxis = new ArrayList<>();
        List<Integer> monitorYData = new ArrayList<>(Collections.nCopies(xList.size(), 0));
        List<Integer> protectYData = new ArrayList<>(Collections.nCopies(xList.size(), 0));

        if (typeEventTrendMap.containsKey(1)) {
            monitorYData.clear();
            processTrendData(typeEventTrendMap.get(1), xList, monitorYData, queryType);
        }
        if (typeEventTrendMap.containsKey(2)) {
            protectYData.clear();
            processTrendData(typeEventTrendMap.get(2), xList, protectYData, queryType);
        }

        yAxis.add(new BaseDataModel.Data(1, monitorYData));
        yAxis.add(new BaseDataModel.Data(2, protectYData));
        baseDataModel.setyAxis(yAxis);
        return baseDataModel;
    }

    @Override
    public List<AssetLogNumTop> getAssetLogNum(String tenantOrgCode, Date startTime, Date endTime, Integer type) {
        AntitamperFileProtectEventClickhouseDO eventDO = new AntitamperFileProtectEventClickhouseDO();
        eventDO.setTenantOrgCode(tenantOrgCode);
        eventDO.setStartTime(startTime);
        eventDO.setEndTime(endTime);
        eventDO.setType(type);
        return mapper.queryAssetLogTop(eventDO, 10);
    }

    @Override
    public List<ProcessLogNumTop> getProcessLogNum(String tenantOrgCode, Date startTime, Date endTime, Integer type) {
        AntitamperFileProtectEventClickhouseDO eventDO = new AntitamperFileProtectEventClickhouseDO();
        eventDO.setTenantOrgCode(tenantOrgCode);
        eventDO.setStartTime(startTime);
        eventDO.setEndTime(endTime);
        eventDO.setType(type);
        return mapper.queryProcessLogTop(eventDO, 10);
    }

    @Override
    public List<AttackIpNumTop> getAttackIpLogNum(String tenantOrgCode, Date startTime, Date endTime) {
        return mapper.queryAttackIpLogTop(tenantOrgCode, startTime, endTime, 10);
    }
    
    @Override
    public List<Map<String, Object>> queryObjectFromDB(Wrapper<AntitamperFileProtectEventClickhouseDO> wrapper) {
        return this.mapper.selectMaps(wrapper);
    }

    @Override
    public List<AntitamperFileProtectEventClickhouseDO> queryList(List<String> eventIds) {
        return this.mapper.selectBatchIds(eventIds);
    }
}
