package com.example.draws;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        LinearLayout layout=(LinearLayout) findViewById(R.id.root);
        final DrawView view=new DrawView(MainActivity.this);

        view.setMinimumHeight(500);
        view.setMinimumWidth(300);
        //通知view组件重绘
        view.invalidate();
        layout.addView(view);

    }
}
