package com.kameo.challenger.utils;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by kmyczkowska on 2016-09-01.
 */
@Component
public class MailService {

    @Data
    @AllArgsConstructor
    public static class Message {
        String toEmail;
        String subject;
        String content;
    }


    public void sendHtml(Message m) {
        System.out.println("SEND "+m);
    }
}
