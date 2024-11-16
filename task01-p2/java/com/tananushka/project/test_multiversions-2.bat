@echo off
setlocal enabledelayedexpansion
set JAVA_VERSIONS="C:\java\jdk-6" "C:\java\jdk-8" "C:\java\jdk-10" "C:\java\jdk-11"
set "PROJECT_ROOT=C:\...\Module02\task01-p2"
set "CLASS_OUTPUT=%PROJECT_ROOT%\out"
set "REPORT_FILE=%PROJECT_ROOT%\task-1-part-2-report.txt"

echo Module 01 Task 01 Part 2 Report > "%REPORT_FILE%"
echo ========================================== >> "%REPORT_FILE%"

if exist "%CLASS_OUTPUT%" rmdir /s /q "%CLASS_OUTPUT%"
mkdir "%CLASS_OUTPUT%"

for %%J in (%JAVA_VERSIONS%) do (
    set "JAVA_HOME=%%~J"
    set "PATH=!JAVA_HOME!\bin;%PATH%"
    
    echo.
    echo ========================================== 
    echo Testing with %%~J 
    echo Date: %date% Time: %time%
    echo ==========================================
    echo ========================================== >> "%REPORT_FILE%"
    echo Testing with %%~J >> "%REPORT_FILE%"
    echo Date: %date% Time: %time% >> "%REPORT_FILE%"
    
    echo Compiling with %%~J... 
    echo Compiling with %%~J... >> "%REPORT_FILE%"
    
    "!JAVA_HOME!\bin\javac" -d "%CLASS_OUTPUT%" "%PROJECT_ROOT%\java\com\tananushka\project\CustomMap.java" "%PROJECT_ROOT%\java\com\tananushka\project\ThreadSafeMap.java" "%PROJECT_ROOT%\java\com\tananushka\project\CustomMapConcurrencyTest.java" >> "%REPORT_FILE%" 2>&1
    
    echo ------------------------------------------ 
    echo ------------------------------------------ >> "%REPORT_FILE%"
    
    if exist "%CLASS_OUTPUT%\com\tananushka\project\CustomMapConcurrencyTest.class" (
        echo Running CustomMapConcurrencyTest with %%~J
        echo Running CustomMapConcurrencyTest with %%~J >> "%REPORT_FILE%"
        "!JAVA_HOME!\bin\java" -cp "%CLASS_OUTPUT%" com.tananushka.project.CustomMapConcurrencyTest > temp_output.txt
        type temp_output.txt
        type temp_output.txt >> "%REPORT_FILE%"
        del temp_output.txt
    ) else (
        echo Compilation failed for %%~J, skipping execution.
        echo Compilation failed for %%~J, skipping execution. >> "%REPORT_FILE%"
    )
)
pause