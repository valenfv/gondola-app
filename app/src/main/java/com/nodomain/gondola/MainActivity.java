package com.nodomain.gondola;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nodomain.gondola.io.ApiAdapter;
import com.nodomain.gondola.model.Producto;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity implements Callback<Producto>, SharedPreferences.OnSharedPreferenceChangeListener{
    ProgressDialog loading;
    TextView tProducto;
    TextView tBriefing;
    TextView tPrecio;
    TextView tImage;
    Producto producto = null;
    /*
    *
    *   MENU FEATURES
    *
    */
    private void startSettings(){
        startActivity(new Intent(this, SettingsActivity.class));
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // Open new activity
            startSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    /*
     *
     *  REST API FEATURE
     *
     */
    private void startQRAct(){
        IntentIntegrator intent = new IntentIntegrator(this);
        intent.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        intent.setPrompt("APUNTE LA CAMARA AL CODIGO DE BARRAS");
        intent.setCameraId(0);
        intent.setOrientationLocked(true);
        intent.setBeepEnabled(true);
        intent.setCaptureActivity(LockedOrientation.class);
        intent.setBarcodeImageEnabled(false);
        intent.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //ProgressDialog.show(this, "Loading", "Wait while loading...");
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelaste el escaneo", Toast.LENGTH_LONG).show();
            } else {
                //Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
                ///////// TEST /////////

                SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                if(SP == null){
                    Snackbar.make(findViewById(R.id.fab), "No se han podido cargar las preferencias", Snackbar.LENGTH_LONG)
                            .setAction("----", null).show();
                }else{
                    String ip = SP.getString("ip", "nulo");
                    String port = SP.getString("port", "nulo");
                    String lista = SP.getString("lista", "nulo");
                    if(ip.equals("nulo") || port.equals("nulo") || lista.equals("nulo")){
                        Snackbar.make(findViewById(R.id.fab), "Configurar datos de conexion primero", Snackbar.LENGTH_LONG)
                                .setAction("----", null).show();
                    }else{
                        ImageView img = (ImageView) findViewById(R.id.imageView);
                        img.setImageResource(R.drawable.loading);
                        // show the loading dialog
                        loading.show();
                        //Snackbar.make(findViewById(R.id.fab), "Requesting with ip: " + ip + " and port: " + port, Snackbar.LENGTH_LONG)
                        //       .setAction("----", null).show();
                        Log.e("preference", "La IP en CALL es: " + ip);
                        Call<Producto> call = ApiAdapter.getApiService(ip, port).getProducto(result.getContents(), lista);
                        call.enqueue(this);
                    }

                }
                /////// END TEST ///////
            }
        }
    }
    @Override
    public void onResponse(Call<Producto> call, Response<Producto> response) {
        if(response.isSuccessful()){
            producto = response.body();

            tProducto = (TextView) findViewById(R.id.textView5);
            tBriefing = (TextView) findViewById(R.id.textView7);
            tPrecio = (TextView) findViewById(R.id.textView9);
            tImage = (TextView) findViewById(R.id.textView);
            //call
            tProducto.setText(producto.getProduct());
            tBriefing.setText(producto.getBriefing());
            tPrecio.setText(producto.getPrecio());
            tImage.setText(producto.getImagen());

            try {
                URL u = new URL(producto.getImagen());
                new SRequest().execute(u);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            loading.dismiss();

        }else{
            Snackbar.make(findViewById(R.id.fab), "Es posible que el producto no exista. Intentelo nuevamente", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            loading.dismiss();
            setDefault();
        }
    }

    @Override
    public void onFailure(Call<Producto> call, Throwable t) {
        Snackbar.make(findViewById(R.id.fab), "Es posible que el producto no exista. Intentelo nuevamente", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        loading.dismiss();

        setDefault();
    }
    /*
     *
     *  SHARED PREFERENCES FEATURE
     *
     */
    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        setupSharedPreferences();
    }
    /*
     *
     *  IMAGE LOADING FEATURE
     *
     */
    private class SRequest extends AsyncTask<URL, Integer, Bitmap> {
        @SuppressLint("WrongThread")
        protected Bitmap doInBackground(URL... urls) {
            Bitmap bm = null;
            try{
                URLConnection conn = urls[0].openConnection();
                conn.connect();
                InputStream is = conn.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                bm = BitmapFactory.decodeStream(bis);
                bis.close();
                is.close();
            }catch(IOException e) {
                Snackbar.make(findViewById(R.id.imageView),"No es por aca XD", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            return bm;
        }
        protected void onProgressUpdate(Integer... progress) {
            // nothing to do here
        }
        protected void onPostExecute(Bitmap result) {
            ImageView iv = (ImageView) findViewById(R.id.imageView);
            iv.setImageBitmap(result);

        }
    }
    public void setDefault(){
        tProducto = (TextView) findViewById(R.id.textView5);
        tBriefing = (TextView) findViewById(R.id.textView7);
        tPrecio = (TextView) findViewById(R.id.textView9);
        tProducto.setText("---");
        tPrecio.setText("---");
        tBriefing.setText("---");
        ImageView img = (ImageView) findViewById(R.id.imageView);
        img.setImageResource(R.drawable.empty);
    }
    /*
     *
     *  ANDROID WORKFLOW
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        tProducto = (TextView) findViewById(R.id.textView5);
        tBriefing = (TextView) findViewById(R.id.textView7);
        tPrecio = (TextView) findViewById(R.id.textView9);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // set image
        ImageView img = (ImageView) findViewById(R.id.imageView);
        img.setImageResource(R.drawable.empty);
        setupSharedPreferences();
        loading = new ProgressDialog(this);
        loading.setTitle("Obteniendo producto");
        loading.setMessage("Espere mientras obtenemos el producto escaneado");
        loading.setCancelable(false);
        ///////////////////
        // get state
        if(savedInstanceState != null){
            tProducto.setText(((Producto) savedInstanceState.getParcelable("producto")).getProduct());
            tBriefing.setText(((Producto) savedInstanceState.getParcelable("producto")).getBriefing());
            tPrecio.setText(((Producto) savedInstanceState.getParcelable("producto")).getPrecio());
            try {
                URL u = new URL(((Producto) savedInstanceState.getParcelable("producto")).getImagen());
                new SRequest().execute(u);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startQRAct();
            }
        });
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if(producto != null)
            savedInstanceState.putSerializable("producto", producto);
        super.onSaveInstanceState(savedInstanceState);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        setupSharedPreferences();
    }
}
