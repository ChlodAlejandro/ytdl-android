package net.chlod.ytdl_android.utils;

import com.orhanobut.logger.Logger;

import net.chlod.ytdl_android.DownloadProgressCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StreamProcessExtractor extends Thread {
    private static final int GROUP_PERCENT = 1;
    private static final int GROUP_MINUTES = 2;
    private static final int GROUP_SECONDS = 3;
    private final InputStream stream;
    private final StringBuffer buffer;
    private final DownloadProgressCallback callback;

    private final Pattern p;

    public StreamProcessExtractor(Pattern p, StringBuffer buffer, InputStream stream, DownloadProgressCallback callback) {
        this.p = p;
        this.stream = stream;
        this.buffer = buffer;
        this.callback = callback;
        this.start();
    }

    public void run() {
        try {
            //noinspection CharsetObjectCanBeUsed
            Reader in = new InputStreamReader(stream, "UTF-8");
            StringBuilder currentLine = new StringBuilder();
            int nextChar;
            while ((nextChar = in.read()) != -1) {
                buffer.append((char) nextChar);
                if (nextChar == '\r' && callback != null) {
                    processOutputLine(currentLine.toString());
                    currentLine.setLength(0);
                    continue;
                }
                currentLine.append((char) nextChar);
            }
        } catch (IOException e) {
            Logger.e(e, "failed to read stream");
        }
    }

    private void processOutputLine(String line) {
        Matcher m = p.matcher(line);
        if (m.matches()) {
            float progress = Float.parseFloat(m.group(GROUP_PERCENT));
            long eta = convertToSeconds(m.group(GROUP_MINUTES), m.group(GROUP_SECONDS));
            callback.onProgressUpdate(progress, eta);
        }
    }

    private int convertToSeconds(String minutes, String seconds) {
        return Integer.parseInt(minutes) * 60 + Integer.parseInt(seconds);
    }
}
