#!/bin/bash

# Caminho da pasta local no Windows mapeada para os containers
# Mantenha esta pasta para colocar seu executável do monitor e os fontes/executáveis do traffic_tunnel
PASTA_LOCAL="/c/lab-redes"
PASTA_CONTAINER="/home/lab-redes"

# Verifica se Podman está instalado
echo "Verificando instalação do Podman..."
if ! command -v podman &> /dev/null
then
    echo "Podman não encontrado. Instale-o manualmente no Windows."
    exit 1
else
    echo "Podman encontrado."
fi

# Baixar imagem labredes
echo "Baixando imagem labredes..."
podman pull ghcr.io/sjohann81/labredes:latest # Adicionado :latest para garantir a versão mais recente 

# Criar rede lab_redes_tunel
# Nome da rede alterado para evitar conflitos e ser mais descritivo
echo "Criando rede virtual 'lab_redes_tunel'..."
if podman network exists lab_redes_tunel; then
    echo "Rede 'lab_redes_tunel' já existe. Pulando criação."
else
    # Subnet para a LAN virtual onde clientes e proxy estarão.
    # Esta subnet (172.31.66.0/24) é mencionada no PDF para a interface tun0 
    podman network create lab_redes_tunel --subnet 172.31.66.0/24
fi

# Definir nomes dos containers
PROXY_CONTAINER_NAME="proxy-server"
CLIENT_CONTAINER_NAME_PREFIX="client"
NUM_CLIENTS=2 # Você pode ajustar o número de clientes aqui

# Remover containers antigos se existirem
echo "Removendo containers antigos..."
podman rm -f "$PROXY_CONTAINER_NAME" &> /dev/null
for i in $(seq 1 $NUM_CLIENTS); do
    podman rm -f "${CLIENT_CONTAINER_NAME_PREFIX}${i}" &> /dev/null
done

# Criar container do Servidor Proxy
echo "Criando container do Servidor Proxy: $PROXY_CONTAINER_NAME"
podman run -d --name "$PROXY_CONTAINER_NAME" \
    -v "$PASTA_LOCAL":"$PASTA_CONTAINER" \
    --cap-add NET_ADMIN --privileged \
    --network lab_redes_tunel \
    ghcr.io/sjohann81/labredes:latest \
    sleep infinity # Mantém o container rodando indefinidamente

# Criar containers Cliente(s)
for i in $(seq 1 $NUM_CLIENTS); do
    container_name="${CLIENT_CONTAINER_NAME_PREFIX}${i}"
    echo "Criando container Cliente: $container_name"
    podman run -d --name "$container_name" \
        -v "$PASTA_LOCAL":"$PASTA_CONTAINER" \
        --cap-add NET_ADMIN --privileged \
        --network lab_redes_tunel \
        ghcr.io/sjohann81/labredes:latest \
        sleep infinity # Mantém o container rodando indefinidamente
done

echo ""
echo "Ambiente criado com sucesso!"
podman ps --format "table {{.Names}}\t{{.Status}}\t{{.ID}}"

echo ""
echo "--- Próximos Passos (Manualmente dentro dos containers) ---"
echo ""
echo "1. Copie o executável do 'traffic_tunnel' e os scripts clientes (se houver) para $PASTA_LOCAL."
echo "   Ex: Crie uma pasta 'traffic_tunnel_bin' em $PASTA_LOCAL e coloque os executáveis lá."
echo ""
echo "2. Acesse o container do Servidor Proxy:"
echo "   podman exec -it $PROXY_CONTAINER_NAME bash"
echo "   Dentro do proxy, vá para $PASTA_CONTAINER (ex: cd /home/lab-redes/traffic_tunnel_bin)."
echo "   Compile o seu programa monitor aqui (ou copie o executável compilado)."
echo "   Obtenha o IP do proxy na rede 'lab_redes_tunel': ip addr show eth0"
echo "   Execute o traffic_tunnel no modo servidor:"
echo "   sudo ./traffic_tunnel <interface_proxy_LAN_ex_eth0> -s <ip_proxy_LAN>"
echo "   Configure as regras de iptables para NAT (conforme instruções do PDF, página 3):"
echo "   echo 1 > /proc/sys/net/ipv4/ip_forward"
echo "   iptables -t nat -A POSTROUTING -o <interface_internet_proxy_ex_eth0> -j MASQUERADE"
echo "   iptables -A FORWARD -i tun0 -o <interface_internet_proxy_ex_eth0> -j ACCEPT"
echo "   iptables -A FORWARD -i <interface_internet_proxy_ex_eth0> -o tun0 -m state --state RELATED,ESTABLISHED -j ACCEPT"
echo "   Execute seu programa monitor para capturar na interface tun0:"
echo "   sudo ./seu_monitor_executavel tun0" # Seu monitor precisará receber a interface como argumento

echo ""
echo "3. Para cada container Cliente (ex: client1):"
echo "   podman exec -it ${CLIENT_CONTAINER_NAME_PREFIX}1 bash"
echo "   Dentro do cliente, vá para $PASTA_CONTAINER (ex: cd /home/lab-redes/traffic_tunnel_bin)."
echo "   Obtenha o IP do cliente na rede 'lab_redes_tunel': ip addr show eth0"
echo "   Execute o traffic_tunnel no modo cliente (ajuste 'client1.sh' para o IP do proxy):"
echo "   sudo ./traffic_tunnel <interface_cliente_LAN_ex_eth0> -c <ip_cliente_LAN> -t client1.sh"
echo "   # O arquivo client1.sh deve conter a configuração do túnel para o IP do proxy (172.31.66.1)"
echo "   # Exemplo do que pode estar em client1.sh (substitua <IP_PROXY_TUN0> pelo IP da tun0 do proxy, geralmente 172.31.66.1):"
echo "   # ip link set tun0 up"
echo "   # ip addr add 172.31.66.X/24 dev tun0" # X é um IP diferente do proxy, ex: 172.31.66.2
echo "   # ip route add default via <IP_PROXY_TUN0> dev tun0"
echo "   # Teste o tráfego: ping google.com ou curl google.com"

echo ""
echo "Lembre-se de que a compilação do traffic_tunnel deve ser feita previamente no seu ambiente Linux."
echo "Certifique-se de que os executáveis e scripts necessários estejam na $PASTA_LOCAL antes de iniciar os containers."