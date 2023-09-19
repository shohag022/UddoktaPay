package com.rbmjltd.uddoktapay;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UddoktaPay {

    private WebView webView;
    private ProgressDialog progressDialog;
    private PaymentCallback paymentCallback;
    private List<String> expectedMetadataKeys;
    private String SapiKey;
    private String SverifyPaymentUrl;
    private String Sredirect;



    public interface PaymentCallback {
        void onPaymentStatus(String status, String fullName, String email, String amount, String invoiceId,
                             String paymentMethod, String senderNumber, String transactionId,
                             String date, Map<String, String> metadataValues,
                             String chargeAmount, String fee);
    }


    public UddoktaPay(WebView webView, PaymentCallback callback) {
        this.webView = webView;
        this.paymentCallback = callback;
        this.expectedMetadataKeys = expectedMetadataKeys; // Initialize expectedMetadataKeys
        webView.getSettings().setJavaScriptEnabled(true);
        // Set up a WebViewClient to handle page loading events
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                dismissProgressDialog(); // Dismiss dialog after page finished loading
                // Extract invoice_id from the URL
                extractInvoiceId(url);
            }
        });
    }

    public void loadPaymentForm(String apiKey, String fullName, String email, String amount,
                                String checkoutUrl, String verifyPaymentUrl,
                                String redirectUrl, String cancelUrl,
                                Map<String, String> metadataMap) {

        JSONObject requestBodyJson = new JSONObject();
        try {
            requestBodyJson.put("full_name", fullName);
            requestBodyJson.put("email", email);
            requestBodyJson.put("amount", amount);
            requestBodyJson.put("redirect_url", redirectUrl);
            requestBodyJson.put("return_type", "get");
            requestBodyJson.put("cancel_url", cancelUrl);
            JSONObject metadataObject = new JSONObject(metadataMap);
            requestBodyJson.put("metadata", metadataObject);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(requestBodyJson.toString(), JSON);

        Request request = new Request.Builder()
                .url(checkoutUrl)
                .post(requestBody)
                .addHeader("accept", "application/json")
                .addHeader("RT-UDDOKTAPAY-API-KEY", apiKey)
                .addHeader("content-type", "application/json")
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                showToast(webView.getContext(), "Request failed");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        boolean status = jsonObject.optBoolean("status");

                        if (status) {
                            String paymentUrl = jsonObject.optString("payment_url");
                            if (!paymentUrl.isEmpty()) {
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    // Load the payment URL in the WebView
                                    webView.loadUrl(paymentUrl);
                                });
                            } else {
                                showToast(webView.getContext(), "Payment URL is empty");
                            }
                        } else {
                            String message = jsonObject.optString("message");
                            showToast(webView.getContext(), message != null ? message : "Payment initiation failed");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showToast(webView.getContext(), "Error parsing JSON response");
                    }
                } else {
                    showToast(webView.getContext(), "Request failed");
                }
            }
        });

        showProcessDialog();
        SapiKey = apiKey;
        SverifyPaymentUrl = verifyPaymentUrl;
        Sredirect = redirectUrl;
    }


    private void showProcessDialog() {
        // Create and show a ProgressDialog with a processing message
        progressDialog = new ProgressDialog(webView.getContext());
        progressDialog.setMessage("Processing Payment...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        // Dismiss the ProgressDialog if it's currently showing
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void extractInvoiceId(String url) {
        String REDIRECT_URL = Sredirect;
        if (url.startsWith(REDIRECT_URL)) {
            // Check if the URL contains the "invoice_id=" parameter
            if (url.contains("invoice_id=")) {
                // Extract the invoice_id parameter from the URL
                String invoiceId = url.substring(url.indexOf("invoice_id=") + 11); // 11 is the length of "invoice_id="
//                verifyPayment(webView.getContext(), invoiceId);
                // Call the verifyPayment method with the provided API key and verification URL
                verifyPayment(webView.getContext(), SapiKey, SverifyPaymentUrl, invoiceId);
            } else {
                showToast(webView.getContext(), "Invoice ID parameter not found in URL");
            }
        }
    }


    private void verifyPayment(Context context, String apiKey, String verifyUrl, String invoiceId) {
        OkHttpClient client = new OkHttpClient();

        // Construct the JSON request body
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String json = "{\"invoice_id\": \"" + invoiceId + "\"}";
        RequestBody requestBody = RequestBody.create(json, JSON);

        // Create the request
        Request request = new Request.Builder()
                .url(verifyUrl)
                .addHeader("accept", "application/json")
                .addHeader("RT-UDDOKTAPAY-API-KEY", apiKey)
                .addHeader("content-type", "application/json")
                .post(requestBody)
                .build();

        // Execute the request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                showToast(context, "Verification request failed");
                if (paymentCallback != null) {
                    paymentCallback.onPaymentStatus("ERROR", null, null, null,
                            null, null, null, null, null, null,
                            null, null); // Add chargeAmount and fee parameters
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String status = jsonObject.optString("status");

                        if ("COMPLETED".equals(status)) {
                            Map<String, String> metadataValues = extractMetadata(jsonObject); // Extract metadata values
                            String fullName = jsonObject.optString("full_name");
                            String email = jsonObject.optString("email");
                            String amount = jsonObject.optString("amount");
                            String paymentMethod = jsonObject.optString("payment_method");
                            String senderNumber = jsonObject.optString("sender_number");
                            String transactionId = jsonObject.optString("transaction_id");
                            String date = jsonObject.optString("date");
                            String fee = jsonObject.optString("fee");
                            String chargedAmount = jsonObject.optString("charged_amount");
                            showToast(context, "Payment completed");
                            if (paymentCallback != null) {
                                paymentCallback.onPaymentStatus(status, fullName, email, amount,
                                        invoiceId, paymentMethod, senderNumber, transactionId, date, metadataValues,fee,chargedAmount);
                            }
                            Log.d("UddoktaPayDebug", "Extracted Metadata: " + metadataValues.toString());
                        } else if ("PENDING".equals(status)) {
                            Map<String, String> metadataValues = extractMetadata(jsonObject); // Extract metadata values
                            String fullName = jsonObject.optString("full_name");
                            String email = jsonObject.optString("email");
                            String amount = jsonObject.optString("amount");
                            String paymentMethod = jsonObject.optString("payment_method");
                            String senderNumber = jsonObject.optString("sender_number");
                            String transactionId = jsonObject.optString("transaction_id");
                            String date = jsonObject.optString("date");
                            String fee = jsonObject.optString("fee");
                            String chargeAmount = jsonObject.optString("charged_amount");
                            showToast(context, "Payment pending");
                            if (paymentCallback != null) {
                                paymentCallback.onPaymentStatus(status, fullName, email, amount,
                                        invoiceId, paymentMethod, senderNumber, transactionId, date, metadataValues,fee,chargeAmount);
                            }
                            Log.d("UddoktaPayDebug", "Extracted Metadata: " + metadataValues.toString());
                        } else if ("ERROR".equals(status)) {
                            showToast(context, "Payment error");
                            if (paymentCallback != null) {
                                paymentCallback.onPaymentStatus("ERROR", null, null, null,
                                        null, null, null, null, null, null,null, null);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showToast(context, "Error parsing JSON response");
                        if (paymentCallback != null) {
                            paymentCallback.onPaymentStatus("ERROR", null, null, null,
                                    null, null, null, null, null, null,null, null);
                        }
                    }
                } else {
                    showToast(context, "Verification request failed");
                    if (paymentCallback != null) {
                        paymentCallback.onPaymentStatus("ERROR", null, null, null,
                                null, null, null, null, null, null,null, null);
                    }
                }
            }

        });

    }

    private void showToast(Context context, String message) {
        // Show a toast message on the UI thread
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        });
    }

    private Map<String, String> extractMetadata(JSONObject jsonObject) {
        Map<String, String> metadataValues = new HashMap<>();

        JSONObject metadataObject = jsonObject.optJSONObject("metadata");
        Log.d("UddoktaPayDebug", "Raw JSON: " + jsonObject.toString());

        if (metadataObject != null) {
            Log.d("UddoktaPayDebug", "Metadata Object: " + metadataObject.toString());

            Iterator<String> keys = metadataObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = metadataObject.optString(key);
                metadataValues.put(key, value);
            }
        }

        Log.d("UddoktaPayDebug", "Extracted Metadata: " + metadataValues.toString());

        return metadataValues;
    }

}
