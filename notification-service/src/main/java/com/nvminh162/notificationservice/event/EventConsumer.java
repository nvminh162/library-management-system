package com.nvminh162.notificationservice.event;

import org.apache.kafka.common.errors.RetriableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.nvminh162.commonservice.service.EmailService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EventConsumer {

    @Autowired
    private EmailService emailService;
    
    @RetryableTopic(
        attempts = "4", // create 3 topic retries + 1 topic dlt
        backOff = @BackOff(
            delay = 1000, // delay cách nhau 1s mỗi lần retries
            multiplier = 2 // delay theo cấp số nhân (1: 1s. 2: 2s, 3: 4s ...)
        ),
        autoCreateTopics = "true", // khi reties tự tạo topic và đặt tên không cần handle name
                   // DltStrategy.NO_DLT, // vượt quá số lần retries => không message tới dlt
                   // DltStrategy.ALWAYS_RETRY_ON_ERROR, // vượt quá số lần retries => gửi message tới dlt (nếu dlt fail thì vẫn reties tới consumer)
        dltStrategy = DltStrategy.FAIL_ON_ERROR, // vượt quá số lần retries => gửi message tới dlt (nếu dlt fail thì dừng không retries tới consumer) // => recommend
        include = {
            RetriableException.class,
            RuntimeException.class
        }
    )
    @KafkaListener(topics = "test", containerFactory = "kafkaListenerContainerFactory")
     public void listen(String message){
        log.info("(i) >>>>>>>>>> Received message: {}" , message);
        // demo processing message to fail emulator
        // throw new RuntimeException("(x) >>>>>>>>>> Error test");
    }

    // Khi có event tới message thì func sẽ work
    @DltHandler
    void processDltMessage(@Payload String message) {
        log.info("(i) >>>>>>>>>> DLT receive message: {}", message);
        // handle monitoring: log count error, email to other ...
    }

    @RetryableTopic(
        attempts = "4", // create 3 topic retries + 1 topic dlt
        backOff = @BackOff(
            delay = 1000, // delay cách nhau 1s mỗi lần retries
            multiplier = 2 // delay theo cấp số nhân (1: 1s. 2: 2s, 3: 4s ...)
        ),
        autoCreateTopics = "true", // khi reties tự tạo topic và đặt tên không cần handle name
                   // DltStrategy.NO_DLT, // vượt quá số lần retries => không message tới dlt
                   // DltStrategy.ALWAYS_RETRY_ON_ERROR, // vượt quá số lần retries => gửi message tới dlt (nếu dlt fail thì vẫn reties tới consumer)
        dltStrategy = DltStrategy.FAIL_ON_ERROR, // vượt quá số lần retries => gửi message tới dlt (nếu dlt fail thì dừng không retries tới consumer) // => recommend
        include = {
            RetriableException.class,
            RuntimeException.class
        }
    )
    @KafkaListener(topics = "testEmail",containerFactory = "kafkaListenerContainerFactory")
    public void testEmail(String message){
        log.info("Received message: " +message);

        String template = "<div>\n" +
                "    <h1>Welcome, %s!</h1>\n" +
                "    <p>Thank you for joining us. We're excited to have you on board.</p>\n" +
                "    <p>Your username is: <strong>%s</strong></p>\n" +
                "</div>";
        String filledTemplate = String.format(template,"Nguyen Van Minh", message);

        emailService.sendEmail(message,"Thanks for buy my course",filledTemplate,true,null);
    }
}
