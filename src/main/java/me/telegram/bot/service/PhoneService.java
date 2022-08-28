package me.telegram.bot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.telegram.bot.config.ObjectMapperConfig;
import me.telegram.bot.model.Phone;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class PhoneService {

    private static final PhoneService INSTANCE = new PhoneService(
            ObjectMapperConfig.getInstance()
    );

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public List<Phone> getPhones() {
        List<Phone> phones = new ArrayList<>();
        try (InputStream phoneStream = getClass().getClassLoader()
                .getResourceAsStream("phones.json")) {
            JsonNode rootNode = objectMapper.readTree(phoneStream);
            if (rootNode.hasNonNull("phones")) {
                JsonNode phonesNode = rootNode.get("phones");
                phones.addAll(List.of(objectMapper.convertValue(phonesNode, Phone[].class)));
            }
        }
        return phones;
    }

    public static PhoneService getInstance() {
        return INSTANCE;
    }

}
