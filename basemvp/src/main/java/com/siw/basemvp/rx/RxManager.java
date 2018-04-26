package com.siw.basemvp.rx;


import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.siw.basemvp.net.CallBackListener;
import com.siw.basemvp.utils.ReflexUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;


/**
 * 管理RX生命周期，防止内存泄漏
 */
public class RxManager<T> {

    private CompositeDisposable compositeDisposable = new CompositeDisposable();// 管理订阅者者
    private ArrayList<CallBackListener<T>> listCallBack = new ArrayList<>();


    public void add(Disposable disposable) {
        compositeDisposable.add(disposable);
    }

    /**
     * @param observable
     * @param callBackListener
     */
    public void add(Observable<T> observable, final CallBackListener<T> callBackListener) {
        Disposable disposable = observable.compose(RxUtil.<T>rxSchedulerHelper()).subscribe(new Consumer<T>() {
            @Override
            public void accept(T t) throws Exception {
                callBackListener.onSuccess(t);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                callBackListener.onError(throwable.toString());
            }
        }, new Action() {
            @Override
            public void run() throws Exception {
            }
        }, new Consumer<Disposable>() {
            @Override
            public void accept(Disposable disposable) throws Exception {
                callBackListener.onPre(true);
                Log.e("Man", "onPre");
            }
        });
        compositeDisposable.add(disposable);
        listCallBack.add(callBackListener);
    }

    /**
     * 加一个Class<T> cla 参数，
     * 主要是为了解决 Retrofit 泛型解析遇到com.google.gson.internal.LinkedTreeMap cannot be cast to object的bug
     * @param observable
     * @param cla
     * @param callBackListener
     */
    public void add(Observable<T> observable, final Class<T> cla , final CallBackListener<T> callBackListener) {
        Disposable disposable = observable.compose(RxUtil.<T>rxSchedulerHelper()).subscribe(new Consumer<T>() {
            @Override
            public void accept(T t) throws Exception {
                Gson gson = new Gson();
                T t2 = gson.fromJson(gson.toJson(t).toString(), cla);
                callBackListener.onSuccess(t2);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                callBackListener.onError(throwable.toString());
            }
        }, new Action() {
            @Override
            public void run() throws Exception {
            }
        }, new Consumer<Disposable>() {
            @Override
            public void accept(Disposable disposable) throws Exception {
                callBackListener.onPre(true);
                Log.e("Man", "onPre");
            }
        });
        compositeDisposable.add(disposable);
        listCallBack.add(callBackListener);
    }


    public void clear() {
        compositeDisposable.dispose();
        compositeDisposable.clear();
        for (CallBackListener<T> callBackListener : listCallBack) {
            callBackListener = null;
        }
        listCallBack.clear();
        listCallBack = null;
        compositeDisposable = null;
    }
}