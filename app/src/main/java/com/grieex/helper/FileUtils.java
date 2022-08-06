package com.grieex.helper;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils
{
    private final static String TAG = FileUtils.class.getName();

    public static void dirChecker(String dir) {
        File f = new File(dir);

        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }

    public static String getPath(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                assert cursor != null;
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }finally {
                if (cursor!=null)
                    cursor.close();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static void FileCopy(File src, File dst) {
        try {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }  catch (Exception e) {
            NLog.e(TAG, e);
        }
    }


    public static boolean FileCopy(String inputPath, String outputPath) {
        InputStream in;
        OutputStream out;
        try {

            // create output directory if it doesn't exist
            File dir = new File(outputPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            in = new FileInputStream(inputPath);
            out = new FileOutputStream(outputPath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();

            // write the output file
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            NLog.e(TAG, e);
            return false;
        }
    }

    public static void FileCopy(String inputFile, String outputPath, String newFileName) {
        InputStream in;
        OutputStream out;
        try {

            // create output directory if it doesn't exist
            File dir = new File(outputPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            in = new FileInputStream(inputFile);
            out = new FileOutputStream(outputPath + newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();

            // write the output file
            out.flush();
            out.close();
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    public static void FileMove(File src, File dst) {
        try {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.flush();
            out.close();

            src.delete();
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    public static void FileMove(String inputPath, String inputFile, String outputPath) {
        InputStream in;
        OutputStream out;
        try {

            // create output directory if it doesn't exist
            File dir = new File(outputPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();

            // write the output file
            out.flush();
            out.close();

            // delete the original file
            new File(inputPath + inputFile).delete();
        }  catch (Exception e) {
            NLog.e(TAG, e);
        }

    }

    public static void FileDelete(String strFile) {
        try {
            new File(strFile).delete();

        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    public static String getFileName(String path) {
        try {
            File f = new File(path);

            // String extension = path.substring(0,path.lastIndexOf("."));
            // String mimeTypeMap =
            // MimeTypeMap.getFileExtensionFromUrl(extension);
            // String mimeType =
            // MimeTypeMap.getSingleton().getMimeTypeFromExtension(mimeTypeMap.toLowerCase());
            // if (TextUtils.isEmpty(mimeType))
            // return Constants.UNDEFINED_FILE_TYPE;

            return f.getName();
        } catch (Exception e) {
            return "";
        }
    }

    public static String getFileExtension(String path) {
        try {
            return path.substring(path.lastIndexOf("."));
        } catch (Exception e) {
            return "";
        }
    }

    public static int getFileSize(String path) {
        try {
            File f = new File(path);
            return Integer.parseInt(String.valueOf(f.length() / 1024));
        } catch (Exception e) {
            return 0;
        }
    }

    public static String getFileMimeType(String url) {
        try {
            String extension = url.substring(url.lastIndexOf("."));
            String mimeTypeMap = MimeTypeMap.getFileExtensionFromUrl(extension);
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(mimeTypeMap.toLowerCase());
            if (TextUtils.isEmpty(mimeType))
                return Constants.UNDEFINED_FILE_TYPE;

            return mimeType;
        } catch (Exception e) {
            return Constants.UNDEFINED_FILE_TYPE;
        }
    }
}
