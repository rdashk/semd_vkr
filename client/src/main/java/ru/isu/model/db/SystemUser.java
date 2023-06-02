package ru.isu.model.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
/*@Table(name = "semd")
@Entity*/
@Document(collection = "users")
public class SystemUser {
    @Id
    @JsonProperty("id")
    private String id;//date
    @JsonProperty("user_name")
    private String userName;
    @JsonProperty("chat_id")
    private String chatId;

}
