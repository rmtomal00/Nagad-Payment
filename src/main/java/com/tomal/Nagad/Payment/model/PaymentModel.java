package com.tomal.Nagad.Payment.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

@Builder
@AllArgsConstructor
@Getter
public class PaymentModel {

    private String orderId;
    private String amount;
    private String orderData;

}
