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
- Sistema Linux (para acesso à interface tun0)
- Privilégios de root (para captura de pacotes)

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
sudo ./traffic_tunnel eth0 -s 192.168.1.1

# Em outro terminal, executar o cliente
sudo ./traffic_tunnel eth0 -c 192.168.1.2 -t client1.sh eth0
```

### 2. Executar o Monitor

```bash
# Executar com privilégios de root
sudo java -jar target/network-monitor-1.0.0.jar
```

## Arquivos de Log

O monitor gera três arquivos CSV em tempo real:

### camada2.csv (Camada de Enlace)

- Data e hora da captura
- Endereço MAC de origem
- Endereço MAC de destino
- Protocolo (EtherType) em hexadecimal
- Tamanho total do quadro em bytes

### camada3.csv (Camada de Rede)

- Data e hora da captura
- Nome do protocolo (IPv4/IPv6)
- Endereço IP de origem
- Endereço IP de destino
- Número identificador do protocolo
- Tamanho total do pacote em bytes

### camada4.csv (Camada de Transporte)

- Data e hora da captura
- Nome do protocolo (TCP/UDP/ICMP/etc.)
- Endereço IP de origem
- Porta de origem
- Endereço IP de destino
- Porta de destino
- Tamanho total do pacote em bytes

## Visualização dos Logs

Os arquivos CSV podem ser visualizados a qualquer momento:

```bash
# Visualizar logs da camada 2
cat camada2.csv

# Visualizar logs da camada 3
cat camada3.csv

# Visualizar logs da camada 4
cat camada4.csv
```

## Interface de Usuário

O monitor exibe uma interface textual com:

- Estatísticas gerais (total de pacotes, bytes, taxa)
- Contadores por protocolo de rede (IPv4, IPv6)
- Contadores por protocolo de transporte (TCP, UDP, ICMP, ARP)
- Atualização automática a cada 5 segundos

## Estrutura do Projeto

```
tf-lab-redes/
├── pom.xml                          # Configuração Maven
├── src/main/java/com/labredes/monitor/
│   ├── NetworkMonitor.java          # Classe principal
│   ├── PacketAnalyzer.java          # Analisador de pacotes
│   ├── StatisticsDisplay.java       # Exibição de estatísticas
│   ├── CsvLogger.java              # Gerador de logs CSV
│   └── PacketInfo.java             # Informações do pacote
├── traffic_tunnel/                  # Implementação do túnel em C
└── README.md                        # Este arquivo
```

## Dependências

- **Pcap4J**: Captura de pacotes usando raw sockets
- **Apache Commons CSV**: Geração de arquivos CSV
- **SLF4J**: Logging

## Notas Importantes

1. **Privilégios**: O monitor precisa ser executado com privilégios de root para acessar raw sockets
2. **Interface tun0**: O monitor é específico para a interface `tun0` criada pelo túnel
3. **Tempo Real**: Os logs são escritos em tempo real e podem ser visualizados com `cat` a qualquer momento
4. **Compatibilidade**: Desenvolvido para sistemas Linux com suporte a interfaces TUN/TAP

## Solução de Problemas

### Interface tun0 não encontrada

- Verifique se o túnel está executando
- Confirme que a interface foi criada: `ip link show tun0`

### Erro de permissão

- Execute com `sudo`: `sudo java -jar target/network-monitor-1.0.0.jar`

### Dependências não encontradas

- Execute `mvn clean install` para baixar as dependências

## Desenvolvimento

Este projeto foi desenvolvido como parte do trabalho de Laboratório de Redes, focado no estudo de protocolos de rede e captura de pacotes usando raw sockets.
