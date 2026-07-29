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

public void sendOtp(String to, String otp){

SimpleMailMessage message=new SimpleMailMessage();

message.setTo(to);
message.setSubject("Password Reset OTP - Food Waste Platform");
message.setText("Hello,\n\nYour OTP for password reset is: " + otp + "\n\nThis OTP is valid for 5 minutes.\n\nIf you did not request this, please ignore this email.");

mailSender.send(message);

}

}