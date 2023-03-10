package com.bknote71.springbootwebsocket.stomp.redis;

import com.bknote71.springbootwebsocket.stomp.MyMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RedisMessageSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate redisTemplate;

    @SneakyThrows
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String body = (String) redisTemplate.getStringSerializer().deserialize(message.getBody());
        ObjectMapper objectMapper = new ObjectMapper();
        MyMessage myMessage = objectMapper.readValue(body, MyMessage.class);
        messagingTemplate.convertAndSend("/sub/room/" + myMessage.getRoomId(), myMessage);
    }
}
