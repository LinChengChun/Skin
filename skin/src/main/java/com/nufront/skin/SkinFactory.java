package com.nufront.skin;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by LinChengChun on 2020/6/28.
 * Author Name: 林程淳
 * GitHub: https://github.com/LinChengChun
 * Package Name: com.nufront.skin
 */
public class SkinFactory implements LayoutInflater.Factory2 {
    private final String TAG = "SkinFactory";
    private final static String androidSystemPackages[] = { // 系统的包名特点，一般都是这几个包下面的
            "android.view.",
            "android.widget.",
            "android.webkit."
    };
    private final static String supportAttributeNames[] = {
            "background", "textColor", "src"
    };
    private List<SkinView> skinViews = new ArrayList<>(); // 存储支持换肤的控件

    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String s, @NonNull Context context, @NonNull AttributeSet attributeSet) {
        View view = null;
        view = onCreateView(s, context, attributeSet);
        return view;
    }

    /**
     * 通过反射查找View的构造方法，并创建View
     *
     * @param packageName  包名
     * @param name         类名
     * @param context      上下文
     * @param attributeSet 属性集合
     * @return 构建完对应的View
     */
    private View build(String packageName, String name, Context context, AttributeSet attributeSet) {
        View view = null;

        try {
            Class<? extends View> aClass = (Class<? extends View>) context.getClassLoader().loadClass(TextUtils.isEmpty(packageName) ? name : packageName + name);
            Constructor<? extends View> constructor = aClass.getConstructor(Context.class, AttributeSet.class);
            view = constructor.newInstance(context, attributeSet);
        } catch (Exception e) {
//            e.printStackTrace();
//            Log.e(TAG, e.getMessage());
        }
        return view;
    }

    /**
     * 用于搜集 一个View里面 可更换皮肤的属性
     *
     * @param view
     * @param attributeSet
     */
    private void parseView(View view, Context context, AttributeSet attributeSet) {
        int count = attributeSet.getAttributeCount();

        List<SkinItem> skinItems = new ArrayList<>();//存储View可更换皮肤属性的集合

        for (int i = 0; i < count; i++) {
            String attrName = attributeSet.getAttributeName(i);//属性名
            String attrValue = attributeSet.getAttributeValue(i);//属性值

            if (!isSupportAttribute(attrName)) continue;

            if (attrValue.charAt(0) == '@') {//也就是引用类型，形如@color/red
                int id = Integer.parseInt(attrValue.substring(1));//资源的id
                String typeName = context.getResources().getResourceTypeName(id);//资源类型，例如color、drawable
                String entryName = context.getResources().getResourceEntryName(id);//资源名，例如text_color_selector
                Log.e(TAG, String.format("%s %s %s %s", attrName, attrValue, entryName, typeName));

                SkinItem item = new SkinItem(attrName, id, typeName, entryName);
                skinItems.add(item);
            }
        }

        if (!skinItems.isEmpty()) {
            SkinView skinView = new SkinView(view, skinItems);
            skinViews.add(skinView);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        View view = null;
        if (-1 == name.indexOf('.')) { // 不带点的，说明是系统的控件
            for (String packageName : androidSystemPackages) {
                view = build(packageName, name, context, attrs);
                if (null != view)
                    break;
            }
        } else { // 带点的说明是 自定义控件
            view = build(null, name, context, attrs);
        }
        if (null != view) {
            parseView(view, context, attrs);
        }
        return view;
    }

    /**
     * 判断是否 支持换肤的属性
     *
     * @param name
     * @return true or false
     */
    private boolean isSupportAttribute(String name) {
        for (String attributeName : supportAttributeNames) {
            if (attributeName.contains(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 对收集到的控件进行换肤
     */
    public void apply() {
        for (SkinView view : skinViews) {
            view.apply();
        }
    }

    private static class SkinItem {
        String name;
        int resId;
        String typeName;
        String entryName;

        public SkinItem(String name, int resId, String typeName, String entryName) {
            this.name = name;
            this.resId = resId;
            this.typeName = typeName;
            this.entryName = entryName;
        }

        public String getName() {
            return name;
        }

        public int getResId() {
            return resId;
        }

        public String getTypeName() {
            return typeName;
        }

        public String getEntryName() {
            return entryName;
        }
    }

    class SkinView {
        View view;
        List<SkinItem> skinItems;

        public SkinView(View view, List<SkinItem> skinItems) {
            this.view = view;
            this.skinItems = skinItems;
        }

        /**
         * 应用主题
         */
        public void apply() {
            for (SkinItem skinItem : skinItems) {

                switch (skinItem.getName()){
                    case "background":
                        if (skinItem.getTypeName().contains("color")){
                            int trueColor = SkinManager.getInstance().getColor(skinItem.getResId());
                            view.setBackgroundColor(trueColor);
                        }else if (skinItem.getTypeName().contains("drawable")){
                            int resId = SkinManager.getInstance().getDrawableId(skinItem.getResId());
                            view.setBackgroundResource(resId);
                        }
                        break;
                    case "textColor":
                        if (skinItem.getTypeName().contains("color")){
                            int resId = SkinManager.getInstance().getColor(skinItem.getResId());
                            ((TextView)view).setTextColor(resId);
                        }
                        break;
                    case "src":
                        ((ImageView) view).setImageDrawable(SkinManager.getInstance().getDrawable(skinItem.getResId()));
                        break;
                    default:break;
                }
            }
        }


    }
}
