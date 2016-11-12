package com.luleo.www.wxhongbao;

/**
 * Created by leo on 2016/2/5.
 */

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName="tab_member")
public class Member implements Serializable {

    @DatabaseField(generatedId = true,useGetSet=true)
    private int id;

    @DatabaseField(useGetSet=true,canBeNull=false)
    private String keyCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(String keyCode) {
        this.keyCode = keyCode;
    }
}
