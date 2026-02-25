package com.aicrm.common.page;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serializable;

@Data
public class PageQuery implements Serializable {

    @Min(1)
    private int pageNum = 1;

    @Min(1)
    @Max(200)
    private int pageSize = 20;

    private String sortField;
    private String sortOrder;
}
