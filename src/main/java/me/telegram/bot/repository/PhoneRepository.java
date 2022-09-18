package me.telegram.bot.repository;

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
public class PhoneRepository {

    private static final PhoneRepository INSTANCE = new PhoneRepository(
            ObjectMapperConfig.getInstance()
    );

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public List<Phone> readPhones() {
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

    public static PhoneRepository getInstance() {
        return INSTANCE;
    }

}
