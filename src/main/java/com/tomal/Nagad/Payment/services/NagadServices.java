package com.tomal.Nagad.Payment.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.tomal.Nagad.Payment.CryptoUtility.CryptoCommon;
import com.tomal.Nagad.Payment.model.ApiInitializeModel;
import netscape.javascript.JSObject;
import okhttp3.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NagadServices {
    private final OkHttpClient client = new OkHttpClient();
    private final CryptoCommon common = new CryptoCommon();
    private ObjectMapper mapper = new ObjectMapper();
    private final String publicKeyPath = "/home/rmtomal/IdeaProjects/Nagad-Payment/src/main/java/com/tomal/Nagad/Payment/path/marchent_public.pem";
    private final String privateKeyPath = "/home/rmtomal/IdeaProjects/Nagad-Payment/src/main/java/com/tomal/Nagad/Payment/path/marchent_private.pem";
    private final String BaseUrl = "http://sandbox.mynagad.com:10080/remote-payment-gateway-1.0";
    private String marchentId = "683002007104225";
    private Base64.Encoder encoder = Base64.getEncoder();
    private Base64.Decoder decoder = Base64.getDecoder();


    public String create(Map<String, Object> data) throws Throwable {
        byte[] KPG_DefaultSeed = ("nagad-dfs-service-ltd" + System.currentTimeMillis() + "").getBytes();;
        String random = common.generateRandomString(20, KPG_DefaultSeed);
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String datetime = format.format(date);
        Long orderId = System.currentTimeMillis();
        Map<String, Object> rawData = new HashMap<>();
        rawData.put("merchantId", marchentId);
        rawData.put("orderId", orderId.toString());
        rawData.put("datetime", datetime);
        rawData.put("challenge", random);


        String rawDataToBeEncrypted = mapper.writeValueAsString(rawData);
        byte[] rawEncryptedBytes = common.encrypt(common.getPublic(publicKeyPath), rawDataToBeEncrypted.getBytes("UTF-8"));
        byte[] rawSignatureBytes = common.sign(common.getPrivate(privateKeyPath), rawDataToBeEncrypted.getBytes("UTF-8"));
        String sensitiveData = encoder.encodeToString(rawEncryptedBytes);
        String signature = encoder.encodeToString(rawSignatureBytes);


        Headers headers = new Headers.Builder()
                .add("Content-Type", "application/json")
                .add("X-KM-IP-V4", "192.168.0.1")
                .add("X-KM-Client-Type", "PC_WEB")
                .add("X-KM-Api-Version", "v-0.2.0")
                .build();

        String body = "{\n" +
                "\t\"dateTime\":\"" + datetime + "\",\n" +
                "\t\"sensitiveData\":\"" + sensitiveData + "\",\n" +
                "\t\"signature\":\"" + signature + "\"\n" +
                "}";
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody b = RequestBody.create(body, mediaType);

        Request request = new Request.Builder()
                .url(BaseUrl+"/api/dfs/check-out/initialize/" + marchentId + "/" + orderId.toString())
                .headers(headers)
                .post(b)
                .build();

        Response response = client.newCall(request).execute();
        String responsedata =  response.body().string();
        System.out.println(responsedata);
        ApiInitializeModel apiData = new Gson().fromJson(responsedata, ApiInitializeModel.class);
        byte[] dataDecode = common.decrypt(common.getPrivate(privateKeyPath), decoder.decode(apiData.getSensitiveData()));
        String responseSensitiveData = new String(dataDecode, StandardCharsets.UTF_8);
        ApiInitializeModel initializeModel = new Gson().fromJson(responseSensitiveData, ApiInitializeModel.class);
        System.out.println(responseSensitiveData);
        Boolean verify = common.verifySign(common.getPublic(publicKeyPath), dataDecode , decoder.decode(apiData.getSignature()));
        if (!verify){
            return null;
        }
        rawData.clear();
        data.put("merchantId", marchentId);
        data.put("orderId", orderId);
        data.put("challenge", initializeModel.getChallenge());
        data.put("currencyCode", "050");
        System.out.println(mapper.writeValueAsString(data));

        rawDataToBeEncrypted = mapper.writeValueAsString(data);
        rawEncryptedBytes = common.encrypt(common.getPublic(publicKeyPath), rawDataToBeEncrypted.getBytes("UTF-8"));
        rawSignatureBytes = common.sign(common.getPrivate(privateKeyPath), rawDataToBeEncrypted.getBytes("UTF-8"));
        sensitiveData = encoder.encodeToString(rawEncryptedBytes);
        signature = encoder.encodeToString(rawSignatureBytes);


        String body1 = "{     \"sensitiveData\": \""+sensitiveData+"\",     \"signature\": \""+signature+"\",     \"merchantCallbackURL\": \"http://localhost:8080/payment-status \", " +
                "    \"additionalMerchantInfo\": {      \"productName\":\"shirt\",      \"productCount\":1     } } ";

        RequestBody b1 = RequestBody.create(body1, mediaType);

        Request request1 = new Request.Builder()
                .url(BaseUrl+"//api/dfs/check-out/complete/"+initializeModel.getPaymentReferenceId())
                .post(b1)
                .headers(headers)
                .build();

        Response response1 = client.newCall(request1).execute();
        String datawithlink = response1.body().string();
        System.out.println(datawithlink);
        return datawithlink;
    }

    public String checkStatus(String payId) throws IOException {
        if (payId == null){
            return null;
        }

        Request request = new Request.Builder()
                .url(BaseUrl+"/api/dfs/verify/payment/"+payId)
                .get()
                .build();

         Response response = client.newCall(request).execute();
        return response.body().string();
    }
}
