package www.a2adesign.net.sindikatzdravstva.firebase;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import www.a2adesign.net.sindikatzdravstva.R;
import www.a2adesign.net.sindikatzdravstva.activity.PostActivity;

import static android.support.v7.app.NotificationCompat.DEFAULT_ALL;

/**
 * Created by FILIP on 03-May-17.
 */

public class MessageService extends FirebaseMessagingService {
    private static final String TAG = "MessageService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String from = remoteMessage.getFrom();

        // Check if message contains a data payload.
        Map<String, String> data = remoteMessage.getData();
        if (data.size() > 0) {
            Log.d(TAG, "Message data payload: " + data);
            switch (from) {
                case "/topics/log":
                    onLogMessage(data);
                    break;
                case "/topics/login":

                case "/topics/new_article":
                    onNewPageMessage(data);
                    break;

            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

    }

    private void onLogMessage(Map<String, String> data) {
    }

    private void onNewPageMessage(Map<String, String> data) {
        String title = data.get("title");
        String postID = data.get("id");
        String content = data.get("content");

        Log.d("STRING_TITLE", title);
        Log.d("STRING_IDID", postID);
        if (!TextUtils.isEmpty(title)) {
            String body = data.get("body");
//            v4 biblioteka za notificationcompat
            Intent postIntent = new Intent(this, PostActivity.class);
            postIntent.putExtra("id", postID);
            PendingIntent intent = PendingIntent.getActivity(this,0, postIntent,PendingIntent.FLAG_UPDATE_CURRENT);
            
            Notification notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.health)
                    .setColor(0x0b6bbf)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setAutoCancel(true)
                    .setDefaults(DEFAULT_ALL)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(body)
                            .setBigContentTitle(title))
                    .setContentIntent(intent)
                    .build();
            showNotification(5, notification,title);
        }
    }

    // More methods handling the other topics

    private void showNotification(int id, Notification notification, String title) {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(id, notification);
    }
}
