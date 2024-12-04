@echo off
echo Running Maven clean install...
mvn clean install

echo Running Maven clean package...
mvn clean package

echo Build completed successfully.
pause
