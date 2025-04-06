package id.chaerul.library.imagestory;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import id.chaerul.library.imageviewcustom.R;

import androidx.cardview.widget.CardView;

public class ImageViewCustom extends FrameLayout {
    private ImageView imageView;
    private CardView cardView;
    private int jumlahBorder = 0;
    private boolean[] readStatuses = new boolean[0];
    private int borderColor = Color.GREEN;        // Warna status belum dibaca
    private int borderColorHint = Color.GRAY;     // Warna status sudah dibaca
    private float borderWidth = 8f;               // Lebar garis border

    public ImageViewCustom(Context context) {
        super(context);
        init(context, null);
    }

    public ImageViewCustom(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ImageViewCustom(Context context, AttributeSet attrs, int defStyleAttr) {
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

            jumlahBorder = typedArray.getInt(R.styleable.ImageViewCustom_jumlahBorder, 0);

            borderColor = typedArray.getColor(R.styleable.ImageViewCustom_borderColor, Color.GREEN);
            borderColorHint = typedArray.getColor(R.styleable.ImageViewCustom_borderColorHint, Color.GRAY);

            typedArray.recycle();
        }
    }

    public void setJumlahBorder(int jumlahBorder) {
        this.jumlahBorder = jumlahBorder;

        readStatuses = new boolean[jumlahBorder];
        for (int i = 0; i < jumlahBorder; i++) {
            readStatuses[i] = false; // default semua belum dibaca
        }

        invalidate();
    }

    public void setRead(boolean[] read) {
        if (read != null && read.length == jumlahBorder) {
            this.readStatuses = read;
            invalidate(); // Redraw
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
        float spaceBetween = 6f;
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
