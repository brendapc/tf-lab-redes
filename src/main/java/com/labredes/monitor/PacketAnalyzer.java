package com.labredes.monitor;

import org.pcap4j.packet.*;
import org.pcap4j.packet.namednumber.EtherType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * Analisador de pacotes de rede que extrai informações das camadas 2, 3 e 4
 */
public class PacketAnalyzer {
  private static final Logger logger = LoggerFactory.getLogger(PacketAnalyzer.class);

  public PacketInfo analyzePacket(Packet packet, LocalDateTime timestamp) {
    PacketInfo info = new PacketInfo(timestamp);

    try {
      // Analisa camada 2 (Ethernet)
      analyzeLayer2(packet, info);

      // Analisa camada 3 (IP)
      analyzeLayer3(packet, info);

      // Analisa camada 4 (Transporte)
      analyzeLayer4(packet, info);

    } catch (Exception e) {
      logger.error("Erro ao analisar pacote: {}", e.getMessage(), e);
    }

    return info;
  }

  private void analyzeLayer2(Packet packet, PacketInfo info) {
    EthernetPacket ethernetPacket = packet.get(EthernetPacket.class);
    if (ethernetPacket != null) {
      EthernetPacket.EthernetHeader header = ethernetPacket.getHeader();

      // Endereços MAC
      info.setSourceMac(formatMacAddress(header.getSrcAddr()));
      info.setDestinationMac(formatMacAddress(header.getDstAddr()));

      // EtherType
      EtherType type = header.getType();
      info.setEtherType(String.format("0x%04x", type.value()));

      // Tamanho do frame
      info.setFrameSize(packet.length());
    }
  }

  private void analyzeLayer3(Packet packet, PacketInfo info) {
    IpPacket ipPacket = packet.get(IpPacket.class);
    if (ipPacket != null) {
      IpPacket.IpHeader header = ipPacket.getHeader();

      // Protocolo de rede
      if (ipPacket instanceof IpV4Packet) {
        info.setNetworkProtocol("IPv4");
        IpV4Packet.IpV4Header ipv4Header = (IpV4Packet.IpV4Header) header;
        info.setPacketSize(ipv4Header.getTotalLengthAsInt());
      } else if (ipPacket instanceof IpV6Packet) {
        info.setNetworkProtocol("IPv6");
        IpV6Packet.IpV6Header ipv6Header = (IpV6Packet.IpV6Header) header;
        info.setPacketSize(ipv6Header.getPayloadLengthAsInt());
      }

      // Endereços IP
      info.setSourceIp(header.getSrcAddr().getHostAddress());
      info.setDestinationIp(header.getDstAddr().getHostAddress());

      // Número do protocolo
      info.setProtocolNumber(header.getProtocol().value());
    }
  }

  private void analyzeLayer4(Packet packet, PacketInfo info) {
    // TCP
    TcpPacket tcpPacket = packet.get(TcpPacket.class);
    if (tcpPacket != null) {
      TcpPacket.TcpHeader header = tcpPacket.getHeader();
      info.setTransportProtocol("TCP");
      info.setSourcePort(header.getSrcPort().valueAsInt());
      info.setDestinationPort(header.getDstPort().valueAsInt());
      return;
    }

    // UDP
    UdpPacket udpPacket = packet.get(UdpPacket.class);
    if (udpPacket != null) {
      UdpPacket.UdpHeader header = udpPacket.getHeader();
      info.setTransportProtocol("UDP");
      info.setSourcePort(header.getSrcPort().valueAsInt());
      info.setDestinationPort(header.getDstPort().valueAsInt());
      return;
    }

    // ICMP - verificar por protocolo número
    if (info.getProtocolNumber() == 1) {
      info.setTransportProtocol("ICMP");
      info.setSourcePort(0);
      info.setDestinationPort(0);
      return;
    }

    // ARP
    ArpPacket arpPacket = packet.get(ArpPacket.class);
    if (arpPacket != null) {
      info.setTransportProtocol("ARP");
      info.setSourcePort(0);
      info.setDestinationPort(0);
      return;
    }

    // Outros protocolos
    if (info.getProtocolNumber() > 0) {
      info.setTransportProtocol("Other");
      info.setSourcePort(0);
      info.setDestinationPort(0);
    }
  }

  private String formatMacAddress(Object macAddress) {
    if (macAddress == null)
      return "00:00:00:00:00:00";

    try {
      // Tentar acessar o endereço MAC usando reflexão
      byte[] bytes = (byte[]) macAddress.getClass().getMethod("getAddress").invoke(macAddress);
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < bytes.length; i++) {
        if (i > 0)
          sb.append(":");
        sb.append(String.format("%02x", bytes[i] & 0xff));
      }

      return sb.toString();
    } catch (Exception e) {
      logger.warn("Erro ao formatar endereço MAC: {}", e.getMessage());
      return "00:00:00:00:00:00";
    }
  }
}