package ru.isu.model.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
/*@Table(name = "semd")
@Entity*/
@Document(collection = "semd")
public class Semd {
    @Id
    @JsonProperty("id")
    private String id;//code
    @JsonProperty("name")
    private String name;
    @JsonProperty("date")
    private Date date;

    /**
     * get string date = MM.yyyy
     * @return string date = MM.yyyy
     */
    public String getStringDate() {
        Date date = this.date;
        SimpleDateFormat DateFor = new SimpleDateFormat("MM.yyyy");
        return DateFor.format(date);
    }

    public String getCode() {
        return this.id;
    }
}
