package www.a2adesign.net.sindikatzdravstva.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import www.a2adesign.net.sindikatzdravstva.domen.Post;

/**
 * Created by FILIP on 27-Apr-17.
 */

public class SaveListIntoDB extends AsyncTask<Post, Void, Void> {
    Context context;
    List<Post> listaZaBazu;
    public SaveListIntoDB(Context context, List<Post> listaZaBazu) {
        this.context = context;
        this.listaZaBazu = listaZaBazu;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected Void doInBackground(Post... posts) {
        JSONArray arr = new JSONArray();
        for (int i = 0; i < listaZaBazu.size();i++){
            JSONObject json = new JSONObject();
            try {
                json.put("title", listaZaBazu.get(i).getdTitle());
                json.put("content", listaZaBazu.get(i).getdContent());
                json.put("picturePath", listaZaBazu.get(i).getdPicturePath());
                arr.put(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        JSONObject jsonListaZaBazu = new JSONObject();
        try {
            jsonListaZaBazu.put("lista", arr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String jsonLista = jsonListaZaBazu.toString();
        SharedPreferences sharedPref = context.getSharedPreferences("appData", Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        prefEditor.clear();
        prefEditor.putString("json",jsonLista);
        prefEditor.commit();
        return null;
    }

}
