package com.sama.antitamper;

import com.alibaba.fastjson2.JSON;
import com.sama.antitamper.mapper.clickhouse.AntitamperFileProtectEventClickhouseMapper;
import com.sama.antitamper.service.AntitamperFileProtectEventClickhouseService;
import com.sama.antitamper.service.AntitamperFileProtectEventService;
import com.sama.antitamper.utils.data.bean.writeEs.WriteAntitamperBean;
import com.sama.api.antitamper.bean.AntitamperFileProtectEventClickhouseDO;
import com.sama.api.antitamper.bean.AntitamperFileProtectEventDO;
import jakarta.annotation.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.sama.antitamper.utils.MockDataUtils.generateRandomDateUsingPattern;

/**
 * @author: huxh
 * @description:
 * @datetime: 2025/1/10 10:00
 */
@SpringBootTest(classes = SamaAntitamperApplication.class)
public class AntitamperFileProtectEventClickhouseServiceTest {

    private final static Logger logger = LogManager.getLogger(AntitamperFileProtectEventClickhouseServiceTest.class);

    @Resource
    AntitamperFileProtectEventClickhouseMapper antitamperFileProtectEventClickhouseMapper;

    @Resource
    AntitamperFileProtectEventService antitamperFileProtectEventService;

    @Resource
    AntitamperFileProtectEventClickhouseService antitamperFileProtectEventClickhouseService;

    @Test
    public void clickhouseRunTest(){
        List<AntitamperFileProtectEventClickhouseDO> list = antitamperFileProtectEventClickhouseMapper.selectAll();
    }

    @Test
    public void mysqlRunTest() {
        AntitamperFileProtectEventDO eventDO = new AntitamperFileProtectEventDO();
        AntitamperFileProtectEventDO res = antitamperFileProtectEventService.add(eventDO);
        logger.info("mysql校验:{}", JSON.toJSONString(res));
    }

    private ArrayList<WriteAntitamperBean> mockFileProtectData(Integer size) {
        ArrayList<WriteAntitamperBean> dataList = new ArrayList<>();
        // 十天内
        long ago = 1_000 * 60 * 60 * 24 * 10;
        String pattern = "yyyy-MM-dd HH:mm:ss";

        for (int i = 0; i < size; i++) {
            WriteAntitamperBean writeAntitamperBean = new WriteAntitamperBean();
            // copy from es
            writeAntitamperBean.setAlarm_time(generateRandomDateUsingPattern(ago, pattern));
            writeAntitamperBean.setAsset_id("4682");
            writeAntitamperBean.setAsset_name("3--pureimage3232238435.novalocal");
            writeAntitamperBean.setBiz_domain("PUBLIC");
            // alarmmsg
            writeAntitamperBean.setDesc("转换来自desc");
            // filePath拆分
            writeAntitamperBean.setFile_path(filePathList.get(4));
            writeAntitamperBean.setHost_ip("192.168.11.99");
            writeAntitamperBean.setHost_name("3--pureimage3232238435.novalocal");
            writeAntitamperBean.setIntercept_status("1");
            writeAntitamperBean.setIpv4("192.168.11.99");
            writeAntitamperBean.setKafka_id(UUID.randomUUID().toString().replace("-", ""));
            writeAntitamperBean.setLevel_desc(3);
            // operationtype
            writeAntitamperBean.setOperation("转换来自operation");
            writeAntitamperBean.setOs_type(2);
            writeAntitamperBean.setPool_org_code("0224005400011001");
            writeAntitamperBean.setProcess("rm");
            writeAntitamperBean.setProtective_type("2");
            // TODO: dealstatus
            writeAntitamperBean.setResult("转换来自result");
            writeAntitamperBean.setSeverity(3);
            writeAntitamperBean.setTenant_org_code("02250044000A");
            writeAntitamperBean.setType("");
            // agentid
            writeAntitamperBean.setUid("转换来自uuid");
            dataList.add(writeAntitamperBean);

            // AntitamperFileProtectEventClickhouseDO targetDO  = new AntitamperFileProtectEventClickhouseDO();
            // targetDO.setAlarmMsg(writeAntitamperBean.getDesc());
            // targetDO.setOperationType(writeAntitamperBean.getOperation());
            // targetDO.setDealStatus(writeAntitamperBean.getResult());
            // targetDO.setAgentId(writeAntitamperBean.getUid());
        }
        return dataList;
    }

    private final List<String> filePathList = Arrays.asList("/home/cuangai/test2",
            "/home/Setup-Clt4Linux.tar.gz/桌面/edr.desktop",
            "C:\\fangcuangai\\New Contact (2).contact",
            "/home/cuangai/test2/newName1.txt -> /home/cuangai/test2/new2",
            "/home/cuangai/test2/234 -> /home/cuangai/test2/555");

    /**
     * 通过
     */
    @Test
    public void fileProtectEventClickhosueTest() {
        ArrayList<WriteAntitamperBean> dataList = mockFileProtectData(1);
        antitamperFileProtectEventClickhouseService.batchMigrate(dataList);
    }

    @Test
    public void testDubboGroupBy() throws ParseException {
        AntitamperFileProtectEventClickhouseDO antitamperFileProtectEventClickhouseDO = new AntitamperFileProtectEventClickhouseDO();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startTime = "2024-01-13 00:00:00";
        String endTime = "2025-02-27 23:59:59";
        antitamperFileProtectEventClickhouseDO.setLogStartTime(simpleDateFormat.parse(startTime));
        antitamperFileProtectEventClickhouseDO.setLogEndTime(simpleDateFormat.parse(endTime));
        antitamperFileProtectEventClickhouseDO.setGroupBy("tenant_org_code, asset_id, create_time");
        antitamperFileProtectEventClickhouseDO.setOrderBy("asset_id desc");
        List<String> list = new LinkedList<>();
        list.add("tenant_org_code");
        list.add("asset_id");
        list.add("create_time");
        System.out.println(antitamperFileProtectEventClickhouseService.selectGroupBy(antitamperFileProtectEventClickhouseDO, list).toString());
    }

}
