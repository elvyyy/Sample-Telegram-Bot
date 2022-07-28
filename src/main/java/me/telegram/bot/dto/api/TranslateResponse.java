package me.telegram.bot.dto.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranslateResponse {

    private List<Translation> translations;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Translation {

        private String text;

        private String detectedLanguageCode;
    }

}
