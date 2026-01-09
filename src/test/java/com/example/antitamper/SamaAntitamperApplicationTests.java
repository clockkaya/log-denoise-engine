package com.sama.antitamper;

import com.alibaba.fastjson2.JSON;
import com.clickhouse.data.value.UnsignedByte;
import com.core4ct.support.Pagination;
import com.sama.antitamper.constant.Constants;
import com.sama.antitamper.mapper.clickhouse.AntitamperFileProtectEventClickhouseMapper;
import com.sama.antitamper.mapper.mysql.AntitamperFilePolicyMapper;
import com.sama.antitamper.mapper.mysql.AntitamperFileProtectEventMapper;
import com.sama.antitamper.proxy.entity.LMFilePolicyRequest;
import com.sama.antitamper.proxy.service.FileProtectionPolicyProxyService;
import com.sama.antitamper.service.*;
import com.sama.antitamper.utils.DateUtil;
import com.sama.api.antitamper.bean.*;
import com.sama.api.antitamper.proxy.LMFilePolicy;
import com.sama.api.antitamper.service.*;
import com.sama.api.baseability.bean.VMwareDevice;
import com.sama.api.baseability.enums.AbilityInfoEnum;
import com.sama.api.baseability.service.PowerDubboService;
import com.sama.api.data.object.DTO.search.AntitamperAuditDTO;
import com.sama.api.data.object.DTO.search.DataBean;
import com.sama.api.data.service.AntitamperAuditSearchDubboService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest(classes = SamaAntitamperApplication.class)
class SamaAntitamperApplicationTests {

    @Resource
    AntitamperOverviewDubboService antitamperOverviewDubboService;

    @DubboReference
    PowerDubboService powerDubboService;

    @Resource
    FileProtectionPolicyProxyService fileProtectionPolicyProxyService;

    @Resource
    AntitamperPolicyDubboService antitamperPolicyDubboService;

    @Resource
    AntitamperAgentDubboService antitamperAgentDubboService;

    @Resource
    AntitamperScheduleDubboService antitamperScheduleDubboService;

    @Resource
    AntitamperFileProtectEventClickhouseService antitamperFileProtectEventClickhouseService;

    @Resource
    AntitamperFilePolicyMapper antitamperFilePolicyMapper;

    @Resource
    AntitamperExportDubboService antitamperExportDubboService;

    @DubboReference
    AntitamperAuditSearchDubboService antitamperAuditSearchDubboService;

    @Resource
    AntitamperFilePolicyService antitamperFilePolicyService;

    @Resource
    AntitamperTrustFileService antitamperTrustFileService;

    @Resource
    AntitamperAssetAgentService antitamperAssetAgentService;

    @Resource
    AntitamperFileProtectEventService antitamperFileProtectEventService;

    @Resource
    AntitamperBaseDubboService antitamperBaseDubboService;

    @Resource
    AntitamperFileProtectEventMapper antitamperFileProtectEventMapper;
    @Autowired
    private AntitamperFileProtectEventClickhouseMapper antitamperFileProtectEventClickhouseMapper;

    @Test
    void attackIpLogStatisticsTest() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date start = sdf.parse("2025-02-01 00:00:00");
        Date end = sdf.parse("2025-02-25 23:17:31");
        List<AttackIpNumTop> attackIpNumTops = antitamperOverviewDubboService.attackIpLogStatistics("02250031", 0, start, end);
        System.out.println("attackIpNumTops = " + attackIpNumTops);
    }

    @Test
    void queryInterceptCountTest() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date start = sdf.parse("2025-04-21 00:00:00");
        Date end = sdf.parse("2025-04-24 17:59:59");
        Map<Integer, Object> integerObjectMap = antitamperFileProtectEventClickhouseMapper.queryInterceptCount("02250031", start, end, null);
        System.out.println("integerObjectMap = " + integerObjectMap);
        System.out.println("Map class: " + integerObjectMap.getClass());
        System.out.println("Keys: " + integerObjectMap.keySet());
        // 检查键的实际类
