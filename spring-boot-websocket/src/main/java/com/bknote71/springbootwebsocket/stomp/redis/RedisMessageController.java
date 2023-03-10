package com.bknote71.springbootwebsocket.stomp.redis;

import com.bknote71.springbootwebsocket.stomp.MyMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@RequiredArgsConstructor
@Controller
public class RedisMessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate redisTemplate;
    private final ChannelTopic channelTopic;

    @MessageMapping("/send")
    public void sendMessage(MyMessage message) {
        log.info("sending message={}", message);
        redisTemplate.convertAndSend(channelTopic.getTopic(), message);
    }


}
