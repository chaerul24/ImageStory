package id.chaerul.library.imagestory;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import id.chaerul.library.imageviewcustom.R;

public class ImageStory extends FrameLayout {
    private ImageView imageView;
    private CardView cardView;
    private int jumlahBorder = 0;
    private boolean[] readStatuses = new boolean[0];
    private int borderColor = Color.GREEN;
    private int borderColorHint = Color.GRAY;
    private float borderWidth = 8f;
    private DownloadCallback downloadCallback;

    public ImageStory(Context context) {
        super(context);
        init(context, null);
    }

    public ImageStory(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ImageStory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setWillNotDraw(false);
        View view = LayoutInflater.from(context).inflate(R.layout.imageviewcustom, this, true);
        imageView = view.findViewById(R.id.image_custom_view);
        cardView = view.findViewById(R.id.card_view_custom);

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ImageViewCustom);
            int imageRes = typedArray.getResourceId(R.styleable.ImageViewCustom_src, -1);
            if (imageRes != -1) {
                imageView.setImageResource(imageRes);
            }
            float radius = typedArray.getDimension(R.styleable.ImageViewCustom_radius, 0f);
            cardView.setRadius(radius);
            borderWidth = typedArray.getDimension(R.styleable.ImageViewCustom_borderWidth, 8f);
            cardView.setCardElevation(typedArray.getDimension(R.styleable.ImageViewCustom_elevation, 0f));
            jumlahBorder = typedArray.getInt(R.styleable.ImageViewCustom_countBorder, 0);
            borderColor = typedArray.getColor(R.styleable.ImageViewCustom_borderColor, Color.GREEN);
            borderColorHint = typedArray.getColor(R.styleable.ImageViewCustom_borderColorHint, Color.GRAY);
            typedArray.recycle();
        }
    }

    public void setImageUrlDownload(String urlImage, String path, DownloadCallback callback) {
        this.downloadCallback = callback;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (callback != null) callback.onError("Izin WRITE_EXTERNAL_STORAGE belum diberikan.");
                return;
            }
        }
        downloadFile(urlImage, path, callback);
    }

    public interface DownloadCallback {
        void onDownloaded(String message);
        void onError(String error);
    }

    private void downloadFile(String urlImage, String path, DownloadCallback callback) {
        String fileName = getFileNameFromUrl(urlImage);

        Glide.with(getContext())
                .asBitmap()
                .load(urlImage)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        if (callback != null) callback.onError("Gagal memuat gambar dari URL.");
                    }

                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap, @Nullable com.bumptech.glide.request.transition.Transition<? super Bitmap> transition) {
                        saveImageToDownloads(bitmap, path, fileName, callback);
                    }
                });
    }

    private void saveImageToDownloads(Bitmap bitmap, String path, String fileName, DownloadCallback callback) {
        OutputStream outputStream;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = getContext().getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri imageUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
                if (imageUri != null) {
                    outputStream = resolver.openOutputStream(imageUri);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.close();
                    if (callback != null) callback.onDownloaded("Gambar berhasil diunduh ke folder Download");
                    imageView.setImageBitmap(bitmap);
                }
            } else {
                File file = new File(path, fileName);
                outputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.close();
                if (callback != null) callback.onDownloaded("Gambar berhasil disimpan");
                imageView.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null) callback.onError("Gagal menyimpan gambar: " + e.getMessage());
        }
    }

    private String getFileNameFromUrl(String url) {
        try {
            return Uri.parse(url).getLastPathSegment();
        } catch (Exception e) {
            e.printStackTrace();
            return "downloaded_image_" + System.currentTimeMillis() + ".jpg";
        }
    }

    public void setCountBorder(int jumlahBorder) {
        this.jumlahBorder = jumlahBorder;
        readStatuses = new boolean[jumlahBorder];
        for (int i = 0; i < jumlahBorder; i++) {
            readStatuses[i] = false;
        }
        invalidate();
    }

    public void setRead(boolean[] read) {
        if (read != null && read.length == jumlahBorder) {
            this.readStatuses = read;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(borderWidth);

        int padding = 12;
        RectF bounds = new RectF(padding, padding, getWidth() - padding, getHeight() - padding);

        int totalSegment = jumlahBorder;
        float spaceBetween = (totalSegment > 1) ? 6f : 0f;
        float sweepAngle = (360f - (totalSegment * spaceBetween)) / totalSegment;

        for (int i = 0; i < totalSegment; i++) {
            float startAngle = i * (sweepAngle + spaceBetween);

            if (readStatuses.length > i && readStatuses[i]) {
                paint.setColor(borderColorHint); // Sudah dibaca
            } else {
                paint.setColor(borderColor);     // Belum dibaca
            }

            canvas.drawArc(bounds, startAngle, sweepAngle, false, paint);
        }
    }
}
