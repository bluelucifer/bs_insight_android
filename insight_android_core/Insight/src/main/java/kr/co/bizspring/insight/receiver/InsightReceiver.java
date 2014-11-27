package kr.co.bizspring.insight.receiver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.Iterator;
import java.util.Set;

import kr.co.bizspring.insight.service.InsightService;

/**
 * Created by caspar on 14. 9. 8.
 */
public class InsightReceiver extends WakefulBroadcastReceiver {

    @SuppressLint("NewApi")
    @Override
    public void onReceive(Context ctx, Intent intent) {
        Intent intent1 = new Intent(ctx, InsightService.class);
        intent1.setAction(intent.getAction());
        intent1.setData(intent.getData());
        Bundle bundle = intent.getExtras();
        if(bundle!=null){
            intent1.putExtras(bundle);
        }
        Set<String>category = intent.getCategories();
        if(category!=null) {
            for (String key : category) {
                intent1.addCategory(key);
            }
        }
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN){
            intent1.setClipData(intent.getClipData());
        }
        intent1.setData(intent.getData());
        intent1.setFlags(intent.getFlags());
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
            intent1.setSelector(intent.getSelector());
        }
        intent1.setSourceBounds(intent.getSourceBounds());
        intent1.setType(intent.getType());

        ctx.startService(intent1);
    }

}
