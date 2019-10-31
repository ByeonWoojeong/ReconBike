package app.cosmos.reconbike;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SMSReceive extends BroadcastReceiver {

    static boolean id_get_is_auth_apply = false, pass_get_is_auth_apply = false, join_auth_is_auth_apply = false, phone_change_is_auth_apply = false;

    boolean checkNumber(String number){
        boolean checkNumber = Pattern.matches("^[0-9]*$", number);
        return checkNumber;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(
                "android.provider.Telephony.SMS_RECEIVED") || intent.getAction().equals(
                "android.provider.Telephony.SMS_DELIVER")) {
            StringBuilder sms = new StringBuilder();
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdusObj = (Object[]) bundle.get("pdus");
                SmsMessage[] messages = new SmsMessage[pdusObj.length];
                for (int i = 0; i < pdusObj.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                }
                for (SmsMessage smsMessage : messages) {
                    sms.append(smsMessage.getMessageBody());
                }
                if (id_get_is_auth_apply || pass_get_is_auth_apply || join_auth_is_auth_apply || phone_change_is_auth_apply) {
                    if (sms.toString().contains("리콘바이크")) {
                        String getAuthNumber = sms.toString().replaceAll("\\[Web발신\\]", "").replaceAll("\\[리콘바이크\\]", "").replaceAll("인증번호는", "").replaceAll("입니다.", "").replaceAll(" ", "").replaceAll("\n", "").replaceAll("\r", "");
                        if (getAuthNumber.length() == 7){
                            if (checkNumber(getAuthNumber)) {
                                String smsBody = sms.toString();
                                Pattern pattern = Pattern.compile("\\d{7}");
                                Matcher matcher = pattern.matcher(smsBody);
                                String authNumber = null;
                                if (matcher.find()) {
                                    authNumber = matcher.group(0);
                                    if (authNumber != null) {
                                        if (id_get_is_auth_apply) {
                                            IdGetActivity.inputAuthNumber(authNumber);
                                        } else if (pass_get_is_auth_apply) {
                                            PassGetActivity.inputAuthNumber(authNumber);
                                        } else if (join_auth_is_auth_apply) {
                                            JoinAuthActivity.inputAuthNumber(authNumber);
                                        } else if (phone_change_is_auth_apply) {
                                            PhoneChangeActivity2.inputAuthNumber(authNumber);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
