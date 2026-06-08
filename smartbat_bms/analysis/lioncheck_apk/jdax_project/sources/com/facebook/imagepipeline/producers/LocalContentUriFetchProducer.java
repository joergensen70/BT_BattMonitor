package com.facebook.imagepipeline.producers;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import com.facebook.common.logging.FLog;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.memory.BitmapCounterProvider;
import com.facebook.imagepipeline.memory.PooledByteBufferFactory;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imageutils.JfifUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes.dex */
public class LocalContentUriFetchProducer extends LocalFetchProducer {
    private static final float ACCEPTABLE_REQUESTED_TO_ACTUAL_SIZE_RATIO = 1.3333334f;
    private static final int NO_THUMBNAIL = 0;
    static final String PRODUCER_NAME = "LocalContentUriFetchProducer";
    private final ContentResolver mContentResolver;
    private static final Class<?> TAG = LocalContentUriFetchProducer.class;
    private static final String DISPLAY_PHOTO_PATH = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "display_photo").getPath();
    private static final String[] PROJECTION = {"_id", "_data"};
    private static final String[] THUMBNAIL_PROJECTION = {"_data"};
    private static final Rect MINI_THUMBNAIL_DIMENSIONS = new Rect(0, 0, 512, BitmapCounterProvider.MAX_BITMAP_COUNT);
    private static final Rect MICRO_THUMBNAIL_DIMENSIONS = new Rect(0, 0, 96, 96);

    public LocalContentUriFetchProducer(Executor executor, PooledByteBufferFactory pooledByteBufferFactory, ContentResolver contentResolver, boolean z) {
        super(executor, pooledByteBufferFactory, z);
        this.mContentResolver = contentResolver;
    }

    @Override // com.facebook.imagepipeline.producers.LocalFetchProducer
    protected EncodedImage getEncodedImage(ImageRequest imageRequest) throws IOException {
        EncodedImage cameraImage;
        InputStream inputStreamOpenContactPhotoInputStream;
        Uri sourceUri = imageRequest.getSourceUri();
        if (!isContactUri(sourceUri)) {
            return (!isCameraUri(sourceUri) || (cameraImage = getCameraImage(sourceUri, imageRequest.getResizeOptions())) == null) ? getEncodedImage(this.mContentResolver.openInputStream(sourceUri), -1) : cameraImage;
        }
        if (sourceUri.toString().endsWith("/photo")) {
            inputStreamOpenContactPhotoInputStream = this.mContentResolver.openInputStream(sourceUri);
        } else {
            inputStreamOpenContactPhotoInputStream = ContactsContract.Contacts.openContactPhotoInputStream(this.mContentResolver, sourceUri);
            if (inputStreamOpenContactPhotoInputStream == null) {
                throw new IOException("Contact photo does not exist: " + sourceUri);
            }
        }
        return getEncodedImage(inputStreamOpenContactPhotoInputStream, -1);
    }

    private static boolean isContactUri(Uri uri) {
        return "com.android.contacts".equals(uri.getAuthority()) && !uri.getPath().startsWith(DISPLAY_PHOTO_PATH);
    }

    private static boolean isCameraUri(Uri uri) {
        String string = uri.toString();
        return string.startsWith(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString()) || string.startsWith(MediaStore.Images.Media.INTERNAL_CONTENT_URI.toString());
    }

    @Nullable
    private EncodedImage getCameraImage(Uri uri, ResizeOptions resizeOptions) throws IOException {
        EncodedImage thumbnail;
        Cursor cursorQuery = this.mContentResolver.query(uri, PROJECTION, null, null, null);
        if (cursorQuery == null) {
            return null;
        }
        try {
            if (cursorQuery.getCount() == 0) {
                return null;
            }
            cursorQuery.moveToFirst();
            String string = cursorQuery.getString(cursorQuery.getColumnIndex("_data"));
            if (resizeOptions != null && (thumbnail = getThumbnail(resizeOptions, cursorQuery.getInt(cursorQuery.getColumnIndex("_id")))) != null) {
                thumbnail.setRotationAngle(getRotationAngle(string));
                return thumbnail;
            }
            if (string != null) {
                return getEncodedImage(new FileInputStream(string), getLength(string));
            }
            return null;
        } finally {
            cursorQuery.close();
        }
    }

    private EncodedImage getThumbnail(ResizeOptions resizeOptions, int i) throws Throwable {
        int thumbnailKind = getThumbnailKind(resizeOptions);
        Cursor cursor = null;
        if (thumbnailKind == 0) {
            return null;
        }
        try {
            Cursor cursorQueryMiniThumbnail = MediaStore.Images.Thumbnails.queryMiniThumbnail(this.mContentResolver, i, thumbnailKind, THUMBNAIL_PROJECTION);
            if (cursorQueryMiniThumbnail == null) {
                if (cursorQueryMiniThumbnail != null) {
                    cursorQueryMiniThumbnail.close();
                }
                return null;
            }
            try {
                cursorQueryMiniThumbnail.moveToFirst();
                if (cursorQueryMiniThumbnail.getCount() > 0) {
                    String string = cursorQueryMiniThumbnail.getString(cursorQueryMiniThumbnail.getColumnIndex("_data"));
                    if (new File(string).exists()) {
                        EncodedImage encodedImage = getEncodedImage(new FileInputStream(string), getLength(string));
                        if (cursorQueryMiniThumbnail != null) {
                            cursorQueryMiniThumbnail.close();
                        }
                        return encodedImage;
                    }
                }
                if (cursorQueryMiniThumbnail != null) {
                    cursorQueryMiniThumbnail.close();
                }
                return null;
            } catch (Throwable th) {
                th = th;
                cursor = cursorQueryMiniThumbnail;
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
        }
    }

    private static int getThumbnailKind(ResizeOptions resizeOptions) {
        if (isThumbnailBigEnough(resizeOptions, MICRO_THUMBNAIL_DIMENSIONS)) {
            return 3;
        }
        return isThumbnailBigEnough(resizeOptions, MINI_THUMBNAIL_DIMENSIONS) ? 1 : 0;
    }

    static boolean isThumbnailBigEnough(ResizeOptions resizeOptions, Rect rect) {
        return ((float) resizeOptions.width) <= ((float) rect.width()) * ACCEPTABLE_REQUESTED_TO_ACTUAL_SIZE_RATIO && ((float) resizeOptions.height) <= ((float) rect.height()) * ACCEPTABLE_REQUESTED_TO_ACTUAL_SIZE_RATIO;
    }

    private static int getLength(String str) {
        if (str == null) {
            return -1;
        }
        return (int) new File(str).length();
    }

    @Override // com.facebook.imagepipeline.producers.LocalFetchProducer
    protected String getProducerName() {
        return PRODUCER_NAME;
    }

    private static int getRotationAngle(String str) {
        if (str == null) {
            return 0;
        }
        try {
            return JfifUtil.getAutoRotateAngleFromOrientation(new ExifInterface(str).getAttributeInt("Orientation", 1));
        } catch (IOException e) {
            FLog.e(TAG, e, "Unable to retrieve thumbnail rotation for %s", str);
            return 0;
        }
    }
}
