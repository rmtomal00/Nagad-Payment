package com.tomal.Nagad.Payment.model;

import lombok.*;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class CompleteApiModel {
    private String callBackUrl;
    private String status;
    private String message;
    private boolean err;
}
