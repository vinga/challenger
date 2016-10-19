package com.kameo.challenger.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;


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
        System.out.println("SEND " + m);
    }
}
