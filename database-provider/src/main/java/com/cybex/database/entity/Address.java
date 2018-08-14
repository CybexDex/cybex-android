package com.cybex.database.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

@Entity(nameInDb = "address")
public class Address {

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

    @Property(nameInDb = "label")
    private String label;//标签

    @Property(nameInDb = "memo")
    private String memo;//memo

    @Property(nameInDb = "type")
    private int type;//类型

    @Generated(hash = 354826096)
    public Address(Long id, String account, String token, String address,
            String label, String memo, int type) {
        this.id = id;
        this.account = account;
        this.token = token;
        this.address = address;
        this.label = label;
        this.memo = memo;
        this.type = type;
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

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
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

}
