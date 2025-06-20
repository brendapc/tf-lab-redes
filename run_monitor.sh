#!/bin/bash

# Script para executar o Monitor de Tráfego de Rede
# Autor: Laboratório de Redes
# Data: 2024

echo "=========================================="
echo "  Monitor de Tráfego de Rede em Tempo Real"
echo "=========================================="
echo ""

# Verificar se está executando como root
if [ "$EUID" -ne 0 ]; then
    echo "ERRO: Este script deve ser executado como root (sudo)"
    echo "Execute: sudo $0"
    exit 1
fi

# Verificar se o Java está instalado
if ! command -v java &> /dev/null; then
    echo "ERRO: Java não está instalado"
    echo "Instale o Java 11 ou superior"
    exit 1
fi

# Verificar se o Maven está instalado
if ! command -v mvn &> /dev/null; then
    echo "ERRO: Maven não está instalado"
    echo "Instale o Maven 3.6 ou superior"
    exit 1
fi

# Verificar se a interface tun0 existe
if ! ip link show tun0 &> /dev/null; then
    echo "AVISO: Interface tun0 não encontrada"
    echo "Certifique-se de que o túnel de tráfego está executando"
    echo ""
    echo "Para iniciar o túnel:"
    echo "  cd traffic_tunnel/traffic_tunnel"
    echo "  make"
    echo "  sudo ./traffic_tunnel eth0 -s 192.168.1.1"
    echo ""
    read -p "Deseja continuar mesmo assim? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

echo "Compilando o projeto..."
mvn clean package

if [ $? -ne 0 ]; then
    echo "ERRO: Falha na compilação"
    exit 1
fi

echo ""
echo "Iniciando o monitor de tráfego..."
echo "Pressione Ctrl+C para parar"
echo ""

# Executar o monitor
java -jar target/network-monitor-1.0.0.jar

echo ""
echo "Monitor parado."
echo "Logs gerados:"
echo "  - camada2.csv (Camada de Enlace)"
echo "  - camada3.csv (Camada de Rede)"
echo "  - camada4.csv (Camada de Transporte)" 