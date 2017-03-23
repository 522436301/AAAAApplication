package com.example.a52243.aaaaapplication;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Field;

public class MainActivity extends Activity {
    private Button button;
    private ImageView mIvHead;//更换的图片

    private View mGoneView;// 参照物
    private PopupWindow mPopupWindow; // popwindow
    private View mpopview; // 弹出框的布局
    private Bitmap photo; // 保存头像的Bitmap对象
    private static final int RESULT_PICK_PHOTO_CAMERA = 1; // 退出
    private static int CAMERA_RESULT = 100; // 拍照 标识码
    private static int LOAD_IMAGE_RESULT = 200; // 相册 标识码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.button1);
        mGoneView = findViewById(R.id.gone_view);
        mIvHead = (ImageView) findViewById(R.id.image_head);

        photo = FileUtill.readerByteArrayToSD(); // 读取保存的图片
        if (photo != null) {
            mIvHead.setImageBitmap(photo);
        } else {
            mIvHead.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));
        }
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub
                showPopupWindon();
            }
        });
    }

    /**
     * 弹出PopupWindow
     */
    private void showPopupWindon() {
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;// px
        LayoutInflater inflater = LayoutInflater.from(this);
        mpopview = inflater.inflate(R.layout.activity_choose_picture, null);// 加载动画布局
        mPopupWindow = new PopupWindow(mpopview, width, height - dip2px(50)
                + getStatusBarHeight());// 设置布局在屏幕中显示的位置，并且获取焦点
        // 设置PopupWindow的显示样式
        mPopupWindow.setAnimationStyle(R.style.PopupAnimation);
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0x00000000);
        // 设置SelectPicPopupWindow弹出窗体的背景
        mPopupWindow.setBackgroundDrawable(dw);
        backgroundAlpha(this, 0.1f);// 设置半透明0.0-1.0
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(false);// 设置不允许在外点击消失
        // 设置当mPopupWindow取消时，界面恢复原来的颜色 不是可透明的
        mPopupWindow.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                // TODO Auto-generated method stub
                backgroundAlpha(MainActivity.this, 1f);// 不透明
            }
        });

        mPopupWindow.showAsDropDown(mGoneView);// 弹出的mPopupWindow左上角正对mGoneView的左下角
        // 偏移量默认为0,0

        Button mTakePhotoBt = (Button) mpopview
                .findViewById(R.id.button_take_photo);// 拍照
        Button mChoicePhotoBt = (Button) mpopview
                .findViewById(R.id.button_choice_photo);// 相册选择
        Button mChoiceCancelBt = (Button) mpopview
                .findViewById(R.id.button_choice_cancel);// 取消
        // 拍照
        mTakePhotoBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
                // destoryImage();
                File saveDirFile = new File(Environment
                        .getExternalStorageDirectory(), "" + "temp.jpg");
                Intent intentCamera = new Intent(
                        MediaStore.ACTION_IMAGE_CAPTURE);// 调用系统相机拍照
                intentCamera.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(saveDirFile));
                startActivityForResult(intentCamera, CAMERA_RESULT);
            }
        });
        // 从相册选择
        mChoicePhotoBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mPopupWindow.dismiss();// 取消弹窗
                // content://media/external/images/media 图片地址
                // Intent.ACTION_PICK 从图片中选择一张 并返回选择的图片
                Intent intentPhotoAlbum = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intentPhotoAlbum, LOAD_IMAGE_RESULT);// 请求码200
                // 打开相册
            }
        });
        // 取消
        mChoiceCancelBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 拍照
        if (requestCode == CAMERA_RESULT && resultCode == RESULT_OK) {
            File temp = new File(Environment.getExternalStorageDirectory()
                    + "/" + "" + "temp.jpg");
            startPhotoZoom(Uri.fromFile(temp));
        }
        // 相册选择
        if (requestCode == LOAD_IMAGE_RESULT && data != null
                && data.getData() != null) {
            // Uri selectedImage = data.getData();
            startPhotoZoom(data.getData());
        }
        // 取消
        if (requestCode == RESULT_PICK_PHOTO_CAMERA && data != null
                && data.getExtras() != null) {
            Bundle extras = data.getExtras();
            Bitmap photo = extras.getParcelable("data");
            mIvHead.setImageBitmap(photo);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 60, stream);
            byte[] bytes = stream.toByteArray();
            mIvHead.setTag(bytes);
            String filePath = Environment.getExternalStorageDirectory() + "/"
                    + "" + "temp.jpg";
            FileUtill.writeByteArrayToSD(filePath, bytes, true);
        }
    }

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param context
     * @param bgAlpha (透明度 取值返回0-1, 0全透明,1不透明)
     * @date 2016年8月6日
     */
    public void backgroundAlpha(Activity context, float bgAlpha) {
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        context.getWindow()
                .addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        context.getWindow().setAttributes(lp);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    private int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 裁剪图片方法实现
     */
    @SuppressLint("NewApi")
    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1); // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 200);
        intent.putExtra("outputY", 200);
        intent.putExtra("scale", true);// 黑边
        intent.putExtra("scaleUpIfNeeded", true);// 黑边
        intent.putExtra("return-data", true);
        startActivityForResult(intent, RESULT_PICK_PHOTO_CAMERA);
    }

    /**
     * 获取状态栏高速的方法
     *
     * @return
     * @date 2016年8月7日
     */
    private int getStatusBarHeight() {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, sbar = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            sbar = getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        return sbar;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
