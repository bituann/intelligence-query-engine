package com.bituan.intelligence_query_engine;

import com.bituan.intelligence_query_engine.config.RSAKeys;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(RSAKeys.class)
@SpringBootApplication
public class IntelligenceQueryEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(IntelligenceQueryEngineApplication.class, args);
	}

}
