package kr.co.wisetracker.insight.lib.util;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Created by mac on 2014. 10. 20..
 */
public class JsonStringCompress {
    // #########################################################################################
    // 1.Compress
    // #########################################################################################
    public static byte[] compress(String src) throws IOException {
        byte[] returnByte = null;
        try {
            String encodeStr = URLEncoder.encode(src, "utf-8");
            byte[] dataByte = encodeStr.getBytes();
            Deflater deflater = new Deflater();
            deflater.setLevel(Deflater.BEST_COMPRESSION);
            deflater.setInput(dataByte);
            deflater.finish();
            ByteArrayOutputStream bao = new ByteArrayOutputStream(dataByte.length);
            byte[] buf = new byte[1024];
            int offset = 0;
            while(!deflater.finished()){
                int compByte = deflater.deflate(buf);
                Log.d("BS_COMPRESS_BEFORE_BYTE",buf.toString());
                bao.write(buf, offset, compByte);
            }
            bao.close();
            deflater.end();
            Log.d("BS_BYTE_COMPRESSED",String.valueOf(bao.toByteArray()));
            returnByte = bao.toByteArray();
        }catch (Exception e){
            e.printStackTrace();
        }
        return returnByte;
    }
    // #########################################################################################
    // 4.Decompress
    // #########################################################################################
    public static String decompress(byte[] data) throws IOException, DataFormatException {
        String dStr = "";
        try {

            Inflater inflater = new Inflater();
            inflater.setInput(data);
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int offset = 0;
            while(!inflater.finished()) {
                int compByte = inflater.inflate(buf);
                bao.write(buf, offset, compByte);
            }
            bao.close();
            inflater.end();
            dStr = URLDecoder.decode(new String(bao.toByteArray()), "utf-8");

        } catch (Exception e) {
            BSDebugger.log(e, data);
        }
        return dStr;
    }
}
