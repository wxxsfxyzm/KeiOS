# KeiOS MCP Skill (For Claw)

## 1. 服务身份
- App: {{APP_LABEL}} ({{APP_PACKAGE}})
- 版本: {{APP_VERSION}}
- MCP 服务名: {{SERVER_NAME}}
- 本地 endpoint: {{LOCAL_ENDPOINT}}
- 局域网 endpoint: {{LAN_ENDPOINTS}}

## 2. Claw 导入方案（推荐 MCP 原生）
1. 读取资源 `{{RESOURCE_IMPORT_URI}}` 获取默认导入 JSON（auto）。
2. 如需指定模式，读取模板资源 `{{RESOURCE_IMPORT_TEMPLATE_URI}}`（`mode=auto|local|lan`）。
3. 如果客户端不支持读取资源，则调用工具 `keios.mcp.get_claw_import_package` 获取同等导入包。
4. 完成导入后执行 `keios.health.ping` 与 `keios.mcp.get_status` 验证连通性。

## 3. 启动与执行顺序
1. 读取资源 `{{RESOURCE_SKILL_URI}}` 获取完整技能说明。
2. 调用 Prompt `{{PROMPT_BOOTSTRAP}}` 生成当前任务执行计划。
3. 再按任务调用对应工具（见下文）。

## 4. 工具能力总览
{{TOOL_LIST}}

## 5. 典型任务流
- MCP 连接排查：`keios.mcp.get_status` -> `keios.mcp.get_logs` -> `keios.mcp.get_claw_import_package`
- 系统参数检索：`keios.system.topinfo.list` -> `keios.system.topinfo.search`
- GitHub 更新检查：`keios.github.tracked.list` -> `keios.github.tracked.check_updates` -> `keios.github.tracked.summary`

## 6. 使用建议
- 同机客户端优先使用 `local`，跨设备再使用 `lan`。
- 若连接异常，先检查 `Authorization` Bearer token 是否与导入包一致。
- 日志来自应用内缓存，适合排障与巡检，不等价于系统全量日志。
