package com.chatapplication.network;

import java.io.Serializable;

public enum MessageType implements Serializable {

    PRIVATE_MESSAGE,

    GROUP_MESSAGE,

    GROUP_CREATED,

    GROUP_MEMBER_ADDED,

    GROUP_MEMBER_REMOVED

}