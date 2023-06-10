package ru.isu.model.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
/*@Table(name = "semd")
@Entity*/
@Document(collection = "semd_package")
public class SemdPackage {
    @Id
    @JsonProperty("id")
    private String id;//code
    @JsonProperty("name")
    private String name;
    @JsonProperty("date")
    private Date date;
    @JsonProperty("files")
    private List<OnePackageFile> files;

    /**
     * get string date = MM.yyyy
     * @return string date = MM.yyyy
     */
    public String getFormatDate() {
        // Formatter for the input date
        DateTimeFormatter inputFormat =
                DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy");

        // The parsed date
        ZonedDateTime parsed = ZonedDateTime.parse(getDate().toString(), inputFormat);

        // The output format
        final DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        return outputFormat.format(parsed);
    }

    public String getCode() {
        return this.id;
    }
}
