# Log Denoise Engine (日志降噪引擎)

本项目是一个针对海量安全告警日志的高性能降噪与聚合系统。它利用 ClickHouse 的强大分析能力，配合动态规则引擎，将原始的碎片化告警日志实时转化为高价值的统计概览，有效解决安全运营中的“告警风暴”问题。

## 🚀 核心痛点与解决方案
**痛点**：在主机防篡改场景下，单一攻击行为可能触发成百上千条重复或相似的告警（如递归删除文件），导致运维人员被淹没在无效信息中。
**方案**：
- **动态规则驱动**：支持通过 JSON 配置灵活定义过滤条件（Where）和分组策略（Group By）。
- **时间窗口聚合**：基于 Cron 表达式和滑动时间窗口，将离散事件归并为“降噪事件”。
- **高性能统计**：利用 ClickHouse 的向量化执行引擎和自定义 UDF，实现秒级亿量数据的聚合统计。

## 🛠 技术架构与亮点

### 1. 动态 SQL 生成引擎
核心服务 `AntitamperFileProtectEventDenoiseStatisticServiceImpl` 实现了一套灵活的规则解析器：
- **JSON 规则解析**：将前端配置的复杂 JSON 规则（支持嵌套 AND/OR 逻辑）动态转换为 MyBatis-Plus 的 `QueryWrapper`。
- **动态分组**：支持默认分组（租户维度）与自定义分组（如按 `file_path` 或 `attack_ip`）的自动拼接。

### 2. ClickHouse 深度集成
- **自定义 UDF 支持**：
    - `flatten_display`：实现组内数据的去重展平。
    - `composite_top_limit`：实现组内 Top N 统计（如统计同一攻击源下攻击次数最多的 Top 10 文件路径）。
- **高效存储**：采用 ClickHouse 作为存储后端，支持海量数据的快速写入与聚合查询。

### 3. 高并发与定时任务
- **多线程并行处理**：通过 `ThreadPoolTaskExecutor` 为每个降噪规则分配独立线程，互不阻塞。
- **智能时间对齐**：实现了 `backwardNearestTime` 算法，确保统计周期与 Cron 步长严格对齐，避免数据遗漏或重叠。

### 4. 完整的数据生命周期管理
- **自动归档**：统计结果自动落库至 MySQL/ClickHouse 结果表。
- **过期清理**：内置 `cleanupCron` 任务，自动维护 3 个月的数据保留周期。

## 🗂 项目结构
- `bean/`: 包含原始日志 DO (`AntitamperFileProtectEventClickhouseDO`) 与统计结果 DO (`DenoiseStatisticClickhouseDO`)。
- `service/impl/`: 核心降噪逻辑实现，包括动态 SQL 拼接、UDF 构造等。
- `mapper/`: MyBatis-Plus 接口，集成 ClickHouse 数据源。
- `utils/`: 日期处理与数据模型转换工具。

## 🔗 技术栈
- **Core**: Java 17, Spring Boot 3
- **ORM**: MyBatis Plus (Dynamic SQL)
- **Database**: ClickHouse (OLAP), MySQL (Meta)
- **Utils**: Fastjson2, Hutool, Guava