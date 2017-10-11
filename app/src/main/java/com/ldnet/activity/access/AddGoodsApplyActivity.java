package com.ldnet.activity.access;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.ldnet.activity.base.BaseActionBarActivity;
import com.ldnet.goldensteward.R;

import static com.ldnet.map.ChString.To;

public class AddGoodsApplyActivity extends BaseActionBarActivity {

    private EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_goods_apply);
        Toast.makeText(this,"添加物品",Toast.LENGTH_SHORT).show();
    }
}
