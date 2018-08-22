package com.cybex.database;

import com.cybex.database.dao.AddressDao;
import com.cybex.database.dao.DaoSession;
import com.cybex.database.entity.Address;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class DBProviderImpl implements DBProvider{

    private DaoSession mDaoSession;

    public DBProviderImpl(DaoSession daoSession){
        mDaoSession = daoSession;
    }

    @Override
    public Observable<List<Address>> getAddress(final String account, final String token, final int type) {
        return Observable.create(new ObservableOnSubscribe<List<Address>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Address>> e) throws Exception {
                if(e.isDisposed()){
                    return;
                }
                e.onNext(mDaoSession.getAddressDao().queryBuilder()
                        .where(AddressDao.Properties.Token.eq(token),
                                AddressDao.Properties.Type.eq(type))
                        .orderAsc(AddressDao.Properties.Note)
                        .list());
                e.onComplete();
            }
        });
    }

    @Override
    public Observable<Long> getCount(final String account, final String token, final int type) {
        return Observable.create(new ObservableOnSubscribe<Long>() {
            @Override
            public void subscribe(ObservableEmitter<Long> e) throws Exception {
                if(e.isDisposed()){
                    return;
                }
                e.onNext(mDaoSession.getAddressDao().queryBuilder()
                        .where(AddressDao.Properties.Token.eq(token),
                                AddressDao.Properties.Type.eq(type))
                        .count());
                e.onComplete();
            }
        });
    }

    @Override
    public Observable<List<Address>> getAddress(final String account, final int type) {
        return Observable.create(new ObservableOnSubscribe<List<Address>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Address>> e) throws Exception {
                if(e.isDisposed()){
                    return;
                }
                e.onNext(mDaoSession.getAddressDao().queryBuilder()
                        .where(AddressDao.Properties.Type.eq(type))
                        .orderAsc(AddressDao.Properties.Note)
                        .list());
                e.onComplete();
            }
        });
    }

    @Override
    public Observable<Long> insertAddress(final Address address) {
        return Observable.create(new ObservableOnSubscribe<Long>() {
            @Override
            public void subscribe(ObservableEmitter<Long> e) throws Exception {
                if(e.isDisposed()){
                    return;
                }
                e.onNext(mDaoSession.getAddressDao().insertOrReplace(address));
                e.onComplete();
            }
        });
    }

    @Override
    public Observable<Boolean> deleteAddress(final long id) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                if(e.isDisposed()){
                    return;
                }
                mDaoSession.getAddressDao().deleteByKey(id);
                e.onNext(true);
                e.onComplete();
            }
        });
    }

    @Override
    public Observable<Boolean> deleteAddress(final Address address) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                if(e.isDisposed()){
                    return;
                }
                mDaoSession.getAddressDao().delete(address);
                e.onNext(true);
                e.onComplete();
            }
        });
    }

    @Override
    public Observable<Boolean> checkAddressExist(final String account, final String address, final int type) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                if(e.isDisposed()){
                    return;
                }
                Address addressResult = mDaoSession.getAddressDao().queryBuilder()
                        .where(AddressDao.Properties.Address.eq(address),
                                AddressDao.Properties.Type.eq(type))
                        .unique();
                e.onNext(addressResult != null);
                e.onComplete();
            }
        });
    }

    @Override
    public Observable<Boolean> checkWithdrawAddressExist(final String account, final String address, final String token, final int type) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                if(e.isDisposed()){
                    return;
                }
                Address addressResult = mDaoSession.getAddressDao().queryBuilder()
                        .where(AddressDao.Properties.Address.eq(address),
                                AddressDao.Properties.Token.eq(token),
                                AddressDao.Properties.Type.eq(type))
                        .unique();
                e.onNext(addressResult != null);
                e.onComplete();
            }
        });
    }
}
