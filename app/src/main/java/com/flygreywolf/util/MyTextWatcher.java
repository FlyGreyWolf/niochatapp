package com.flygreywolf.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public abstract class MyTextWatcher implements TextWatcher {

    public EditText editText;
    int temp;

    public MyTextWatcher(EditText editText) {
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        temp = editText.getLineCount();
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        int scrollRange = editText.getLineCount();
        if (scrollRange != temp) {
            temp = scrollRange;
            setHeightChange();
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    protected abstract void setHeightChange();
}
