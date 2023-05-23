package ru.isu.model.db;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "semd")
@Entity
public class Semd {
    @Id
    private Long code;
    private String name;
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
}
