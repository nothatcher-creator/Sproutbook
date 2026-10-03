package com.nothatcher.sproutbook;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Test-only image barrier, using platform/Java classes in the separate test APK process. */
public final class HomeBackgroundTestProvider extends ContentProvider {
    public static final String AUTHORITY = "com.nothatcher.sproutbook.test.home-background";
    private static final class ReadBarrier {
        final byte[] image;
        final CountDownLatch requested = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        ReadBarrier(byte[] image) { this.image = image; }
    }
    private final ConcurrentHashMap<String, ReadBarrier> reads = new ConcurrentHashMap<>();
    @Override public boolean onCreate() { return true; }
    @Override public Bundle call(String method, String arg, Bundle extras) {
        if (arg == null) throw new IllegalArgumentException("Image token missing");
        Bundle result = new Bundle();
        ReadBarrier read;
        switch (method) {
            case "arm":
                byte[] image = extras == null ? null : extras.getByteArray("image");
                if (image == null) throw new IllegalArgumentException("Image bytes missing");
                read = reads.put(arg, new ReadBarrier(image));
                if (read != null) read.release.countDown();
                break;
            case "await_read":
                read = reads.get(arg);
                if (read == null) throw new IllegalArgumentException("Image not armed");
                try { result.putBoolean("waiting", read.requested.await(5, TimeUnit.SECONDS)); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException(e); }
                break;
            case "release":
                read = reads.get(arg);
                if (read != null) read.release.countDown();
                break;
            case "clear":
                read = reads.remove(arg);
                if (read != null) read.release.countDown();
                break;
            default: throw new IllegalArgumentException("Unknown image barrier action");
        }
        return result;
    }
    @Override public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        if (!"r".equals(mode)) throw new FileNotFoundException("Test image is read only");
        final ReadBarrier read = reads.get(uri.getLastPathSegment());
        if (read == null) throw new FileNotFoundException("Test image was not armed");
        final ParcelFileDescriptor[] pipe;
        try { pipe = ParcelFileDescriptor.createPipe(); }
        catch (IOException e) { throw new FileNotFoundException(e.getMessage()); }
        Thread writer = new Thread(() -> {
            try (ParcelFileDescriptor.AutoCloseOutputStream output = new ParcelFileDescriptor.AutoCloseOutputStream(pipe[1])) {
                read.requested.countDown();
                if (read.release.await(15, TimeUnit.SECONDS)) output.write(read.image);
            } catch (IOException ignored) { /* Cancellation closes the reader. */ }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "home-background-test-pipe");
        writer.setDaemon(true);
        writer.start();
        return pipe[0];
    }
    @Override public String getType(Uri uri) { return "image/png"; }
    @Override public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) { return null; }
    @Override public Uri insert(Uri uri, ContentValues values) { throw new UnsupportedOperationException(); }
    @Override public int delete(Uri uri, String selection, String[] selectionArgs) { return 0; }
    @Override public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) { return 0; }
}
