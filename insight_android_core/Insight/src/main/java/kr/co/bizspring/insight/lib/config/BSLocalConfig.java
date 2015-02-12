package kr.co.bizspring.insight.lib.config;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.XmlResourceParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import kr.co.bizspring.insight.lib.util.BSDebugger;

/**
 * Created by mac on 2014. 11. 4..
 */
public class BSLocalConfig {
    private static BSLocalConfig instance = null;
    Context mContext = null;
    private int reportTime = 300;
    private int sessionTime = 1800;
    private int alarmTime = 60*60*24;
    private int retryTime = 5*60;
    private BSLocalConfig(Context _context){
        mContext = _context;
        init();
    }
    public static BSLocalConfig getInstance(Context _context){
        if(instance == null){
            instance = new BSLocalConfig(_context);
        }
        return instance;
    }
    private void init(){
        ApplicationInfo applicationInfo = mContext.getApplicationInfo();
        String packageName = applicationInfo.packageName;
        int xmlId = 0;
        xmlId = mContext.getResources().getIdentifier("bs_local_config","xml",packageName);
        if(xmlId==0){
            xmlId = mContext.getResources().getIdentifier("bs_local_config","xml","kr.co.bizspring.insight");

        }
        XmlResourceParser localConfigXml = mContext.getResources().getXml(xmlId);
        try {
            localConfigXml.next();
            int eventType = localConfigXml.getEventType();
            String NodeValue;
            while (eventType != XmlPullParser.END_DOCUMENT)  //Keep going until end of xml document
            {
                if(eventType == XmlPullParser.START_DOCUMENT)
                {
                    //Start of XML, can check this with myxml.getName() in Log, see if your xml has read successfully
                }
                else if(eventType == XmlPullParser.START_TAG)
                {
                    NodeValue = localConfigXml.getName();//Start of a Node
                    if (NodeValue.equalsIgnoreCase("Integer"))
                    {
                        int attributeCount = localConfigXml.getAttributeCount();
                        for(int i = 0; i < attributeCount ; i++){
                            String attributeName = localConfigXml.getAttributeName(i);
                            if(attributeName.equalsIgnoreCase("name")){
                                String attributeValue = localConfigXml.getAttributeValue(i);
                                if(attributeValue.equalsIgnoreCase("reportTime")){
                                    String nextText = localConfigXml.nextText();
                                    reportTime = Integer.parseInt(nextText);
                                }else if(attributeValue.equalsIgnoreCase("sessionTime")){
                                    String nextText = localConfigXml.nextText();
                                    sessionTime = Integer.parseInt(nextText);
                                }else if(attributeValue.equalsIgnoreCase("alarmTime")){
                                    String nextText = localConfigXml.nextText();
                                    alarmTime = Integer.parseInt(nextText);
                                }else if(attributeValue.equalsIgnoreCase("retryTime")){
                                    String nextText = localConfigXml.nextText();
                                    retryTime = Integer.parseInt(nextText);
                                }
                            }
                        }
                        // use myxml.getAttributeValue(x); where x is the number
                        // of the attribute whose data you want to use for this node

                    }

                    if (NodeValue.equalsIgnoreCase("SecondNodeNameType"))
                    {
                        // use myxml.getAttributeValue(x); where x is the number
                        // of the attribute whose data you want to use for this node

                    }
                    //etc for each node name
                }
                else if(eventType == XmlPullParser.END_TAG)
                {
                    //End of document
                }
                else if(eventType == XmlPullParser.TEXT)
                {
                    //Any XML text
                }

                eventType = localConfigXml.next(); //Get next event from xml parser
            }

        } catch (XmlPullParserException e) {
            BSDebugger.log(e, this);
        } catch (IOException e) {
            BSDebugger.log(e,this);
        }
    }


    public int getReportTime() {
        return reportTime;
    }
    public int getSessionTime(){
        return sessionTime;
    }

    public long getAlarmScheduleTime() {
        return alarmTime;
    }

    public int getRetryTime() {
        return retryTime;
    }
}
