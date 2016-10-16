package com.kameo.challenger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.lang.invoke.SerializedLambda;

@SpringBootApplication
public class ChallengerApplication {

    public static void main(String[] args) {

       new KotlinHelloWorld().main(null,new String[]{});
        new KotlinHelloWorld().test();
    //    System.out.println(new KotlinHelloWorld().toString());

        SpringApplication.run(ChallengerApplication.class, args);
    }
}