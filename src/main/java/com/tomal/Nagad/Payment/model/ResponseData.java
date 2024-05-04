package com.tomal.Nagad.Payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class ResponseData {
    private String message;
    private int code;
    private Object data;
    private boolean err = true;
}
