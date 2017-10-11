package com.ldnet.activity.access;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import com.ldnet.goldensteward.R;
import com.ldnet.service.BaseService;

/**
 * Created by zjy on 2017/9/29
 */

public class GoodsRecordFragment extends Fragment implements View.OnClickListener {

    private SwipeRefreshLayout refreshLayout;
    private ListView listView;
    private Button addInviteCard;

    public GoodsRecordFragment() {
    }

   public static GoodsRecordFragment newInstance() {
        return new GoodsRecordFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accesss_control_record, container, false);
        initView(view);
        return view;
    }


    private void initView(View view) {
        addInviteCard = (Button) view.findViewById(R.id.btn_add_invite_visitor);
        listView = (ListView) view.findViewById(R.id.lv_account_detail);

        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.account_detail_scrollview);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

            }
        });

        addInviteCard.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_add_invite_visitor:
                Intent intent=new Intent(getActivity(),AddGoodsApplyActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                break;
        }
    }

    Handler getDataHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BaseService.DATA_SUCCESS:

                    break;
                case BaseService.DATA_FAILURE:
                case BaseService.DATA_REQUEST_ERROR:
                    Toast.makeText(getActivity(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}
