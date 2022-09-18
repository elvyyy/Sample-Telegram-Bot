package me.telegram.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.PreCheckoutQuery;
import com.pengrad.telegrambot.model.SuccessfulPayment;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.LabeledPrice;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.AnswerPreCheckoutQuery;
import com.pengrad.telegrambot.request.CreateInvoiceLink;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.StringResponse;
import me.telegram.bot.service.OrderService;
import me.telegram.bot.service.PhoneService;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TelegramBotApplication extends TelegramBot {

    private final ExecutorService executorService;

    private final PhoneService phoneService;

    private final OrderService orderService;

    private final String providerToken;

    @lombok.Builder
    public TelegramBotApplication(String botToken, String providerToken) {
        super(botToken);
        this.providerToken = Optional.ofNullable(providerToken).orElse("");
        this.executorService = Executors.newFixedThreadPool(8);
        this.phoneService = PhoneService.getInstance();
        this.orderService = OrderService.getInstance();
    }

    public void run() {
        this.setUpdatesListener(updates -> {
            updates.stream()
                    .<Runnable>map(update -> () -> process(update))
                    .forEach(executorService::submit);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        }, exception -> System.out.println(exception.response().description()));
    }

    private void process(Update update) {
        Message message = update.message();
        if (message != null) {
            Optional.ofNullable(message.text())
                    .ifPresent(commandName -> this.serveCommand(commandName, message.chat().id()));
            Optional.ofNullable(message.successfulPayment())
                    .ifPresent(payment -> servePayment(payment, message.chat().id()));
        } else if (update.preCheckoutQuery() != null) {
            PreCheckoutQuery preCheckoutQuery = update.preCheckoutQuery();
            execute(new AnswerPreCheckoutQuery(preCheckoutQuery.id()));
        }
    }

    private void servePayment(SuccessfulPayment payment, Long id) {
        orderService.createPurchase(payment, id);
    }

    private void serveCommand(String commandName, Long chatId) {
        switch (commandName) {
            case "/start": {
                SendMessage response = new SendMessage(chatId,
                        "Список команд:\n/menu - Главное меню\n/start - Начало работы");
                this.execute(response);
                break;
            }
            case "/menu": {
                SendMessage response = new SendMessage(chatId, "Меню")
                        .replyMarkup(new ReplyKeyboardMarkup(new String[][] {
                                {"Товары", "Отзывы"},
                                {"Поддержка"}
                        }).resizeKeyboard(true));
                this.execute(response);
                break;
            }
            case "Товары": {
                phoneService.getPhones()
                        .forEach(phone -> {
                            CreateInvoiceLink link = new CreateInvoiceLink(phone.getName(), phone.getDescription(), phone.getId(),
                                    providerToken, "RUB",
                                    new LabeledPrice("Цена", phone.getPrice().multiply(BigDecimal.valueOf(100L)).intValue()))
                                    .needShippingAddress(true)
                                    .photoUrl(phone.getPhoto())
                                    .needName(true)
                                    .needPhoneNumber(true);
                            StringResponse response = execute(link);
                            SendPhoto sendPhoto = new SendPhoto(chatId, phone.getPhoto())
                                    .caption(String.format("%s - %s", phone.getName(), phone.getDescription()))
                                    .replyMarkup(new InlineKeyboardMarkup(
                                            new InlineKeyboardButton("Оплатить").url(response.result())
                                    ));
                            execute(sendPhoto);
                        });
                break;
            }
            default: {
                SendMessage response = new SendMessage(chatId, "Команда не распознана");
                this.execute(response);
                break;
            }
        }
    }

}
