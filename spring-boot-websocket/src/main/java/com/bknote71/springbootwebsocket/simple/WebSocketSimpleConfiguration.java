package com.bknote71.springbootwebsocket.simple;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

//@Configuration
//@EnableWebSocket
public class WebSocketSimpleConfiguration implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // path로 요청이 왔을 때 handler가 이벤트 처리
        registry.addHandler(webSocketHandler(), "/room/**")
                .setAllowedOrigins("*");
    }

    @Bean
    public WebSocketHandler webSocketHandler() {
        return new SimpleWebSocketHandler();
    }
}
