package com.desafiopicpay.services;

import com.desafiopicpay.domain.user.User;
import com.desafiopicpay.dtos.NotificationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NotificationService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${servico.externo.notificacao}")
    private String URL_SERVICO_NOTIFICACAO;

    public void sendNotification(User user, String message) throws Exception {
        String email = user.getEmail();
        NotificationDTO notificationRequest = new NotificationDTO(email, message);

       ResponseEntity<String> notificationResponse = restTemplate.postForEntity(URL_SERVICO_NOTIFICACAO, notificationRequest, String.class);

       if(!(notificationResponse.getStatusCode() == HttpStatus.OK)){
           throw new Exception("Serviço de notificação indisponível");
       }

    }


}
