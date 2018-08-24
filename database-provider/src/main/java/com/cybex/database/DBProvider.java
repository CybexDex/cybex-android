package com.cybex.database;

import com.cybex.database.entity.Address;

import java.util.List;

import io.reactivex.Observable;

public interface DBProvider {

    Observable<List<Address>> getAddress(String account, String token, int type);

    Observable<Long> getCount(String account, String token, int type);

    Observable<List<Address>> getAddress(String account, int type);

    Observable<Long> insertAddress(Address address);

    Observable<Boolean> deleteAddress(long id);

    Observable<Boolean> deleteAddress(Address address);

    /**
     * 检查地址是否存在
     * @param account 登录账户名
     * @param address 地址
     * @param type 地址类型
     * @return
     */
    Observable<Address> checkAddressExist(String account, String address, int type);

    Observable<Boolean> checkWithdrawAddressExist(String account, String address, String token, int type);

}
