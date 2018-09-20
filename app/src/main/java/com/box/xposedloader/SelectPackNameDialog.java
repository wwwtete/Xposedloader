package com.box.xposedloader;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangw on 2018/9/19.
 */
public class SelectPackNameDialog extends DialogFragment {

    public static SelectPackNameDialog newInstance(int type) {
        Bundle args = new Bundle();
        SelectPackNameDialog fragment = new SelectPackNameDialog();
        args.putInt("type",type);
        fragment.setArguments(args);
        return fragment;
    }

    RecyclerView mRecyclerView;
    TextView mTvTitle;
    int mType;
    List<PackageInfo> mApps;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_select_packname, container, false);
        mTvTitle = view.findViewById(R.id.tv_title);
        mRecyclerView = view.findViewById(R.id.recyclerview);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mType = getArguments().getInt("type");
        mTvTitle.setText(mType == 0 ? "选择要Hook的目标Apk" : "选择要Load的Xposed插件Apk");
        getApps();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(new SelectPackNameAdapter());

    }

    private void getApps() {
        PackageManager pm = getActivity().getPackageManager();
        mApps = new ArrayList<>();
        for (PackageInfo pk : pm.getInstalledPackages(0)) {
            if ((pk.applicationInfo.flags & 1) == 0){
//                if (mType == 1){
//                    Bundle metaData = pk.applicationInfo.metaData;
//                    if (metaData != null && metaData.getBoolean("xposedmodule",false)) {
//                        mApps.add(pk);
//                    }
//                }else {
                    mApps.add(pk);
//                }
            }
        }
    }


    class SelectPackNameAdapter extends RecyclerView.Adapter{

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PackNameVH(LayoutInflater.from(getActivity()).inflate(R.layout.item_packname,parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((PackNameVH)holder).onBind(mApps.get(position));
        }

        @Override
        public int getItemCount() {
            if (mApps == null)
                return 0;
            else
                return mApps.size();
        }
    }


    class PackNameVH extends RecyclerView.ViewHolder{

        public TextView mTvName;
        private PackageInfo mData;

        public PackNameVH(View itemView) {
            super(itemView);
            mTvName = itemView.findViewById(R.id.tv_name);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mType == 1){
                        MainActivity.SP.edit()
                                .putString(StrConstants.KEY_HOOK_CLASS,mData.packageName+StrConstants.DEFAULT_CLASS_NAME).apply();
//                        MainActivity.SP.edit()
//                                .putString(StrConstants.KEY_HOOK_MEHTOD,MainActivity.SP.getString(StrConstants.KEY_HOOK_CLASS,"")+StrConstants.DEFAULT_METHOD_NAME).apply();
                        onSave(StrConstants.KEY_XPOSED_APK);
                    }else {
                        onSave(StrConstants.KEY_TARGET_APK);
                       SelectPackNameDialog.newInstance(1)
                               .show(getFragmentManager(),"select_xposed_apk_dialog");
                    }
                    ((MainActivity)getActivity()).update();
                    dismiss();
                }
            });
        }

        private void onSave(String key) {
            MainActivity.SP.edit()
                    .putString(key,mData.packageName).apply();

        }

        public void onBind(PackageInfo packageInfo) {
            mData = packageInfo;
            mTvName.setText(mData.packageName);
        }
    }


}
