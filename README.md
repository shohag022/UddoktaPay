# UddoktaPay SDK Integration for Android
This document provides instructions for integrating the UddoktaPay Android SDK into your project. 

# Getting Started
> 
How to use ?

To get a Git project into your build:

Step 1. Add the JitPack repository to your build file 

``` build.gradle

allprojects {
		repositories {
			
			maven { url 'https://jitpack.io' }
		}
	}
 ```
> Step 2. Add the dependency to your `build.gradle`:
``` gradle
dependencies {

        implementation 'com.github.shohag022:UddoktaPay:1.0'

	}
```
> Step 3. Add the okhttp3 dependency to your `build.gradle`:
``` gradle
dependencies {

        implementation 'com.squareup.okhttp3:okhttp:5.0.0-alpha.2'

	}
```
# Constants for Payment
Add the following code inside your `AppCompatActivity`:
``` gradle

private static final String API_KEY = "982d381360a69d419689740d9f2e26ce36fb7a50";    
private static final String CHECKOUT_URL = "https://sandbox.uddoktapay.com/api/checkout-v2";
private static final String VERIFY_PAYMENT_URL = "https://sandbox.uddoktapay.com/api/verify-payment";
private static final String REDIRECT_URL = "https://uddoktapay.com";
private static final String CANCEL_URL = "https://uddoktapay.com";
```  
# Instance Variables
Add the following code inside your `AppCompatActivity`:
``` gradle

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
```
# Payment Process
Note: Use this code when the payment button is clicked:
``` gradle
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
                    // Handle payment completed case
                } else if ("PENDING".equals(status)) {
                    // Handle payment pending case
                } else if ("ERROR".equals(status)) {
                    // Handle payment error case
                }
            }
        });
    }
};

UddoktaPay uddoktapay = new UddoktaPay(UddoktaPayWebView, paymentCallback);
uddoktapay.loadPaymentForm(API_KEY, FULL_NAME, EMAIL, enteredAmount, CHECKOUT_URL, VERIFY_PAYMENT_URL, REDIRECT_URL, CANCEL_URL, metadataMap);
```

# Authors
<a href="https://www.facebook.com/M220719" rel="nofollow">Shohag Hossain (Monir)</a>

