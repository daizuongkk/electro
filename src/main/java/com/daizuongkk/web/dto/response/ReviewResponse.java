package com.daizuongkk.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long productId;
    private Long userId;
    private String userDisplayName;
    private String message;
    private Integer oneStars;

    private Integer twoStars;
    private Integer threeStars;
    private Integer fourStars;
    private Integer fiveStars;
    private Date createdAt;

}
