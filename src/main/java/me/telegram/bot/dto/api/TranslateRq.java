package me.telegram.bot.dto.api;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Builder()
public class TranslateRq {

    private String folderId;

    private String targetLanguageCode;

    private List<String> texts;

    public void putText(String text) {
        texts.add(text);
    }

}
