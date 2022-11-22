package com.amazonaws.awssamples.microservices.common;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Review {

    private long productId;
    private String review;
}
