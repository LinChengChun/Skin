package com.besmart.skin;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

/**
 * Created by LinChengChun on 2020/6/28.
 * Package Name: com.besmart.skin
 * 换肤思路：
 * 1.加载SD卡里面的主题包资源
 * 2.实现
 */
public class SkinManager {

    private static volatile SkinManager mInstance;
    private final String KEY = "key_resources_path";
    private final String TAG = "SkinManager";
    private SharedPreferences sp;
    private WeakReference<Context> mWeakRefContext;
    private Resources mResources = null;
    private String mResourcesPackageName;//皮肤的包名
    private String mResourcesPath;//皮肤路径

    private SkinManager() {
    }

    public static SkinManager getInstance() {
        if (mInstance == null) {
            synchronized (SkinManager.class) {
                if (mInstance == null) {
                    mInstance = new SkinManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 用于初始化换肤管理器
     *
     * @param ctx
     */
    public void init(Context ctx) {
        mWeakRefContext = new WeakReference<>(ctx);
        sp = ctx.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        Log.d(TAG, "用于初始化换肤管理器");
    }

    public String getLastResourcesPath(){
        return getCache(KEY);
    }

    public void clearCache(){
        saveCache(KEY, null);}
    /**
     * 从 缓存 里面读取配置信息
     * @param key
     * @return
     */
    private String getCache(String key){
        return sp.getString(key, null);
    }

    /**
     * 存储到缓存里面
     * @param key
     * @param value
     */
    private void saveCache(String key, String value){
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(key, value);
        edit.apply();
    }

    /**
     * 用于加载主题包
     * @param skinPkgPath
     * @return 返回加载主题包结果
     */
    public boolean load(String skinPkgPath) {
        boolean result = false;
        try {
            File file = new File(skinPkgPath);
            if (!file.exists()) {
                Log.e(TAG, String.format("主题包 %s 不存在!", skinPkgPath));
                return false;
            }

            PackageManager mPm = mWeakRefContext.get().getPackageManager();
            PackageInfo mInfo = mPm.getPackageArchiveInfo(skinPkgPath, PackageManager.GET_ACTIVITIES);
            mResourcesPackageName = mInfo.packageName; // 获取主题包的包名

            AssetManager assetManager = AssetManager.class.newInstance();

            Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
            addAssetPath.invoke(assetManager, skinPkgPath);


            Resources superRes = mWeakRefContext.get().getResources();
            Resources skinResource = new Resources(assetManager, superRes.getDisplayMetrics(), superRes.getConfiguration());
            mResources = skinResource;

            mResourcesPath = skinPkgPath;
            Log.d(TAG, String.format("主题包路径：%s 包名：%s", mResourcesPath, mResourcesPackageName));

            saveCache(KEY, skinPkgPath);
            result = true;
        } catch (Exception e) {
//            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            clearCache();
            result = false;
        }

        return result;
    }

    /**
     * 判断 是否配置了资源
     * @return
     */
    private boolean isResourcesEmpty(){
        return null == mResources;
    }

    /**
     * 根据属性值ID获取颜色值
     * @param resId
     * @return
     */
    public int getColor(int resId) {
        Resources originResources = mWeakRefContext.get().getResources();
        int originColor = originResources.getColor(resId);
        if (isResourcesEmpty()) {
            return originColor;
        }

        String resName = originResources.getResourceEntryName(resId);
        int trueResId = mResources.getIdentifier(resName, "color", mResourcesPackageName);
        int trueColor = 0;

        try {
            trueColor = mResources.getColor(trueResId);
        } catch (Resources.NotFoundException e) {
//            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            trueColor = originColor;
        }

        return trueColor;
    }

    public Drawable getDrawable(int resId) {
        Resources originResources = mWeakRefContext.get().getResources();
        String resName = originResources.getResourceEntryName(resId);
        String defType = originResources.getResourceTypeName(resId);
//        String packageName = mWeakRefContext.get().getPackageName();

        Drawable originDrawable = originResources.getDrawable(resId);
        if (isResourcesEmpty()) {
            return originDrawable;
        }

        int trueResId = mResources.getIdentifier(resName, defType, mResourcesPackageName);

        Drawable trueDrawable = null;
        try {
            if (android.os.Build.VERSION.SDK_INT < 22) {
                trueDrawable = mResources.getDrawable(trueResId);
            } else {
                trueDrawable = mResources.getDrawable(trueResId, null);
            }
        } catch (Resources.NotFoundException e) {
//            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            trueDrawable = originDrawable;
        }

        return trueDrawable;
    }

    public int getDrawableId(int resId) {
        Resources originResources = mWeakRefContext.get().getResources();

        String resName = originResources.getResourceEntryName(resId);
        String packageName = mWeakRefContext.get().getPackageName();

        int originResId = originResources.getIdentifier(resName, "drawable", packageName);

        if (isResourcesEmpty()) {
            return originResId;
        }

        int trueResId = mResources.getIdentifier(resName, "drawable", mResourcesPackageName);
        return trueResId;
    }
}
