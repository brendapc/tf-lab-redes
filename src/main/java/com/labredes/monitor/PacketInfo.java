package com.labredes.monitor;

import java.time.LocalDateTime;

public class PacketInfo {
  private final LocalDateTime timestamp;

  // Camada 2 - Enlace
  private String sourceMac;
  private String destinationMac;
  private String etherType;
  private int frameSize;

  // Camada 3 - Rede
  private String networkProtocol; // IPv4, IPv6
  private String sourceIp;
  private String destinationIp;
  private int protocolNumber;
  private int packetSize;

  // Camada 4 - Transporte
  private String transportProtocol; // TCP, UDP, ICMP, etc.
  private int sourcePort;
  private int destinationPort;

  public PacketInfo(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  // Getters e Setters para Camada 2
  public String getSourceMac() {
    return sourceMac;
  }

  public void setSourceMac(String sourceMac) {
    this.sourceMac = sourceMac;
  }

  public String getDestinationMac() {
    return destinationMac;
  }

  public void setDestinationMac(String destinationMac) {
    this.destinationMac = destinationMac;
  }

  public String getEtherType() {
    return etherType;
  }

  public void setEtherType(String etherType) {
    this.etherType = etherType;
  }

  public int getFrameSize() {
    return frameSize;
  }

  public void setFrameSize(int frameSize) {
    this.frameSize = frameSize;
  }

  // Getters e Setters para Camada 3
  public String getNetworkProtocol() {
    return networkProtocol;
  }

  public void setNetworkProtocol(String networkProtocol) {
    this.networkProtocol = networkProtocol;
  }

  public String getSourceIp() {
    return sourceIp;
  }

  public void setSourceIp(String sourceIp) {
    this.sourceIp = sourceIp;
  }

  public String getDestinationIp() {
    return destinationIp;
  }

  public void setDestinationIp(String destinationIp) {
    this.destinationIp = destinationIp;
  }

  public int getProtocolNumber() {
    return protocolNumber;
  }

  public void setProtocolNumber(int protocolNumber) {
    this.protocolNumber = protocolNumber;
  }

  public int getPacketSize() {
    return packetSize;
  }

  public void setPacketSize(int packetSize) {
    this.packetSize = packetSize;
  }

  // Getters e Setters para Camada 4
  public String getTransportProtocol() {
    return transportProtocol;
  }

  public void setTransportProtocol(String transportProtocol) {
    this.transportProtocol = transportProtocol;
  }

  public int getSourcePort() {
    return sourcePort;
  }

  public void setSourcePort(int sourcePort) {
    this.sourcePort = sourcePort;
  }

  public int getDestinationPort() {
    return destinationPort;
  }

  public void setDestinationPort(int destinationPort) {
    this.destinationPort = destinationPort;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    return String.format("PacketInfo{timestamp=%s, srcMac=%s, dstMac=%s, srcIp=%s, dstIp=%s, protocol=%s}",
        timestamp, sourceMac, destinationMac, sourceIp, destinationIp, transportProtocol);
  }
}