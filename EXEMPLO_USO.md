# Exemplo de Uso - Monitor de Tráfego de Rede

## Cenário de Teste

Este exemplo demonstra como configurar e usar o sistema completo de monitoramento de tráfego de rede.

## Pré-requisitos

- Sistema Linux (Ubuntu/Debian recomendado)
- Java 11+
- Maven 3.6+
- Privilégios de root
- Duas máquinas na mesma rede (ou VMs)

## Passo a Passo

### 1. Preparar o Ambiente

```bash
# Instalar dependências (Ubuntu/Debian)
sudo apt update
sudo apt install build-essential iptables openjdk-11-jdk maven

# Verificar instalações
java -version
mvn -version
```

### 2. Compilar o Túnel de Tráfego

```bash
# Navegar para o diretório do túnel
cd traffic_tunnel/traffic_tunnel

# Compilar
make

# Verificar se foi criado o executável
ls -la traffic_tunnel
```

### 3. Configurar a Rede

#### No Servidor Proxy (Máquina 1):

```bash
# Verificar interfaces de rede
ip addr show

# Executar o servidor proxy
sudo ./traffic_tunnel eth0 -s 192.168.1.1
```

#### No Cliente (Máquina 2):

```bash
# Executar o cliente
sudo ./traffic_tunnel eth0 -c 192.168.1.2 -t client1.sh eth0
```

### 4. Verificar se o Túnel Está Funcionando

```bash
# No servidor, verificar se a interface tun0 foi criada
ip link show tun0

# Verificar endereço IP da interface tun0
ip addr show tun0

# Deve mostrar algo como:
# 172.31.66.1/24
```

### 5. Executar o Monitor de Tráfego

```bash
# No servidor, em um novo terminal
cd /caminho/para/tf-lab-redes

# Executar o script de monitoramento
sudo ./run_monitor.sh
```

### 6. Gerar Tráfego para Teste

#### No Cliente:

```bash
# Fazer ping para testar conectividade
ping 8.8.8.8

# Fazer requisição HTTP
curl http://www.google.com

# Usar wget
wget http://www.example.com
```

### 7. Observar os Resultados

#### Interface do Monitor:

Você verá uma interface como esta:

```
╔══════════════════════════════════════════════════════════════╗
║                MONITOR DE TRÁFEGO DE REDE                   ║
║                    Interface: tun0                          ║
║                    2024-01-15 14:30:25                      ║
╠══════════════════════════════════════════════════════════════╣
║                        ESTATÍSTICAS GERAIS                  ║
╠══════════════════════════════════════════════════════════════╣
║  Total de Pacotes: 1250                                      ║
║  Total de Bytes: 156800                                      ║
║  Taxa Média: 125.00 pacotes/s                                ║
╠══════════════════════════════════════════════════════════════╣
║                    PROTOCOLOS DE REDE                       ║
╠══════════════════════════════════════════════════════════════╣
║  IPv4           : 1250                                       ║
╠══════════════════════════════════════════════════════════════╣
║                  PROTOCOLOS DE TRANSPORTE                   ║
╠══════════════════════════════════════════════════════════════╣
║  TCP            : 850                                        ║
║  UDP            : 350                                        ║
║  ICMP           : 50                                         ║
╚══════════════════════════════════════════════════════════════╝
```

#### Verificar Logs CSV:

```bash
# Em outro terminal, verificar os logs
cat camada2.csv
cat camada3.csv
cat camada4.csv
```

### 8. Exemplo de Saída dos Logs

#### camada2.csv:

```csv
Data e Hora,Endereço MAC de Origem,Endereço MAC de Destino,Protocolo (EtherType),Tamanho Total do Quadro (bytes)
2024-01-15 14:30:25,00:00:00:33:33:33,ff:ff:ff:ff:ff:ff,0x0800,1518
2024-01-15 14:30:26,00:00:00:22:22:22,00:00:00:33:33:33,0x0800,1518
```

#### camada3.csv:

```csv
Data e Hora,Nome do Protocolo,Endereço IP de Origem,Endereço IP de Destino,Número Identificador do Protocolo,Tamanho Total do Pacote (bytes)
2024-01-15 14:30:25,IPv4,172.31.66.101,8.8.8.8,1,1500
2024-01-15 14:30:26,IPv4,8.8.8.8,172.31.66.101,1,1500
```

#### camada4.csv:

```csv
Data e Hora,Nome do Protocolo,Endereço IP de Origem,Porta de Origem,Endereço IP de Destino,Porta de Destino,Tamanho Total do Pacote (bytes)
2024-01-15 14:30:25,TCP,172.31.66.101,12345,8.8.8.8,80,1500
2024-01-15 14:30:26,UDP,172.31.66.101,54321,8.8.8.8,53,1500
```

## Testes Adicionais

### Teste de Diferentes Protocolos:

```bash
# DNS (UDP)
nslookup google.com

# HTTP (TCP)
curl -I http://www.google.com

# HTTPS (TCP)
curl -I https://www.google.com

# Ping (ICMP)
ping -c 5 8.8.8.8

# Traceroute (UDP/ICMP)
traceroute google.com
```

### Teste de Volume:

```bash
# Gerar tráfego intenso
for i in {1..100}; do
    curl -s http://www.google.com > /dev/null &
done
wait
```

## Solução de Problemas

### Interface tun0 não aparece:

```bash
# Verificar se o túnel está executando
ps aux | grep traffic_tunnel

# Verificar logs do sistema
dmesg | tail -20
```

### Erro de permissão:

```bash
# Verificar se está executando como root
whoami

# Se não for root, executar com sudo
sudo ./run_monitor.sh
```

### Nenhum pacote capturado:

```bash
# Verificar se há tráfego na interface
tcpdump -i tun0 -c 5

# Verificar roteamento
ip route show
```

## Limpeza

```bash
# Parar o monitor (Ctrl+C)

# Parar o túnel (Ctrl+C nos terminais do túnel)

# Remover interface tun0 (se necessário)
sudo ip link delete tun0

# Limpar logs (opcional)
rm -f camada2.csv camada3.csv camada4.csv
```

## Análise dos Resultados

1. **Camada 2**: Observe os endereços MAC e EtherTypes
2. **Camada 3**: Analise os padrões de endereços IP
3. **Camada 4**: Identifique os serviços mais utilizados pelas portas

Este exemplo demonstra como o sistema captura e analisa tráfego real em uma rede com túnel, fornecendo insights sobre o comportamento da rede.
