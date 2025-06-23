#!/bin/bash

# Este script configura um ambiente de rede com Docker para o Trabalho Final de Redes de Computadores.
# Inclui um servidor proxy e múltiplos clientes em uma rede virtual,
# permitindo o monitoramento de tráfego com raw sockets.

# Variáveis de Configuração
# Caminho da pasta local no Windows que será mapeada para os containers.
# Ajuste este caminho se sua estrutura for diferente.
# Ex: C:\lab-redes\tf-lab-redes no Windows será /mnt/c/lab-redes/tf-lab-redes no WSL2.
PASTA_LOCAL="/mnt/c/lab-redes/tf-lab-redes"
# Como esta pasta será vista dentro dos containers (caminho absoluto)
PASTA_CONTAINER="/home/lab-redes-project"

# Nomes dos containers
PROXY_CONTAINER_NAME="proxy-server"
CLIENT_CONTAINER_NAME_PREFIX="cliente"
NUM_CLIENTS=1 # Ajustado para 1 cliente, como você mencionou que usará apenas um

# Nome da rede virtual Docker
NETWORK_NAME="lab_redes_tunel"
# Subnet para a LAN virtual onde clientes e proxy estarão.
# Esta subnet (172.31.66.0/24) é mencionada no PDF para a interface tun0. 
NETWORK_SUBNET="172.31.66.0/24"

# --- Início do Script ---

echo "--- Iniciando Configuração do Ambiente Docker ---"

# 1. Verifica se Docker está instalado no WSL2
echo "Verificando instalação do Docker CLI..."
if ! command -v docker &> /dev/null; then
    echo "ERRO: Docker CLI não encontrado. Certifique-se de que o Docker Desktop está rodando no Windows"
    echo "e que o Docker CLI está configurado para o seu WSL2 (sudo apt install docker.io)."
    exit 1
else
    echo "Docker CLI encontrado."
fi

# 2. Baixar imagem labredes
echo "Baixando imagem ghcr.io/sjohann81/labredes:latest..."
docker pull --quiet ghcr.io/sjohann81/labredes:latest
if [ $? -ne 0 ]; then
    echo "ERRO: Falha ao baixar a imagem labredes. Verifique sua conexão ou o nome da imagem."
    exit 1
fi
echo "Imagem labredes pronta."

# 3. Remover a rede virtual antiga para garantir um estado limpo
echo "Removendo rede virtual antiga '$NETWORK_NAME' (se existir)..."
docker network rm "$NETWORK_NAME" &> /dev/null
echo "Rede antiga removida (ou não existia)."

# 4. Criar rede virtual customizada
echo "Criando rede virtual '$NETWORK_NAME' com subnet $NETWORK_SUBNET..."
docker network create --subnet "$NETWORK_SUBNET" "$NETWORK_NAME"
if [ $? -ne 0 ]; then
    echo "ERRO: Falha ao criar a rede '$NETWORK_NAME'. Pode haver um conflito de IP com outra rede."
    echo "Verifique 'docker network ls' e as configurações de rede do seu sistema."
    exit 1
fi
echo "Rede '$NETWORK_NAME' criada com sucesso."

# 5. Remover containers antigos para garantir um estado limpo
echo "Removendo containers antigos (se existirem)..."
docker rm -f "$PROXY_CONTAINER_NAME" &> /dev/null
for i in $(seq 1 $NUM_CLIENTS); do
    docker rm -f "${CLIENT_CONTAINER_NAME_PREFIX}${i}" &> /dev/null
done
echo "Containers antigos removidos (ou não existiam)."

# 6. Criar container do Servidor Proxy
echo "Criando container do Servidor Proxy: $PROXY_CONTAINER_NAME"
docker run -d --name "$PROXY_CONTAINER_NAME" \
    -v "$PASTA_LOCAL":"$PASTA_CONTAINER" \
    --privileged \
    --network "$NETWORK_NAME" \
    --dns 8.8.8.8 \
    --dns 8.8.4.4 \
    ghcr.io/sjohann81/labredes:latest \
    sleep infinity # Mantém o container rodando indefinidamente

if [ $? -ne 0 ]; then
    echo "ERRO: Falha ao criar o container do Servidor Proxy '$PROXY_CONTAINER_NAME'."
    echo "Verifique a saída do comando acima para detalhes."
    exit 1
fi
echo "Container '$PROXY_CONTAINER_NAME' criado com sucesso."

# 7. Criar containers Cliente(s)
for i in $(seq 1 $NUM_CLIENTS); do
    container_name="${CLIENT_CONTAINER_NAME_PREFIX}${i}"
    echo "Criando container Cliente: $container_name"
    docker run -d --name "$container_name" \
        -v "$PASTA_LOCAL":"$PASTA_CONTAINER" \
        --privileged \
        --network "$NETWORK_NAME" \
        --dns 8.8.8.8 \
        --dns 8.8.4.4 \
        ghcr.io/sjohann81/labredes:latest \
        sleep infinity # Mantém o container rodando indefinidamente

    if [ $? -ne 0 ]; then
        echo "ERRO: Falha ao criar o container Cliente '$container_name'."
        echo "Verifique a saída do comando acima para detalhes."
        exit 1
    fi
    echo "Container '$container_name' criado com sucesso."
