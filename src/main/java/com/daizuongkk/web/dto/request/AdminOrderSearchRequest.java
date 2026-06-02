package com.daizuongkk.web.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class AdminOrderSearchRequest {
	private String keyword;
	private String status;
	private Double minTotal;
	private Double maxTotal;
	private LocalDate fromDate;
	private LocalDate toDate;
	private String sortBy;
}
