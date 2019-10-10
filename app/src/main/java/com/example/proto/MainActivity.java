package com.example.proto;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;


import com.google.zxing.Result;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{

    private ZXingScannerView mScannerView;
    private RadioButton conf1;//,con2=findViewById(R.id.RB2),con3=findViewById(R.id.RB3),con4=findViewById(R.id.RB4),con5=findViewById(R.id.RB5),con6=findViewById(R.id.RB6),con7=findViewById(R.id.RB7),con8=findViewById(R.id.RB8),con9=findViewById(R.id.RB9);
    private boolean rb1,rb2,rb3,rb4,rb5,rb6,rb7,rb8,rb9;
    private  Time today = new Time(Time.getCurrentTimezone());
    private TextView TextViewResult;

    private Spinner spinner1;
    private Calendar calendario;
    private String confe = " ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner1 = (Spinner)findViewById(R.id.Conferencias);
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                confe = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        calendario = Calendar.getInstance();
       // TextViewResult=findViewById(R.id.Resultado);
    }

    public void Scann(View view){



        today.setToNow();
        mScannerView=new ZXingScannerView(this);
        setContentView(mScannerView);
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void handleResult(Result result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        try{
            this.POST(this.confe,result.getText());
        }catch (IOException E){
            E.printStackTrace();
        }

        builder.setTitle("Bienvenido");
        builder.setMessage("Hora de llegada: "+today.format("%H:%M:%S") + " " + this.confe);

        setContentView(R.layout.activity_main);

        if (URLUtil.isValidUrl(String.valueOf(result))){
        Intent buscar=new Intent(Intent.ACTION_VIEW, Uri.parse(String.valueOf(result)));
        startActivity(buscar);}
        else{
            AlertDialog alertDialog=builder.create();
            alertDialog.show();
        }
        mScannerView.resumeCameraPreview(this);
    }



    public void POST(String Platica,String Hash)throws IOException{
        MediaType MEDIA_TYPE = MediaType.parse("application/json");
        String url = "https://infinite-oasis-46852.herokuapp.com/registrarAsistencia";

        OkHttpClient client = new OkHttpClient();

        JSONObject postdata = new JSONObject();

        int horaRegistrada = calendario.get(Calendar.HOUR_OF_DAY);
        int minutoRegistrada = calendario.get(Calendar.MINUTE);
        String hora = horaRegistrada + ":" + minutoRegistrada;
        try{
            postdata.put("Platica",Platica);
            postdata.put("Hash",Hash);
            postdata.put("Hora",hora);
        }catch(JSONException e){
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(MEDIA_TYPE,postdata.toString());

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Accept","application/json")
                .header("Content-Type","application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage().toString();
                Log.w("failure response",mMessage);
                //call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String mMessage = response.body().string();
                Log.e("Response",mMessage);
            }
        });
    }

}
