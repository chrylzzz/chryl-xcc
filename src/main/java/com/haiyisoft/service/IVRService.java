package com.haiyisoft.service;

import com.haiyisoft.entry.ChannelEvent;
import io.nats.client.Connection;
import org.springframework.scheduling.annotation.Async;

/**
 * Created by Chr.yl on 2023/7/16.
 *
 * @author Chr.yl
 */
public interface IVRService {

    @Async
    void handlerChannelEvent(Connection nc, ChannelEvent channelEvent);

}
