package com.daizuongkk.web.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class AdminReviewSearchRequest {
    private String keyword;
    private Long productId;
    private Integer score;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String sortBy;
}
