package com.ldnet.activity.access;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ldnet.activity.base.BaseActionBarActivity;
import com.ldnet.entities.CommunityRoomInfo;
import com.ldnet.entities.MyProperties;
import com.ldnet.entities.Rooms;
import com.ldnet.goldensteward.R;
import com.ldnet.service.AccessControlService;
import com.ldnet.service.BaseService;
import com.ldnet.service.CommunityService;
import com.ldnet.utility.ListViewAdapter;
import com.ldnet.utility.Utility;
import com.ldnet.utility.ViewHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.R.attr.tag;

public class AddVisitorInviteActivity extends BaseActionBarActivity implements RadioGroup.OnCheckedChangeListener {

    private EditText editTextName, editTextTel, editTextCarNo, editTextOtherReason, editTextDate;
    private RadioGroup radioGroupIsDriving;
    private RadioButton rbIsDriving, rbIsNotDriving;
    private Spinner spinnerReaason, spinnerCommunity;
    private Button btnSubmit;
    private ImageButton imageButtonBack;
    private TextView textViewTitle,tvCarTitle;
    private ArrayAdapter<String> adapter1;
    private ListViewAdapter<CommunityRoomInfo> adapter2;
    private List<CommunityRoomInfo> communityList = new ArrayList<>();

    private String paramsReason;
    private String paramsIsDriving;
    private String paramsID;
    private CommunityRoomInfo selectRoom;
    private String paramsName;
    private String paramsTel;


    private boolean other;

    private AccessControlService service;
    private CommunityService communityService;

    private String tag = AddVisitorInviteActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_visitor);

        service = new AccessControlService(AddVisitorInviteActivity.this);
        communityService = new CommunityService(AddVisitorInviteActivity.this);
        communityService.getMyCommunity(getCommunityHandler);
        initView();
        initEvent();
    }

    private void initView() {
        editTextCarNo = (EditText) findViewById(R.id.ed_add_invite_visitor_car_no);
        editTextDate = (EditText) findViewById(R.id.ed_add_invite_visitor_date);
        editTextName = (EditText) findViewById(R.id.ed_add_invite_visitor_name);
        editTextTel = (EditText) findViewById(R.id.ed_add_invite_visitor_tel);
        editTextOtherReason = (EditText) findViewById(R.id.ed_add_invite_visitor_other);
        spinnerCommunity = (Spinner) findViewById(R.id.spinner_add_good_community);
        spinnerReaason = (Spinner) findViewById(R.id.spinner_add_good_reason);
        textViewTitle = (TextView) findViewById(R.id.tv_page_title);
        imageButtonBack = (ImageButton) findViewById(R.id.btn_back);
        btnSubmit = (Button) findViewById(R.id.ed_add_invite_visitor_submit);
        radioGroupIsDriving = (RadioGroup) findViewById(R.id.rg_is_driving);
        rbIsDriving = (RadioButton) findViewById(R.id.radio_button_is_driving);
        tvCarTitle=(TextView)findViewById(R.id.ed_add_invite_visitor_car_no_title);
        textViewTitle.setText("添加访客");


        radioGroupIsDriving.setOnCheckedChangeListener(this);
        imageButtonBack.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
        editTextDate.setOnClickListener(this);

    }


    private void initEvent() {

        //来访原因
        adapter1 = new ArrayAdapter<String>(AddVisitorInviteActivity.this, R.layout.dropdown_check_item,
                getResources().getStringArray(R.array.invite_visitor_reason));
        spinnerReaason.setAdapter(adapter1);
        spinnerReaason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                List<String> list = Arrays.asList(getResources().getStringArray(R.array.invite_visitor_reason));
                paramsReason = list.get(position);
                Log.e(tag, "reason:" + paramsReason);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        //进出小区选择
        adapter2 = new ListViewAdapter<CommunityRoomInfo>(AddVisitorInviteActivity.this, R.layout.item_drop_down, communityList) {
            @Override
            public void convert(ViewHolder holder, CommunityRoomInfo communityRoomInfo) {
                holder.setText(R.id.tv_community_room, communityRoomInfo.getCommunityName() + " " + communityRoomInfo.getRoomName());
            }
        };
        spinnerCommunity.setAdapter(adapter2);

        spinnerCommunity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectRoom = communityList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }


    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.ed_add_invite_visitor_submit:

                break;
            case R.id.btn_back:
                finish();
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.radio_button_not_driving) {
            editTextCarNo.setVisibility(View.GONE);
            tvCarTitle.setVisibility(View.GONE);
            paramsIsDriving = "false";
        } else if (checkedId == R.id.radio_button_is_driving) {
            editTextCarNo.setVisibility(View.VISIBLE);
            tvCarTitle.setVisibility(View.VISIBLE);
            paramsIsDriving = "true";
        }
    }

    //提交数据
    private void submit() {
        if (editTextName.getText() != null && !TextUtils.isEmpty(editTextName.getText())) {
            if (editTextTel.getText() != null && !TextUtils.isEmpty(editTextTel.getText())) {

                if (rbIsDriving.isChecked() && (editTextCarNo.getText() != null || TextUtils.isEmpty(editTextCarNo.getText()))) {

                    showToast("请输入汽车牌照");
                } else if (!rbIsDriving.isChecked() || (rbIsDriving.isChecked() && editTextCarNo.getText() != null && !TextUtils.isEmpty(editTextCarNo.getText()))) {

                    if (other && editTextOtherReason.getText() == null || TextUtils.isEmpty(editTextOtherReason.getText())) {
                        showToast("请输入其他来访事由");
                    } else if (!other || (other == true && editTextOtherReason.getText() != null && !TextUtils.isEmpty(editTextOtherReason.getText()))) {


                        //可提交
                        paramsID = Utility.generateGUID();
                        service.addAccessInvite(paramsID, "0", "卡芙卡", "18603419370", "2017-10-03 00:00:00", "作客", "false", "", addRecordHandler);

                    }
                }

            } else {
                showToast("请输入电话");
            }
        } else {
            showToast("请输入姓名");
        }
    }


    Handler addRecordHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BaseService.DATA_SUCCESS:
                    Toast.makeText(AddVisitorInviteActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                    //根据ID获取二维码图片


                    break;
            }
        }
    };


    Handler getCommunityHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case BaseService.DATA_SUCCESS:
                    List<MyProperties> list = (List<MyProperties>) msg.obj;
                    communityList = getNewCommunity(list);
                    adapter2.notifyDataSetChanged();
                    break;
            }
        }
    };


    private List<CommunityRoomInfo> getNewCommunity(List<MyProperties> list) {
        List<CommunityRoomInfo> communityList = new ArrayList<>();

        for (MyProperties myProperties : list) {

            List<Rooms> roomsList = myProperties.getRooms();

            if (roomsList != null && roomsList.size() > 0) {

                for (Rooms rooms : roomsList) {
                    CommunityRoomInfo info = new CommunityRoomInfo(myProperties.getCommunityId(), myProperties.getName(), rooms.RoomId, rooms.getAbbreviation());
                    communityList.add(info);
                }
            }
        }

        return communityList;
    }

}
