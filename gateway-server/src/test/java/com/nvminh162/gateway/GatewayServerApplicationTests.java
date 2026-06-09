package com.nvminh162.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"app.api-key=test-api-key",
		"eureka.client.enabled=false",
		"spring.cloud.discovery.enabled=false",
		"spring.cloud.gateway.server.webflux.discovery.locator.enabled=false",
		"spring.data.redis.host=localhost",
		"spring.data.redis.port=6379",
		"spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8180/realms/test",
		"spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8180/realms/test/protocol/openid-connect/certs"
})
class GatewayServerApplicationTests {

	@Test
	void contextLoads() {
	}

}
