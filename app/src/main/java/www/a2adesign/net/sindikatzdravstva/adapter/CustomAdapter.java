package www.a2adesign.net.sindikatzdravstva.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import www.a2adesign.net.sindikatzdravstva.R;
import www.a2adesign.net.sindikatzdravstva.domen.Post;

/**
 * Created by FILIP on 25-Apr-17.
 */

public class CustomAdapter extends ArrayAdapter<Post> {

    private Context context;
    private List<Post> listaPostova;
    private LayoutInflater inflater;

    public CustomAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public CustomAdapter(@NonNull Context context, int resource, @NonNull List<Post> objects) {
        super(context, resource, objects);

        this.context = context;
        this.listaPostova = objects;

        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(null==convertView){
            convertView = inflater.inflate(R.layout.custom_post, parent,false);
        }
        TextView naslov = (TextView) convertView.findViewById(R.id.textViewNaslov);
        ImageView slika = (ImageView) convertView.findViewById(R.id.imageViewNaslovna);

        Post post = getItem(position);
        String naslovPosta = post.getdTitle();
        String url = post.getdPicturePath();
        if(url.endsWith("default.png")){
            Picasso.with(getContext()).load(R.drawable.postovi_lista_thumb).resize(80,80).into(slika);
        }else {
            Picasso.with(context)
                    .load(url)
                    .placeholder(R.drawable.postovi_lista_thumb)
                    .resize(80, 80)
                    .centerCrop()
                    .tag(context)
                    .into(slika);
        }
        naslov.setText(naslovPosta);
        return convertView;
    }

    @Nullable
    @Override
    public Post getItem(int position) {
        return listaPostova.get(position);
    }
}
