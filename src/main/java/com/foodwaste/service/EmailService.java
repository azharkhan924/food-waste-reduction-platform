package com.foodwaste.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

@Autowired
private JavaMailSender mailSender;

public void sendMail(String to){

SimpleMailMessage message=new SimpleMailMessage();

message.setTo(to);
message.setSubject("Testing Spring Boot Mail");
message.setText("Hello!\n\nThis email was sent from Food Waste Platform.");

mailSender.send(message);

}

}