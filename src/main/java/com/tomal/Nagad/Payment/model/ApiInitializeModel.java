package com.tomal.Nagad.Payment.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ApiInitializeModel {
    private String sensitiveData;
    private String signature;
    private String paymentReferenceId;
    private String challenge;
    private String acceptDateTime;
}
