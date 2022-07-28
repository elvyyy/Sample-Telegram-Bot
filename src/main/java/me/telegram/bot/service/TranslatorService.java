package me.telegram.bot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.telegram.bot.config.ObjectMapperConfig;
import me.telegram.bot.dto.api.TranslateResponse;
import me.telegram.bot.dto.api.TranslateRq;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class TranslatorService {
    public static final TranslatorService INSTANCE = new TranslatorService();

    private static final String API_URL = "https://translate.api.cloud.yandex.net/translate/v2/translate";

    private static final String IAM_TOKEN = "t1.9euelZqXisrKlJKcj5mOyYyMnM-VyO3rnpWakomJzpqMyI2WmpaNx8mVnovl9PdgYxBp-e8WK3Po3fT3IBIOafnvFitz6A.WGiBKVmnsiq6-lKWkpINZvNOx6Ldqr-_z6jJ2zqtHe8bx5yF2WTWVwaz1rladkCwvarrB6RMbYWxxxzlFHliCA";

    private static final String FOLDER_ID = "b1gmh94c54ddqna83j1u";

    private final ObjectMapper objectMapper = ObjectMapperConfig.getInstance();

    public static TranslatorService getInstance() {
        return INSTANCE;
    }

    @SneakyThrows
    public String translate(String text, String translateTo) {
        URL url = new URL(API_URL);
        URLConnection urlConnection = url.openConnection();
        HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setRequestProperty("Authorization", "Bearer " + IAM_TOKEN);
        httpURLConnection.setDoOutput(true);
        DataOutputStream dataOutputStream = new DataOutputStream(urlConnection.getOutputStream());
        TranslateRq rq = TranslateRq.builder()
                .folderId(FOLDER_ID)
                .targetLanguageCode(translateTo)
                .texts(List.of(text))
                .build();
        dataOutputStream.write(objectMapper.writeValueAsBytes(rq));

        TranslateResponse response = objectMapper.readValue(urlConnection.getInputStream(), TranslateResponse.class);
        return response.getTranslations()
                .get(0)
                .getText();
    }

    public static void main(String[] args) {
        String translate = TranslatorService.getInstance()
                .translate("Привет мир", "en");
        System.out.println(translate);
    }

}
