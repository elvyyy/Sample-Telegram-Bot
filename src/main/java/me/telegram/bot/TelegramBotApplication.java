package me.telegram.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

import java.util.Optional;

public class TelegramBotApplication extends TelegramBot {

    @lombok.Builder
    public TelegramBotApplication(String botToken) {
        super(botToken);
    }

    public void run() {
        this.setUpdatesListener(updates -> {
            updates.forEach(this::process);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void process(Update update) {
        Message message = update.message();
        if (message != null) {
            String text = message.text();
            Optional.ofNullable(text)
                    .ifPresent(commandName -> this.serveCommand(commandName, message.chat().id()));
        }
    }

    private void serveCommand(String commandName, Long chatId) {
        switch (commandName) {
            case "/start": {
                SendMessage response = new SendMessage(chatId, "Вы ввели команду /start");
                this.execute(response);
                break;
            }
            case "/help": {
                SendMessage response = new SendMessage(chatId, "Вы ввели команду /help");
                this.execute(response);
                break;
            }
            default: {
                SendMessage response = new SendMessage(chatId, "Команда не найдена");
                this.execute(response);
                break;
            }
        }
    }

}
