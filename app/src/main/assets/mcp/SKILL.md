# KeiOS MCP Skill

## 服务身份
- App: {{APP_LABEL}} ({{APP_PACKAGE}})
- 版本: {{APP_VERSION}}
- MCP 服务名: {{SERVER_NAME}}
- 本机 endpoint: {{LOCAL_ENDPOINT}}
- 局域网 endpoint: {{LAN_ENDPOINTS}}

## 快速接入
1. 先读取资源 `{{RESOURCE_CONFIG_URI}}`，拿到默认可导入 JSON（auto）。
2. 需要指定模式时读取模板 `{{RESOURCE_CONFIG_TEMPLATE_URI}}`，填入 `mode=auto|local|lan`。
3. 客户端不支持资源读取时，调用 `keios.mcp.runtime.config` 生成同等配置。
4. 接入完成后执行 `keios.health.ping` 与 `keios.mcp.runtime.status` 验证连通性。

## 初始化顺序
1. 读取资源 `{{RESOURCE_OVERVIEW_URI}}` 获取入口摘要。
2. 调用 Prompt `{{PROMPT_BOOTSTRAP}}` 生成当前任务计划。
3. 按任务读取资源 `{{RESOURCE_SKILL_URI}}` 与工具模板 `keios://skill/tool/{tool}`。
4. 执行工具前先确认参数范围，再落地调用。

## 工具能力总览
{{TOOL_LIST}}

## 推荐任务流
- 运行排障
  - `keios.health.ping`
  - `keios.mcp.runtime.status`
  - `keios.mcp.runtime.logs`
  - `keios.shizuku.status`
- OS 页面巡检
  - `keios.os.cards.snapshot`
  - `keios.os.activity.cards`
  - `keios.os.shell.cards`
  - `keios.system.topinfo.query`
- GitHub 跟踪巡检
  - `keios.github.tracked.snapshot`
  - `keios.github.tracked.check`
  - `keios.github.tracked.summary`
  - `keios.github.tracked.list`
- BA 缓存巡检
  - `keios.ba.snapshot`
  - `keios.ba.calendar.cache`
  - `keios.ba.pool.cache`
  - `keios.ba.guide.catalog.cache`
  - `keios.ba.guide.cache.inspect`
- 缓存清理
  - `keios.github.tracked.cache.clear`
  - `keios.ba.cache.clear`

## 参数建议
- 所有 `limit` 参数建议先用 20~80，确认结构后再放大。
- `keios.github.tracked.check` 建议先用 `onlyUpdates=true` 快速筛选。
- `keios.os.shell.cards` 在排障时开启 `includeOutput=true`，常规巡检保持关闭。
- `keios.ba.cache.clear` 用 `scope` 精确清理，避免全量重建缓存。

## 输出约定
- 工具输出以 `key=value` 为主，适合检索与日志归档。
- 列表工具输出含摘要头和条目行，条目行格式固定，便于二次解析。
- 网络或缓存异常会返回显式错误信息，调用端应保留原文。

## 导入模式建议
- `auto`: 同时给出 Local/LAN 入口，适合多环境调试。
- `local`: 只保留 127.0.0.1 入口，适合同机客户端。
- `lan`: 优先局域网入口，适合跨设备联调。