//        integerObjectMap.keySet().forEach(key -> {
//            System.out.println(integerObjectMap.get(key));
//        });
        System.out.println(integerObjectMap.get(0));
        System.out.println(integerObjectMap.get(UnsignedByte.valueOf(Constants.BLOCKED_STATUS.byteValue())));

        HashMap hashMap = (HashMap) integerObjectMap.get(0);
        Object num = hashMap.get("num");
        System.out.println("num = " + num);
        System.out.println(integerObjectMap.get(3));
    }

    @Test
    void dateSplitTest() throws ParseException {
        // 2025-02-01 00:00:00  --  2025-02-18 11:15:12
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date start = sdf.parse("2025-02-18 00:00:00");
        Date end = sdf.parse("2025-02-18 15:50:26");
        List<String> timeXaxis = DateUtil.getTimeXaxis(1, start, end);
        System.out.println("timeXaxis = " + timeXaxis);
    }

    @Test
    void updateTenantForAntiTamperTest() {
        List<Long> assetIds = Arrays.asList(516L, 456L);
        String newOrgCode = "6666OrgCode";
        antitamperBaseDubboService.updateTenantForAntiTamper(assetIds, newOrgCode);
    }

    @Test
    void delTrustTest() {
        antitamperPolicyDubboService.delTrust(9L);
    }

    @Test
    void pagingQueryTest() {
        AntitamperAuditDTO antitamperAuditDTO = new AntitamperAuditDTO();
        antitamperAuditDTO.setTenant_org_code("200531010010");
        antitamperAuditDTO.setPage_num(1);
        antitamperAuditDTO.setPage_size(10000);
        DataBean dataBean = antitamperAuditSearchDubboService.pagingQuery(antitamperAuditDTO);
        System.out.println("dataBean = " + dataBean);
    }

    @Test
    void assetLogStatisticsTest() {
        List<AssetLogNumTop> assetLogNumTops = antitamperOverviewDubboService.assetLogStatistics("", 1, null, null, null);
        System.out.println("assetLogNumTops = " + assetLogNumTops);
        Map<Long, List<AssetLogNumTop>> collect = assetLogNumTops.stream().collect(Collectors.groupingBy(AssetLogNumTop::getAssetId));
        System.out.println("collect = " + collect);

    }

    @Test
    void getAssetExportData() {
        ProtectConfigShow protectConfigShow = new ProtectConfigShow();
        protectConfigShow.setTenantOrgCode("200531150029");
        List<AssetDetailExportDTO> assetExportData = antitamperExportDubboService.getAssetExportData(protectConfigShow);
        System.out.println("assetExportData = " + assetExportData);
    }

    @Test
    void comparePolicyTest() {
        AntitamperFilePolicyDO policyDO = new AntitamperFilePolicyDO();
        policyDO.setFilePath("/ch/test/aa.txt");
        policyDO.setAssetId(516L);
        policyDO.setAgentId("8d896f24-2226-42b0-aa1c-59c5258dd755");
//        policyDO.setTenantOrgCode("200531150029");
        policyDO.setDelFlag(0);
        List<AntitamperFilePolicyDO> antitamperFilePolicyDOS = antitamperFilePolicyMapper.comparePolicy(policyDO);
        System.out.println("antitamperFilePolicyDOS = " + antitamperFilePolicyDOS);
    }

    @Test
    void serverInfoTest() {
        antitamperScheduleDubboService.serverInfo();
    }

    /*======================= 底层测试 ==========================*/

    @Test
    void addPolicyOfLMTest() {
        VMwareDevice vMwareDevice = powerDubboService.getDeviceInfoWithAt(42346L, AbilityInfoEnum.ANTI_TAMPER);
        LMFilePolicyRequest lmFilePolicyRequest = new LMFilePolicyRequest();
        LMFilePolicy lmFilePolicy = new LMFilePolicy();
        String uuid = UUID.randomUUID().toString();
        lmFilePolicy.setFilePolicyId(uuid);
        lmFilePolicy.setFilePolicyName(uuid);
        lmFilePolicy.setAgentId("38084215-b44f-442b-ba7c-ce9a532c4406");
        lmFilePolicy.setFilePath("/home/test/");
        lmFilePolicy.setExceptPath(Collections.singletonList("/home/test/4454"));
        lmFilePolicy.setType(1);
        lmFilePolicy.setAction("link|write");
        lmFilePolicy.setEnable(0);
        lmFilePolicy.setTenantId("5555662c-b1f1-064d-c662-cb1f1364dc99");
        lmFilePolicyRequest.setData(lmFilePolicy);
        fileProtectionPolicyProxyService.addPolicyOfLM(lmFilePolicyRequest, vMwareDevice);
    }

    @Test
    void queryPolicy() throws Exception {
        AntitamperTrustDO antitamperTrustDO = new AntitamperTrustDO();
        antitamperTrustDO.setAssetId(460L);
        System.out.println(antitamperPolicyDubboService.queryTrust(antitamperTrustDO).getId());
    }

    @Test
    void addPolicy() throws Exception {
        AntitamperFilePolicyDO filePolicyDO = new AntitamperFilePolicyDO();
        filePolicyDO.setVmId(31939L);
        filePolicyDO.setFilePath("/test/");
        filePolicyDO.setAssetId(460L);
        filePolicyDO.setAgentId("ba3290e971b2e08f34d73eaf5754be19ecbbae1f8e435c69206b6a618fec705a");
        filePolicyDO.setAgentGroupId("GF824ecd");
        antitamperPolicyDubboService.addPolicy(111L, filePolicyDO);
    }

    @Test
    void editPolicyOfLMTest() {
        VMwareDevice vMwareDevice = powerDubboService.getDeviceInfoWithAt(42346L, AbilityInfoEnum.ANTI_TAMPER);
        LMFilePolicyRequest lmFilePolicyRequest = new LMFilePolicyRequest();
        LMFilePolicy lmFilePolicy = new LMFilePolicy();
        lmFilePolicy.setFilePolicyId("3c068088-1c79-4285-8ae2-d2ee262be23a");
        lmFilePolicy.setFilePolicyName("update-test");
        lmFilePolicy.setAgentId("38084215-b44f-442b-ba7c-ce9a532c4406");
        lmFilePolicy.setFilePath("/home/test/");
        lmFilePolicy.setExceptPath(Collections.singletonList("/home/test/4454"));
        lmFilePolicy.setType(1);
        lmFilePolicy.setAction("link|write");
        lmFilePolicy.setEnable(1);
        lmFilePolicy.setTenantId("5555662c-b1f1-064d-c662-cb1f1364dc99");
        lmFilePolicyRequest.setData(lmFilePolicy);
        fileProtectionPolicyProxyService.editPolicyOfLM(lmFilePolicyRequest, vMwareDevice);
    }

    @Test
    void delPolicyOfLMTest() {
        VMwareDevice vMwareDevice = powerDubboService.getDeviceInfoWithAt(42346L, AbilityInfoEnum.ANTI_TAMPER);
        LMFilePolicyRequest lmFilePolicyRequest = new LMFilePolicyRequest();
        LMFilePolicy lmFilePolicy = new LMFilePolicy();
        lmFilePolicy.setFilePolicyId("3c068088-1c79-4285-8ae2-d2ee262be23a");
        lmFilePolicy.setAgentId("38084215-b44f-442b-ba7c-ce9a532c4406");
        lmFilePolicy.setTenantId("5555662c-b1f1-064d-c662-cb1f1364dc99");
        lmFilePolicyRequest.setData(lmFilePolicy);
        fileProtectionPolicyProxyService.delPolicyOfLM(lmFilePolicyRequest, vMwareDevice);
    }

    @Test
    void queryPolicyOfLMTest() {
        VMwareDevice vMwareDevice = powerDubboService.getDeviceInfoWithAt(42346L, AbilityInfoEnum.ANTI_TAMPER);
        LMFilePolicyRequest lmFilePolicyRequest = new LMFilePolicyRequest();
        LMFilePolicy lmFilePolicy = new LMFilePolicy();
        lmFilePolicy.setAgentId("38084215-b44f-442b-ba7c-ce9a532c4406");
        lmFilePolicy.setTenantId("5555662c-b1f1-064d-c662-cb1f1364dc99");
        lmFilePolicyRequest.setData(lmFilePolicy);
        fileProtectionPolicyProxyService.queryPolicyOfLM(lmFilePolicyRequest, vMwareDevice);
    }

    /*======================= 概览测试 ==========================*/

    @Test
    void eventAssetPathShowTest() throws Exception {
        EventAssetPathNum eventAssetPathNum = antitamperOverviewDubboService.eventAssetPathShow("200531010010", true);
        System.out.println(eventAssetPathNum);
    }

    @Test
    void queryEventTrendTest() throws Exception {
        OverviewDTO overviewDTO = new OverviewDTO();
        AntitamperFileProtectEventClickhouseDO eventDO = new AntitamperFileProtectEventClickhouseDO();

//        overviewDTO.setTimeType(1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startTime = sdf.parse("2025-04-21 00:00:00");
        Date endTime = sdf.parse("2025-04-24 17:59:59");

        overviewDTO.setStartTime(startTime);
        overviewDTO.setEndTime(endTime);
        overviewDTO.setTimeType(4);
        BaseDataModel baseDataModel = antitamperOverviewDubboService.queryEventTrend("02250031", overviewDTO);
//        BaseDataModel baseDataModel = antitamperFileProtectEventClickhouseService.getEventTrend("02250031", 3, endTime, startTime);
        System.out.println("baseDataModel = " + JSON.toJSONString(baseDataModel));

//        eventDO.setStartTime(startTime);
//        eventDO.setEndTime(endTime);
//        eventDO.setTimeType(3);
//        eventDO.setTenantOrgCode("02250031");
//        List<AntitamperEventTrendDTO> tempEventTrend = antitamperFileProtectEventClickhouseMapper.queryEventTrend(eventDO);
//        Map<Integer, List<AntitamperEventTrendDTO>> typeEventTrendMap = tempEventTrend.stream()
//                .collect(Collectors.groupingBy(AntitamperEventTrendDTO::getType));
//        System.out.println("typeEventTrendMap = " + typeEventTrendMap);
//        System.out.println("Keys: " + typeEventTrendMap.keySet());
//        System.out.println("Values: " + typeEventTrendMap.get(1));
    }


    @Test
    void queryEventNumTest() throws Exception {
        OverviewDTO overviewDTO = new OverviewDTO();
//        overviewDTO.setTimeType(0);
//        overviewDTO.setTimeType(1);
        overviewDTO.setTimeType(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date start = sdf.parse("2025-02-01 00:00:00");
        Date end = sdf.parse("2025-02-06 23:59:59");
        overviewDTO.setStartTime(start);
        overviewDTO.setEndTime(end);
        AntitamperEventSum antitamperEventSum = antitamperOverviewDubboService.queryEventNum("02250031", overviewDTO);
        System.out.println(antitamperEventSum);
    }

    @Test
    void alarmLogPageTest() throws Exception {
        AlarmLogDisplay alarmLogDisplay = new AlarmLogDisplay();

        OverviewDTO overviewDTO = new OverviewDTO();
        overviewDTO.setTimeType(2);

        Pagination<AlarmLogDisplay> pagination = antitamperOverviewDubboService.alarmLogPage("02250031",
                alarmLogDisplay, overviewDTO, 1, 10, false);
        Long total = pagination.getTotal();
        System.out.println("总数：" + total);
        List<AlarmLogDisplay> records = pagination.getRecords();
        System.out.println("记录");
        for (AlarmLogDisplay record : records) {
            System.out.println(record);
        }
    }

    /*======================= 防护配置测试 ==========================*/

    @Test
    void queryBizDomainTest() throws Exception {
//        List<String> list = antitamperOverviewDubboService.queryBizDomain("215", false);
//        System.out.println(list);
    }

    @Test
    void protectConfigPageTest() throws Exception {
        ProtectConfigShow protectConfigShow = new ProtectConfigShow();
        protectConfigShow.setTenantOrgCode("200531150029");
        Pagination<ProtectConfigShow> pagination = antitamperPolicyDubboService.protectConfigPage(protectConfigShow, 1, 10);
        Long total = pagination.getTotal();
        System.out.println("总数：" + total);
        for (ProtectConfigShow record : pagination.getRecords()) {
            System.out.println(record);
        }
    }

    @Test
    void addPolicyTest() throws Exception {
        AntitamperFilePolicyDO policyDO = new AntitamperFilePolicyDO();
        policyDO.setTenantOrgCode("215");
        policyDO.setVmId(16599L);
        List<String> agentIdList = new ArrayList<>();
        agentIdList.add("78dcd661e337cd7f6861cfa6352ae3c294b751f043dc5f5e69b0bb5cb76769c3");
        policyDO.setAgentIdList(agentIdList);
        policyDO.setFilePath("/root/File_tamper/wer");
        policyDO.setExceptPath("/root/File_tamper/wer/qwe");
        policyDO.setType(2);
        policyDO.setAction("read|write|create");
//        antitamperOverviewDubboService.addPolicy(100L, policyDO);
    }

    @Test
    void editPolicyTest() throws Exception {
        AntitamperFilePolicyDO policyDO = new AntitamperFilePolicyDO();
        List<Long> ids = new ArrayList<>();
        ids.add(1L);
        policyDO.setIds(ids);
        policyDO.setFilePath("/root/File_tamper/");
        policyDO.setExceptPath("/root/File_tamper/IG/");
        policyDO.setType(2);
        policyDO.setAction("read|write");
//        antitamperOverviewDubboService.editPolicy(100L, policyDO);
    }

    @Test
    void delPolicyTest() throws Exception {
        List<Long> ids = new ArrayList<>();
        ids.add(2L);
//        antitamperOverviewDubboService.delPolicy(ids);
    }

    @Test
    void policyPageOfSingleTest() {
//        Pagination<AntitamperFilePolicyDO> pagination = antitamperOverviewDubboService.policyPageOfSingle("215", 380435L, 1, 10);
//        Long total = pagination.getTotal();
//        System.out.println("总数：" + total);
//        List<AntitamperFilePolicyDO> records = pagination.getRecords();
//        System.out.println("记录");
//        for (AntitamperFilePolicyDO record : records) {
//            System.out.println(record);
//        }
    }

    @Test
    void policyShowOfBatchTest() {
//        AntitamperFilePolicyDO policyDO = new AntitamperFilePolicyDO();
//        policyDO.setTenantOrgCode("215");
//        policyDO.setFilePath("/root/File_tamper/");
//        List<Long> assetIds = new ArrayList<>();
//        assetIds.add(380435L);
//        policyDO.setAssetIdList(assetIds);
//        AntitamperFilePolicyDO policyShowOfBatch = antitamperOverviewDubboService.policyShowOfBatch(policyDO);
//        System.out.println(policyShowOfBatch);
//        System.out.println(policyShowOfBatch.getIds());
    }


    /*======================= 日志中心测试 ==========================*/

    @Test
    void fileProtectEventPageTest() throws Exception {
        AntitamperFileProtectEventDO eventDO = new AntitamperFileProtectEventDO();
        eventDO.setTenantOrgCode("200531010010");
//        eventDO.setPoolId(5L);
////        eventDO.setLevelDesc(0);
//        Calendar calendar1 = Calendar.getInstance();
//        calendar1.set(2023,Calendar.AUGUST,1,23,59,59);
//        eventDO.setStartTime(calendar1.getTime());
//        Calendar calendar2 = Calendar.getInstance();
////        calendar2.set(2023,Calendar.AUGUST,23,59,59);
//        eventDO.setEndTime(calendar2.getTime());

//        Pagination<AntitamperFileProtectEventDO> pagination = antitamperOverviewDubboService.fileProtectEventPage(eventDO, 1, 10);
//        Long total = pagination.getTotal();
//        System.out.println("总数：" + total);
//
//        List<AntitamperFileProtectEventDO> records = pagination.getRecords();
//        System.out.println("记录");
//        for (AntitamperFileProtectEventDO record : records) {
//            System.out.println(record);
//        }
    }

    /*======================= 下载中心测试 ==========================*/

    @Test
    void generateCmdTest() throws Exception {
//        String cmd = antitamperOverviewDubboService.generateCmd("215", 16599L, 1);
//        System.out.println(cmd);
    }

    /*======================= 租户创建测试 ==========================*/

    @Test
    void addTenantIdTest() throws Exception {
        AntitamperTenantObjectDO tenantObjectDO = new AntitamperTenantObjectDO(42444L, "200531000000");
        antitamperAgentDubboService.addTenantId(tenantObjectDO);
    }
}
