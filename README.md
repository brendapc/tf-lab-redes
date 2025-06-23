# Monitor de Tráfego de Rede em Tempo Real

## Descrição

Este projeto implementa uma ferramenta de monitoramento de tráfego de rede em tempo real que captura pacotes na interface `tun0` usando raw sockets. A ferramenta é desenvolvida em Java e utiliza a biblioteca Pcap4J para captura de pacotes.

## Funcionalidades

- **Captura de Pacotes**: Captura pacotes em tempo real na interface `tun0` usando raw sockets
- **Análise de Protocolos**: Identifica e classifica protocolos das camadas 2, 3 e 4 (Ethernet, IP, TCP, UDP, ICMP, ARP)
- **Estatísticas em Tempo Real**: Exibe contadores e estatísticas de tráfego em interface textual
- **Logs CSV**: Gera arquivos CSV separados para cada camada do modelo TCP/IP:
  - `camada2.csv`: Informações da camada de enlace (MAC, EtherType, tamanho)
  - `camada3.csv`: Informações da camada de rede (IP, protocolo, tamanho)
  - `camada4.csv`: Informações da camada de transporte (portas, protocolo)

## Arquitetura

O projeto é composto pelas seguintes classes principais:

- **NetworkMonitor**: Classe principal que coordena a captura e análise
- **PacketAnalyzer**: Analisa pacotes e extrai informações das camadas 2, 3 e 4
- **StatisticsDisplay**: Exibe estatísticas em tempo real
- **CsvLogger**: Gerencia a escrita dos logs CSV
- **PacketInfo**: Armazena informações extraídas dos pacotes

## Pré-requisitos

- Java 11 ou superior
- Maven 3.6 ou superior

## Compilação

```bash
# Compilar o projeto
mvn clean compile

# Criar JAR executável
mvn clean package
```

## Execução

### 1. Configurar o Túnel de Tráfego

Primeiro, configure o túnel de tráfego conforme os arquivos C fornecidos:

```bash
# No diretório traffic_tunnel/traffic_tunnel
make

# Executar o servidor proxy
sudo ./traffic_tunnel eth0 -s <ip>

# Em outro terminal, executar o cliente
sudo ./traffic_tunnel eth0 -c <ip> -t client1.sh eth0
```

### 2. Executar o Monitor

```bash
sudo java -jar target/network-monitor-1.0.0.jar
```

## Dependências

- **Pcap4J**: Captura de pacotes usando raw sockets
- **Apache Commons CSV**: Geração de arquivos CSV
- **SLF4J**: Logging

