package com.tomal.Nagad.Payment.controller;
import com.google.gson.Gson;
import com.tomal.Nagad.Payment.model.CompleteApiModel;
import com.tomal.Nagad.Payment.model.PaymentModel;
import com.tomal.Nagad.Payment.model.ResponseData;
import com.tomal.Nagad.Payment.services.NagadServices;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class UsersController {
    private final NagadServices services = new NagadServices();

    @GetMapping("/")
    public ResponseEntity<String>test(){
        return ResponseEntity.ok("Work");
    }

    @PostMapping("/create-link")
    public ResponseEntity<Object> create_link(@RequestBody PaymentModel model){
        String amount = model.getAmount();
        String order_data = model.getOrderData();
        CompleteApiModel model1 = new CompleteApiModel(null, null, null, true);
        if (amount == null || amount.isEmpty() || amount.isBlank()
        || order_data == null || order_data.isEmpty() || order_data.isBlank()){
            model1.setMessage("You need to send amount parameter with string value and send order ID");
            model1.setStatus("Fail");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(model1);
        }
        try{
            Map<String, Object> map = new HashMap<>();
            String response;
            map.put("amount", amount.trim());
            try{
                response = services.create(map);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            if (response == null){
                return ResponseEntity.status(493).body(new CompleteApiModel(null, "Failed", "Server can't create payment link", true));
            }
            CompleteApiModel model2 = new Gson().fromJson(response, CompleteApiModel.class);
            if (model2.getMessage() == null){
                model2.setMessage("Successfully Link created");
                model2.setErr(false);
            }else {
                model2.setErr(false);
            }
            return ResponseEntity.ok(model2);
        }catch (Exception er){
            System.out.println(er.getMessage());
            Map<String, Object> e = new HashMap<>();
            e.put("message", "Server Internal error");
            e.put("code", 500);
            e.put("err", true);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e);
        }
    }

    @GetMapping("/payment-status")
    public ResponseEntity<Object> checkPaymentStatus(@RequestParam String payment_ref_id) throws IOException {

        if (payment_ref_id == null || payment_ref_id.isBlank() || payment_ref_id.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseData("You don't provide payId", 400, null, true));
        }
        String s = String.format(services.checkStatus(payment_ref_id));

        return ResponseEntity.status(HttpStatus.OK).body(s);
    }
}
