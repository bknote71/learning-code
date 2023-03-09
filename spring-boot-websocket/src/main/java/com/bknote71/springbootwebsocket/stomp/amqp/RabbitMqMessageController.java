package com.bknote71.springbootwebsocket.stomp.amqp;

import com.bknote71.springbootwebsocket.stomp.MyMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.support.NativeMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

import java.security.Principal;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Controller
public class RabbitMqMessageController {

    // 외부로부터 메시지를 받고 rabbitMQ 로 메세지를 전송하는 relay 역할을 한다.
    // 중간 매개체 역할
    private final SimpMessageSendingOperations operations;

    @EventListener
    public void socketConnect(SessionConnectedEvent event) {
        // header(Accessor) 얻어오기
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("stomp header accessor={}", headerAccessor);
        MessageHeaders messageHeaders = headerAccessor.getMessageHeaders();
        Long roomId = (Long) messageHeaders.get("roomId");
        Principal user = headerAccessor.getUser(); // <<<< 이거는 스프링 시큐리티로 연동할 수 있을듯 하다.
        // send: ~님이 입장하셨습니다.
    }

    @MessageMapping("/msg") // 겹치면 안된다 << 글로벌 적용
    public void sendMessage(MyMessage message) {
        operations.convertAndSend("/topic/" + message.getRoomId(), message.getContent());
    }
}
