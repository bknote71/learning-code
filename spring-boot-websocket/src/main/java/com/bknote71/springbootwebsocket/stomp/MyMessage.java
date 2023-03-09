package com.bknote71.springbootwebsocket.stomp;

import lombok.Data;

@Data
public class MyMessage {
    private Long roomId;
    private String content;
}
