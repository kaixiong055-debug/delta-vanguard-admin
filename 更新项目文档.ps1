$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)

$agentsPath = Join-Path $repoRoot "AGENTS.md"
$skillDir = Join-Path $repoRoot ".agents\skills\delta-java-backend"
$skillPath = Join-Path $skillDir "SKILL.md"
$docPath = Join-Path $repoRoot "接口文档.md"
$headerPath = Join-Path $repoRoot "接口文档_新顶部.md"

if (-not (Test-Path $docPath)) {
    throw "未找到接口文档.md。请把整个更新包解压到 delta-vanguard-admin 项目根目录后再运行。"
}

if (-not (Test-Path $headerPath)) {
    throw "未找到接口文档_新顶部.md。请确认更新包已完整解压。"
}

New-Item -ItemType Directory -Force -Path $skillDir | Out-Null

# AGENTS.md 和 SKILL.md 已随压缩包提供；这里统一转为 UTF-8 无 BOM。
$agentsContent = [System.IO.File]::ReadAllText((Join-Path $repoRoot "AGENTS.md"))
[System.IO.File]::WriteAllText($agentsPath, $agentsContent, $utf8NoBom)

$skillContent = [System.IO.File]::ReadAllText((Join-Path $repoRoot ".agents\skills\delta-java-backend\SKILL.md"))
[System.IO.File]::WriteAllText($skillPath, $skillContent, $utf8NoBom)

# 仅替换接口文档顶部，保留“## 3. 服务订单状态枚举”及后面的全部原内容。
$oldDoc = [System.IO.File]::ReadAllText($docPath)
$marker = "## 3. 服务订单状态枚举"
$markerIndex = $oldDoc.IndexOf($marker)

if ($markerIndex -lt 0) {
    throw "接口文档.md 中未找到标记：$marker。为避免误删内容，脚本已停止。"
}

$header = [System.IO.File]::ReadAllText($headerPath).TrimEnd()
$remaining = $oldDoc.Substring($markerIndex)
$newDoc = $header + "`r`n`r`n" + $remaining

$backupPath = Join-Path $repoRoot ("接口文档.md.backup-" + (Get-Date -Format "yyyyMMdd-HHmmss"))
[System.IO.File]::WriteAllText($backupPath, $oldDoc, $utf8NoBom)
[System.IO.File]::WriteAllText($docPath, $newDoc, $utf8NoBom)

Write-Host ""
Write-Host "更新完成：" -ForegroundColor Green
Write-Host "1. AGENTS.md"
Write-Host "2. .agents/skills/delta-java-backend/SKILL.md"
Write-Host "3. 接口文档.md 顶部"
Write-Host ""
Write-Host "原接口文档备份：" -ForegroundColor Yellow
Write-Host $backupPath
Write-Host ""
Write-Host "建议检查：" -ForegroundColor Cyan
Write-Host "git diff --check"
Write-Host "git status --short"
Write-Host "git diff --name-only"
