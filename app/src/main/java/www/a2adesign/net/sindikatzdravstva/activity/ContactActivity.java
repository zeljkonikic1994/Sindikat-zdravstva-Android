package www.a2adesign.net.sindikatzdravstva.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;

import www.a2adesign.net.sindikatzdravstva.R;
import www.a2adesign.net.sindikatzdravstva.util.URLSpanNoUnderline;

public class ContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        TextView telefon1 = (TextView) findViewById(R.id.telefon1);
        final String tel = telefon1.getText().toString().trim();
        telefon1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:"+tel));
                startActivity(callIntent);
            }
        });

        TextView telefon2 = (TextView) findViewById(R.id.telefon2);
        final String tel2 = telefon2.getText().toString().trim();
        telefon2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:"+tel2));
                startActivity(callIntent);
            }
        });
        TextView aadesign = (TextView) findViewById(R.id.link);
        aadesign.setText(Html.fromHtml("<a href=http://www.2awebdesign.net> 2A Design"));
        aadesign.setMovementMethod(LinkMovementMethod.getInstance());
        stripUnderlines(aadesign);

        TextView feedback = (TextView) findViewById(R.id.mail);
        feedback.setText(Html.fromHtml("<a href=\"mailto:sindikatzdravstva@yahoo.com\">sindikatzdravstva@yahoo.com</a>"));
        feedback.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void stripUnderlines(TextView textView) {
        Spannable s = new SpannableString(textView.getText());
        URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan span: spans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL());
            s.setSpan(span, start, end, 0);
        }
        textView.setText(s);
    }


}
