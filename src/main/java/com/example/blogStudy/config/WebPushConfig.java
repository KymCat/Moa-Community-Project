package com.example.blogStudy.config;

import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class WebPushConfig {
    @Bean
    public PushService pushService(
            @Value("${web-push.vapid.public-key}") String publicKey,
            @Value("${web-push.vapid.private-key}") String privateKey,
            @Value("${web-push.vapid.subject}") String subject
    ) throws GeneralSecurityException {

        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        return new PushService(publicKey, privateKey, subject);
    }

    @Bean(name = "webPushExecutor")
    public Executor webPushExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("web-push-");
        executor.initialize();

        return executor;
    }
}
