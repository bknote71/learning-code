package com.bknote71.springbootwebsocket.stomp.simple;

import com.bknote71.springbootwebsocket.stomp.MyMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class SimpleMessageController {

    // read only JSON
    // 내부 simple broker에 메시지 전송
    private final SimpMessageSendingOperations operations;

    @MessageMapping("/enter")
    public void enterRoom(MyMessage message) { // json 요청 <<
        operations.convertAndSend("/sub/room/" + message.getRoomId(), message.getContent() + "님이 입장하셨습니다.");
    }

    @MessageMapping("/message")
    public void sendMessage(MyMessage message) {
        operations.convertAndSend("/sub/room/" + message.getRoomId(), message.getContent());
    }

}