done

echo ""
echo "--- Ambiente de Rede Docker Configurado ---"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.ID}}"

echo ""
echo "--- Próximos Passos (Manualmente dentro dos containers) ---"
echo ""
echo "Este script configurou a rede Docker e iniciou os containers."
echo "Agora, configure e execute os programas dentro dos containers conforme as etapas abaixo:"
echo ""
echo "1. **Compilação e Executáveis:**"
echo "   Certifique-se de que o 'traffic_tunnel' (em $PASTA_CONTAINER/traffic_tunnel/) e"
echo "   seu programa monitor (em $PASTA_CONTAINER/src/) já foram compilados e seus executáveis estão presentes."
echo "   (Isso deve ser feito no seu ambiente WSL2 antes de executar este script)."
echo ""
echo "2. **Configuração do Servidor Proxy (dentro do container '$PROXY_CONTAINER_NAME'):**"
echo "   Abra um novo terminal WSL2 e acesse o container:"
echo "   docker exec -it $PROXY_CONTAINER_NAME bash"
echo "   Dentro do container, execute os comandos:"
echo "   a) Instale o JRE se seu monitor for Java: apt update && apt install -y openjdk-17-jre"
echo "   b) Habilite o roteamento de IP: echo 1 > /proc/sys/net/ipv4/ip_forward"
echo "   c) Navegue para a pasta do projeto: cd $PASTA_CONTAINER"
echo "   d) Obtenha o IP da LAN do proxy: ip addr show eth0 (Anote o IP, ex: 172.31.66.2)"
echo "   e) Inicie o traffic_tunnel no modo servidor (com nohup para rodar em segundo plano):"
echo "      nohup sudo $PASTA_CONTAINER/traffic_tunnel/traffic_tunnel eth0 -s <IP_DA_ETH0_DO_PROXY> &"
echo "      (Aguarde 5 segundos e verifique tun0 com 'ip addr show tun0', deve ser 172.31.66.1)"
echo "   f) Configure as regras de iptables para NAT (essencial para acesso à internet):"
echo "      iptables -t nat -A POSTROUTING -s 172.31.66.0/24 -o eth0 -j MASQUERADE"
echo "      iptables -A FORWARD -i tun0 -o eth0 -j ACCEPT"
echo "      iptables -A FORWARD -i eth0 -o tun0 -m state --state RELATED,ESTABLISHED -j ACCEPT"
echo "   g) Execute seu programa monitor na interface tun0:"
echo "      sudo java -jar $PASTA_CONTAINER/target/seu_monitor.jar tun0"
echo ""
echo "3. **Configuração do Cliente (dentro do container '$CLIENT_CONTAINER_NAME_PREFIX1'):**"
echo "   Abra um novo terminal WSL2 e acesse o container:"
echo "   docker exec -it ${CLIENT_CONTAINER_NAME_PREFIX}1 bash"
echo "   Dentro do container, execute os comandos:"
echo "   a) Instale o JRE e ferramentas de teste: apt update && apt install -y openjdk-17-jre iputils-ping curl e2fsprogs"
echo "   b) Navegue para a pasta do projeto: cd $PASTA_CONTAINER"
echo "   c) Obtenha o IP da LAN do cliente: ip addr show eth0 (Anote o IP, ex: 172.31.66.3)"
echo "   d) Crie ou edite o script cliente (client1.sh) em $PASTA_CONTAINER/client1.sh:"
echo "      (Conteúdo exato do script do professor, mas com a certeza de que tun0 do proxy é 172.31.66.1):"
echo "      #!/bin/sh"
echo "      ifconfig tun0 mtu 1472 up 172.31.66.101 netmask 255.255.255.0"
echo "      route del default"
echo "      route add default gw 172.31.66.1 tun0"
echo "      "
echo "   e) Dê permissão de execução: chmod +x $PASTA_CONTAINER/client1.sh"
echo "   f) Inicie o traffic_tunnel no modo cliente (com nohup):"
echo "      nohup sudo $PASTA_CONTAINER/traffic_tunnel/traffic_tunnel eth0 -c <IP_DA_ETH0_DO_CLIENTE> -t $PASTA_CONTAINER/client1.sh &"
echo "      (Aguarde 5 segundos e verifique tun0 com 'ip addr show tun0', deve ser 172.31.66.101)"
echo "   g) **NÃO É NECESSÁRIO EDITAR /etc/resolv.conf MANUALMENTE AGORA (Docker já configurou via --dns).**"
echo "   h) Teste o tráfego: ping google.com OU curl https://www.example.com"
echo "      (Observe o monitor no container do Servidor Proxy!)"

echo ""
echo "--- Fim do Script de Configuração ---"