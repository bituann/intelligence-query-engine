package com.bituan.intelligence_query_engine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rsa")
public record RSAKeys (String publicKey, String privateKey) {}
