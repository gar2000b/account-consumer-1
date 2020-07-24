package com.onlineinteract.workflow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationProperties {
	
	@Value("${standup.new.service}")
	private boolean standupNewService;

	public boolean isStandupNewService() {
		return standupNewService;
	}

	public void setStandupNewService(boolean standupNewService) {
		this.standupNewService = standupNewService;
	}
}
