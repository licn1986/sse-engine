package com.bally.evolve.core.sse.engine;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "sse-engine")
@Getter
@Setter
public class SseEngineProperties {

	private Long eventTimeout;

	private String eventPublishFixedDelay;

	private String eventQueueCleanupFixedDelay;

	private String eventFluxRegistryCleanupFixedDelay;
}
