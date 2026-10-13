<#
.SYNOPSIS
    One entry point for the quality gates: tests by layer, style checks, coverage.

.DESCRIPTION
    Wraps Maven so you ask for a gate instead of memorising lifecycle phases:

      .\scripts\gate.ps1 help                     every command, this text
      .\scripts\gate.ps1 list                     every class per layer, with counts
      .\scripts\gate.ps1 list UserServiceTest     the methods in one class
      .\scripts\gate.ps1 list=e2e                 one layer only
      .\scripts\gate.ps1 unit                     no Spring context, no Docker
      .\scripts\gate.ps1 unit=UserServiceTest     one class
      .\scripts\gate.ps1 unit=UserServiceTest#shouldCreateUser
      .\scripts\gate.ps1 integration              MockMvc tests, in-memory HSQLDB
      .\scripts\gate.ps1 integration=AuthControllerTest
      .\scripts\gate.ps1 e2e                      all E2E, with the stack brought up
      .\scripts\gate.ps1 e2e=LoginViewE2E -Headed one E2E class
      .\scripts\gate.ps1 all                      everything, one `mvn verify`
      .\scripts\gate.ps1 all=UserServiceTest+LoginViewE2E
      .\scripts\gate.ps1 reset-db                 throws the local database away and rebuilds it
      .\scripts\gate.ps1 check                    Checkstyle, PMD, CPD, Prettier
      .\scripts\gate.ps1 coverage                 runs the suite, prints the coverage matrix

    Bare `.\scripts\gate.ps1` prints this help rather than starting a build.

.EXAMPLE
    .\scripts\gate.ps1 unit -Fast
    .\scripts\gate.ps1 integration=SecurityConfigTest
    .\scripts\gate.ps1 e2e LoginViewE2E -Headed -SlowMo 300
    .\scripts\gate.ps1 all -Port 9090
    .\scripts\gate.ps1 check -Fix
    .\scripts\gate.ps1 list

