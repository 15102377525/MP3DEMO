package mp3.stk.com.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LrcHandle {
    private List<String> mWords = new ArrayList<String>();
    private List<Integer> mTimeList = new ArrayList<Integer>();

    //处理歌词文件
    public void readLRC(String path) {
        String[] split = path.split("\\[");
        for (int i = 0; i < split.length; i++) {
            split[i] = "[" + split[i];
            Log.e("str3", split[i]);
            addTimeToList(split[i]);
            if ((split[i].indexOf("[ar:") != -1) || (split[i].indexOf("[ti:") != -1)
                    || (split[i].indexOf("[by:") != -1)) {
                split[i] = split[i].substring(split[i].indexOf(":") + 1, split[i].indexOf("]"));
            } else {
                String ss = split[i].substring(split[i].indexOf("["), split[i].indexOf("]") + 1);
                split[i] = split[i].replace(ss, "");
            }

            mWords.add(split[i]);
        }
    }

    public List<String> getWords() {
        return mWords;
    }

    public List<Integer> getTime() {
        return mTimeList;
    }

    private int timeHandler(String string) {
        string = string.replace(".", ":");
        String timeData[] = string.split(":");
        // 分离出分、秒并转换为整型
        int minute = Integer.parseInt(timeData[0]);
        int second = Integer.parseInt(timeData[1]);
        int millisecond = Integer.parseInt(timeData[2]);

        // 计算上一行与下一行的时间转换为毫秒数
        int currentTime = ((minute) * 60 + second) * 1000 + millisecond * 10;
        return currentTime;
    }

    private void addTimeToList(String string) {
        Matcher matcher = Pattern.compile(
                "\\[\\d{1,2}:\\d{1,2}([\\.:]\\d{1,2})?\\]").matcher(string);
        if (matcher.find()) {
            String str = matcher.group();
            mTimeList.add(timeHandler(str.substring(1,
                    str.length() - 1)));


        }

    }


}
