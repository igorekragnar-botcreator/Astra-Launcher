# Builds libbasalt_engine.so for Android on this Windows machine.
# Host linker comes from w64devkit (mingw-w64); Android linker from the NDK.
$ErrorActionPreference = "Stop"

$env:PATH = "D:\tools\w64devkit\bin;$env:USERPROFILE\.cargo\bin;$env:PATH"

$ndk = "C:\Users\user\AppData\Local\Android\Sdk\ndk\27.0.12077973\toolchains\llvm\prebuilt\windows-x86_64\bin"
$clang = Join-Path $ndk "clang.exe"
$ar = Join-Path $ndk "llvm-ar.exe"

foreach ($pair in @(
        @("aarch64_linux_android", "aarch64-linux-android26", "AARCH64_LINUX_ANDROID", "aarch64-linux-android"),
        @("x86_64_linux_android", "x86_64-linux-android26", "X86_64_LINUX_ANDROID", "x86_64-linux-android")
    )) {
    $envKey = $pair[0]
    $triple = $pair[1]
    $envUpper = $pair[2]
    $target = $pair[3]

    Set-Item -Path "Env:CC_$envKey" -Value $clang
    Set-Item -Path "Env:CFLAGS_$envKey" -Value "--target=$triple"
    Set-Item -Path "Env:CXX_$envKey" -Value $clang
    Set-Item -Path "Env:CXXFLAGS_$envKey" -Value "--target=$triple"
    Set-Item -Path "Env:AR_$envKey" -Value $ar
    Set-Item -Path "Env:CARGO_TARGET_${envUpper}_LINKER" -Value $clang
    Set-Item -Path "Env:CARGO_TARGET_${envUpper}_RUSTFLAGS" -Value "-C link-arg=--target=$triple"
}

Push-Location $PSScriptRoot
try {
    foreach ($target in @("aarch64-linux-android", "x86_64-linux-android")) {
        Write-Host "=== building $target"
        & cargo build --release --target $target
        if ($LASTEXITCODE -ne 0) { throw "cargo build failed for $target" }
    }
} finally {
    Pop-Location
}

Write-Host "=== done"
Get-ChildItem "target\aarch64-linux-android\release\libbasalt_engine.so",
              "target\x86_64-linux-android\release\libbasalt_engine.so" |
    ForEach-Object { "{0}  {1} KB" -f $_.FullName, [math]::Round($_.Length / 1KB) }
