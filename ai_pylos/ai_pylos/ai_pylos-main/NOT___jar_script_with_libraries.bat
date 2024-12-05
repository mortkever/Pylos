@echo off
set MAIN_CLASS=be.kuleuven.pylos.PylosMLReinforcementTrainer
set LIB_DIR=libs
set OUTPUT_JAR=player_ai.jar

:: Create MANIFEST.MF
echo Creating MANIFEST.MF...
(
    echo Manifest-Version: 1.0
    echo Main-Class: %MAIN_CLASS%
) > MANIFEST.MF

:: Search for .class files
echo Searching for .class files...
dir /b /s *.class > filelist.txt

:: Add all .class files to the JAR
echo Creating the JAR file...
jar cmf MANIFEST.MF %OUTPUT_JAR% @filelist.txt

:: Add all library JARs into the JAR file
if exist "%LIB_DIR%" (
    for %%i in ("%LIB_DIR%\*.jar") do (
        echo Adding library: %%~fi
        jar uf %OUTPUT_JAR% -C %%~dpi %%~nxi
    )
) else (
    echo Library directory "%LIB_DIR%" not found. Skipping library inclusion.
)

:: Clean up
echo Cleaning up temporary files...
del filelist.txt
del MANIFEST.MF

echo Fat JAR created successfully: %OUTPUT_JAR%
pause
