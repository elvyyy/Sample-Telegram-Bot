package me.telegram.bot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.model.SuccessfulPayment;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.telegram.bot.config.ObjectMapperConfig;
import me.telegram.bot.model.Purchase;

import java.io.File;
import java.io.FileOutputStream;
import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class OrderService {

    private static final OrderService INSTANCE = new OrderService(PhoneService.getInstance(),
            ObjectMapperConfig.getInstance());

    private final PhoneService phoneService;

    private final ObjectMapper objectMapper;

    public void createPurchase(SuccessfulPayment payment, Long chatId) {
        String phoneId = payment.invoicePayload();
        phoneService.getPhones().stream()
                .filter(phone -> phone.getId().equals(phoneId))
                .findAny()
                .map(phone -> Purchase.builder()
                        .id(UUID.randomUUID().toString())
                        .currency(payment.currency())
                        .chatId(chatId.toString())
                        .orderInfo(payment.orderInfo())
                        .phone(phone)
                        .purchaseDate(Instant.now())
                        .build())
                .ifPresent(purchase -> {
                    String orderPath = String.format("orders/%s.json", purchase.getId());
                    createPurchaseFile(orderPath, purchase);
                });
    }

    @SneakyThrows
    private void createPurchaseFile(String path, Purchase purchase) {
        File file = new File(path);
        if (!file.exists()) {
            file.getParentFile().mkdir();
            file.createNewFile();
        }
        try (FileOutputStream stream = new FileOutputStream(file)) {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(stream, purchase);
        }
    }

    public static OrderService getInstance() {
        return INSTANCE;
    }

}
