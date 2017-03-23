package com.example.a52243.aaaaapplication;

import java.io.InputStream;
import java.lang.reflect.Field;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * ���Ŷ���������
 * 
 */
public class GifImageView extends ImageView implements OnClickListener {
  
  private Movie mMovie;//���Ŷ�����Ҫ�õ��ģ�ϵͳ��
  private int mImageWidth;//������imageview�Ŀ��
  private int mImageHeight;//����imageview�ĸ߶�
  private long mMovieStart = 0;// ���ſ�ʼ
  private boolean isAutoPlay;//�Ƿ��Զ�����
  private Bitmap mStartPlay;//��ʼ��ť
  private boolean isPlaying=false;//��¼�Ƿ����ڲ���
	/**
	 * Scaling factor to fit the animation within view bounds.
	 */
  private float mScale;

	/**
	 * Scaled movie frames width and height.
	 */
  private int mMeasuredGifWidth;
  private int mMeasuredGifHeight;

  public GifImageView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context, attrs);
  }
  public GifImageView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public GifImageView(Context context) {
    super(context);
  }

  private void init(Context context, AttributeSet attrs) {
    TypedArray attributes = context.obtainStyledAttributes(attrs,R.styleable.GifImageView);
    // ͨ�������ò�����src����Դid,����gif�ļ���Ҫ���ڲ��ֵ�src��
    int resourceId = getResourceId(attributes, context, attrs);
    if (resourceId != 0) {
      // ˵����gif����
      // 1.��resourcesId�����
      // 2.��Move��decode������
      // 3.���bitmap�ĳ���
      InputStream is = getResources().openRawResource(resourceId);
      mMovie = Movie.decodeStream(is);
      if (mMovie != null) {
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        mImageWidth = bitmap.getWidth();
        mImageHeight = bitmap.getHeight();
        // �����ͷ�
        bitmap.recycle();
        // ����Ƿ������Զ����ţ�����������Զ����ţ����ʼ�����Ű�ť
        isAutoPlay = attributes.getBoolean(R.styleable.GifImageView_auto_play, false);
        if (!isAutoPlay) {
          mStartPlay = BitmapFactory.decodeResource(getResources(),R.drawable.start_play);
          setOnClickListener(this);
        }      
      }
    }
    //������Դ
    if (attributes != null) {
        attributes.recycle();
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    if (mMovie != null) {
		/*
		 * Calculate horizontal scaling
		 */
		float scaleW = 1f;
		int measureModeWidth = MeasureSpec.getMode(widthMeasureSpec);
		if (measureModeWidth != MeasureSpec.UNSPECIFIED) {
			int maximumWidth = MeasureSpec.getSize(widthMeasureSpec);
			scaleW = (float) mImageWidth / (float) maximumWidth;
		}
		/*
		 * calculate vertical scaling
		 */
		float scaleH = 1f;
		int measureModeHeight = MeasureSpec.getMode(heightMeasureSpec);
		if (measureModeHeight != MeasureSpec.UNSPECIFIED) {
			int maximumHeight = MeasureSpec.getSize(heightMeasureSpec);
			scaleH = (float) mImageHeight / (float) maximumHeight;
		}
		/*
		 * calculate overall scale
		 */
		mScale = 1f / Math.max(scaleH, scaleW);
		mMeasuredGifWidth = (int) (mImageWidth * mScale);
		mMeasuredGifHeight = (int) (mImageHeight * mScale);
        setMeasuredDimension(mMeasuredGifWidth, mMeasuredGifHeight);
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (mMovie == null) {
      // mMovie����null��˵��������ͨ��ͼƬ����ֱ�ӵ��ø����onDraw()����
      super.onDraw(canvas);
    } else {
        // mMovie������null��˵������GIFͼƬ
      if (isAutoPlay) {
        // ��������Զ����ţ��Ͳ���
        playMovie(canvas);
        invalidate();
      } else {
        // �������Զ����ŵĻ�
        // 1.�ж��Ƿ����ڲ���
        // 2.��õ�һ֡��ͼ��
        // 3.Ȼ����Ӳ��Ű�ť
        if (isPlaying) {
          // ������ڲ��ž�playmoive��������
          if (playMovie(canvas)) {
            isPlaying = false;
          }
          invalidate();
        } else {
          // ��һ֡
          mMovie.setTime(0);
          canvas.save(Canvas.MATRIX_SAVE_FLAG);
          canvas.scale(mScale, mScale);
          mMovie.draw(canvas, 0, 0);// ��
          canvas.restore();
          // ���ƿ�ʼ��ť
          int offsetW = (mMeasuredGifWidth - mStartPlay.getWidth()) / 2;
          int offsetH = (mMeasuredGifHeight - mStartPlay.getHeight()) / 2;
          canvas.drawBitmap(mStartPlay, offsetW, offsetH, null);
        }
      }
    }
  }

  /**
   * ����gif����
   * 
   * @param canvas
   */
  private boolean playMovie(Canvas canvas) {
    // 1.��ȡ���ŵ�ʱ��
    // 2.�����ʼstart=0������Ϊ�ǿ�ʼ
    // 3.��¼���ŵ�ʱ��
    // 4.���ý���
    // 5.������
    // 6.���ʱ������˲��ŵ�ʱ�䣬��֤������
    long now = SystemClock.uptimeMillis();
    if (mMovieStart == 0) {
      mMovieStart = now;
    }
    int duration = mMovie.duration();
    if (duration == 0) {
      duration = 1000;
    }
    //��¼gif�����˶���ʱ��
    int relTime = (int) ((now - mMovieStart) % duration);
    mMovie.setTime(relTime);// ����ʱ��
    canvas.save(Canvas.MATRIX_SAVE_FLAG);
    canvas.scale(mScale, mScale);
    mMovie.draw(canvas, 0, 0);// ��
    canvas.restore();
    if ((now - mMovieStart) >= duration) {
      // ����
      mMovieStart = 0;
      return true;
    }
    return false;
  }



  /**
   * ͨ�������ò�����src����Դid
   * 
   * @param attrs
   * @param context
   * @param attributes
   */
  private int getResourceId(TypedArray attributes, Context context, AttributeSet attrs) {
    try {
      Field filed = TypedArray.class.getDeclaredField("mValue");
      filed.setAccessible(true);
      TypedValue typeValue = (TypedValue) filed.get(attributes);
      return typeValue.resourceId;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return 0;
  }

  @Override
  public void onClick(View v) {
    if(v.getId()==getId()){
      isPlaying=true;
      invalidate();
    }
  }
}