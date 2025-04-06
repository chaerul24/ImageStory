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
import com.bumptech.glide.request.transition.Transition;

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

    public void setScale(ImageScale.ImageScaleType scale) {
        switch (scale) {
            case CENTER:
                imageView.setScaleType(ImageView.ScaleType.CENTER);
                break;
            case CENTER_CROP:
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                break;
            case CENTER_INSIDE:
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                break;
            case FIT_CENTER:
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                break;
            case FIT_START:
                imageView.setScaleType(ImageView.ScaleType.FIT_START);
                break;
            case FIT_END:
                imageView.setScaleType(ImageView.ScaleType.FIT_END);
                break;
            case FIT_XY:
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                break;
        }
    }


    public interface DownloadCallback {
        void onDownloaded(String message);
        void onError(String error);
    }

    public void setCheckFile(final String urlImage, final String path, final DownloadCallback callback) {
        final String fileName = getFileNameFromUrl(urlImage);
        File file = new File(path, fileName);

        if (file.exists()) {
            Glide.with(getContext())
                    .load(file)
                    .into(imageView);

            if (callback != null) {
                callback.onDownloaded("File sudah ada");
            }
        } else {
            Glide.with(getContext())
                    .asBitmap()
                    .load(urlImage)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            imageView.setImageBitmap(resource);

                            // Simpan ke storage
                            saveImageToDownloads(resource, path, fileName, callback);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            if (callback != null) {
                                callback.onError("Gagal download gambar dari URL");
                            }
                        }
                    });
        }
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
