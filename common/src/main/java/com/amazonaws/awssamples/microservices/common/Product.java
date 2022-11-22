package com.amazonaws.awssamples.microservices.common;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Product {

    private long id;
    private String name;
    private String review;


}
