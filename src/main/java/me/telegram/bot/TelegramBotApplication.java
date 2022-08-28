package me.telegram.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.PreCheckoutQuery;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.AnswerPreCheckoutQuery;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import me.telegram.bot.service.PhoneService;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TelegramBotApplication extends TelegramBot {

    private final ExecutorService executorService = Executors.newFixedThreadPool(8);

    @lombok.Builder
    public TelegramBotApplication(String botToken) {
        super(botToken);
    }

    public void run() {
        this.setUpdatesListener(updates -> {
            updates.stream()
                    .<Runnable>map(update -> () -> process(update))
                    .forEach(executorService::submit);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        }, exception -> {
            System.out.println(exception.response().description());
        });
    }

    private void process(Update update) {
        Message message = update.message();
        if (message != null) {
            String text = message.text();
            Optional.ofNullable(text)
                    .ifPresent(commandName -> this.serveCommand(commandName, message.chat().id()));
        }
        PreCheckoutQuery preCheckoutQuery = update.preCheckoutQuery();
        if (preCheckoutQuery != null) {
            AnswerPreCheckoutQuery answerPreCheckoutQuery = new AnswerPreCheckoutQuery(preCheckoutQuery.id());
            execute(answerPreCheckoutQuery);
        }
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
                PhoneService.getInstance().getPhones().stream()
                        .map(phone -> new SendPhoto(chatId, phone.getPhoto())
                                .caption(String.format("%s - %s", phone.getName(), phone.getDescription())))
                        .forEach(this::execute);
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
