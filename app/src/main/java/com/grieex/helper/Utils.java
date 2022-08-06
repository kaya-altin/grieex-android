package com.grieex.helper;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.grieex.model.NameValue;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import okhttp3.RequestBody;
import okio.Buffer;

public class Utils {

    public static void showSoftKeyboard(Context activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View view = activity.getCurrentFocus();
        if (view != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    public static boolean isProInstalled(Context context) {
        PackageManager manager = context.getPackageManager();
        return manager.checkSignatures(context.getPackageName(), GrieeXSettings.GrieeXPro) == PackageManager.SIGNATURE_MATCH;
//        if (context.getPackageName().equals(GrieeXSettings.GrieeXPro)) {
//            return true;
//        }
    }

//    public static boolean isProInstalled(Context context) {
//        // get the package manager
//        final PackageManager pm = context.getPackageManager();
//        // get a list of installed packages
//        List<PackageInfo> list = pm.getInstalledPackages(PackageManager.GET_DISABLED_COMPONENTS);
//        // let's iterate through the list
//        for (PackageInfo p : list) {
//            // check if proPackage is in the list AND whether that package is
//            // signed
//            // with the same signature as THIS package
//            if ((p.packageName.equals(GrieeXSettings.GrieeXPro)) && (pm.checkSignatures(context.getPackageName(), p.packageName) == PackageManager.SIGNATURE_MATCH))
//                return true;
//        }
//        return false;
//    }

    public static void setDefaultLocale(Context context, String locale) {
        Locale loc = new Locale(locale);
        Locale.setDefault(loc);

        Configuration config = new Configuration();
        config.locale = loc;

        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }


    /**
     * Checks if {@link Environment}.MEDIA_MOUNTED is returned by
     * {@code getExternalStorageState()} and therefore external storage is read-
     * and writeable.
     *
     * @return
     */
    public static boolean isExtStorageAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (; ; ) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;
                os.write(bytes, 0, count);
            }
        } catch (Exception ex) {
        }
    }

    public static void MoveFile(String inputFile, String outputPath, String outputFile) {

        InputStream in = null;
        OutputStream out = null;
        try {

            // create output directory if it doesn't exist
            File dir = new File(outputPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            in = new FileInputStream(inputFile);
            out = new FileOutputStream(outputPath + outputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;

            // delete the original file
            new File(inputFile).delete();
        } catch (Exception e) {
            Log.e("Utilities.MoveFile", e.getMessage());
        }

    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device
     * density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need
     *                to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on
     * device density
     */
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * (metrics.densityDpi / 160f);
    }

    /**
     * This method converts device specific pixels to density independent
     * pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return px / (metrics.densityDpi / 160f);
    }

    public static boolean isNumeric(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c))
                return false;
        }
        return true;
    }

    public static String getRandomString() {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder ret = new StringBuilder();
        int length = chars.length();
        for (int i = 0; i < 7; i++) {
            ret.append(chars.split("")[(int) (Math.random() * (length - 1))]);
        }
        return ret.toString();
    }

    public static String reverseCharacters(String strColumn) {
        String str = "replace(replace(replace(replace(replace(replace(" + strColumn + ", 'Ç','c'), 'Ğ', 'g'), 'İ', 'i'), 'Ö', 'o'), 'Ş', 's'), 'Ü', 'u')";
        str = "replace(replace(replace(replace(replace(replace(" + str + ", 'ç','c'), 'ğ', 'g'), 'ı', 'i'), 'ö', 'o'), 'ş', 's'), 'ü', 'u')";
        return str;
    }

    public static String reverseCharactersLower(String str) {
        return str.replace("ç", "c").replace("ğ", "g").replace("ı", "i").replace("ö", "o").replace("ş", "s").replace("ü", "u");
    }

    public static String reverseCharactersUpper(String str) {
        return str.replace("Ç", "C").replace("Ğ", "G").replace("İ", "I").replace("Ö", "O").replace("Ş", "S").replace("Ü", "U");
    }


    public static boolean parseBoolean(int i) {
        return i != 0;
    }

    public static boolean parseBoolean(String str) {
        return str != null && !TextUtils.isEmpty(str) && str.equals("1") | str.equals("true");

    }

    public static String parseBooleanInt(String str) {
        if (str == null) {
            return "0";
        }

        if (TextUtils.isEmpty(str)) {
            return "0";
        }

        if (str.equals("1") | str.equals("true"))
            return "1";
        else
            return "0";

    }

    public static String parseBooleanInt(boolean b) {
        try {
            return b ? "1" : "0";
        } catch (Exception e) {
            return "0";
        }
    }


    public static int parseBooleanToInt(boolean b) {
        try {
            return b ? 1 : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public static Float parseFloat(String value) {
        try {
            if (!TextUtils.isEmpty(value)) {
                value = value.replace(",", ".");
            }

            return Float.parseFloat(value);
        } catch (Exception e) {
        }
        return 0f;
    }

    public static double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
        }
        return -1;
    }

    public static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
        }
        return -1;
    }

    public static long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
        }
        return -1;
    }

    public static String parseString(Object value) {
        try {
            return String.valueOf(value);
        } catch (Exception e) {
        }
        return "";
    }

    public static int getVersionCode(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            return pi.versionCode;
        } catch (NameNotFoundException ex) {
        }
        return 0;
    }

    public static String getVersion(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return pInfo.versionName;
        } catch (NameNotFoundException e) {
            return "1.0.0";
        }
    }

    public static String getFolderSize(String path) {
        try {
            long lSize = folderSize(new File(path));
            return getReadableSize(lSize);
        } catch (Exception e) {
            return "0 B";
        }
    }

    private static long folderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }

    private static String getReadableSize(long size) {
        if (size <= 0)
            return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }


    public static String getQuery(List<NameValue> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValue pair : params) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    /**
     * @param connection object; note: before calling this function,
     *                   ensure that the connection is already be open, and any writes to
     *                   the connection's output stream should have already been completed.
     * @return String containing the body of the connection response or
     * null if the input stream could not be read correctly
     */
    private String readInputStreamToString(HttpURLConnection connection) {
        String result = null;
        StringBuilder sb = new StringBuilder();
        InputStream is = null;

        try {
            is = new BufferedInputStream(connection.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String inputLine = "";
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            result = sb.toString();
        } catch (Exception e) {
            result = null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }

        return result;
    }

    public static String getFileSize(long size) {
        if (size <= 0)
            return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static String GetWebPage(String sAddress) throws IOException {
        URL url = new URL(sAddress);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        int responseCode;

        con.setConnectTimeout(10000);
        con.setReadTimeout(10000);

        responseCode = con.getResponseCode();

        if (responseCode == 200) {
            InputStream is = con.getInputStream();
            String result = convertStreamToString(is);
            con.disconnect();
            return result;
        }

        return "";
    }

    public static String bodyToString(final RequestBody request) {
        try {
            final Buffer buffer = new Buffer();
            if (request != null)
                request.writeTo(buffer);
            else
                return "";
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }

}
