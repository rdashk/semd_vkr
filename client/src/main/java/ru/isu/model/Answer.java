package ru.isu.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Answer {

    @JsonProperty("chat_id")
    private String chatId;
    @JsonProperty("text_message")
    private String message;
}
