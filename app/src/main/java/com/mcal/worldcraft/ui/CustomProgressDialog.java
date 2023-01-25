package com.mcal.worldcraft.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.mcal.worldcraft.activity.OptionActivity;
import com.mcal.worldcraft.factories.DescriptionFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class CustomProgressDialog extends ProgressDialog {
    public CustomProgressDialog(Context context) {
        super(context);
        setProgressStyle(1);
        setTitle("Loading world..");
        setMessage("Loading chunks");
        setCancelable(true);
        setProgress(0);
        setButton(OptionActivity.CANCEL, (dialog, which) -> buttonClick());
    }

    public void updateMax(int loadLimit) {
        setMax(loadLimit);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Method method = TextView.class.getMethod("setVisibility", Integer.TYPE);
            Field[] fields = getClass().getSuperclass().getDeclaredFields();
            for (Field field : fields) {
                if (field.getName().equalsIgnoreCase("mProgressNumber")) {
                    field.setAccessible(true);
                    TextView textView = (TextView) field.get(this);
                    method.invoke(textView, 8);
                }
            }
        } catch (Exception e) {
            Log.e(DescriptionFactory.emptyText, "Failed to invoke the progressDialog method 'setVisibility' and set 'mProgressNumber' to GONE.", e);
        }
    }

    public void buttonClick() {
    }
}
