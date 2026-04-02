@echo off
echo Starting Victus Store Application with GeoDB API...
echo.
echo Available endpoints:
echo - POST http://localhost:8080/api/geo-data/import/egypt
echo - GET http://localhost:8080/api/geo-data/import/status  
echo - GET http://localhost:8080/api/geo-data/test/us-regions
echo.
echo Starting Spring Boot application...
mvnw.cmd spring-boot:run
pause
