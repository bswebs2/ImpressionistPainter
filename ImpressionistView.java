package edu.umd.hcil.impressionistpainter434;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Random;

/**
 * Created by jon on 3/20/2016.
 */
public class ImpressionistView extends View {

    private ImageView _imageView;

    private Canvas _offScreenCanvas = null;
    private Bitmap _offScreenBitmap = null;
    private Paint _paint = new Paint();
    private VelocityTracker vt = VelocityTracker.obtain();

    private int _alpha = 150;
    private float _defaultRadius = 7.5f;
    private Point _lastPoint = null;
    private long _lastPointTime = -1;
    private boolean _useMotionSpeedForBrushStrokeSize = true;
    private Paint _paintBorder = new Paint();
    private BrushType _brushType = BrushType.Square;
    private float _minBrushRadius = 5;


    private int savedNum = 1;
    private boolean inverted = false;

    public ImpressionistView(Context context) {
        super(context);
        init(null, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    /**
     * Because we have more than one constructor (i.e., overloaded constructors), we use
     * a separate initialization method
     * @param attrs
     * @param defStyle
     */
    private void init(AttributeSet attrs, int defStyle){

        // Set setDrawingCacheEnabled to true to support generating a bitmap copy of the view (for saving)
        // See: http://developer.android.com/reference/android/view/View.html#setDrawingCacheEnabled(boolean)
        //      http://developer.android.com/reference/android/view/View.html#getDrawingCache()
        this.setDrawingCacheEnabled(true);

        _paint.setColor(Color.RED);
        _paint.setAlpha(_alpha);
        _paint.setAntiAlias(true);
        _paint.setStyle(Paint.Style.FILL);
        _paint.setStrokeWidth(4);

        _paintBorder.setColor(Color.BLACK);
        _paintBorder.setStrokeWidth(3);
        _paintBorder.setStyle(Paint.Style.STROKE);
        _paintBorder.setAlpha(50);

        //_paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh){

        Bitmap bitmap = getDrawingCache();
        Log.v("onSizeChanged", MessageFormat.format("bitmap={0}, w={1}, h={2}, oldw={3}, oldh={4}", bitmap, w, h, oldw, oldh));
        if(bitmap != null) {
            _offScreenBitmap = getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
            _offScreenCanvas = new Canvas(_offScreenBitmap);
        }
    }

    /**
     * Sets the ImageView, which hosts the image that we will paint in this view
     * @param imageView
     */
    public void setImageView(ImageView imageView){
        _imageView = imageView;
    }

    /**
     * Sets the brush type. Feel free to make your own and completely change my BrushType enum
     * @param brushType
     */
    public void setBrushType(BrushType brushType){
        _brushType = brushType;
    }

    /**
     * Clears the painting
     */
    public void clearPainting(){
        //TODO
        _offScreenCanvas.drawColor(Color.WHITE);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(_offScreenBitmap != null) {
            canvas.drawBitmap(_offScreenBitmap, 0, 0, _paint);
        }

        // Draw the border. Helpful to see the size of the bitmap in the ImageView
        canvas.drawRect(getBitmapPositionInsideImageView(_imageView), _paintBorder);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){
        Bitmap b = _imageView.getDrawingCache();

        //TODO
        //Basically, the way this works is to listen for Touch Down and Touch Move events and determine where those
        //touch locations correspond to the bitmap in the ImageView. You can then grab info about the bitmap--like the pixel color--
        //at that location
        float curX = motionEvent.getX();
        float curY = motionEvent.getY();
        float velocity = 0.0f;

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                int historySize = motionEvent.getHistorySize();

                for (int i = 0; i < historySize; i++) {

                    float touchX = motionEvent.getHistoricalX(i);
                    float touchY = motionEvent.getHistoricalY(i);
                    if (_brushType == BrushType.Square) {
                        _offScreenCanvas.drawRect(touchX, touchY, touchX + 25.0f, touchY + 25.0f, _paint);
                    } else if (_brushType == BrushType.Circle) {
                        _offScreenCanvas.drawCircle(touchX, touchY, _defaultRadius, _paint);
                    }
                }
                if (curX >= 0 && curX < getWidth() && curY >= 0 && curY < getHeight()) {
                    int pix = b.getPixel((int) curX, (int) curY);
                    int rgb = Color.rgb(Color.red(pix), Color.green(pix), Color.blue(pix));
                    int c = 0;
                    if (inverted) {
                        float[] hsv = new float[3];
                        Color.RGBToHSV(Color.red(pix), Color.green(pix),
                                Color.blue(pix), hsv);
                        hsv[0] = (hsv[0] + 180) % 360;
                        c = Color.HSVToColor(hsv);
                        _paint.setColor(c);
                    } else {
                        _paint.setColor(rgb);
                    }
                    _paint.setAlpha(_alpha);
                    if (_brushType == BrushType.Square) {
                        _offScreenCanvas.drawRect(curX, curY, curX + 15.0f, curY + 15.0f, _paint);
                    } else if (_brushType == BrushType.Circle) {
                        _offScreenCanvas.drawCircle(curX, curY, _defaultRadius, _paint);
                    } else if (_brushType == BrushType.Triangle) {
                        vt.addMovement(motionEvent);
                        vt.computeCurrentVelocity(1000);
                        velocity = (float) Math.sqrt((float) Math.pow(vt.getXVelocity(), 2) +
                                (float) Math.pow(vt.getYVelocity(), 2));
                        if (curX >= velocity / 150.0f && curX < getWidth() - velocity / 150.0f && curY < getHeight() - velocity / 150.0f) {
                            Path path = new Path();
                            path.moveTo(curX, curY);
                            path.lineTo(curX - velocity / 150.0f, curY + velocity / 150.0f);
                            path.lineTo(curX + velocity / 150.0f, curY + velocity / 150.0f);
                            path.lineTo(curX, curY);
                            path.close();
                            _offScreenCanvas.drawPath(path, _paint);
                        } else if (curX >= 5.0f && curX < getWidth() - 5.0f && curY < getHeight() - 5.0f) {
                            Path path = new Path();
                            path.moveTo(curX, curY);
                            path.lineTo(curX - 5.0f, curY + 5.0f);
                            path.lineTo(curX + 5.0f, curY + 5.0f);
                            path.lineTo(curX, curY);
                            path.close();
                            _offScreenCanvas.drawPath(path, _paint);
                        }
                    }
                    invalidate();
                }
            case MotionEvent.ACTION_UP:
                break;
        }

        return true;
    }




    /**
     * This method is useful to determine the bitmap position within the Image View. It's not needed for anything else
     * Modified from:
     *  - http://stackoverflow.com/a/15538856
     *  - http://stackoverflow.com/a/26930938
     * @param imageView
     * @return
     */
    private static Rect getBitmapPositionInsideImageView(ImageView imageView){
        Rect rect = new Rect();

        if (imageView == null || imageView.getDrawable() == null) {
            return rect;
        }

        // Get image dimensions
        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int widthActual = Math.round(origW * scaleX);
        final int heightActual = Math.round(origH * scaleY);

        // Get image position
        // We assume that the image is centered into ImageView
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (int) (imgViewH - heightActual)/2;
        int left = (int) (imgViewW - widthActual)/2;

        rect.set(left, top, left + widthActual, top + heightActual);

        return rect;
    }

    public void saveImage() {
        File sdcard = Environment.getExternalStorageDirectory();
        if (sdcard != null) {
            File mediaDir = new File(sdcard, "DCIM/Camera");
            if (!mediaDir.exists()) {
                mediaDir.mkdirs();
            }
        }
        Bitmap b = _offScreenBitmap;
        if (MediaStore.Images.Media.insertImage(getContext().getContentResolver(), b, "Painting" + savedNum,
                "This is painting number " + savedNum++) == null) {
            Toast.makeText(getContext(), "Image failed to save.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Image saved.", Toast.LENGTH_SHORT).show();
        }
    }

    public void invert() {
        if (inverted) {
            inverted = false;
            Toast.makeText(getContext(), "Colors are no longer inverted.", Toast.LENGTH_SHORT).show();
        } else {
            inverted = true;
            Toast.makeText(getContext(), "Colors are now inverted.", Toast.LENGTH_SHORT).show();
        }
    }
}
