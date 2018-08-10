package com.tvd12.ezyfoxserver.client.entity;

import java.net.SocketAddress;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.tvd12.ezyfox.entity.EzyEntity;
import com.tvd12.ezyfoxserver.delegate.EzySessionDelegate;
import com.tvd12.ezyfoxserver.socket.EzyPacket;

import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EzySimpleClientSession extends EzyEntity implements EzyClientSession {
	private static final long serialVersionUID = 8916844385176991522L;
	
	protected long id;
	private String clientId;
	protected long creationTime;
	protected long lastReadTime;
	protected long lastWriteTime;
	protected long readBytes;
	protected long writtenBytes;
	protected long lastActivityTime;
	
	protected byte[] privateKey;
	protected byte[] publicKey;
	protected byte[] serverKey;
	
	protected boolean loggedIn;
	protected boolean activated;
	protected long loggedInTime;
	
	protected String clientType;
	protected String clientVersion;
	protected String reconnectToken;
	protected String fullReconnectToken;

	protected long maxWaitingTime  = 5 * 1000;
	protected long maxIdleTime     = 3 * 60 * 1000;
	
	protected transient EzySessionDelegate delegate;
	protected transient Lock lock = new ReentrantLock();
	protected transient Channel channel;
	
	@Override
	public void addReadBytes(long bytes) {
		this.readBytes += bytes;
	}

	@Override
	public void addWrittenBytes(long bytes) {
		this.writtenBytes += bytes;
	}
	
	@Override
	public SocketAddress getClientAddress() {
		return channel != null ? channel.remoteAddress() : null;
	}
	
	@Override
	public SocketAddress getServerAddress() {
		return channel != null ? channel.localAddress() : null;
	}
	
	@Override
	public void send(EzyPacket packet) {
		channel.writeAndFlush(packet.getData());
	}
	
	@Override
	public void sendNow(EzyPacket packet) {
		send(packet);
	}
	
	@Override
	public void disconnect() {
		channel.disconnect().syncUninterruptibly();
	}
	
	@Override
	public void close() {
		channel.close().syncUninterruptibly();
	}
	
	@Override
	public void destroy() {
		this.setChannel(null);
	}

}
