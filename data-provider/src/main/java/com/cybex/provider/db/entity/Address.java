package com.cybex.provider.db.entity;

import android.support.annotation.NonNull;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

@Entity(nameInDb = "address")
public class Address implements Comparable<Address>{

    public static final int TYPE_TRANSFER = 1;//转账类型
    public static final int TYPE_WITHDRAW = 2;//提现类型

    @Id(autoincrement = true)
    @Property(nameInDb = "id")
    private Long id;

    @Property(nameInDb = "account")
    private String account;//账户名

    @Property(nameInDb = "token")
    private String token;//币种

    @Property(nameInDb = "address")
    private String address;//地址

    @Property(nameInDb = "note")
    private String note;//标签

    @Property(nameInDb = "memo")
    private String memo;//memo

    @Property(nameInDb = "type")
    private int type;//类型

    @Property(nameInDb = "createTime")
    private long createTime;//创建时间


    @Generated(hash = 1619031669)
    public Address(Long id, String account, String token, String address, String note, String memo,
            int type, long createTime) {
        this.id = id;
        this.account = account;
        this.token = token;
        this.address = address;
        this.note = note;
        this.memo = memo;
        this.type = type;
        this.createTime = createTime;
    }

    @Generated(hash = 388317431)
    public Address() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccount() {
        return this.account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNote() {
        return this.note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getMemo() {
        return this.memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    @Override
    public int compareTo(@NonNull Address address) {
        String pingyin = toPinYin(address.getNote());
        String pingyinCurr = toPinYin(getNote());
        if (pingyinCurr.startsWith("#") && !pingyin.startsWith("#")) {
            return 1;
        } else if (!pingyinCurr.startsWith("#") && pingyin.startsWith("#")) {
            return -1;
        } else {
            return pingyinCurr.compareToIgnoreCase(pingyin);
        }
    }

    private String toPinYin(String str){
        StringBuffer sb = new StringBuffer();
        char[] nameChar = str.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < nameChar.length; i++) {
            if (nameChar[i] > 128) {
                try {
                    sb.append(PinyinHelper.toHanyuPinyinStringArray(nameChar[i], defaultFormat)[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                    sb.append(nameChar[i]);
                }
            } else {
                sb.append(nameChar[i]);
            }
        }
        String result = sb.toString();
        if(result.length() == 0){
            return "#";
        }
        String first = result.substring(0, 1);
        if (!first.matches("[a-zA-Z]")) { // 如果不在A-Z中则默认为“#”
            result = "#" + result;
        }
        return result;
    }

}
