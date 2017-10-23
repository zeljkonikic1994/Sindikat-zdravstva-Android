package www.a2adesign.net.sindikatzdravstva.activity;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import www.a2adesign.net.sindikatzdravstva.R;

public class PostActivity extends AppCompatActivity {
    boolean connection = false;
    TextView title;
    WebView content;

    ProgressDialog progressDialog;
    Gson gson;
    Map<String, Object> mapPost;
    Map<String, Object> mapTitle;
    Map<String, Object> mapContent;
    String picturePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        connection = haveNetworkConnection();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        //sklanja title bar ako je u horizontalnom
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.action_bar_id);
            linearLayout.setVisibility(LinearLayout.GONE);
        }

        final String id = getIntent().getExtras().getString("id");

        title = (TextView) findViewById(R.id.title);
        content = (WebView)findViewById(R.id.content);
//        content.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        content.getSettings().setJavaScriptEnabled(true);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            content.setWebViewClient(new WebViewClient() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    String url = request.getUrl().toString();
                    Log.d("URL",url);
                    if (url.endsWith(".pdf")) {
                        if(connection){
                            Uri uri = Uri.parse(url);
                            Intent intent= new Intent(Intent.ACTION_VIEW, uri);
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(intent);
                            return true;
                        }else{
                            Toast.makeText(PostActivity.this, "Nema internet konekcije.", Toast.LENGTH_LONG).show();
                            return true;
                        }
                    }
                    return false;

                }
            });
        } else {
            content.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Log.d("URLBRE", url);
                    if (url.endsWith(".pdf")) {
                        if(connection){
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));


                            return true;
                        }else{
                            Toast.makeText(PostActivity.this, "Nema internet konekcije.", Toast.LENGTH_LONG).show();
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
        progressDialog = new ProgressDialog(PostActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        if(connection){
            String url = "http://zdravko.org.rs/wp-json/wp/v2/posts/"+id+"?fields=title,content,featured_image_src";

            StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String s) {
                    gson = new Gson();
                    mapPost = (Map<String, Object>) gson.fromJson(s, Map.class);
                    mapTitle = (Map<String, Object>) mapPost.get("title");
                    mapContent = (Map<String, Object>) mapPost.get("content");
                    picturePath = (String) mapPost.get("featured_image_src");

                    //da bi se sredili eventualni Unicode znaci
                    if(mapTitle.get("rendered").toString()!=null) {
                        String naslovPosta = srediUnicode(mapTitle.get("rendered").toString());

                        title.setText(naslovPosta);
                    }
                    String html = mapContent.get("rendered").toString();
                    String htmlSredjen = srediHTML(html);

                    content.loadDataWithBaseURL(null, htmlSredjen,
                            "text/html","UTF-8", null);
                    if(progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    progressDialog.dismiss();
                    Toast.makeText(PostActivity.this, "Greška prilikom komunikacije sa serverom. Pokušajte ponovo.", Toast.LENGTH_LONG).show();
                }
            });

            request.setRetryPolicy(new DefaultRetryPolicy(
                    10000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            RequestQueue rQueue = Volley.newRequestQueue(PostActivity.this);
            rQueue.add(request);

        }else{
            String naslovPosta = getIntent().getStringExtra("title");
            String naslov = srediUnicode(naslovPosta);
            String imgPath = getIntent().getStringExtra("picturePath");
            picturePath = imgPath;
            String contentPosta = srediHTML(getIntent().getStringExtra("content"));
            title.setText(naslov);
            content.loadDataWithBaseURL(null, contentPosta,
                    "text/html","UTF-8", null);
            if(progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    private String srediUnicode(String rendered) {
        String novi = rendered;
        if(rendered.contains("&#8211;")) {
            novi = rendered.replace("&#8211;", "-");
        }
        return novi;
    }

    public String srediHTML(String html) {
        boolean zagrada = false;
        boolean link=false;
        String zagrade = "";
        String htmlSredjen="";
        String linkIzZagrada="";
        List<String> linkovi = new ArrayList<>();

        if(!picturePath.endsWith("default.png")) {
//            String featuredSlika = "<center><img src=\"" + dPicturePath + "\"></center>\n";
            linkovi.add(picturePath);
        }
//        int index = -1;
        //za slucaj da je picturePath default slika

        for(int i = 0; i < html.length(); i++){
            if(html.charAt(i)=='['){
                zagrada = true;
                continue;
            }
            if(html.charAt(i)==']'){
                zagrada=false;
                continue;
            }
            if(zagrada) {
                zagrade+=html.charAt(i);
                continue;
            }
            else {
                htmlSredjen += html.charAt(i);
            }
        }

        if(htmlSredjen.contains("href=\"/wp-content/")){
            htmlSredjen = htmlSredjen.replace("href=\"/wp-content/","href=\"http://zdravko.org.rs/wp-content/");
        }

        if(zagrade.contains("http://")){
            for (int i = 0; i < zagrade.length();i++){
                if(i < zagrade.length()-4 && zagrade.charAt(i)=='h' && zagrade.charAt(i+1)=='t' && zagrade.charAt(i+2)=='t' && zagrade.charAt(i+3)=='p'){
                    link = true;
                }
                if(zagrade.charAt(i)=='&'){
                    if(link) {
//                        linkovi.add(linkIzZagrada);
                        linkIzZagrada="";
                        link = false;
                    }
                }
                if(link){
                    linkIzZagrada+=zagrade.charAt(i);
                }
            }
        }

        if(linkovi.size()>0){
            String linkoviSviSredjeni = "";
            for(int i = 0; i < linkovi.size(); i++){
                String linkSlike = "<center><img src=\"" + linkovi.get(i)+"\"></center>";
                linkoviSviSredjeni+=linkSlike;
                linkoviSviSredjeni+="\n";
            }
            htmlSredjen = new StringBuilder(htmlSredjen).insert(0, linkoviSviSredjeni).toString();
        }
        if(htmlSredjen.contains(" target=\"_blank\" rel=\"noopener noreferrer\"")){
            htmlSredjen = htmlSredjen.replace(" target=\"_blank\" rel=\"noopener noreferrer\"","");
        }
        String deo = htmlSredjen.substring(htmlSredjen.length()-150,htmlSredjen.length()-1);
        Log.d("HTMLSredjen",deo);
        return htmlSredjen;
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.finish();
    }

    public void onConfigurationChanged(Configuration newConfig)
    {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.action_bar_id);
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            linearLayout.setVisibility(View.GONE);
        }else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            linearLayout.setVisibility(View.VISIBLE);
        }
        super.onConfigurationChanged(newConfig);
    }
}
