package com.thefidebox.fidebox.firebasecloudmessaging;

import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.RemoteMessage;
import com.thefidebox.fidebox.R;
import com.thefidebox.fidebox.models.CV;
import com.thefidebox.fidebox.models.CommentLog;
import com.thefidebox.fidebox.models.NotificationSettings;
import com.thefidebox.fidebox.utils.ViewCommentActivity;

public class FCMService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String APP_PACKAGE_NAME = "com.thefidebox.fidebox";
    private static final String TAG = "FirebaseMsgService";
    public static final int COMMENT_NOTIFICATION_ID = 0;
    public static final int CV_NOTIFICATION_ID = 1;
    public static final String ACTION_NOTIFICATION = "com.fidebox.action.NOTIFICATION";
    Context context;
    private CV CV;
    String notifyee_id;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        //Check whether background is restricted, ActivityManager activityManager=new ActivityManager();
        // Check if message contains a data payload.

        context = getApplicationContext();

        //receive data, if log in,then noti
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            final String title = remoteMessage.getData().get(context.getString(R.string.title));
            final String body = remoteMessage.getData().get(context.getString(R.string.body));
            final String cv_id = remoteMessage.getData().get("cv_id");
            final String cv_giggles = remoteMessage.getData().get("cv_giggles");
            final String cv_comments = remoteMessage.getData().get("cv_comments");
            final String type = remoteMessage.getData().get(context.getString(R.string.type));
            final String category=remoteMessage.getData().get("category");
            notifyee_id = remoteMessage.getData().get("notifyee_id");

            Log.d(TAG, "onMessageReceived: " + type);
