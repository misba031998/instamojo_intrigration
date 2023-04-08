package com.example.misbatestproject.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.misbatestproject.R;
import com.example.misbatestproject.databinding.ActivityMainBinding;
import com.instamojo.android.Instamojo;
import com.instamojo.android.activities.PaymentDetailsActivity;
import com.instamojo.android.helpers.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements Instamojo.InstamojoPaymentCallback {
    private ActivityMainBinding binding;
    private Context context;
    private ProgressDialog dialog;
    private static final String TAG = PaymentDetailsActivity.class.getSimpleName();

    private String currentEnv = null;
    private String accessToken = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        Instamojo.getInstance().initialize(this, Instamojo.Environment.PRODUCTION);
        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setMessage("please wait...");
        dialog.setCancelable(false);

        context = this;
        clickMethod();
        binding.getRoot();
    }

    private void clickMethod() {


        binding.instamojoPayment.setOnClickListener(m -> {
            tokenCreate();
        });
    }

    private void tokenCreate() {
        StringRequest str = new StringRequest(com.android.volley.Request.Method.POST, "https://api.instamojo.com/oauth2/token/", new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("LOG_VOLLEY", response);
                try {
                    JSONObject responseObject = new JSONObject(response);
                    accessToken = responseObject.getString("access_token");
                    orderPayment(responseObject.getString("access_token"));
                    //Payment(accessToken);
                } catch (JSONException e) {
                }

            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                // Toast.makeText(context, "Failed to Send Message", Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("grant_type", "client_credentials");
                map.put("client_id", "Your clint id");
                map.put("client_secret", "your client secret");
                System.out.println(map.toString());
                return map;
            }
        };

        str.setRetryPolicy(new DefaultRetryPolicy(60000, 2, 0));
        Volley.newRequestQueue(context).add(str);
    }

    private void orderPayment(String accessToken) {
        StringRequest str = new StringRequest(com.android.volley.Request.Method.POST, "https://api.instamojo.com/v2/payment_requests/", new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("LOG_VOLLEY  mm", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    CreateOrderId(jsonObject.optString("id"));
                } catch (Exception e) {
                }


            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(" mm", error.toString());
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                // Toast.makeText(context, "Failed to Send Message", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + accessToken);
                System.out.println("From Header:----" + params.toString());
                return params;
            }

            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> map = new HashMap<>();
                map.put("purpose", "TESTING");
                map.put("amount", "10.00");
                map.put("buyer_name", "Prodhan");
                map.put("email", "admin@gmail.com");
                map.put("phone", "8145505281");
                map.put("redirect_url", "https://test.instamojo.com/integrations/android/redirect/");
                map.put("webhook", "");
                map.put("allow_repeated_payments", "False");
                System.out.println(map.toString());
                return map;
            }
        };

        str.setRetryPolicy(new DefaultRetryPolicy(60000, 2, 0));
        Volley.newRequestQueue(context).add(str);
    }

    private void CreateOrderId(String id) {
        StringRequest str = new StringRequest(com.android.volley.Request.Method.POST, "https://api.instamojo.com/v2/gateway/orders/payment-request/", new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("LOG_VOLLEY  mm", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    initiateSDKPayment(jsonObject.optString("order_id"));
                } catch (Exception e) {
                }


            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(" mm", error.toString());
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                // Toast.makeText(context, "Failed to Send Message", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + accessToken);
                System.out.println("From Header:----" + params.toString());
                return params;
            }

            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> map = new HashMap<>();
                map.put("id", id);
                System.out.println(map.toString());
                return map;
            }
        };

        str.setRetryPolicy(new DefaultRetryPolicy(60000, 2, 0));
        Volley.newRequestQueue(context).add(str);
    }


    private void initiateSDKPayment(String orderID) {
        Log.e("RRRR", orderID.toString());
        Instamojo.getInstance().initiatePayment(this, orderID, MainActivity.this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE && data != null) {
            String orderID = data.getStringExtra(Constants.ORDER_ID);
            String transactionID = data.getStringExtra(Constants.TRANSACTION_ID);
            String paymentID = data.getStringExtra(Constants.PAYMENT_ID);

            // Check transactionID, orderID, and orderID for null before using them to check the Payment status.
            if (transactionID != null || paymentID != null) {
                //mk checkPaymentStatus(transactionID, orderID);
            } else {
                showToast("Oops!! Payment was cancelled");
            }
        }
    }

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public void onInstamojoPaymentComplete(String s, String s1, String s2, String s3) {
        Log.e("RESPONSE", s + s1 + s2 + s3);
        Log.d(TAG, "Payment complete. Order ID: " + s + ", Transaction ID: " + s1
                + ", Payment ID:" + s2 + ", Status: " + s3);
    }

    @Override
    public void onPaymentCancelled() {
        Log.d(TAG, "Payment cancelled");
        Log.e("MISBA", "Payment cancelled");
    }

    @Override
    public void onInitiatePaymentFailure(String s) {
        Log.d(TAG, "Initiate payment failed");
        Log.e("LAST", "Initiate payment failed" + s);
        showToast("Initiating payment failed. Error: " + s);
    }
}