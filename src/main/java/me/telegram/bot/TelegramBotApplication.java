package me.telegram.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.PreCheckoutQuery;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.LabeledPrice;
import com.pengrad.telegrambot.request.AnswerPreCheckoutQuery;
import com.pengrad.telegrambot.request.SendInvoice;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

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
                SendInvoice sendInvoice = new SendInvoice(chatId, "title", "desc", "my_payload",
                        "381764678:TEST:39440", "my_start_param", "RUB", new LabeledPrice("label", 200 * 100))
                        .needPhoneNumber(false)
                        .needShippingAddress(false)
                        .needPhoneNumber(true)
                        .startParameter("get_access")
                        .isFlexible(false)
                        .maxTipAmount(5000 * 100)
                        .suggestedTipAmounts(new Integer[] {5 * 100, 10 * 100, 25 * 100, 50 * 100})
                        .replyMarkup(new InlineKeyboardMarkup(
                                new InlineKeyboardButton("just pay").pay(),
                                new InlineKeyboardButton("google it").url("www.google.com"))
                        );
                SendResponse response = execute(sendInvoice);
                System.out.println(response.toString());
                break;
            }
        }
    }

}