//            pls also send userId to confirm the currentUser is the one as one person may have multiple acs
            try {
                CommentLog commentLog;

                if (type.equals("comment")) {
                    String commentPath = remoteMessage.getData().get("route");
                    String commentId = remoteMessage.getData().get("comment_id");

                    commentLog = new CommentLog();
                    commentLog.setRoute(commentPath);
                    commentLog.setCommentId(commentId);
                } else {
                    commentLog = null;
                }


                //avoid getting the same cv over and over again
                if (CV != null) {
                    if (cv_id.equals(CV.getCvId())) {

                        CV.setGiggles(Integer.parseInt(cv_giggles));
                        CV.setComments(Integer.parseInt(cv_comments));

                        if (type.equals("CV")) {
                            //notification already stored in database
                            if (notifyee_id.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) && !checkIfForeground()) {

                                checkNotificationSettingsThenSendNoti(type, body, title, commentLog);
                            }
                        } else {

                            //notification already stored in database
                            if (notifyee_id.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) && !checkIfForeground()) {

                                checkNotificationSettingsThenSendNoti(type, body, title, commentLog);
                            }
                        }

                    } else {

                        if (notifyee_id.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                            checkNotificationSettingsThenFetchCV(cv_id, type, body, title, commentLog,category);
                        }

                    }
                } else {

                    if (notifyee_id.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                        checkNotificationSettingsThenFetchCV(cv_id, type, body, title, commentLog,category);
                    }

                }

            } catch (NullPointerException e) {

            }

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                // scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());

        }

    }
    // [END receive_message]


    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

    //    SharedPrefManager.getInstance(getApplicationContext()).storeToken(token);

        sendRegistrationToServer(token);
    }
    // [END on_new_token]

    /**
     * Schedule async work using WorkManager.
     */
    /*
    private void scheduleJob() {
        // [START dispatch_job]
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(MyWorker.class)
                .build();
        WorkManager.getInstance().beginWith(work).enqueue();
        // [END dispatch_job]
    }

     */

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    /**
     * Persist token to third-party servers.
     * <p>
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param tokenID The new token.
     */
    private void sendRegistrationToServer(String tokenID) {

        Log.d(TAG, "sendRegistrationToServer: ");
        Token token = new Token(tokenID);


        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(getApplicationContext());
            sharedPrefManager.storeToken(tokenID);

            FirebaseFirestore.getInstance().collection(getString(R.string.col_users))
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection(getString(R.string.col_token)).document(tokenID).set(token);

        }
    }

    public void cancelNotification(int notificationId, NotificationManager notificationManager) {
        notificationManager.cancel(notificationId);
    }


    private void setAndSendNotification(CV CV, String cv_body, String cv_title, int notificationID, CommentLog commentLog, String type) {

        Log.d(TAG, "setAndSendNotification: noti");
        Intent intent = new Intent(FCMService.this, ViewCommentActivity.class);
//        intent.putExtra(getString(R.string.activity_number), 0);

        if (commentLog != null) {
            intent.putExtra(context.getString(R.string.comment_log), commentLog);
        }
        intent.putExtra("cvFromFCMService", CV);
        intent.putExtra("type", type);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        /* PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 *//* Request code *//*, intent,
                PendingIntent.FLAG_ONE_SHOT);*/

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(FCMService.this);
        stackBuilder.addNextIntentWithParentStack(intent);
//                                stackBuilder.addParentStack((Activity) context);
//                                stackBuilder.addNextIntent(resultIntent);

//        /fix intent
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        //Set the values
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(FCMService.this, channelId)
                        .setSmallIcon(R.drawable.ic_app_logo)
                        .setContentTitle(cv_title)
                        .setContentText(cv_body)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(resultPendingIntent)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_app_logo))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(cv_body))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setChannelId(channelId)
                        .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                        .setTicker(cv_body);


        //.setVibrate()
        //.setActionbutton()
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            CharSequence name = "Channel Name";
            String description = "Channel Description";

            NotificationChannel channel = new NotificationChannel(channelId,
                    name,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setShowBadge(true);
            channel.setDescription(description);

            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(notificationID /* ID of notification */, notificationBuilder.build());

    }

    private void fetchCV(final String cvId, final String type, final String body, final String title,
                         final CommentLog commentLog,final String category) {

        FirebaseFirestore.getInstance().collection(category)
                .document(cvId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            Log.d(TAG, "onComplete: " + task.getResult().exists());
                            if (document.exists()) {

                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                                CV = document.toObject(CV.class);

                                if (type.equals("CV")) {

                                    if (notifyee_id.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) && !checkIfForeground()) {

                                        setAndSendNotification(CV, body, title, CV_NOTIFICATION_ID, null, type);
                                    }

                                } else {

                                    if (notifyee_id.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) && !checkIfForeground()) {

                                        setAndSendNotification(CV, body, title, COMMENT_NOTIFICATION_ID, commentLog, type);
                                    }
                                }


                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });

    }

    private boolean checkIfForeground() {
        //Check if app is in background
//        ProcessLifecycleOwner.get().getLifecycle().getCurrentState() == Lifecycle.State.CREATED;

        //Check if app is in foreground
        return ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED);

    }

    private void checkNotificationSettingsThenSendNoti(final String type, final String body, final String title, final CommentLog commentLog) {

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            FirebaseFirestore.getInstance().collection(context.getString(R.string.users))
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection(context.getString(R.string.col_settings))
                    .document(context.getString(R.string.doc_notificationSettings))
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            NotificationSettings notificationSettings = document.toObject(NotificationSettings.class);

                            if (type.equals("CV") && notificationSettings.isChange_on_cv()) {

                                setAndSendNotification(CV, body, title, CV_NOTIFICATION_ID, null, type);
                            } else if (type.equals("comment") && notificationSettings.isChange_on_comment()) {

                                setAndSendNotification(CV, body, title, COMMENT_NOTIFICATION_ID, commentLog, type);
                            }

                            Log.d(TAG, "DocumentSnapshot data11111: " + document.getData());

                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });

        }
    }

    private void checkNotificationSettingsThenFetchCV(final String cvId, final String type, final String body, final String title,
                                                      final CommentLog commentLog,final String category) {

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            FirebaseFirestore.getInstance().collection(context.getString(R.string.users))
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection(context.getString(R.string.col_settings))
                    .document(context.getString(R.string.doc_notificationSettings))
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            NotificationSettings notificationSettings = document.toObject(NotificationSettings.class);

                            if (type.equals("CV") && notificationSettings.isChange_on_cv()) {
                                fetchCV(cvId, type, body, title, commentLog,category);

                            } else if (type.equals("comment") && notificationSettings.isChange_on_comment()) {


                                fetchCV(cvId, type, body, title, commentLog,category);
                            }

                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });

        }
    }

}
