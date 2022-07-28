package me.telegram.bot.config;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperConfig {

    private static final ObjectMapper INSTANCE = new ObjectMapper();

    public static ObjectMapper getInstance() {
        return INSTANCE;
    }

}
