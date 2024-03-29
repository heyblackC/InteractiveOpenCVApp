package com.heyblack.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private DrawView Draw1 = null;//画板视图
    private DrawView Draw2 = null;//画板视图dummy
    private Button btn1 = null;
    private Button btn2 = null;
    private Button btn3 = null;
    private Button btn4 = null;
    private Button btn5 = null;
    private Button btn6 = null;
    private Button confirmBtn = null;
    private Button onlineCut = null;
    private ImageView underView = null;
    private int countNum = 0;
    public Bitmap bitmap=BitmapFactory.decodeStream(getClass().getResourceAsStream("/res/drawable/l58.png"));

    private int mode = 0;


    ImageView img;
    String path;
    String filepath;

    String interPath = "/data/data/com.heyblack.myapplication/ImgStorage";
    String exterPath;

    //风格化处理等待提示框、完成提示
    AlertDialog waitMsg,finishMsg;

    private static int RESULT_LOAD_IMAGE = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        underView = findViewById(R.id.imageView);

        waitMsg = new AlertDialog.Builder(this)
                .setTitle("请稍等")
                .setMessage("正在为您风格化中，可能需要30秒左右的处理时间")
                .setIcon(R.mipmap.ic_launcher)
                .setCancelable(false)
                .create();

        finishMsg = new AlertDialog.Builder(this)
                .setTitle("完成通知")
                .setMessage("您的图片风格化已完成")
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton("确定" ,  null )
                .create();


        //图版初始化、设置等操作
        inite();



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //用来处理上传图片的返回
    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = data.getString("value");
            // UI界面的更新等相关操作
            Draw1.resultMap = base64ToBitmap(val);
            if (Draw1.imageView != null) {

                Draw1.imageView.setImageBitmap(Draw1.resultMap);

                waitMsg.dismiss();
                finishMsg.show();
            }
        }
    };

    //用来处理在线graphCut的返回
    Handler handler2 = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = data.getString("value");
            // UI界面的更新等相关操作
            Draw1.resultMap = base64ToBitmap(val);
            if (Draw1.imageView != null) {

                Draw1.imageView.setImageBitmap(Draw1.resultMap);

            }
        }
    };


    private void inite(){
        Draw1=(DrawView)findViewById(R.id.writting);
        Draw2=(DrawView)findViewById(R.id.writting2);
        Draw2.dummy=true;

        btn1 = (Button)findViewById(R.id.button1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                img = (ImageView) findViewById(R.id.imageView1);

                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);

                //getPhoto();

                //if(countNum==2){
                //   countNum=0;
                //    getPhoto1();
                //}else {
                //    countNum++;
                //}
            }
        });

        btn2 = (Button)findViewById(R.id.button2);
        btn2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Draw1.changeSta(2);

                if (Draw1.resultMap == null) return;
                String img = bitmapToBase64(Draw1.resultMap);

                final String url = "http://120.79.67.94/finalversion.php";
                final Map<String,String> params = new HashMap<>();
                params.put("img",img);

                new Thread(new Runnable() {//创建子线程
                    @Override
                    public void run() {
                        NetWork netWork = new NetWork();
                        netWork.getwebinfo(url,params,handler);//把路径选到MainActivity中
                    }
                }).start();//启动子线程


                waitMsg.show();

            }
        });




        btn3 = (Button)findViewById(R.id.button3);
        btn3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Draw1.setcolor(Color.RED);
                Draw1.changeSta(2);
                mode = 0;
            }
        });

        confirmBtn = findViewById(R.id.confirm);
        confirmBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //开始切图
                switch (mode)
                {
                    case 0:
                        Draw1.onDrawImage();
                        break;
                    case 1:
                        Draw1.beginManualOperate();
                        break;
                }
            }
        });

        onlineCut = findViewById(R.id.onlineCut);
        onlineCut.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {



                //开始切图
                switch (mode)
                {
                    case 0:
                        final String url = "http://120.79.67.94/demo.php";
                        final Map<String,String> params = new HashMap<>();
                        Draw1.getPoint(params);

                        new Thread(new Runnable() {//创建子线程
                            @Override
                            public void run() {
                                NetWork netWork = new NetWork();
                                netWork.getwebinfo(url,params,handler2);//把路径选到MainActivity中
                            }
                        }).start();//启动子线程
                        break;
                    case 1:
                        break;
                }
            }
        });

        btn4 = (Button)findViewById(R.id.button4);
        btn4.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Draw1.setcolor(Color.BLACK);
                Draw1.changeSta(0);
                mode = 1;
            }
        });
        btn5 = (Button)findViewById(R.id.button5);
        btn5.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Draw1.setcolor(Color.WHITE);
                Draw1.changeSta(0);
                mode = 1;
            }
        });
        btn6 = (Button)findViewById(R.id.button6);
        btn6.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Draw1.clearall();
            }
        });


    }

    private  void getPhoto(){
        img = (ImageView) findViewById(R.id.imageView1);
        File sd = Environment.getExternalStorageDirectory();
        path = sd.getPath();//获得手机内存storage的位置
        filepath = path + "/Pictures/Screenshots/S90606-180018.JPG";//storage下需要全屏显示的图片路径（要根据自己手机中需要显示图片路径位置进行修改）
        File file = new File(filepath);
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<"+filepath);
        if (file.exists()) {
            System.out.println("<<<<<<<<<<<<<<<<<<<<<<<4");
            Bitmap bm = BitmapFactory.decodeFile(filepath);//获得设置路径下图片并编码为Bitmap格式

            System.out.println("<<<<<<<<<<<<<<<<<5");
            img.setImageBitmap(bm);//设置图片为背景图
        }
        else {
            System.err.println("<<<<<<<<<<<<<404 Not Find");//控制台输出没找到图片
        }
    }

    private void getPhoto1(){
        Intent intent2 = new Intent(Intent.ACTION_PICK, null);
        intent2.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        //intent2.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Environment.getExternalStorageDirectory()+change_path
        //        ,"temp.jpg")));
        startActivityForResult(intent2, 2);
        Uri data = intent2.getData();
        if (data != null) {
            //Draw1.setImageURI(data);
            //underView.setImageURI(data);
            //startPhotoZoom(intent2.getData());

            Bundle extras = intent2.getExtras();

                Bitmap photo = extras.getParcelable("data");
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                img.setImageBitmap(photo); //把图片显示在ImageView控件上

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 0)
            return;
        // 拍照
        if (requestCode == 1) {
            // 设置文件保存路径
            String change_path = "";
            String filePath = Environment.getExternalStorageDirectory()+change_path;
            File localFile = new File(filePath);
            if (!localFile.exists()) {
                localFile.mkdir();
            }
            File picture = new File(Environment.getExternalStorageDirectory()+change_path
                    + "/temp.jpg");
            startPhotoZoom(Uri.fromFile(picture));
        }
        if (data == null)
            return;
        // 读取相册缩放图片
        if (requestCode == 1) {
            startPhotoZoom(data.getData());
        }
        // 处理结果
        if (requestCode == 2) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap photo = extras.getParcelable("data");
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.JPEG, 75, stream);// (0-100)压缩文件
                //此处可以把Bitmap保存到sd卡中
                img.setImageBitmap(photo); //把图片显示在ImageView控件上
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            exterPath = picturePath;

            //使用兼容库就无需判断系统版本
            int hasWriteStoragePermission = ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.READ_EXTERNAL_STORAGE);
            if (hasWriteStoragePermission == PackageManager.PERMISSION_GRANTED) {

                bitmap = BitmapFactory.decodeFile(exterPath);
                img = (ImageView) findViewById(R.id.imageView1);
                img.setImageBitmap(bitmap);
                underView.setImageBitmap(bitmap);
                Draw1.bringToFront();
                Draw1.rawImg = bitmap;
                Draw1.resultMap = bitmap;
                Draw1.imageView = img;

            }else{
                //没有权限，向用户请求权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            }


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case 100: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    bitmap = BitmapFactory.decodeFile(exterPath);
                    img = (ImageView) findViewById(R.id.imageView1);
                    img.setImageBitmap(bitmap);
                    underView.setImageBitmap(bitmap);
                    Draw1.bringToFront();
                    Draw1.rawImg = bitmap;
                    Draw1.resultMap = bitmap;
                    Draw1.imageView = img;

                }
            }
        }

    }

    /**
     * 收缩图片
     *
     * @param uri
     */
    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 500);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, 3);
    }


    /**
     * 将Bitmap转换成Base64
     * @param bit
     * @return
     */
    private String bitmapToBase64(Bitmap bit)
    {
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        bit.compress(Bitmap.CompressFormat.JPEG, 40, bos);//参数100表示不压缩
        byte[] bytes=bos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    /**
     * 将Base64转换成Bitmap
     * @param base64String
     * @return
     */
    public static Bitmap base64ToBitmap(String base64String) {

        byte[] decode = Base64.decode(base64String, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(decode, 0, decode.length);

        return bitmap;
    }

    //创建文件夹
    public void createDirectory(){

        File destDir = new File(interPath);
        if (!destDir.exists()) {
            destDir.mkdirs();

        }
    }

    //保存风格化图片
    public void createImg(String img){

        //通过UUID生成字符串文件名
        String fileName = UUID.randomUUID().toString() + ".png";
        try {
            File file = new File(interPath + fileName);
            FileOutputStream out = new FileOutputStream(file);
            base64ToBitmap(img).compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
