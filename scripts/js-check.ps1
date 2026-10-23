<#
.SYNOPSIS
    Checks the frontend JavaScript contract described in docs/guide-js.md.

.DESCRIPTION
    A grep gate, not a linter: it looks for the handful of shapes that already broke
    this project -- an inline expression inside a plain <script> that emits HTML
    escapes into a JS literal, a popup built by string concatenation, alert() as the
    error channel. Each one has a rule id, and the rule ids are the vocabulary
    docs/guide-js.md uses.

    Reports only by default, which is the right shape for a local run: it names what to
    fix and exits 0 either way. CI runs it with -Strict (the "Frontend JS contract" step
    of the backend job in .github/workflows/ci.yml), so a FAIL rule is a red build; what
    is left in the tree is INFO-only by design.

    INFO rules never fail, with or without -Strict. They mark structure to fix when
    the file is already open, not defects.

.EXAMPLE
    .\scripts\js-check.ps1
    .\scripts\js-check.ps1 -Strict
    .\scripts\gate.ps1 js
    .\scripts\gate.ps1 js -Strict
#>

param(
    [switch]$Strict
)

$ErrorActionPreference = 'Stop'

$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$templates = Join-Path $root 'src/main/webapp/WEB-INF/templates'
$jsRoot = Join-Path $root 'src/main/webapp/resources/core/js'

# Third-party bundles. Vendored verbatim, never held to this contract.
# Matched by name rather than by `*.min.js`, because vue.global.prod.js,
# tailwind-browser.js and leaflet.js are not minified by name (Leaflet is not
# minified at all) but are still not ours.
$vendored = @('vue.global.prod.js', 'tailwind-browser.js', 'lucide.min.js', 'leaflet.js')

# A script under this many lines stays inline; over it, it moves to /js/pages.
# 20 is the point where a template stops being a template with some behaviour and
# starts being a page whose JavaScript happens to live in HTML.
$inlineBudget = 20

$ruleInfo = [ordered]@{
    'JS01' = @{ Sev = 'FAIL'; Name = 'server expression inside a script without th:inline' }
    'JS02' = @{ Sev = 'FAIL'; Name = 'var instead of const/let' }
    'JS03' = @{ Sev = 'FAIL'; Name = 'bindPopup(string) -- HTML built from data' }
    'JS04' = @{ Sev = 'FAIL'; Name = 'alert() as the error channel' }
    'JS05' = @{ Sev = 'FAIL'; Name = 'innerHTML assigned a non-literal' }
    'JS06' = @{ Sev = 'INFO'; Name = 'inline script over budget' }
    # JS07 is FAIL because it is the one rule whose violation is invisible in a browser with
    # network: a remote asset renders fine here and dies in the demo room. The tree has zero
    # findings, so it costs nothing to enforce.
    'JS07' = @{ Sev = 'FAIL'; Name = 'runtime dependency from a remote origin' }
}

$findings = New-Object System.Collections.ArrayList

function Add-Finding {
    param($Id, $File, $Line, $Message)
    [void]$findings.Add([pscustomobject]@{
            Id      = $Id
            Sev     = $ruleInfo[$Id].Sev
            File    = $File
            Line    = $Line
            Message = $Message
        })
}

# The JS rules, applied to a single line of JavaScript, whether that line sits in a
# template's <script> or in a real .js file. $SkipExpressionInlining is false only for
# template scripts, where JS01 is meaningful.
function Test-JsLine {
    param($File, $Line, $Number, $SkipExpressionInlining)

    # Comments are skipped for the two rules a comment can plausibly mention, so
    # `// var used to live here` does not read as a violation. Strings are not
    # excluded: a rule that also has to parse quoting has stopped being a grep gate.
    $trimmed = $Line.TrimStart()
    $isComment = $trimmed.StartsWith('//') -or $trimmed.StartsWith('*') -or $trimmed.StartsWith('/*')
    if ($isComment) { return }

    # $SkipExpressionInlining is true when the enclosing <script> already declares
    # th:inline="javascript", which is the safe form. Every other th:inline value, and no
    # th:inline at all, means the expression is rendered as escaped HTML text.
    if (-not $SkipExpressionInlining) {
        # [[]] is HTML-escaped text inlining and [()] is unescaped: both put a server
        # value into a JS context that Thymeleaf is not escaping for. Inside a plain
        # <script> an `&` comes out as `&amp;`, and a newline inside the value ends the
        # string literal -- the page's script dies with a SyntaxError. [[@{}]] is the
        # same defect with a URL, so it belongs to the same rule.
        if ($Line -match '\[\[[\$@]\{|\[\([\$@]\{') {
            Add-Finding 'JS01' $File $Number 'use th:inline="javascript" or pass the value through data-*'
        }
    }

    if ($Line -match '\bvar\s') {
        Add-Finding 'JS02' $File $Number 'declare with const, or let when it is reassigned'
    }

    if ($Line -match '(?<![\w.$])alert\s*\(') {
        Add-Finding 'JS04' $File $Number 'set an error ref rendered with v-if; alert blocks and cannot be styled'
    }

    # A literal assignment is fine: `innerHTML = ''` clears, and a constant template
    # is reviewable. Anything else is data reaching the parser.
    if ($Line -match 'innerHTML\s*=\s*[^''"\s;)]') {
        Add-Finding 'JS05' $File $Number 'build DOM nodes instead of assigning markup'
    }
}

