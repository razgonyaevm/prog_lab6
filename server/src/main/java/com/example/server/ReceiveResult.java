package com.example.server;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

public record ReceiveResult(ByteBuffer buffer, SocketAddress clientAddress) {}
