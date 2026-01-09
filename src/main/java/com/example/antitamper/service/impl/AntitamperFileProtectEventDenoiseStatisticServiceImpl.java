package com.sama.antitamper.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.core4ct.base.nokey.impl.NkBaseServiceImpl;
import com.core4ct.constants.Constants;
import com.core4ct.exception.GenericException;
import com.core4ct.support.Pagination;
import com.core4ct.utils.DataUtils;
import com.core4ct.utils.DateUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sama.antitamper.mapper.clickhouse.AntitamperFileProtectEventDenoiseStatisticClickhouseMapper;
import com.sama.antitamper.service.AntitamperDenoiseRuleService;
import com.sama.antitamper.service.AntitamperFileProtectEventClickhouseService;
import com.sama.antitamper.service.AntitamperFileProtectEventDenoiseStatisticClickhouseService;
import com.sama.antitamper.utils.SelfApacheConverter;
import com.sama.api.antitamper.bean.*;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.sama.antitamper.constant.Constants.MAX_PAGE_TOTAL;

/**
 * @author: huxh
 * @description:
 * @datetime: 2025/3/19 10:27
 */
@Service
public class AntitamperFileProtectEventDenoiseStatisticServiceImpl
        extends NkBaseServiceImpl<AntitamperFileProtectEventDenoiseStatisticClickhouseDO, AntitamperFileProtectEventDenoiseStatisticClickhouseMapper>
        implements AntitamperFileProtectEventDenoiseStatisticClickhouseService {

    private final static Logger logger = LogManager.getLogger(AntitamperFileProtectEventDenoiseStatisticServiceImpl.class);

    private final static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    private final static String ISO_8601_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    private final static Integer TOP_LIMIT = 11;

    /**
     * 默认分组
     */
    private final static String TENANT_ORG_CODE = "tenant_org_code";

    @Resource(name = "fileProtectDenoiseCron")
    ThreadPoolTaskExecutor fileProtectDenoiseCron;

    @Resource
    AntitamperFileProtectEventClickhouseService antitamperFileProtectEventClickhouseService;

    @Resource
    AntitamperDenoiseRuleService antitamperDenoiseRuleService;

    public AntitamperFileProtectEventDenoiseStatisticServiceImpl() {
        // 注册自定义的 Converter
        ConvertUtils.register(new SelfApacheConverter(YYYY_MM_DD_HH_MM_SS), Date.class);
    }

    @Override
    public void batchInsert(List<AntitamperFileProtectEventDenoiseStatisticClickhouseDO> toInsertList) {
        this.mapper.batchInsert(toInsertList);
    }

    @Override
    public void persistentDenoiseStatisticsCron(Integer cronInterval){
        // 1 获取所有开启的规则
        AntiTamperDenoiseRuleDO queryDO = new AntiTamperDenoiseRuleDO();
        queryDO.setSwitchStatus(1);
        queryDO.setDelFlag(Constants.DelFlag.AVAILABLE);
        List<AntiTamperDenoiseRuleDO> rules = antitamperDenoiseRuleService.queryList(queryDO);
        if (DataUtils.isEmpty(rules)){
            logger.info("【降噪】 当前无开启的规则，跳过统计！");
            return;
        }

        // 2 多线程对每个规则执行
        Date standingTime = new Date();
        Date currentDot = backwardNearestTime(standingTime, cronInterval);

        rules.forEach(rule -> fileProtectDenoiseCron.execute(() -> {
            // 2.1 在每个定时时间隔上，判断是否进入 timeWindow 的统计周期
            int zeroStartMinutes = DateUtil.hour(currentDot, true) * 60 + DateUtil.minute(currentDot);
            int timeWindow = rule.getTimeWindow();
            if ( zeroStartMinutes % timeWindow != 0){
                logger.info("【降噪】 当前定时任务 ({}) 下，该规则 (id: {}, timeWindow: {} 分钟) 未进入统计周期！",
                        DateUtils.format(currentDot, YYYY_MM_DD_HH_MM_SS), rule.getId(), timeWindow);
                return;
            }

            // 2.2 拼接动态 sql 并查询
            Date lastDot = DateUtils.addDate(currentDot, Calendar.MINUTE, -timeWindow);
            List<AntitamperFileProtectEventDenoiseStatisticClickhouseDO> selectedEvents = completeDynamicSqlAndQuery(rule, lastDot, currentDot, standingTime);
            if (DataUtils.isEmpty(selectedEvents)){
                logger.info("【降噪】 该规则 (id: {}) 在统计周期 [{}, {}) 内无匹配日志，跳过统计！",
                        rule.getId(), DateUtils.format(lastDot, YYYY_MM_DD_HH_MM_SS), DateUtils.format(currentDot, YYYY_MM_DD_HH_MM_SS));
                return;
            }

            // 2.3 插入DB
            batchInsert(selectedEvents);
            logger.info("【降噪】 成功！该规则 (id: {}) 在统计周期 [{}, {}) 内完成匹配处理并计入！\n{}",
                    rule.getId(), DateUtils.format(lastDot, YYYY_MM_DD_HH_MM_SS), DateUtils.format(currentDot, YYYY_MM_DD_HH_MM_SS), JSON.toJSON(selectedEvents));
        }));
    }

    @Override
    public List<AntitamperFileProtectEventDenoiseStatisticClickhouseDO> completeDynamicSqlAndQuery(AntiTamperDenoiseRuleDO ruleDO, Date lastDot, Date currentDot, Date standingTime){
        try {
            QueryWrapper<AntitamperFileProtectEventClickhouseDO> queryWrapper = new QueryWrapper<>();

            // 1 解析 where 条件
            // 1.1 默认筛选（统计时间、生效组织）
            queryWrapper.and(defaultBracket ->
                    defaultBracket.ge("alarm_time", lastDot).lt("alarm_time", currentDot)
                            .like(TENANT_ORG_CODE, ruleDO.getTenantOrgCode() + "%"));
            // 1.1 自定义筛选
            if (DataUtils.isNotEmpty(ruleDO.getWhereCondition())){
                WhereConditionBO whereConditions = JSON.parseObject(ruleDO.getWhereCondition(), new TypeReference<WhereConditionBO>() {});
                List<WhereConditionBO.OrCondition> orConditions = whereConditions.getOrConditions();
                // “非空”
                if (DataUtils.isNotEmpty(orConditions)){
                    queryWrapper.and(customBracket -> {
                        for (WhereConditionBO.OrCondition orCondition : orConditions){
                            // “块级用 or”
                            customBracket.or(unitQueryWrapper -> {
                                for (WhereConditionBO.OrCondition.AndCondition andCondition : orCondition.getAndConditions()){
                                    // “链式用 and”
                                    decodeConditionIntoWrapper(andCondition).accept(unitQueryWrapper);
                                }
                            });
                        }
                    });
                }
            }

            // 2 解析 group by 条件
            List<String> groupFields = new ArrayList<>();
            // select 必须保留分组字段(驼峰)
            List<String> selectFields = new ArrayList<>();
            // 2.1 默认数据分域为第一分组
            groupFields.add(TENANT_ORG_CODE);
            selectFields.add(TENANT_ORG_CODE + " AS " + StrUtil.toCamelCase(TENANT_ORG_CODE));
            // 2.2 自定义分组
            if (DataUtils.isNotEmpty(ruleDO.getGroupCondition())){
                List<String> groupFieldsFromRule = JSON.parseObject(ruleDO.getGroupCondition(), new TypeReference<List<String>>() {});
                groupFields.addAll(groupFieldsFromRule);
                String groupPivot = gradedRealValue(groupFieldsFromRule, null);
                selectFields.add(groupPivot);
            }
            queryWrapper.groupBy(groupFields);

            // 3 在分组基础上计数COUNT、极值MIN/MAX、展平udf、复合udf
            selectFields.addAll(Arrays.asList(
                    "COUNT(event_id) AS matchedCount",
                    "MIN(alarm_time) AS earliestTime",
                    "MAX(alarm_time) AS latestTime",
                    ufdFlattenDisplay("event_id", "flattened_event_id"),
                    udfCompositeTopLimit("asset_id", TOP_LIMIT, "composited_asset_id"),
                    udfCompositeTopLimit("file_path", TOP_LIMIT, "composited_file_path"),
                    udfCompositeTopLimit("operation_type", TOP_LIMIT, "composited_operation_type"),
                    udfCompositeTopLimit("intercept_status", TOP_LIMIT, "composited_intercept_status"),
                    udfCompositeTopLimit("level_desc", TOP_LIMIT, "composited_level_desc"),
                    udfCompositeTopLimit("split_path", TOP_LIMIT, "composited_split_path"),
                    udfCompositeTopLimit("split_file", TOP_LIMIT, "composited_split_file"),
                    udfCompositeTopLimit("process", TOP_LIMIT, "composited_process"),
                    udfCompositeTopLimit("type", TOP_LIMIT, "composited_type"),
                    udfCompositeTopLimit("host_name", TOP_LIMIT, "composited_host_name"),
                    udfCompositeTopLimit("os_type", TOP_LIMIT, "composited_os_type"),
                    udfCompositeTopLimit("biz_domain", TOP_LIMIT, "composited_biz_domain"),
                    udfCompositeTopLimit("attack_ip", TOP_LIMIT, "composited_attack_ip")
            ));
            queryWrapper.select(selectFields.toArray(new String[0]));

            logger.info("【降噪】 该格式化规则(id: {}) 解析为:\nSELECT {} \nFROM sama_anti_tamper.antitamper_file_protect_event \nWHERE {}",
                    ruleDO.getId(), queryWrapper.getSqlSelect(), queryWrapper.getTargetSql());

            // 4 查询
            List<Map<String, Object>> rawRes = antitamperFileProtectEventClickhouseService.queryObjectFromDB(queryWrapper);
            // logger.info("【降噪临时】 queryObjectFromDB 查询结果\n{}", JSON.toJSONString(rawRes));

            // 5 最终转换为 DO list
            List<AntitamperFileProtectEventDenoiseStatisticClickhouseDO> statisticDOList = new ArrayList<>();
            rawRes.forEach(map -> {
                AntitamperFileProtectEventDenoiseStatisticClickhouseDO statisticDO = new AntitamperFileProtectEventDenoiseStatisticClickhouseDO();
                try {
                    // 配合 select 驼峰实现
                    BeanUtils.populate(statisticDO, map);
                    statisticDO.setUid(UUID.randomUUID().toString().replace("-", ""));
                    statisticDO.setRuleId(ruleDO.getId());
                    statisticDO.setStatisticTime(lastDot);
                    statisticDO.setCreateTime(standingTime);
                    statisticDO.setUpdateTime(standingTime);
                    statisticDO.setStatus(0);
                    statisticDOList.add(statisticDO);
                } catch (Exception e) {
                    throw new GenericException(e);
                }
            });

            return statisticDOList;
        } catch (Exception e){
            logger.warn("【降噪】 错误！该格式化规则 (id: {}) 无法解析处理，请排查！", ruleDO.getId(), e);
            return null;
        }
    }

    @Deprecated
    @Override
    public void cleanupCron(){
        fileProtectDenoiseCron.execute(() -> {
            Date threeMonthsAgo = DateUtils.addDate(new Date(), Calendar.MONTH, -3);
            int cleanupCnt = mapper.cleanup(threeMonthsAgo);
            logger.info("【降噪】 完成 {} 前清理共 {} 条 ", DateUtils.format(threeMonthsAgo, YYYY_MM_DD_HH_MM_SS), cleanupCnt);
        });
    }

    /**
     * 用于将给定时间 anchorTime 向前取整到最近的 interval 分钟间隔的整分钟。建议 interval 和定时任务保持一致，相当于只处理的调用造成的低延迟。
     *
     * @param anchorTime    给定时间
     * @param interval      取整间隔（分钟）
     * @return              向前最邻近的整分钟
     */
    private Date backwardNearestTime(Date anchorTime, int interval){
        Calendar calendar = DateUtils.getCalendar(anchorTime);

        // 计算距离当前分钟向前最近间隔的分钟数
        int originalTime = calendar.get(Calendar.MINUTE);
        int targetTime = (originalTime / interval) * interval;

        // 设置返整分钟
        calendar.set(Calendar.MINUTE, targetTime);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    /**
     * 使用 Consumer 接口包装，将条件对象转换为MyBatis-Plus查询条件；省略了 "notBetween"、"likeLeft"、"likeRight" 等不常用操作
     *
     * @param andCondition  条件对象
     * @return              已经转义后的查询条件
     */
    private Consumer<QueryWrapper<AntitamperFileProtectEventClickhouseDO>> decodeConditionIntoWrapper(WhereConditionBO.OrCondition.AndCondition andCondition) {
        String field = andCondition.getField();
        String operator = andCondition.getOperator();
        String value = andCondition.getValue();
        if (DataUtils.isEmpty(field) || DataUtils.isEmpty(operator) || DataUtils.isEmpty(value)){
            throw new GenericException("缺少必要字段，无法解析：" + JSONArray.toJSONString(andCondition));
        }

        return wrapper -> {
            switch (operator) {
                // 等于 =
                case "eq":
                    wrapper.eq(field, value);
                    break;
                // 不等于 <>
                case "ne":
                    wrapper.ne(field, value);
                    break;
/*                // 大于 >
                case "gt":
                    wrapper.gt(field, value);
                    break;
                // 大于等于 >=
                case "ge":
                    wrapper.ge(field, value);
                    break;
                // 小于 <
                case "lt":
                    wrapper.lt(field, value);
                    break;
                // 小于等于 <=
                case "le":
                    wrapper.le(field, value);
                    break;
                // BETWEEN 值1 AND 值2
                case "between":
                    // 处理逗号分隔的字符串值
                    String[] betweenValues = String.valueOf(value).split(",");
                    if (betweenValues.length != 2) {
                        throw new GenericException("BETWEEN 操作需要两个参数，当前参数：" + value);
                    }
                    wrapper.between(field, betweenValues[0].trim(), betweenValues[1].trim());*/
                // LIKE '%值%'
                case "like":
                    wrapper.like(field, "%" + value + "%");
                    break;
                // NOT LIKE '%值%'
                case "notLike":
                    wrapper.notLike(field, "%" + value + "%");
                    break;
                // 字段 IN (v0, v1, ...)
                case "in":
                    // 形如 "[3,7]"
                    List<String> inValues = JSON.parseObject(value, new TypeReference<List<String>>() {});
                    if (DataUtils.isEmpty(inValues)) {
                        throw new GenericException("IN 操作需要至少一个参数，当前参数：" + value);
                    }
                    wrapper.in(field, inValues);
                    break;
                // 字段 NOT IN (v0, v1, ...)
/*                case "notIn":
                    List<String> notInValues = JSON.parseObject(value, new TypeReference<List<String>>() {});
                    if (DataUtils.isEmpty(notInValues)) {
                        throw new GenericException("NOT IN 操作需要至少一个参数，当前参数：" + value);
                    }
                    wrapper.notIn(field, notInValues);
                    break;*/
                default:
                    throw new GenericException("不支持的运算符: " + operator);
            }
        };
    }

    /**
     * 通过 clickhouse 的内置函数，实现分组的真实值用 | 表示递进为单行字符串
     *
     * @param columns   需要取得实际值的列项名列表
     * @param alias     需要被展平的列项名
     * @return          形如 arrayStringConcat(array(toString(file_path), toString(level_desc), toString(operation_type), toString(process)), '| ') AS groupPivot
     */
    private String gradedRealValue(List<String> columns, String alias){
        // toString() 会将 null 转换为空字符串
        String arrayExpression = columns.stream()
                .map(field -> "toString(" + field + ")")
                .collect(Collectors.joining(", ", "array(", ")"));

        return String.format(
                "arrayStringConcat(%s, ' | ') AS %s",
                arrayExpression,
                StrUtil.toCamelCase(DataUtils.isNotEmpty(alias) ? alias : "groupPivot")
        );
    }

    /**
     * 实现组内简单全排列，展平为单行字符串；作用同等于 clickhouse udf: flatten_display
     *
     * @param column    需要复合处理的列项名
     * @param alias     重命名（默认转驼峰）
     * @return          CREATE FUNCTION flatten_display AS column_name ->
     *                  arrayStringConcat(arrayDistinct(groupArray(column_name)), ',')
     */
    private String ufdFlattenDisplay(String column, String alias){
        return String.format("flatten_display(%s) AS %s",
                column,
                StrUtil.toCamelCase(DataUtils.isNotEmpty(alias) ? alias : column));
    }

    /**
     * 实现组内计数、排序后取前N项，展平为单行字符串；作用同等于 clickhouse udf: composite_top_limit
     *
     * @param column    需要复合处理的列项名
     * @param topLimit  前N个值
     * @param alias     重命名（默认转驼峰）
     * @return          CREATE FUNCTION composite_top_limit AS (column_name, n) ->
     *                  arrayStringConcat(arraySlice(arraySort(x -> (-(x.2)), arrayMap(x -> (x, arrayCount(y -> (y = x), groupArray(column_name))), arrayDistinct(groupArray(column_name)))), 1, n + 1), ', ')
     */
    private String udfCompositeTopLimit(String column, Integer topLimit, String alias){
        return String.format("composite_top_limit(%s, %d) AS %s",
                column,
                topLimit,
                StrUtil.toCamelCase(DataUtils.isNotEmpty(alias) ? alias : column));
    }

    @Override
    public Pagination<DenoiseEventDTO> queryDenoiseEventList(
            AntitamperFileProtectEventDenoiseStatisticClickhouseDO clickhouseDO,Integer current, Integer size) {
        Page<DenoiseEventDTO> page = PageHelper.startPage(current, size);
        List<DenoiseEventDTO> clickhouseDOS = this.mapper.queryList(clickhouseDO);
        Pagination<DenoiseEventDTO> pagination = new Pagination<>(current, size);
        pagination.setTotal(page.getTotal() < MAX_PAGE_TOTAL ? page.getTotal() : MAX_PAGE_TOTAL);
        pagination.setRecords(clickhouseDOS);
        return pagination;
    }

    @Override
    public AntitamperFileProtectEventDenoiseStatisticClickhouseDO queryDenoiseEventDetail(String uid) {
        return this.mapper.selectById(uid);
    }

    @Override
    public void updateDenoiseEvent(AntitamperFileProtectEventDenoiseStatisticClickhouseDO clickhouseDO) {
        this.mapper.updateById(clickhouseDO);
    }

    @Override
    public Integer countDenoiseLog(String tenantOrgCode, Long ruleId) {
        return this.mapper.queryAccCount(tenantOrgCode, ruleId);
    }
}