# Third-party assets fetched at runtime: a <script src> or a stylesheet <link> pointing at a
# remote origin. Matched as whole tags, not lines, because these tags are routinely split
# across lines -- `layouts/base.html` puts `rel="stylesheet"` and `href` on separate lines,
# which a line scan reads as two harmless lines and passes.
#
# Google Fonts is the one deliberate exception: brand.css declares a local fallback family,
# so an unreachable fonts.googleapis.com changes the typeface and nothing else, while an
# unreachable Leaflet or Bootstrap leaves a dead map or an unstyled page.
function Add-RemoteAssetFindings {
    param($File, $Raw)

    foreach ($tag in [regex]::Matches($Raw, '(?is)<script\b[^>]*>')) {
        if ($tag.Value -notmatch '\bsrc\s*=\s*[''"]https?://') { continue }
        $number = ($Raw.Substring(0, $tag.Index) -split "`n").Count
        Add-Finding 'JS07' $File $number 'remote <script src>; vendor it under /js'
    }

    foreach ($tag in [regex]::Matches($Raw, '(?is)<link\b[^>]*>')) {
        if ($tag.Value -notmatch 'stylesheet') { continue }
        if ($tag.Value -notmatch '\bhref\s*=\s*[''"]https?://') { continue }
        if ($tag.Value -match 'fonts\.googleapis\.com') { continue }
        $number = ($Raw.Substring(0, $tag.Index) -split "`n").Count
        Add-Finding 'JS07' $File $number 'remote stylesheet; vendor it under /css'
    }
}

# Leaflet renders a string popup as HTML, so a name interpolated into one is the injection
# sink; an HTMLElement built with textContent is not. Matched against the whole file rather
# than line by line, because the string usually opens on the line after `bindPopup(`: a
# per-line scan reads that as a bare quote and misses it, which is how three of the four
# call sites in this tree hid from the first version of this rule.
function Add-BindPopupFindings {
    param($File, $Raw)
    foreach ($match in [regex]::Matches($Raw, 'bindPopup\s*\(\s*[''"]')) {
        $number = ($Raw.Substring(0, $match.Index) -split "`n").Count
        Add-Finding 'JS03' $File $number 'pass an HTMLElement built with textContent, not a string'
    }
}

# ---- templates ---------------------------------------------------------------------
foreach ($file in (Get-ChildItem -LiteralPath $templates -Recurse -Filter '*.html' | Sort-Object FullName)) {
    $relative = ($file.FullName.Substring($root.Length + 1) -replace '\\', '/')
    $raw = Get-Content -LiteralPath $file.FullName -Raw
    Add-BindPopupFindings $relative $raw
    Add-RemoteAssetFindings $relative $raw
    $lines = @(Get-Content -LiteralPath $file.FullName)
    $inScript = $false
    $scriptStart = 0
    $scriptBody = 0
    $scriptHasInline = $false

    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i]
        $number = $i + 1

        if (-not $inScript) {
            # `(?![^>]*\bsrc=)` is what keeps a src script out: those are one-liners with
            # no body to scan, and reading them as a block start would swallow the page.
            if ($line -match '<script(?![^>]*\bsrc=)') {
                $inScript = $true
                $scriptStart = $number
                $scriptBody = 0
                $scriptHasInline = $line -match 'th:inline\s*=\s*[''"]\s*javascript'
                continue
            }
            continue
        }

        if ($line -match '</script>') {
            $inScript = $false
            if ($scriptBody -gt $inlineBudget) {
                Add-Finding 'JS06' $relative $scriptStart ("inline script is {0} lines; move it to /js/pages" -f $scriptBody)
            }
            continue
        }

        $scriptBody++
        Test-JsLine $relative $line $number $scriptHasInline
    }
}

# ---- project JavaScript -------------------------------------------------------------
# Recurse: `pages/` and `lib/` are where the contract actually lives now, and a rule that
# only reads the root would stop seeing it.
foreach ($file in (Get-ChildItem -LiteralPath $jsRoot -Recurse -Filter '*.js' | Sort-Object FullName)) {
    if ($vendored -contains $file.Name) { continue }
    $relative = ($file.FullName.Substring($root.Length + 1) -replace '\\', '/')
    $raw = Get-Content -LiteralPath $file.FullName -Raw
    Add-BindPopupFindings $relative $raw
    $lines = @(Get-Content -LiteralPath $file.FullName)
    for ($i = 0; $i -lt $lines.Count; $i++) {
        Test-JsLine $relative $lines[$i] ($i + 1) $true
    }
}

# ---- report ------------------------------------------------------------------------
if ($findings.Count -eq 0) {
    Write-Host 'js-check: clean. No contract violations.'
    exit 0
}

Write-Host ''
Write-Host 'js-check -- docs/guide-js.md'
foreach ($group in ($findings | Group-Object Id | Sort-Object Name)) {
    $info = $ruleInfo[$group.Name]
    $plural = if ($group.Count -eq 1) { 'finding' } else { 'findings' }
    Write-Host ''
    Write-Host ("  {0}  {1,-4}  {2}  ({3} {4})" -f $group.Name, $info.Sev, $info.Name, $group.Count, $plural)
    $group.Group | Sort-Object File, Line | ForEach-Object {
        Write-Host ("    {0}:{1}  {2}" -f $_.File, $_.Line, $_.Message)
    }
}

$fails = @($findings | Where-Object { $_.Sev -eq 'FAIL' })
$infos = @($findings | Where-Object { $_.Sev -eq 'INFO' })

Write-Host ''
Write-Host ("  {0} FAIL, {1} INFO" -f $fails.Count, $infos.Count)
if ($fails.Count -gt 0 -and -not $Strict) {
    Write-Host '  report-only: add -Strict to fail on FAIL rules.'
}
Write-Host ''

if ($Strict -and $fails.Count -gt 0) { exit 1 }
exit 0
