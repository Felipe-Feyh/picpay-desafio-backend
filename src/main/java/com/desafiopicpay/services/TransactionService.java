package com.desafiopicpay.services;

import com.desafiopicpay.domain.transaction.Transaction;
import com.desafiopicpay.domain.user.User;
import com.desafiopicpay.dtos.TransactionDTO;
import com.desafiopicpay.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class TransactionService {
    @Value("${autorizador.externo}")
    private String URL_AUTORIZADOR_EXTERNO;

    @Autowired
    private UserService userService;

    @Autowired
    TransactionRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private NotificationService notificationService;

    public Transaction createTransaction(TransactionDTO transaction) throws Exception {
        User sender = this.userService.findUserById(transaction.senderId());
        User receiver = this.userService.findUserById(transaction.receiverId());

        userService.validateTransaction(sender, transaction.value());

        boolean isAuthorized = this.autorizeTransaction(sender, transaction.value());
        if(!isAuthorized) {
            throw new Exception("Transação não autorizada");
        }

        Transaction newTransaction = Transaction.builder()
                .amount(transaction.value())
                .sender(sender)
                .receiver(receiver)
                .timestamp(java.time.LocalDateTime.now())
                .build();

        sender.setBalance(sender.getBalance().subtract(transaction.value()));
        receiver.setBalance(receiver.getBalance().add(transaction.value()));

        this.repository.save(newTransaction);
        this.userService.saveUser(sender);
        this.userService.saveUser(receiver);

        this.notificationService.sendNotification(sender, "Transação realizada com sucesso");
        this.notificationService.sendNotification(receiver, "Você recebeu uma transação de R$" + transaction.value() + " de " + sender.getFirstName() + " " + sender.getLastName());

        return newTransaction;
    }

    public boolean autorizeTransaction(User sender, BigDecimal value) {
        ResponseEntity<Map> authorizationResponse = restTemplate.getForEntity(URL_AUTORIZADOR_EXTERNO, Map.class);

        if(authorizationResponse.getStatusCode().equals(HttpStatus.OK)){
            String message = authorizationResponse.getBody().get("message").toString();
            return "Autorizado".equalsIgnoreCase(message);
        } else return false;
    }
}