.NOTES
    unit, integration and e2e are disjoint: `all` of them is the whole test suite.
    The split is by package, not by a new naming convention, so no test moves.

    Passing -Dtest to surefire replaces the <includes> in pom.xml, and with them the
    <excludes>**/e2e/**</excludes> that keeps E2E out of surefire. Every surefire gate
    here therefore re-states the e2e exclusion itself; forgetting it makes surefire run
    the E2E classes with no server and no database, which fails in ways that look like
    product bugs.

    `e2e` also states a -Dtest that matches nothing, because mvn verify always passes
    through surefire. `all` is the gate that runs the layers together.

    Arguments are parsed by hand rather than with a param block: PowerShell binds a bare
    positional like `unit` to the first positional parameter, which here takes a
    different type, and then fails to convert it.
#>

$ErrorActionPreference = 'Stop'

$usage = @'
gate.ps1 -- the quality gates, one command.

  .\scripts\gate.ps1 <command>[=<target>] [options]

Commands
  help                  this text (also -h, -?, --help)
  list                  the classes per layer, with test counts; list=unit|integration|e2e narrows it
  list <Class>           the methods in one class, fully qualified (both packages if the
                         name exists twice)
  unit                  every unit test              no Spring context, no Docker
  unit=<target>         one class, or Class#method
  integration           every MockMvc integration   in-memory HSQLDB, no Docker
  integration=<target>  one class, or Class#method
  e2e                   every E2E                   brings up PostgreSQL + Chromium;
  e2e=<target>          one class, or Class#method  unit and integration are skipped
  all                   unit + integration + e2e    one `mvn verify`, stack included
  all=<a>+<b>           a mix; + separates, e2e names go to failsafe, the rest to surefire
  check                 Checkstyle, PMD, CPD, Prettier. Changes nothing
  coverage              runs the whole suite, prints the coverage matrix
  reset-db              DESTROYS every local database and rebuilds it. See below

  No command means help. Nothing runs unless you name a layer.

Targets
  UserServiceTest                        a class
  UserServiceTest#shouldCreateUser       one method
  com.valhalla.integration.LoginControllerTest   fully qualified, when names collide
                                                (LoginControllerTest exists twice)

Options
  -Fast          skip Checkstyle/PMD/CPD/Prettier/JaCoCo (the -Pdev profile)
  -Fix           with `check`: let Prettier rewrite the files (check alone never does)
  -Headed        show the browser (e2e only)
  -SlowMo <ms>   per-action delay in the browser (e2e only)
  -Port <n>      moves Jetty and e2e.baseUrl together
  -Keep          leave PostgreSQL running afterwards

Examples
  .\scripts\gate.ps1 unit -Fast
  .\scripts\gate.ps1 unit=UserServiceTest
  .\scripts\gate.ps1 integration=AuthControllerTest#shouldRenderTheRegisterPage
  .\scripts\gate.ps1 e2e=LoginViewE2E -Headed -SlowMo 300
  .\scripts\gate.ps1 e2e -Keep
  .\scripts\gate.ps1 all -Port 9090
  .\scripts\gate.ps1 all=UserServiceTest+LoginViewE2E
  .\scripts\gate.ps1 check
  .\scripts\gate.ps1 check -Fix
  .\scripts\gate.ps1 coverage
  .\scripts\gate.ps1 list=e2e
  .\scripts\gate.ps1 reset-db

Coverage matrix
  unit, integration and all end with one: LINE, BRANCH and METHOD ratios per package,
  a TOTAL row, and the path to the html report. `coverage` prints the same table. A
  class or method target prints it too, scoped to what that run alone covered.

  It appears only when this run actually wrote the report. jacoco:report sits in the
  `test` phase after surefire, with no testFailureIgnore, so a failing run stops the
  build before the report is generated and the csv on disk is an older one; showing
  that would pass old numbers off as new. -Fast skips JaCoCo (jacoco.skip) and lands
  in the same place.

  `e2e` never prints one. The app under test runs in Jetty's JVM, which prepare-agent
  does not instrument, and surefire is starved with __NoUnitGate__, so an E2E run
  yields no coverage at all.

reset-db DESTROYS ALL LOCAL DATA
  JpaConfig sets hibernate.hbm2ddl.auto=update and there is no Flyway or Liquibase in the pom.
  `update` adds missing columns but never drops or renames one, so a renamed or removed column
  stays in the database: old rows keep it and land NULL in the new primary key, unreachable
  through the app, and the orphans pile up. The only local remedy is to throw the volume away
  and let Hibernate build the schema again -- which is what this command does. The data is
  expected to be lost; the stale schema is not.
'@

# Whole-file, not line-by-line, and anchored on `@Test` rather than with `@Test` optional:
# `@Test` sits on its own line above the declaration, so a per-line match also lists
# @BeforeEach helpers that no runner can execute. @Test\b keeps @ParameterizedTest and
# friends out.
#
# The gap between `@Test` and `void name(` is `[^;]*?`, and both of its ends are load
# bearing. `[^;{}]` looks safer and silently drops 27 of 382 methods, because Spring
# annotations carry braces: @WithMockUser(..., roles = { "ADMIN" }). A `;` is the honest
# stop, since it means the match has walked out of the declaration and into a statement.
$testMethod = '@Test\b[^;]*?\bvoid\s+([A-Za-z0-9_]+)\s*\('

# Passing -Dtest replaces surefire's <includes>, and with them <excludes>**/e2e/**</excludes>.
# Restate the e2e exclusion on every surefire gate, or surefire runs the E2E classes with
# no server and no database.
$surefireKeepOut = '!**/e2e/**'
$integrationPath = '**/integration/*Test'
$unitPatterns = "!**/e2e/**,!**/integration/**"
# A -Dtest value nothing can match. Combined with the surefire-prefixed
# failIfNoSpecifiedTests switch it runs zero surefire tests without failing, which is how
# `e2e` means E2E and not E2E-plus-the-other-layers. The `surefire.` prefix is the point:
# failsafe reads `failIfNoTests` from the pom, and a bare -DfailIfNoTests=false would
# disarm it, letting a bogus -Dit.test name report a green build over zero tests.
$surefireNone = '__NoUnitGate__'

$commands = 'help', 'list', 'unit', 'integration', 'e2e', 'all', 'check', 'coverage', 'reset-db'

# Resolve before listing: Get-ChildItem echoes back the path it was given, and the package
# is computed by cutting a fixed prefix off it, so the prefix has to be the same string
# on both sides.
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$srcTest = Join-Path $root 'src/test/java'
$qualityDir = Join-Path $root '.code-quality'
$jacocoCsv = Join-Path $qualityDir 'jacoco/jacoco.csv'
$jacocoHtml = Join-Path $qualityDir 'jacoco/index.html'

# $env: is process-wide, not scope-wide, and Set-Location is not scoped either: both would
# otherwise survive the script and land in the caller's session. That matters because
# `docker compose` prefers the shell environment over .env, so a leaked DB_HOST=localhost
# plus DB_NAME=valhalla_e2e makes the next `docker compose up` point the app container at
# itself and at the E2E database. Save both, restore both on the way out.
$originalLocation = $PWD
$savedEnv = @{}
foreach ($name in 'DB_NAME', 'DB_HOST', 'DB_USER', 'DB_PASSWORD') {
    $savedEnv[$name] = [Environment]::GetEnvironmentVariable($name)
}

$command = $null
$targets = @()
$fast = $false
$fix = $false
$headed = $false
$slowmo = 0
$port = 0
$keep = $false

function Stop-With($message) {
    Write-Host $message
    Set-Location $originalLocation
    exit 1
}

# One metric as a right-aligned percentage. JaCoCo reports zero branches for a class
# with none, and 0/0 has to read `n/a`: 100.0% would claim branch coverage that was
# never exercised, 0.0% would claim the opposite.
function Format-CoverageRatio([int]$covered, [int]$missed) {
    $total = $covered + $missed
    if ($total -le 0) { return 'n/a' }
    '{0:P1}' -f ($covered / $total)
}

# The csv mtime, or $null when there is no csv. Compared before and after a Maven run
# to tell whether the report on disk is this run's.
function Get-CoverageStamp {
    if (Test-Path $jacocoCsv) { (Get-Item $jacocoCsv).LastWriteTimeUtc } else { $null }
}

# True only when this run rewrote the report.
#
# The mtime, not the exit code, and not the phase order: jacoco:report and
# jacoco:check both sit in `test` after surefire with no testFailureIgnore, so a
# failing test stops the build before the report runs and leaves the previous csv
# behind, while a jacoco:check failure exits non-zero *after* a fresh report. The
# exit code therefore gets both directions wrong, whereas the file already knows. It
# also covers -Fast (jacoco.skip) without a second condition.
function Test-CoverageFresh([object]$before) {
    $after = Get-CoverageStamp
    return ($null -ne $after -and $after -ne $before)
}

# The one coverage table, at the end of every gate that just produced a fresh report.
# Three metrics because they answer three different questions: LINE is what
# jacoco:check enforces, BRANCH is the second half of that rule in domain, and METHOD
# counts classes entirely covered rather than merely touched. INSTRUCTION and
# COMPLEXITY are dropped as redundant with LINE.
#
# Never called after `e2e`: the app under test runs in Jetty's JVM, which
# prepare-agent does not instrument, and surefire is starved with __NoUnitGate__, so
# there is no honest matrix to show and no note that would teach the wrong lesson.
function Show-CoverageMatrix {
    if (-not (Test-Path $jacocoCsv)) {
        Write-Host "[coverage] no report at $jacocoCsv -- the run did not reach the report phase."
        return
    }

    # The csv's GROUP column is the pom's <name> ("spring web mvc"), not the
    # artifactId, so it cannot be used to scope the report. The package prefix is
    # what identifies the module, and taking it from <groupId> rather than hardcoding
    # it means a report left behind by a module rename cannot be averaged in silently.
    $groupId = (Select-String -Path pom.xml -Pattern '<groupId>([^<]+)<').Matches[0].Groups[1].Value
    $rows = @(Import-Csv $jacocoCsv | Where-Object { $_.PACKAGE -like "$groupId.*" })
    if (-not $rows) {
        Write-Host "[coverage] $jacocoCsv has no rows under $groupId."
        return
    }

    $metrics = 'LINE', 'BRANCH', 'METHOD'
    $sum = @{}
    foreach ($m in $metrics) { $sum[$m] = @{ Covered = 0; Missed = 0 } }

    Write-Host ''
    Write-Host 'coverage by package (this run)'
    Write-Host ('  {0,-46} {1,9} {2,9} {3,9}' -f 'package', 'LINE', 'BRANCH', 'METHOD')

    foreach ($group in ($rows | Group-Object PACKAGE | Sort-Object Name)) {
        $cell = @{}
        foreach ($m in $metrics) {
            $covered = ($group.Group | Measure-Object -Property "${m}_COVERED" -Sum).Sum
            $missed = ($group.Group | Measure-Object -Property "${m}_MISSED" -Sum).Sum
            $sum[$m].Covered += $covered
            $sum[$m].Missed += $missed
            $cell[$m] = Format-CoverageRatio $covered $missed
        }
        Write-Host ('  {0,-46} {1,9} {2,9} {3,9}' -f $group.Name, $cell['LINE'], $cell['BRANCH'], $cell['METHOD'])
    }

    Write-Host ('  {0,-46} {1,9} {2,9} {3,9}' -f 'TOTAL',
        (Format-CoverageRatio $sum['LINE'].Covered $sum['LINE'].Missed),
        (Format-CoverageRatio $sum['BRANCH'].Covered $sum['BRANCH'].Missed),
        (Format-CoverageRatio $sum['METHOD'].Covered $sum['METHOD'].Missed))
    Write-Host ''
    Write-Host "html report  $jacocoHtml"
}

# if/elseif rather than switch: switch keeps evaluating after a match and falls through to
# default, so a target ahead of a flag made the flag look unknown.
while ($args.Count -gt 0) {
    $arg = $args[0]
    if ($arg -match '^-(Headed)$') {
        $headed = $true
    }
    elseif ($arg -match '^-(Fast|f)$') {
        $fast = $true
    }
    elseif ($arg -match '^-(Fix)$') {
        $fix = $true
    }
    elseif ($arg -match '^-(Keep|k)$') {
        $keep = $true
    }
    elseif ($arg -match '^-(List|l)$') {
        # The old e2e.ps1 spelling. Kept because muscle memory is real and this is one line.
        $command = 'list'
    }
    elseif ($arg -match '^-(SlowMo|s)$') {
        # TryParse, not a bare [int] cast: a typo would otherwise surface as a cast
        # exception with a stack trace instead of naming the flag that ate it.
        if ($args.Count -lt 2 -or -not [int]::TryParse($args[1], [ref]$slowmo)) {
            throw "-SlowMo needs a number, got '$($args[1])'"
        }
        $args = @($args | Select-Object -Skip 1)
    }
    elseif ($arg -match '^-(Port|p)$') {
        if ($args.Count -lt 2 -or -not [int]::TryParse($args[1], [ref]$port)) {
            throw "-Port needs a number, got '$($args[1])'"
        }
        $args = @($args | Select-Object -Skip 1)
    }
    elseif ($arg -match '^-(h|\?|help|--help)$') {
        $command = 'help'
    }
    elseif ($arg.StartsWith('-')) {
        throw "unknown option: $arg`nRun '.\scripts\gate.ps1 help' for the list."
    }
    elseif ($null -eq $command) {
        $command = $arg
    }
    else {
        $targets += $arg
    }
    $args = @($args | Select-Object -Skip 1)
}

Set-Location $root

# ---- help ----------------------------------------------------------------------------
if ($null -eq $command) { $command = 'help' }

# `unit=UserServiceTest` and `list=e2e` are one token: split on the first `=`.
$target = ''
if ($command.Contains('=')) {
    $target = $command.Substring($command.IndexOf('=') + 1)
    $command = $command.Substring(0, $command.IndexOf('='))
}
# What `name=target` said on its own, before bare tokens are folded in. `list` needs the
# difference: for it the `=` half is the layer, and a bare token is a class name.
$named = $target

# PowerShell splits `all=A,B` into two bare arguments before this script sees it, so a
# stray token after a `command=target` is a second target, not a typo. $extra keeps the
# bare tokens: `list` reads one as a class name, where folding it into the target with a
# `+` would make the two impossible to tell apart.
$extra = @($targets)
if ($targets) { $target = (@($target) + $targets | Where-Object { $_ }) -join '+' }
$targets = @($target -split '[+]' | Where-Object { $_ })

if ($command -notin $commands) {
    Stop-With "unknown command: '$command'`nOne of: $($commands -join ', ')`nFull list: .\scripts\gate.ps1 help"
}

if ($command -eq 'help') {
    Write-Host $usage
    Set-Location $originalLocation
    exit 0
}

if ($fix -and $command -ne 'check') {
    Stop-With "-Fix only applies to 'check'."
}
# list is absent on purpose: `list=e2e` is a scope, and the block below validates it.
if ($targets.Count -gt 0 -and $command -in 'help', 'check', 'coverage', 'reset-db') {
    Stop-With "'$command' does not take a target. Targets apply to unit, integration, e2e and all."
}
if ($targets -match 'E2E' -and $command -in 'unit', 'integration') {
    Stop-With "'$($targets -join ',')' is an E2E class. Use 'e2e=$($targets -join ',')'."
}

# ---- list ----------------------------------------------------------------------------
if ($command -eq 'list') {
    # Two shapes on purpose. `list` answers "which class do I run", which is a question
    # about classes and their weight; every method name belongs to the next question,
    # "which method in it", and printing them all up front buries the answer to the first
    # one under a wall of names you cannot run.
    #
    # `list=unit LoginControllerTest` and `list unit LoginControllerTest` both read
    # naturally, so a leading layer name is accepted as a bare token too. The layer comes
    # from $named, not $target: $target has by now swallowed the class name as well.
    $layer = $named
    $rest = @($extra)
    if ($rest.Count -gt 0 -and $rest[0] -in 'unit', 'integration', 'e2e') {
        $layer = $rest[0]
        $rest = @($rest | Select-Object -Skip 1)
    }
    if ($layer -and $layer -notin 'unit', 'integration', 'e2e') {
        Stop-With "list takes unit, integration or e2e, got '$layer'."
    }
    if ($rest.Count -gt 1) {
        Stop-With "list takes one class name, got $($rest.Count): $($rest -join ', ')"
    }
    $filter = if ($rest) { $rest[0] } else { '' }

    $byLayer = @(
        @{ Layer = 'unit'; Pattern = '*Test.java'; Where = { $_.FullName -notmatch '[\\/](e2e|integration)[\\/]' } }
        @{ Layer = 'integration'; Pattern = '*Test.java'; Where = { $_.FullName -match '[\\/]integration[\\/]' } }
        @{ Layer = 'e2e'; Pattern = '*E2E.java'; Where = { $true } }
    )
    $runners = @{
        unit = '`mvn test`, in-memory HSQLDB, no Docker'
        integration = '`mvn test`, MockMvc + in-memory HSQLDB, no Docker'
        e2e = '`mvn verify`, plus PostgreSQL + Chromium'
    }

    # Package comes from the directory chain under src/test/java, so classes group the way
    # they sit in the tree. Qualified, because LoginControllerTest exists in two packages
    # and a bare target would silently run both.
    $entries = @(
        foreach ($l in $byLayer) {
            Get-ChildItem $srcTest -Recurse -Filter $l.Pattern |
                Where-Object $l.Where |
                Sort-Object FullName |
                ForEach-Object {
                    $raw = Get-Content $_.FullName -Raw

                    # `*Test.java` also matches this project's own annotations:
                    # @JpaIntegrationTest and @WebIntegrationTest end in Test, declare no
                    # tests, and without this they print a 0 that reads like an empty class.
                    if ($raw -match '(?m)^\s*(?:public\s+)?@interface\b') { return }

                    $rel = $_.FullName.Substring($srcTest.Length + 1)
                    $package = (((Split-Path $rel -Parent) -replace '\\', '/') -replace '/', '.')
                    [pscustomobject]@{
                        Layer = $l.Layer
                        Package = $package
                        Class = $_.BaseName
                        Qualified = if ($package) { "$package.$($_.BaseName)" } else { $_.BaseName }
                        Methods = @([regex]::Matches($raw, $testMethod) |
                                ForEach-Object { $_.Groups[1].Value })
                    }
                }
        }
    )

    if ($filter) {
        $hits = @($entries | Where-Object { $_.Class -ieq $filter -or $_.Qualified -ieq $filter })
        if (-not $hits) {
            Stop-With "no class named '$filter'. Run 'list' to see the $($entries.Count) that exist."
        }
        foreach ($hit in $hits) {
            Write-Host ''
            Write-Host ('{0}  [{1}, {2} tests]' -f $hit.Qualified, $hit.Layer, $hit.Methods.Count)
            $hit.Methods | ForEach-Object { Write-Host "  $_" }
        }
        # The whole point of matching loosely: a name that exists twice shows both, with
        # the package that tells them apart, instead of picking one for you.
        if ($hits.Count -gt 1) {
            Write-Host ''
            Write-Host 'That name is in more than one package. Copy the qualified one you want.'
        }
        Write-Host ''
        Write-Host "run   .\scripts\gate.ps1 $($hits[0].Layer)=$($hits[0].Class)#<method>"
        Set-Location $originalLocation
        exit 0
    }

    foreach ($l in $byLayer) {
        if ($layer -and $layer -ne $l.Layer) { continue }
        $mine = @($entries | Where-Object { $_.Layer -eq $l.Layer })
        $tests = ($mine | ForEach-Object { $_.Methods.Count } | Measure-Object -Sum).Sum

        Write-Host ''
        Write-Host ('{0}  {1,3} tests in {2} classes   {3}' -f $l.Layer, $tests, $mine.Count, $runners[$l.Layer])

        # Blank line between packages, so the eye finds the package boundary without
        # needing a rule to make the indent legible.
        $previous = ''
        foreach ($row in ($mine | Group-Object Package | Sort-Object Name)) {
            if ($previous) { Write-Host '' }
            $previous = $row.Name
            Write-Host "  $($row.Name)"
            $row.Group |
                Sort-Object Class |
                ForEach-Object { Write-Host ('    {0,-36} {1,4}' -f $_.Class, $_.Methods.Count) }
        }
    }

    Write-Host ''
    Write-Host 'methods  .\scripts\gate.ps1 list <Class>'
    Write-Host 'run one  .\scripts\gate.ps1 unit=<Class>#<method>'
    Write-Host '         .\scripts\gate.ps1 e2e=<Class>'
    Set-Location $originalLocation
    exit 0
}

# ---- check ---------------------------------------------------------------------------
if ($command -eq 'check') {
    # The four goals, invoked directly: -Fix needs `prettier:write`, which no phase binds
    # (the build only runs `prettier:check`), and a gate named `check` must not edit your
    # files unless asked. Direct invocation gets all four read-only; -Fix is the opt-in
    # that formats.
    $mvnArgs = @('checkstyle:check', 'pmd:check', 'pmd:cpd-check')
    $mvnArgs += if ($fix) { 'prettier:write' } else { 'prettier:check' }
    & mvn @mvnArgs
    $exitCode = $LASTEXITCODE
    Set-Location $originalLocation
    exit $exitCode
}

# ---- reset-db -------------------------------------------------------------------------
if ($command -eq 'reset-db') {
    if ($fast) {
        Write-Host "[reset-db] -Fast ignored: nothing here runs Maven."
    }

    # Said before anything is destroyed, not after: the point of this command is to throw data
    # away, and a user who ran it on the wrong database deserves to have read that first.
    Write-Host '[reset-db] DESTROYS ALL LOCAL DATA: every row in every local database is deleted.'
    Write-Host '[reset-db] Needed because JpaConfig sets hibernate.hbm2ddl.auto=update with no'
    Write-Host '[reset-db] migration tool: `update` adds columns but never drops or renames them,'
    Write-Host '[reset-db] so a renamed or removed column survives and old rows become unreachable.'
    Write-Host ''

    # -v, not `stop`: the schema lives in the volume, so stopping the container would keep the
    # very columns this command exists to remove. The maven_cache volume goes with it, which only
    # costs a re-download.
    Write-Host '[reset-db] docker compose down -v'
    docker compose down -v
    if ($LASTEXITCODE -ne 0) { Stop-With '[reset-db] docker compose down -v failed; nothing was rebuilt.' }

    Write-Host '[reset-db] starting an empty postgres'
    docker compose up -d postgres
    if ($LASTEXITCODE -ne 0) { Stop-With '[reset-db] docker compose up failed.' }

    # Same readiness probe the e2e gate uses, retried: a brand new container has not finished
    # initdb yet, and `up -d` returns the moment the container starts.
    $ready = $false
    foreach ($attempt in 1..20) {
        docker compose exec -T postgres psql -U user -d postgres -tAc 'SELECT 1' | Out-Null
        if ($LASTEXITCODE -eq 0) { $ready = $true; break }
        Start-Sleep -Seconds 1
    }
    if (-not $ready) { Stop-With '[reset-db] postgres never answered; is the image pulled?' }

    Write-Host '[reset-db] done. Hibernate rebuilds the schema on the next start.'
    Set-Location $originalLocation
    exit 0
}

# ---- coverage ------------------------------------------------------------------------
if ($command -eq 'coverage') {
    if ($fast) {
        Write-Host "[coverage] -Fast ignored: the dev profile skips JaCoCo, so there would be nothing to report."
    }

    # haltOnFailure=false: the pom's jacoco:check is bound to the test phase and would
    # fail the run on a sub-threshold suite, taking the report with it. Coverage is worth
    # looking at most when tests are failing or barely passing, so a below-threshold
    # suite must not cost you the numbers.
    #
    # It does not help when surefire itself fails: surefire runs earlier in the same
    # phase, so the build stops before jacoco:report ever executes. That is what the
    # freshness check below exists for -- it refuses to pass an older csv off as this
    # run's, which is what this command did before.
    $stamp = Get-CoverageStamp
    & mvn test '-Djacoco.haltOnFailure=false'
    $exitCode = $LASTEXITCODE

    if (Test-CoverageFresh $stamp) {
        Show-CoverageMatrix
        if ($exitCode -ne 0) { Write-Host "[coverage] the suite itself failed (exit $exitCode); the numbers above are partial." }
    }
    else {
        if (Test-Path $jacocoCsv) {
            Write-Host "[coverage] the suite stopped before jacoco:report, so $jacocoCsv still holds an"
            Write-Host "[coverage] older run and is not shown as this one's."
        }
        else {
            Write-Host "[coverage] no report at $jacocoCsv -- the run did not reach the report phase."
        }
        if ($exitCode -ne 0) { Write-Host "[coverage] the suite itself failed (exit $exitCode)." }
    }

    Set-Location $originalLocation
    exit $exitCode
}

# ---- unit / integration / e2e / all ---------------------------------------------------
$mvnArgs = @()
$needsStack = $false

switch ($command) {
    'unit' {
        $mvnArgs += 'test'
        # With no target, name the layers to skip rather than the ones to run: a positive
        # pattern would have to enumerate every package, and any new one would silently
        # drop out of `unit`.
        if ($targets) { $mvnArgs += "-Dtest=$($targets -join ',')" }
        else { $mvnArgs += "-Dtest=$unitPatterns" }
    }
    'integration' {
        $mvnArgs += 'test'
        if ($targets) {
            # Qualify with the package: AuthControllerTest and LoginControllerTest each
            # exist in integration/ as well as in presentation/, and a bare name runs both.
            $mvnArgs += "-Dtest=$(($targets | ForEach-Object { if ($_ -match '\.') { $_ } else { "**/integration/$_" } }) -join ',')"
        }
        else { $mvnArgs += "-Dtest=$integrationPath" }
    }
    'e2e' {
        $mvnArgs += 'verify'
        $needsStack = $true
        if ($targets) { $mvnArgs += "-Dit.test=$($targets -join ',')" }
        # surefire still runs inside verify. Starve it so `e2e` means E2E: a broken unit
        # test must not block a browser run, and iterating on one E2E class should not pay
        # for the rest of the suite.
        $mvnArgs += "-Dtest=$surefireNone"
        $mvnArgs += '-Dsurefire.failIfNoSpecifiedTests=false'
    }
    'all' {
        # One call, not `test` then `verify`: surefire already runs inside verify, and
        # splitting it would pay compile, Prettier, Checkstyle, PMD and JaCoCo twice for
        # the same result.
        $mvnArgs += 'verify'
        $needsStack = $true
        if ($targets) {
            # Mirror the pom's split: failsafe owns **/e2e/*E2E.java, surefire the rest.
            # Matching on the name keeps this a pure string pass, and no unit class in
            # this tree ends in E2E.
            $e2eTargets = @($targets | Where-Object { ($_ -split '#')[0].EndsWith('E2E') })
            $surefireTargets = @($targets | Where-Object { -not ($_ -split '#')[0].EndsWith('E2E') })
            if ($e2eTargets) { $mvnArgs += "-Dit.test=$($e2eTargets -join ',')" }
            if ($surefireTargets) {
                $mvnArgs += "-Dtest=$($surefireTargets -join ',')"
            }
            else {
                # Every name was an E2E, so `all=A+B` here is really an E2E-only run.
                $mvnArgs += "-Dtest=$surefireNone"
                $mvnArgs += '-Dsurefire.failIfNoSpecifiedTests=false'
            }
            if (-not $e2eTargets) {
                Stop-With "no usable target in '$($targets -join ',')'."
            }
        }
    }
}

