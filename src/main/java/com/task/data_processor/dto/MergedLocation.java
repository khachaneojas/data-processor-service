package com.task.data_processor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MergedLocation {

    private String id;
    private double latitude;
    private double longitude;
    private String type;
    private double rating;
    private int reviews;

}

