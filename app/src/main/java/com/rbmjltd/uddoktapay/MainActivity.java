package com.rbmjltd.uddoktapay;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // Constants for payment
    private static final String API_KEY = "api key hear";
    private static final String CHECKOUT_URL = "https://domain_name_hear/api/checkout-v2";
    private static final String VERIFY_PAYMENT_URL = "https://domain_name_hear/api/verify-payment";
    private static final String REDIRECT_URL = "https://domain_name_hear.com";
    private static final String CANCEL_URL = "https://domain_name_hear.com";


    // Instance variables to store payment information
    private String storedFullName;
    private String storedEmail;
    private String storedAmount;
    private String storedInvoiceId;
    private String storedPaymentMethod;
    private String storedSenderNumber;
    private String storedTransactionId;
    private String storedDate;
    private String storedFee;
    private String storedChargedAmount;

    private String storedMetaKey1;
    private String storedMetaValue1;

    private String storedMetaKey2;
    private String storedMetaValue2;

    private String storedMetaKey3;
    private String storedMetaValue3;

    EditText name,email,amount;
    Button pay;
    LinearLayout mainlay,weblay;
    WebView webView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        amount = findViewById(R.id.amount);
        pay = findViewById(R.id.pay);
        mainlay = findViewById(R.id.mainlay);
        weblay = findViewById(R.id.weblay);


        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paymentDone();
            }
        });



    }//==========OnCreate Bundle End ================================================

    private void paymentDone() {

        mainlay.setVisibility(View.GONE);
        weblay.setVisibility(View.VISIBLE);

        String FULL_NAME = name.getText().toString();
        String EMAIL = email.getText().toString();
        String enteredAmount = amount.getText().toString();

        // Set your metadata values in the map
        Map<String, String> metadataMap = new HashMap<>();
        metadataMap.put("CustomMetaData1", "Meta Value 1");
        metadataMap.put("CustomMetaData2", "Meta Value 2");
        metadataMap.put("CustomMetaData3", "Meta Value 3");

        UddoktaPay.PaymentCallback paymentCallback = new UddoktaPay.PaymentCallback() {
            @Override
            public void onPaymentStatus(String status, String fullName, String email, String amount, String invoiceId,
                                        String paymentMethod, String senderNumber, String transactionId,
                                        String date, Map<String, String> metadataValues, String fee,String chargeAmount) {
                // Callback method triggered when the payment status is received from the payment gateway.
                // It provides information about the payment transaction.
                storedFullName = fullName;
                storedEmail = email;
                storedAmount = amount;
                storedInvoiceId = invoiceId;
                storedPaymentMethod = paymentMethod;
                storedSenderNumber = senderNumber;
                storedTransactionId = transactionId;
                storedDate = date;
                storedFee = fee;
                storedChargedAmount = chargeAmount;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Clear previous metadata values to avoid duplication
                        storedMetaKey1 = null;
                        storedMetaValue1 = null;
                        storedMetaKey2 = null;
                        storedMetaValue2 = null;
                        storedMetaKey3 = null;
                        storedMetaValue3 = null;

                        // Iterate through the metadata map and store the key-value pairs
                        for (Map.Entry<String, String> entry : metadataValues.entrySet()) {
                            String metadataKey = entry.getKey();
                            String metadataValue = entry.getValue();

                            if ("CustomMetaData1".equals(metadataKey)) {
                                storedMetaKey1 = metadataKey;
                                storedMetaValue1 = metadataValue;
                            } else if ("CustomMetaData2".equals(metadataKey)) {
                                storedMetaKey2 = metadataKey;
                                storedMetaValue2 = metadataValue;
                            } else if ("CustomMetaData3".equals(metadataKey)) {
                                storedMetaKey3 = metadataKey;
                                storedMetaValue3 = metadataValue;
                            }
                        }

                        // Update UI based on payment status
                        if ("COMPLETED".equals(status)) {
                            mainlay.setVisibility(View.VISIBLE);
                            weblay.setVisibility(View.GONE);
                        } else if ("PENDING".equals(status)) {
                            mainlay.setVisibility(View.VISIBLE);
                            weblay.setVisibility(View.GONE);
                        } else if ("ERROR".equals(status)) {
                            mainlay.setVisibility(View.VISIBLE);
                            weblay.setVisibility(View.GONE);
                        }
                    }
                });
            }
        };

        UddoktaPay uddoktapay = new UddoktaPay(webView, paymentCallback);
        uddoktapay.loadPaymentForm(API_KEY, FULL_NAME, EMAIL, enteredAmount, CHECKOUT_URL, VERIFY_PAYMENT_URL, REDIRECT_URL, CANCEL_URL, metadataMap);

    }


}