@echo off
REM Script para executar o Monitor de Tráfego de Rede (Windows)
REM Autor: Laboratório de Redes
REM Data: 2024

echo ==========================================
echo   Monitor de Tráfego de Rede em Tempo Real
echo ==========================================
echo.

REM Verificar se o Java está instalado
java -version >nul 2>&1
if errorlevel 1 (
    echo ERRO: Java não está instalado
    echo Instale o Java 11 ou superior
    pause
    exit /b 1
)

REM Verificar se o Maven está instalado
mvn -version >nul 2>&1
if errorlevel 1 (
    echo ERRO: Maven não está instalado
    echo Instale o Maven 3.6 ou superior
    pause
    exit /b 1
)

echo Compilando o projeto...
mvn clean package

if errorlevel 1 (
    echo ERRO: Falha na compilação
    pause
    exit /b 1
)

echo.
echo Iniciando o monitor de tráfego...
echo Pressione Ctrl+C para parar
echo.

REM Executar o monitor
java -jar target/network-monitor-1.0.0.jar

echo.
echo Monitor parado.
echo Logs gerados:
echo   - camada2.csv (Camada de Enlace)
echo   - camada3.csv (Camada de Rede)
echo   - camada4.csv (Camada de Transporte)
pause 