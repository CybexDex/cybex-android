package com.cybex.database;

import com.cybex.database.entity.Address;

import java.util.List;

import io.reactivex.Observable;

public interface DBProvider {

    Observable<List<Address>> getAddress(String account, String token, int type);

    Observable<Long> insertAddress(Address address);

    Observable<Void> deleteAddress(long id);

    Observable<Void> deleteAddress(Address address);

}
