package com.arieljin.library.abs;

import org.json.JSONObject;

/**
 * @time 2018/8/16.
 * @email ariel.jin@tom.com
 */
public class AbsDBModel extends AbsModel{

    public String uid;//primary key
    public String byUser;//primary key

    public AbsDBModel() {
    }

    public AbsDBModel(JSONObject jsonObject) {
        super(jsonObject);
    }
}
