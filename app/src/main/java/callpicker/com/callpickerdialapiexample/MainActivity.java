package callpicker.com.callpickerdialapiexample;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Callpicker Dial API Example *
 * Please review Callpicker API Documentation:
 * https://api.callpicker.com/docs/dial.html
 *
 * Compile Info *
 * Min SKD: API 21, Android 5.0
 * */
public class MainActivity extends AppCompatActivity {

    public final String CALLPICKER_API_BASE_URL = "https://api.callpicker.com";

    //KEEP SECURE THIS INFO
    private final String CALLPICKER_CLIENT_ID = "<Client ID>";
    private final String CALLPICKER_SECRET_ID = "<Client Secret>";
    //KEEP SECURE THIS INFO
    private String api_token="";
    private JSONObject extension_data;

    private TextView lbl_ext_name;
    private EditText edt_phone;
    private Button btn_dial;
    private TextView lbl_json_result;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context this_activity=getBaseContext();
        //Activity Objects
        lbl_ext_name = findViewById(R.id.lbl_ext_name);
        edt_phone = findViewById(R.id.edt_phone);
        btn_dial = findViewById(R.id.btn_dial);
        lbl_json_result = findViewById(R.id.lbl_json_result);

        //Set here extension Data
        //Extension 101

        lbl_ext_name.setText("Extension 101");
        extension_data=new JSONObject();
        try {
            extension_data.put("destination_type","Extension");
            extension_data.put("destination_id",65116);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        btn_dial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone_dial=edt_phone.getText().toString();
                Log.d("_btn_dial", "Phone: "+phone_dial);
                if (phone_dial.length()<=0){
                    Toast toast=Toast.makeText(this_activity,"Coloque un teléfono destino para enlazar la llamada",Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }else if (phone_dial.length()<10){
                    Toast toast=Toast.makeText(this_activity,"El teléfono colocado debe ser de 10 dígitos",Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }

                _requestDial(CALLPICKER_CLIENT_ID, CALLPICKER_SECRET_ID, "calls", phone_dial);
            }
        });

    }

    /*---- Internal Functions ----*/

    /**
     * This function request API Token, if success invoques _makeDial
     * @param client_id Developers client_id
     * @param client_secret Developers client_secret
     * @param scope API Scope: calls
     * @param phone Phone destination
     */
    private void _requestDial(String client_id, String client_secret, String scope, String phone){
        String url=CALLPICKER_API_BASE_URL+"/oauth/token"; //Build Token URL
        Log.d("_requestDial", "Url: "+url);
        RequestQueue queue=Volley.newRequestQueue(this);
        //First Get Token, send a Request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        lbl_json_result.setText(response);
                        try {
                            Log.d("_requestDial", "onResponse: "+response);
                            //Convert Response to Object
                            JSONObject api_response=new JSONObject(response);
                            //Validate if Response has an access_token
                            if (!api_response.has("access_token")){
                                Log.e("_requestDial", "No access_token in JSON Object: "+api_response.toString());
                                lbl_json_result.setText("Error, no Access Token: "+response);
                                return;
                            }
                            //Retrieve an access_token
                            String api_token=api_response.getString("access_token");
                            //Make Dial to phone
                            _makeDial(api_token,phone);
                        } catch (JSONException e) {
                            Log.e("_requestDial", "onErrorResponse: ", e);
                            lbl_json_result.setText("Error: "+e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            //Handle Errors Here
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("_requestDial", "onErrorResponse: ", error);
                try {
                    lbl_json_result.setText("Error: "+(new String(error.networkResponse.data, "UTF-8")));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    lbl_json_result.setText("Error: "+e.getMessage());
                }
            }
        }){
            //Set Form params here
            @Nullable @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //See oAuth Documentation: https://api.callpicker.com/docs/oauth.html#operation/token
                Map<String, String> params = new HashMap<String, String>();
                params.put("grant_type", "client_credentials");
                params.put ("scope", scope);
                params.put ("client_id", client_id);
                params.put ("client_secret", client_secret);
                return params;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    /**
     * This function request a Dial using an API Token and phone destination
     * @param api_token "access_token" from oAuth Api
     * @param phone phone to dial
     */
    private void _makeDial(String api_token, String phone){
        String url=CALLPICKER_API_BASE_URL+"/calls/dial";
        Log.d("_makeDial", "Url: "+url);
        RequestQueue queue=Volley.newRequestQueue(this);
        //Request Dial
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        lbl_json_result.setText(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("_makeDial", "onErrorResponse: ", error);
                try {
                    lbl_json_result.setText("Error: "+(new String(error.networkResponse.data, "UTF-8")));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    lbl_json_result.setText("Error: "+e.getMessage());
                }
            }
        }){
            @Nullable @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //See Dial Documentation: https://api.callpicker.com/docs/dial.html#operation/dial
                Map<String, String> params = new HashMap<String, String>();
                JSONObject second_call=new JSONObject();
                try {
                    //Build JSON destination object
                    second_call.put("destination_type","Phone");
                    second_call.put("destination_id",phone); //phone to Dial
                    params.put("token", api_token);
                    params.put ("datetime", "now"); //dial now or in the future, check api documentation
                    params.put ("first_call", extension_data.toString()); //Set here extension data (this android phone)
                    params.put ("second_call", second_call.toString()); //Set here destination data
                } catch (JSONException e) {
                    e.printStackTrace();
                    lbl_json_result.setText("Error: "+e.getMessage());
                }

                Log.d("_makeDial", "getParams: "+params.toString());

                return params;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    /*---- Internal Class ----*/

}

