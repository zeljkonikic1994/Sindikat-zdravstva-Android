package www.a2adesign.net.sindikatzdravstva.util;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.URLSpan;

/**
 * Created by zeljk on 5/13/2017.
 */

public class URLSpanNoUnderline extends URLSpan {
    public URLSpanNoUnderline(String url) {
        super(url);
    }
    @Override public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
        ds.setFakeBoldText(true);
        ds.setColor(Color.parseColor("#0B6BBF"));
    }
}
