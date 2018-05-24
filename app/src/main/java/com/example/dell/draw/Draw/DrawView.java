package com.example.dell.draw.Draw;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Environment;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DrawView extends View{
    private int view_width=0;//屏幕的宽度
    private int view_height=0;//屏幕的高度
    private float preX;//起始点的x坐标
    private float preY;//起始点的y坐标
    private Path path;//路径
    public Paint paint ;//画笔
    Bitmap cacheBitmap=null;//定义一个内存中的图片，该图片将作为缓冲区
    Canvas cacheCanvas=null;//定义cacheBitmap上的Canvas对象
    private  Context context;
    /*
     * 功能：构造方法
     * */
    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context =context;
        view_width=context.getResources().getDisplayMetrics().widthPixels;//获取屏幕宽度
        view_height=context.getResources().getDisplayMetrics().heightPixels;//获取屏幕高度
        //创建一个与该View相同大小的缓存区
        cacheBitmap=Bitmap.createBitmap(view_width,view_height, Bitmap.Config.ARGB_8888);
        cacheCanvas=new Canvas();//创建一个新的画布
        path=new Path();
        //在cacheCanvas上绘制cacheBitmap
        cacheCanvas.setBitmap(cacheBitmap);
        paint=new Paint(Paint.DITHER_FLAG);//Paint.DITHER_FLAG防抖动的
        paint.setColor(Color.RED);
        //设置画笔风格
        paint.setStyle(Paint.Style.STROKE);//设置填充方式为描边
        paint.setStrokeJoin(Paint.Join.ROUND);//设置笔刷转弯处的连接风格
        paint.setStrokeCap(Paint.Cap.ROUND);//设置笔刷的图形样式(体现在线的端点上)
        paint.setStrokeWidth(5);//设置默认笔触的宽度为1像素
        paint.setAntiAlias(true);//设置抗锯齿效果
        paint.setDither(true);//使用抖动效果
    }

    /*
     * 功能：重写onDraw方法
     * */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(0xFFFFFFFF);//设置背景色
//        Paint bmpPaint=new Paint();//采用默认设置创建一个画笔
        canvas.drawBitmap(cacheBitmap, 0, 0,paint);//绘制cacheBitmap// 用来临时保存绘制的图形
        canvas.drawPath(path, paint);//绘制路径
        canvas.save(Canvas.ALL_SAVE_FLAG);//保存canvas的状态
        //恢复canvas之前保存的状态，防止保存后对canvas执行的操作对后续的绘制有影响
        canvas.restore();

        drawText(cacheCanvas,"画图：",150,150,60,paint);
        drawCirCle(cacheCanvas,200,300,50,paint);
        drawTriangle(cacheCanvas,100,400,100,500,200,400,paint);
        drawLine(cacheCanvas,100,600,700,600,paint);

    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //获取触摸事件发生的位置
        float x=event.getX();
        float y=event.getY();
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                //将绘图的起始点移到(x,y)坐标点的位置
                path.moveTo(x, y);
                preX=x;
                preY=y;
                break;
            case MotionEvent.ACTION_MOVE:
                //保证横竖绘制距离不能超过625
                float dx=Math.abs(x-preX);
                float dy=Math.abs(y-preY);
                if(dx>5||dy>5){
                    //.quadTo贝塞尔曲线，实现平滑曲线(对比lineTo)
                    //x1，y1为控制点的坐标值，x2，y2为终点的坐标值
                    path.quadTo(preX, preY, (x+preX)/2, (y+preY)/2);
                    preX=x;
                    preY=y;
                }
                break;
            case MotionEvent.ACTION_UP:
                cacheCanvas.drawPath(path, paint);//绘制路径
                path.reset();
                break;
        }
        invalidate();
        return true;//返回true,表明处理方法已经处理该事件
    }
    public void clear(){
        //设置图形重叠时的处理方式
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        //设置笔触的宽度
        paint.setStrokeWidth(50);
    }
    public void save(){
        try{
            saveBitmap(getCurrentTime()+"-myPitcture");
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     *
     *      获取当前时间  来构建  文件名
     * @return
     */
    public  String getCurrentTime(){
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd:hh:mm:ss");
        String str = dateFormat.format(date);
        return  str;
    }
    private void saveBitmap(String fileName) throws IOException {
//        File file=new File(getSDPath()+fileName+".png");
        File file=new File(getPath()+fileName+".png");
        file.createNewFile();
        FileOutputStream fileOS=new FileOutputStream(file);
        //将绘图内容压缩为PNG格式输出到输出流对象中
        cacheBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOS);
        fileOS.flush();//将缓冲区中的数据全部写出到输出流中
        fileOS.close();//关闭文件输出流对象
        Toast.makeText(context,"保存成功",Toast.LENGTH_SHORT).show();
    }

    //  获取内部目录
     public String getPath(){
         File file = context.getFilesDir();
         return file.getPath();
     }

    //获得目录
    public String getSDPath(){
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在

        if (sdCardExist)  //如果SD卡存在，则获取跟目录
        {
            sdDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);//获取数据目录
//            sdDir = Environment.getDataDirectory();
        }
        return sdDir.toString();

    }
    /**
     *
     *      画文本
     */
    public void drawText(Canvas canvas,String text,int x, int y,float textSize,Paint bmpPaint){
        bmpPaint.setTextSize(textSize);//  size  以xp为单位
        canvas.drawText("画图：", x, y, bmpPaint); //  x y  为基线的坐标
    }
    /**
     *     画圆
     */
    public  void drawCirCle(Canvas canvas,int x,int y,int rad,Paint bmpPaint){
        bmpPaint.setStyle(Paint.Style.STROKE);// 设置空心
        bmpPaint.setAntiAlias(true);// 设置画笔的锯齿效果。 true是去除，大家一看效果就明白了
        canvas.drawCircle(x, y, rad, bmpPaint);// x y 是坐标  rad 是半径
    }
    /**
     *   画三角
     *
     */
    public  void drawTriangle(Canvas canvas,int x1,int y1, int x2 ,int y2,int x3,  int y3,Paint bmpPaint){
        Path path = new Path();
        path.moveTo(x1, y1);// 此点为多边形的起点
        path.lineTo(x2, y2);
        path.lineTo(x3, y3);
        path.close(); // 使这些点构成封闭的多边形
        canvas.drawPath(path, bmpPaint);
    }
    /**
     *      画线
     *
     */
    public  void  drawLine(Canvas canvas ,int startX, int startY ,int endX,int endY ,Paint bmpPaint){
        canvas.drawLine(startX, startY, endX, endY, bmpPaint);// 画线
    }
//    /**
//     * 画上升直线
//     */
//    public  void drawAscendingLine(){
//
//    }
//
//    /**
//     *    画下降直线
//     */
//    public  void drawDescendingLine(){
//
//    }
//    /**
//     *  画水平直线
//     */
//    public  void drawHorizontalLine(){
//
//    }

}