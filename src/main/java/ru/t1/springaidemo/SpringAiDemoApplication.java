package ru.t1.springaidemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SpringAiDemoApplication {

    static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SpringAiDemoApplication.class, args);
        //ChatClient chatClient = context.getBean(ChatClient.class);
        //System.out.println(chatClient.prompt().user("Дай первую строчку Bohemian Rhapsody").call().content());
    }

}
