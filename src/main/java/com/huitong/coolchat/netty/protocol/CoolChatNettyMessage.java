package com.huitong.coolchat.netty.protocol;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CoolChatNettyMessage {

    private int length;
    private byte[] content;
}
