package www.a2adesign.net.sindikatzdravstva.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import www.a2adesign.net.sindikatzdravstva.adapter.CustomAdapter;
import www.a2adesign.net.sindikatzdravstva.domen.Post;
import www.a2adesign.net.sindikatzdravstva.R;
import www.a2adesign.net.sindikatzdravstva.util.SaveListIntoDB;

public class MainActivity extends AppCompatActivity {

    boolean imaNeta = false;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    ListView postList;
    ProgressDialog progressDialog;

    //lista od 10 poslednjih postova koja se cuva u sharedPreferences
    private List<Post> listaZaBazu;
    public String url = "http://zdravko.org.rs/wp-json/wp/v2/posts/?per_page=20";
    public Gson gson;
    //lista koja se uzima iz Gson-a
    List<Object> list;
    //post iz liste
    Map<String,Object> mapPost;
    //naslov iz posta
    Map<String,Object> mapTitle;
    //sadrzaj iz posta
    Map<String,Object> mapContent;
    //putanja do slike, takodje iz posta
    String picturePath;
    //objekat koji se pakuje za shared preferences
    Post post;
    //lista postova koja se prosledjuje custom adapteru
    List<Post> listaPostova;
    RequestQueue rQueue;
    int postID;

    public void ucitajMainActivity() {
        postList = (ListView)findViewById(R.id.postList);
        imaNeta = haveNetworkConnection();
        listaPostova= new ArrayList<>();

        if(imaNeta){
            FirebaseMessaging.getInstance().subscribeToTopic("new_article");
            listaZaBazu = new ArrayList<>();
            StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String s) {
                    gson = new Gson();
                    list = (ArrayList) gson.fromJson(s, List.class);
                    for(int i=0;i<list.size();++i){
                        post = new Post();
                        mapPost = (Map<String,Object>)list.get(i);
                        mapTitle = (Map<String, Object>) mapPost.get("title");
                        mapContent = (Map<String, Object>) mapPost.get("content");
                        picturePath = (String) mapPost.get("featured_image_src");

                        String title = srediUnicode((String) mapTitle.get("rendered"));
                        post.setdTitle(title);
                        post.setdContent(srediUnicode(mapContent.get("rendered").toString()));
                        post.setdPicturePath(picturePath);

                        listaPostova.add(post);

                        if(listaZaBazu.size() < 10) {
                            listaZaBazu.add(post);
                        }
                    }
                    //novi thread koji cuva u shared preferences listu za bazu
                    SaveListIntoDB slid = new SaveListIntoDB(getApplicationContext(), listaZaBazu);
                    slid.execute();

                    CustomAdapter ca = new CustomAdapter(MainActivity.this, R.layout.custom_post, listaPostova);
//                    ca.setListaPostova(listaPostova);
                    postList.setAdapter(ca);
                    ca.notifyDataSetChanged();

                    if(progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    if(mSwipeRefreshLayout!=null)
                        mSwipeRefreshLayout.setRefreshing(false);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Toast.makeText(MainActivity.this, "Serveru je potrebno predugo da odgovori. Pokušajte ponovo.", Toast.LENGTH_LONG).show();
                    if(mSwipeRefreshLayout!=null)
                        mSwipeRefreshLayout.setRefreshing(false);
                    if(progressDialog.isShowing())
                        progressDialog.dismiss();
                }
            });
            request.setRetryPolicy(new DefaultRetryPolicy(
                    10000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            if(rQueue==null) {
                rQueue = Volley.newRequestQueue(MainActivity.this);
            }
//            mSwipeRefreshLayout.setRefreshing(false);
            rQueue.add(request);

        }else{
            Toast.makeText(MainActivity.this, "Nema internet konekcije", Toast.LENGTH_LONG).show();
            listaPostova = new ArrayList<>();
            SharedPreferences sp = getSharedPreferences("appData", Context.MODE_WORLD_READABLE);
            try {
                String json = sp.getString("json",null);
                listaPostova = ucitajIzJson(json);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception ex) {
                Toast.makeText(MainActivity.this, "Nema internet konekcije. Pokušajte ponovo.", Toast.LENGTH_SHORT).show();
            }
            CustomAdapter ca = new CustomAdapter(MainActivity.this, R.layout.custom_post, listaPostova);
//            ca.setListaPostova(listaPostova);
            postList.setAdapter(ca);
            if(progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if(mSwipeRefreshLayout!=null)
                mSwipeRefreshLayout.setRefreshing(false);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        Toast.makeText(MainActivity.this, "onCreate",Toast.LENGTH_SHORT).show();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), ContactActivity.class);
                startActivity(i);
            }
        });
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        ucitajMainActivity();

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //            PicassoTools.clearCache(Picasso.with(this));
                ucitajMainActivity();
            }
        });

       postList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(haveNetworkConnection()) {
                    mapPost = (Map<String, Object>) list.get(position);
                    postID = ((Double) mapPost.get("id")).intValue();

                    Intent intent = new Intent(getApplicationContext(), PostActivity.class);
                    intent.putExtra("id", "" + postID);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(getApplicationContext(),PostActivity.class);
                    intent.putExtra("title", listaPostova.get(position).getdTitle());
                    intent.putExtra("content",listaPostova.get(position).getdContent());
                    intent.putExtra("picturePath",listaPostova.get(position).getdPicturePath());
                    startActivity(intent);
                }
            }
        });
    }

    private List<Post> ucitajIzJson(String json) throws JSONException {
        List<Post> listaPostova = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(json);
        JSONArray jsonArray = jsonObject.getJSONArray("lista");
        for (int i = 0; i < jsonArray.length();i++){
            JSONObject jobj = jsonArray.getJSONObject(i);
            String title = jobj.getString("title");
            String content = jobj.getString("content");
            String imgPath = jobj.getString("picturePath");
            listaPostova.add(new Post(title, content, imgPath));
        }
        return listaPostova;

    }

    public boolean haveNetworkConnection() {

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
    public void onConfigurationChanged(Configuration newConfig)
    {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.action_bar_id);
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            linearLayout.setVisibility(View.GONE);
            SwipeRefreshLayout srl = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0,0,0,0);
            srl.setLayoutParams(layoutParams);
        }else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            linearLayout.setVisibility(View.VISIBLE);
            SwipeRefreshLayout srl = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            Resources r = getApplicationContext().getResources();
            int px = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    72,
                    r.getDisplayMetrics()
            );
            layoutParams.setMargins(0, px, 0, 0);
            srl.setLayoutParams(layoutParams);
        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        ucitajMainActivity();
    }
}
