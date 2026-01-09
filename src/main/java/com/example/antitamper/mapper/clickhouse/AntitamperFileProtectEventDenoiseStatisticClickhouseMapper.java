package com.sama.antitamper.mapper.clickhouse;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.core4ct.base.nokey.NkBaseMapper;
import com.sama.api.antitamper.bean.AntitamperFileProtectEventDenoiseStatisticClickhouseDO;
import com.sama.api.antitamper.bean.DenoiseEventDTO;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * @author: huxh
 * @description:
 * @datetime: 2025/3/19 10:32
 */
@DS("clickhouse")
public interface AntitamperFileProtectEventDenoiseStatisticClickhouseMapper extends NkBaseMapper<AntitamperFileProtectEventDenoiseStatisticClickhouseDO> {

    void batchInsert(@Param("list") List<AntitamperFileProtectEventDenoiseStatisticClickhouseDO> list);

    int cleanup(@Param("anchor") Date anchor);

    List<DenoiseEventDTO> queryList(@Param("cm") AntitamperFileProtectEventDenoiseStatisticClickhouseDO clickhouseDO);

    /**
     * 查询日志累计次数
     *
     * @param tenantOrgCode 租户code
     * @param ruleId        规则id
     * @return 累计次数
     */
    Integer queryAccCount(@Param("tenantOrgCode") String tenantOrgCode, @Param("ruleId") Long ruleId);
}
