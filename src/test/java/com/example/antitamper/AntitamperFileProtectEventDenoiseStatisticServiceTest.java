package com.sama.antitamper;

import com.alibaba.fastjson2.JSON;
import com.core4ct.utils.DataUtils;
import com.core4ct.utils.DateUtils;
import com.sama.antitamper.mapper.clickhouse.AntitamperFileProtectEventClickhouseMapper;
import com.sama.antitamper.service.AntitamperFileProtectEventDenoiseStatisticClickhouseService;
import com.sama.api.antitamper.bean.AntiTamperDenoiseRuleDO;
import com.sama.api.antitamper.bean.AntitamperFileProtectEventDenoiseStatisticClickhouseDO;
import com.sama.api.antitamper.bean.RiskLevelNum;
import com.sama.api.antitamper.bean.WhereConditionBO;
import jakarta.annotation.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.core4ct.utils.DateUtils.DatePattern.YYYY_MM_DD_HH_MM_SS;

/**
 * @author: huxh
 * @description:
 * @datetime: 2025/3/19 16:17
 */
@SpringBootTest(classes = SamaAntitamperApplication.class)
public class AntitamperFileProtectEventDenoiseStatisticServiceTest {

    private final static Logger logger = LogManager.getLogger(AntitamperFileProtectEventDenoiseStatisticServiceTest.class);

    @Resource
    AntitamperFileProtectEventDenoiseStatisticClickhouseService antitamperFileProtectEventDenoiseStatisticService;

    @Resource
    AntitamperFileProtectEventClickhouseMapper antitamperFileProtectEventClickhouseMapper;

    /**
     * 通过：正确的 ForkDenoiseRuleDO
     */
    @Test
    public void successSenarioTest(){
        AntiTamperDenoiseRuleDO ruleDO = new AntiTamperDenoiseRuleDO();
        ruleDO.setId(0L);
        ruleDO.setGroupCondition("[\"file_path\", \"level_desc\"]");
        // forkDenoiseRuleDO.setDenoiseEventLevel(3);

        // 链式 where
        WhereConditionBO.OrCondition.AndCondition andCondition1 = new WhereConditionBO.OrCondition.AndCondition();
        andCondition1.setField("level_desc");
        andCondition1.setOperator("eq");
        andCondition1.setValue("2");
        WhereConditionBO.OrCondition.AndCondition andCondition2 = new WhereConditionBO.OrCondition.AndCondition();
        andCondition2.setField("type");
        andCondition2.setOperator("eq");
        andCondition2.setValue("1");
        // 块级 where
        WhereConditionBO.OrCondition orConditionA = new WhereConditionBO.OrCondition();
        orConditionA.setAndConditions(Arrays.asList(andCondition1, andCondition2));

        // 链式 where
        WhereConditionBO.OrCondition.AndCondition andCondition3 = new WhereConditionBO.OrCondition.AndCondition();
        andCondition3.setField("file_path");
        andCondition3.setOperator("like");
        andCondition3.setValue("%test2%");
        // 块级 where
        WhereConditionBO.OrCondition orConditionB = new WhereConditionBO.OrCondition();
        orConditionB.setAndConditions(Arrays.asList(andCondition3));

        // 组合所有 where
        WhereConditionBO whereConditionBOs = new WhereConditionBO();
        whereConditionBOs.setOrConditions(Arrays.asList(orConditionA, orConditionB));
        ruleDO.setWhereCondition(JSON.toJSONString(whereConditionBOs));
        logger.info("whereConditionBOs: {}", JSON.toJSONString(whereConditionBOs));

        mockAfterGettingRule(ruleDO);
    }

    /**
     * 通过：错误的 ForkDenoiseRuleDO
     */
    @Test
    public void failureSenarioTest(){
        AntiTamperDenoiseRuleDO ruleDO = new AntiTamperDenoiseRuleDO();
        ruleDO.setId(1L);

        // 链式 where
        WhereConditionBO.OrCondition.AndCondition wrongCondition = new WhereConditionBO.OrCondition.AndCondition();
        wrongCondition.setField("level_desc");
        // 缺少 wrongCondition.setOperator("eq");
        wrongCondition.setValue("2");
        // 块级 where
        WhereConditionBO.OrCondition orCondition = new WhereConditionBO.OrCondition();
        orCondition.setAndConditions(Arrays.asList(wrongCondition));
        // 组合所有 where
        WhereConditionBO whereConditionBOs = new WhereConditionBO();
        whereConditionBOs.setOrConditions(Arrays.asList(orCondition));
        ruleDO.setWhereCondition(JSON.toJSONString(whereConditionBOs));
        logger.info("whereConditionBOs: {}", JSON.toJSONString(whereConditionBOs));

        mockAfterGettingRule(ruleDO);
    }

    /**
     * 模拟已经从数据库中取出 forkDenoiseRuleDO 后，执行 completeDynamicSqlAndQuery() 方法和 batchInsert() 方法
     */
    private void mockAfterGettingRule(AntiTamperDenoiseRuleDO ruleDO){
        Date standingTime = new Date();
        Date currentDot = backwardNearestTime(standingTime, 5);
        Date lastDot = DateUtils.addDate(currentDot, Calendar.MONTH, -2);
        List<AntitamperFileProtectEventDenoiseStatisticClickhouseDO> res =
                antitamperFileProtectEventDenoiseStatisticService.completeDynamicSqlAndQuery(ruleDO, lastDot, currentDot, standingTime);
        logger.info("【降噪Test】 在统计周期 [{}, {}) 内查询结果：\n{}",
                DateUtils.format(lastDot, YYYY_MM_DD_HH_MM_SS), DateUtils.format(currentDot, YYYY_MM_DD_HH_MM_SS), JSON.toJSONString((res)));

        if (DataUtils.isNotEmpty(res)){
            antitamperFileProtectEventDenoiseStatisticService.batchInsert(res);
            logger.info("【降噪Test】 写入成功！");
        }
    }

    /**
     * copy
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
     * pass
     */
    @Test
    public void cornTest() throws InterruptedException {
        antitamperFileProtectEventDenoiseStatisticService.persistentDenoiseStatisticsCron(10);
        Thread.sleep(60_000);
    }

    @Test
    public void testMapper() {
        Date date1 = stringToDate("2023-11-24 00:00:00");
        Date date2 = stringToDate("2025-11-24 00:00:00");
        List<RiskLevelNum> riskLevelNums = antitamperFileProtectEventClickhouseMapper.queryRiskLevelNum("", date1, date2);
        logger.info(riskLevelNums.toString());
    }

    public static Date stringToDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return sdf.parse(dateStr);
        } catch ( ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

}
