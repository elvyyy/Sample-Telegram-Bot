package me.telegram.bot;

public class Main {

    private static final String BOT_TOKEN = "<your bot token>";

    public static void main(String[] args) {
        TelegramBotApplication application = TelegramBotApplication.builder()
                .botToken(BOT_TOKEN)
                .build();

        application.run();
    }

}