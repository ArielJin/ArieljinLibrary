package com.arieljin.library.abs;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * @time 2018/8/16.
 * @email ariel.jin@tom.com
 */
public class AbsModel implements Serializable {


    public AbsModel() {
    }

    public AbsModel(JSONObject jsonObject) {
        if (jsonObject != null) {
            parse(jsonObject);
        }
    }

    protected void parse(JSONObject jsonObject) {

    }
}
