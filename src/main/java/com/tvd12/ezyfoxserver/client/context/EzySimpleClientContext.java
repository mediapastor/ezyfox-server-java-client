package com.tvd12.ezyfoxserver.client.context;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import com.tvd12.ezyfox.entity.EzyArray;
import com.tvd12.ezyfox.entity.EzyEntity;
import com.tvd12.ezyfoxserver.client.EzyClient;
import com.tvd12.ezyfoxserver.client.cmd.EzyEnableSocket;
import com.tvd12.ezyfoxserver.client.cmd.EzyEnableWebSocket;
import com.tvd12.ezyfoxserver.client.cmd.EzyPingSchedule;
import com.tvd12.ezyfoxserver.client.cmd.EzySendRequest;
import com.tvd12.ezyfoxserver.client.cmd.impl.EzyClientShutdownImpl;
import com.tvd12.ezyfoxserver.client.cmd.impl.EzyEnableSocketImpl;
import com.tvd12.ezyfoxserver.client.cmd.impl.EzyEnableWebSocketImpl;
import com.tvd12.ezyfoxserver.client.cmd.impl.EzyPingScheduleImpl;
import com.tvd12.ezyfoxserver.client.cmd.impl.EzySendRequestImpl;
import com.tvd12.ezyfoxserver.client.entity.EzyClientUser;
import com.tvd12.ezyfoxserver.client.entity.EzySimpleClientUser;
import com.tvd12.ezyfoxserver.client.request.EzyRequest;
import com.tvd12.ezyfoxserver.client.request.EzyRequestPluginRequest;
import com.tvd12.ezyfoxserver.command.EzyRunWorker;
import com.tvd12.ezyfoxserver.command.EzyShutdown;
import com.tvd12.ezyfoxserver.command.impl.EzyRunWorkerImpl;

import lombok.Getter;
import lombok.Setter;

public class EzySimpleClientContext 
		extends EzyEntity 
		implements EzyClientContext {

	@Setter
	@Getter
	protected EzyClient client;
	
	@Getter
	protected final EzyClientUser me = new EzySimpleClientUser();
	
	@Setter
	@Getter
	protected ExecutorService workerExecutor;
	
	@SuppressWarnings("rawtypes")
	protected Map<Class, Supplier> commandSuppliers = defaultCommandSuppliers();
	
	protected Map<Integer, EzyClientAppContext> appContextByIds = new ConcurrentHashMap<>();
	protected Map<String, EzyClientAppContext> appContextByNames = new ConcurrentHashMap<>();
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Class<T> clazz) {
		if(commandSuppliers.containsKey(clazz))
			return (T) commandSuppliers.get(clazz).get();
		if(containsKey(clazz))
			return getProperty(clazz);
		return null;
	}
	
	@Override
	public EzyClientAppContext getAppContext(int appId) {
		return appContextByIds.get(appId);
	}
	
	@Override
	public EzyClientAppContext getAppContext(String appName) {
		return appContextByNames.get(appName);
	}
	
	@Override
	public void addAppContext(EzyClientAppContext context) {
		appContextByIds.put(context.getAppId(), context);
		appContextByNames.put(context.getAppName(), context);
	}
	
	@Override
	public void sendPluginRequest(String pluginName, EzyArray data) {
		EzyRequest pluginRequest = newPluginRequest(pluginName, data);
		get(EzySendRequest.class)
			.sender(getMe())
			.request(pluginRequest)
			.execute();
	}
	
	private EzyRequest newPluginRequest(String pluginName, EzyArray data) {
		return EzyRequestPluginRequest.builder()
				.pluginName(pluginName)
				.data(data)
				.build();
	}
	
	@SuppressWarnings("rawtypes")
	protected Map<Class, Supplier> defaultCommandSuppliers() {
		EzyPingSchedule pingSchedule = new EzyPingScheduleImpl(this);
		Map<Class, Supplier> answer = new HashMap<>();
		answer.put(EzyPingSchedule.class, () -> pingSchedule);
		answer.put(EzySendRequest.class, () -> new EzySendRequestImpl(this));
		answer.put(EzyShutdown.class, () -> new EzyClientShutdownImpl(this));
		answer.put(EzyRunWorker.class, () -> new EzyRunWorkerImpl(getWorkerExecutor()));
		answer.put(EzyEnableSocket.class, () -> new EzyEnableSocketImpl(this));
		answer.put(EzyEnableWebSocket.class, () -> new EzyEnableWebSocketImpl(this));
		return answer;
	}
	
	@Override
	public void destroy() {
		properties.clear();
		workerExecutor.shutdown();
	}

}
