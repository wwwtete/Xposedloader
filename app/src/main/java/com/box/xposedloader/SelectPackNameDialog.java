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
        mTvTitle.setText(mType == 0 ? StrConstants.SELECTHOOKTARGET : StrConstants.SELECTXPOSEDPLUGIN);
        getApps();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(new SelectPackNameAdapter());

    }

    private void getApps() {
        PackageManager pm = getActivity().getPackageManager();
        mApps = new ArrayList<>();
        Bundle metaData=null;
        boolean isXposedPlugin = false;
        for (PackageInfo pk : pm.getInstalledPackages(0)) {
            if ((pk.applicationInfo.flags & 1) == 0){
                try {
                    metaData = pm.getApplicationInfo(pk.packageName,PackageManager.GET_META_DATA).metaData;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                isXposedPlugin = metaData != null && metaData.getBoolean("xposedmodule",false);
                if (mType == 1){
                    if (isXposedPlugin && !getActivity().getPackageName().equals(pk.packageName)) {
                        mApps.add(pk);
                    }
                }else if (!isXposedPlugin){
                    mApps.add(pk);
                }
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
        public TextView mTvPackage;
        public TextView mTvVersion;
        private PackageInfo mData;

        public PackNameVH(View itemView) {
            super(itemView);
            mTvName = itemView.findViewById(R.id.tv_name);
            mTvPackage = itemView.findViewById(R.id.tv_packagename);
            mTvVersion = itemView.findViewById(R.id.tv_version_name);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mType == 1){
                        MainActivity.SP.edit()
                                .putString(StrConstants.KEY_HOOK_CLASS,mData.packageName+StrConstants.DEFAULT_CLASS_NAME)
                                .putString(StrConstants.KEY_HOOK_MEHTOD,StrConstants.DEFAULT_METHOD_NAME)
                                .apply();
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
            mTvName.setText(SysUtils.getAppName(getActivity(),mData.packageName));
            mTvVersion.setText("v "+ mData.versionName);
            mTvPackage.setText(mData.packageName);
        }
    }


}
