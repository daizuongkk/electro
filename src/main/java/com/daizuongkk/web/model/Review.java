package com.daizuongkk.web.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;


@Getter
@Setter
@Builder

public class Review {
    private Long id;
    private Long productId;
    private Long userId;
    private String message;
    private Integer score;
    private Date createdAt;
}
