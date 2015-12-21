package com.huotu.smartui.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.util.StreamUtils;

public class FileUtil {

	public static String getStringByInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StreamUtils.copy(inputStream, outputStream);
        inputStream.close();
        return outputStream.toString();
    }
}
