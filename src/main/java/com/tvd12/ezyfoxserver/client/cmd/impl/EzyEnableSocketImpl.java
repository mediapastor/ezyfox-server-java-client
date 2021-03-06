package com.tvd12.ezyfoxserver.client.cmd.impl;

import java.util.concurrent.ExecutorService;

import com.tvd12.ezyfox.codec.EzyCodecCreator;
import com.tvd12.ezyfox.concurrent.EzyExecutors;
import com.tvd12.ezyfox.reflect.EzyClasses;
import com.tvd12.ezyfoxserver.client.EzyClient;
import com.tvd12.ezyfoxserver.client.EzyClientBootstrap;
import com.tvd12.ezyfoxserver.client.EzyClientSocketBootstrap;
import com.tvd12.ezyfoxserver.client.cmd.EzyEnableSocket;
import com.tvd12.ezyfoxserver.client.context.EzyClientContext;
import com.tvd12.ezyfoxserver.client.setting.EzySocketSetting;
import com.tvd12.ezyfoxserver.client.setting.EzySocketSettingBuilder;
import com.tvd12.ezyfoxserver.command.impl.EzyAbstractCommand;

public class EzyEnableSocketImpl 
		extends EzyAbstractCommand 
		implements EzyEnableSocket {

	protected boolean fromMainThread;
	protected EzySocketSetting socketSetting;
	
	protected final EzyClient client;
	protected final EzyClientContext context;
	
	public EzyEnableSocketImpl(EzyClientContext context) {
		this.context = context;
		this.client = context.getClient();
	}
	
	@Override
	public EzyEnableSocket fromMainThread(boolean fromMainThread) {
		this.fromMainThread = fromMainThread;
		return this;
	}
	
	@Override
	public EzyEnableSocket socketSetting(EzySocketSetting socketSetting) {
		this.socketSetting = socketSetting;
		return this;
	}
	
	@Override
	public EzyEnableSocket socketSetting(EzySocketSettingBuilder socketSettingBuilder) {
		return socketSetting(socketSettingBuilder.build());
	}
	
	@Override
	public void execute() throws Exception {
		EzyCodecCreator codecCreator = newCodecCreator();
		ExecutorService startExecutorService = newStartExecutorService();
		EzyClientBootstrap boostrap = EzyClientSocketBootstrap.builder()
			.clientContext(context)
			.codecCreator(codecCreator)
			.host(socketSetting.getServerHost())
			.port(socketSetting.getServerPort())
			.startExecutorService(startExecutorService)
			.build();
		boostrap.start();
		context.setProperty(EzyClientBootstrap.class, boostrap);
	}
	
	private EzyCodecCreator newCodecCreator() {
		return EzyClasses.newInstance(socketSetting.getCodecCreator());
	}
	
	private ExecutorService newStartExecutorService() {
		if(fromMainThread) return null;
		ExecutorService answer = EzyExecutors.newSingleThreadExecutor("client-starter");
    		Runtime.getRuntime().addShutdownHook(new Thread(() -> answer.shutdown()));
    		return answer;
	}
	
}