# The coverage thresholds assume the whole suite, but jacoco:check sits in the `test`
# phase, which every surefire run passes through, so a run of one layer measured only
# that layer and failed the build with all of its own tests green (unit alone lands at
# 0.78 line against a 0.80 bundle floor). Only a bare `all` executes everything the
# rules assume, so only it keeps the gate; `e2e` measures nothing at all.
if ($command -ne 'all' -or $targets) { $mvnArgs += '-Djacoco.gate.skip=true' }

if ($fast) { $mvnArgs += '-Pdev' }
if ($headed) { $mvnArgs += '-De2e.headed=true' }
if ($slowmo -gt 0) { $mvnArgs += "-De2e.slowMo=$slowmo" }
# One property moves Jetty and Playwright together: failsafe derives e2e.baseUrl from
# jetty.port, so passing only the port here cannot desync the two.
if ($port -gt 0) { $mvnArgs += "-Djetty.port=$port" }

# unit and integration run on in-memory HSQLDB, so they stay a plain mvn call: no
# container, no env to mutate, nothing to tear down on Ctrl+C.
if (-not $needsStack) {
    $stamp = Get-CoverageStamp
    & mvn @mvnArgs
    $exitCode = $LASTEXITCODE
    if (Test-CoverageFresh $stamp) { Show-CoverageMatrix }
    else { Write-Host '[coverage] no report from this run, so there is no matrix to show.' }
    Set-Location $originalLocation
    exit $exitCode
}

