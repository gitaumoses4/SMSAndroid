package decoding.com.decoding;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Moses Gitau on 6/6/17.
 */

public class SMSListener extends BroadcastReceiver {

    private String phoneNumber = "0740287216";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            Log.d("SMSListener", intent.getAction().toString());
        }
        if (intent != null && intent.getAction() != null && intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            String phoneNumbers = PreferenceManager.getDefaultSharedPreferences(
                    context).getString("phone_entries", "");
            StringTokenizer tokenizer = new StringTokenizer(phoneNumbers, ",");
            Set<String> phoneEnrties = new HashSet<String>();
            while (tokenizer.hasMoreTokens()) {
                phoneEnrties.add(tokenizer.nextToken().trim());
            }
            Bundle bundle = intent.getExtras();
            Object[] pdus = (Object[]) bundle.get("pdus");
            SmsMessage[] messages = new SmsMessage[pdus.length];
            String title = "";
            for (int i = 0; i < messages.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                Pattern pattern = Pattern.compile("http://maps.google.com/maps\\?q=(\\w)(\\d+.\\d+),(\\w)(\\d+.\\d+)");
                Log.d("SMSListener", messages[i].getDisplayMessageBody().trim().split("\n")[0]);
                Matcher matcher = pattern.matcher(messages[i].getDisplayMessageBody().trim().split("\n")[0].trim());
                Log.d("SMSListener", "Matcher: " + matcher.matches());
                title += messages[i];
                if (matcher.matches()) {
                    Double latitude = matcher.group(1).equalsIgnoreCase("S") ? -Double.parseDouble(matcher.group(2)) : Double.parseDouble(matcher.group(2));
                    Double longitude = Double.parseDouble(matcher.group(4));
                    MainActivity.map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 12.0f));
                    MainActivity.marker.setPosition(new LatLng(latitude, longitude));
                    Log.d("SMSListener", "Lat: " + latitude + " Longitude: " + longitude);
                } else {
                    if (messages[i].getDisplayMessageBody().contains("Speed")) {
                        Pattern pattern1 = Pattern.compile(".*Speed:(\\d+.\\d+km/h).*");
                        Matcher matcher1 = pattern1.matcher(messages[i].getDisplayMessageBody().trim());
                        if (matcher1.matches()) {
                            if (MainActivity.speedTextView != null)
                                MainActivity.speedTextView.setText(matcher1.group(1));
                        }
                    }
                }
            }
        }
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, "smslink654321", null, null);
    }
}
