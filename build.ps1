# Usage (from the project folder, in PowerShell):
#   .\build.ps1          compile only
#   .\build.ps1 test     compile and run all JUnit 4 tests
#   .\build.ps1 run      compile and run the program (menu in the terminal)
#   .\build.ps1 driver   compile and run the model-only driver (no view)
param([string]$Task = "build")

$ErrorActionPreference = "Stop"
# This bundled jar includes JUnit 4.13.2 and its dependencies.
$junit = "lib/junit-platform-console-standalone-1.10.2.jar"

if (Test-Path out) { Remove-Item -Recurse -Force out }
New-Item -ItemType Directory out | Out-Null

$mainFiles = Get-ChildItem -Recurse src/main -Filter *.java | ForEach-Object { $_.FullName }
javac -d out/main $mainFiles
if ($LASTEXITCODE -ne 0) { exit 1 }

switch ($Task) {
    "test" {
        $testFiles = Get-ChildItem -Recurse src/test -Filter *.java | ForEach-Object { $_.FullName }
        javac -cp "out/main;$junit" -d out/test $testFiles
        if ($LASTEXITCODE -ne 0) { exit 1 }
        $testClasses = $testFiles | ForEach-Object {
            $source = Get-Content -LiteralPath $_ -Raw
            $package = [regex]::Match($source, '(?m)^package\s+([\w.]+);').Groups[1].Value
            $className = [IO.Path]::GetFileNameWithoutExtension($_)
            if ($package) { "$package.$className" } else { $className }
        }
        java -cp "out/main;out/test;$junit" org.junit.runner.JUnitCore $testClasses
        exit $LASTEXITCODE
    }
    "run"    { java -cp out/main eatz.controller.EatzApp }
    "driver" { java -cp out/main eatz.ModelDriver }
    default  { Write-Host "Compiled to out/main." }
}
