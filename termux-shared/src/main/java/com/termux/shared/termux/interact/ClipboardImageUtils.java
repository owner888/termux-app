package com.termux.shared.termux.interact;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.termux.shared.logger.Logger;
import com.termux.shared.termux.TermuxConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ClipboardImageUtils {

    private static final String LOG_TAG = "ClipboardImageUtils";

    public static final String CLIPBOARD_IMAGE_DIR_PATH = TermuxConstants.TERMUX_HOME_DIR_PATH + "/.cache/clipboard";

    /**
     * Check if the clipboard primary clip contains an image. If found, save it to Termux
     * clipboard cache directory (~/.cache/clipboard/clip_yyyyMMdd_HHmmss.png) and return the absolute path.
     *
     * @param context The Context for operations
     * @return The local file path if an image was saved, or null if no image in clipboard.
     */
    @Nullable
    public static String saveClipboardImageToFileIfSet(Context context) {
        if (context == null) return null;

        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager == null) return null;

        ClipData clipData = clipboardManager.getPrimaryClip();
        if (clipData == null || clipData.getItemCount() == 0) return null;

        ClipDescription description = clipData.getDescription();
        ClipData.Item item = clipData.getItemAt(0);
        if (item == null) return null;

        Uri uri = item.getUri();
        String mimeType = null;
        if (description != null && description.hasMimeType("image/*")) {
            for (int i = 0; i < description.getMimeTypeCount(); i++) {
                String type = description.getMimeType(i);
                if (type != null && type.startsWith("image/")) {
                    mimeType = type;
                    break;
                }
            }
        } else if (uri != null) {
            try {
                mimeType = context.getContentResolver().getType(uri);
            } catch (Exception e) {
                Logger.logError(LOG_TAG, "Failed to resolve MIME type for uri: " + uri);
            }
        }

        if (uri != null && mimeType != null && mimeType.startsWith("image/")) {
            return saveImageStreamToFile(context, uri, mimeType);
        }

        return null;
    }

    /**
     * Save image data from Content Uri to Termux cache directory.
     *
     * @param context The Context for operations
     * @param uri The Content Uri of the image
     * @param mimeType The MIME type of the image (e.g. image/png, image/jpeg)
     * @return The absolute path of the saved file, or null on error.
     */
    @Nullable
    public static String saveImageStreamToFile(Context context, Uri uri, String mimeType) {
        if (context == null || uri == null) return null;

        String extension = "png";
        if (mimeType != null) {
            if (mimeType.contains("jpeg") || mimeType.contains("jpg")) extension = "jpg";
            else if (mimeType.contains("webp")) extension = "webp";
            else if (mimeType.contains("gif")) extension = "gif";
        }

        File cacheDir = new File(CLIPBOARD_IMAGE_DIR_PATH);
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            Logger.logError(LOG_TAG, "Failed to create clipboard cache dir: " + cacheDir.getAbsolutePath());
            return null;
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File destFile = new File(cacheDir, "clip_" + timeStamp + "." + extension);
        int counter = 1;
        while (destFile.exists()) {
            destFile = new File(cacheDir, "clip_" + timeStamp + "_" + counter + "." + extension);
            counter++;
        }

        try (InputStream in = context.getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(destFile)) {
            if (in == null) return null;
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
            Logger.logInfo(LOG_TAG, "Saved clipboard image to: " + destFile.getAbsolutePath());
            return destFile.getAbsolutePath();
        } catch (Exception e) {
            Logger.logError(LOG_TAG, "Failed to save image from uri: " + uri + ", error: " + e.getMessage());
            return null;
        }
    }

}
