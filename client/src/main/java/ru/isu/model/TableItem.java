package ru.isu.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TableItem {
    private String code;
    private String name;
    private String date;

    public TableItem(String[] arr) {
        if (arr.length == 3) {
            this.code = arr[0];
            this.name = arr[1];
            this.date = arr[2];
        }
    }
}