$e2eDb = 'valhalla_e2e'

docker compose up -d postgres
if ($LASTEXITCODE -ne 0) { throw 'docker compose up failed' }

# From here on the container is up, so everything below runs inside try/finally: a failing
# mvn, a throw, or Ctrl+C all still shut the database down and restore the caller's
# environment. Captured before the stop, because the stop overwrites $LASTEXITCODE and
# that is the code the caller needs.
try {
    # createdb is not idempotent, so ask the catalog first. -d postgres is required:
    # without it psql connects to a database named after the user, which does not exist.
    $exists = docker compose exec -T postgres psql -U user -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='$e2eDb'"
    if ($LASTEXITCODE -ne 0) { throw 'could not query pg_database' }
    if (-not ($exists -join '').Trim()) {
        Write-Host "[e2e] creating database $e2eDb"
        docker compose exec -T postgres createdb -U user $e2eDb
    }

    # Install Chromium through the Maven-managed Playwright CLI. The browser version comes
    # from the resolved playwright dependency (the pom's), so a bump cannot drift out of
    # sync, and nothing is downloaded through npm: this repo has no package.json, and an
    # `npx playwright install` there pulls a throwaway package and prints a
    # "running without installing your project's dependencies" warning that reads like a
    # misconfiguration when it is just npx seeing a Java project.
    mvn -q exec:java "-Dexec.mainClass=com.microsoft.playwright.CLI" "-Dexec.args=install chromium"
    if ($LASTEXITCODE -ne 0) { throw 'playwright install failed' }

    # Jetty and failsafe must agree: Jetty needs the schema, ResetDatabase needs the
    # same data. The name must contain "e2e" or ResetDatabase refuses to run.
    $env:DB_NAME = $e2eDb
    $env:DB_HOST = 'localhost'
    $env:DB_USER = 'user'
    $env:DB_PASSWORD = 'user'

    $stamp = Get-CoverageStamp
    & mvn @mvnArgs
    $exitCode = $LASTEXITCODE
}
finally {
    if ($keep) {
        Write-Host "[e2e] leaving postgres up (-Keep); stop it with 'docker compose stop postgres'"
    }
    else {
        Write-Host '[e2e] stopping postgres'
        docker compose stop postgres
    }

    Set-Location $originalLocation
    foreach ($name in $savedEnv.Keys) {
        # Remove-Item, not [Environment]::SetEnvironmentVariable($name, $null): on this
        # runtime that defines the variable as an empty string instead of unsetting it,
        # and an empty DB_NAME still beats the .env file and breaks the next `up`.
        if ($null -eq $savedEnv[$name]) { Remove-Item "env:$name" -ErrorAction SilentlyContinue }
        else { [Environment]::SetEnvironmentVariable($name, $savedEnv[$name]) }
    }
}

# `all` ends with the matrix. `e2e` ends with nothing: its app ran inside Jetty's
# uninstrumented JVM and its surefire was starved with __NoUnitGate__, so a report
# there would be neither fresh nor about the tests it just ran. The reason is printed
# nowhere on purpose -- a note would invite reading the next stale csv as an E2E one.
if ($command -eq 'all') {
    if (Test-CoverageFresh $stamp) { Show-CoverageMatrix }
    else { Write-Host '[coverage] no report from this run, so there is no matrix to show.' }
}

exit $exitCode
