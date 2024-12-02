@echo off
set MAIN_CLASS=pylos-ml.src.main.java.be.kuleuven.pylos.PylosMLReinforcementTrainer

:: Create MANIFEST.MF with Main-Class defined
echo Manifest-Version: 1.0 > MANIFEST.MF
echo Main-Class: %MAIN_CLASS% >> MANIFEST.MF
echo. >> MANIFEST.MF

echo Searching for .class files...
dir /b /s *.class > filelist.txt

echo Creating the JAR file...
jar cmf MANIFEST.MF player_ai.jar @filelist.txt

:: Clean up
del filelist.txt
del MANIFEST.MF

echo JAR file created successfully.
pause