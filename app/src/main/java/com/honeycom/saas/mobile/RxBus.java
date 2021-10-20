package com.honeycom.saas.mobile;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by zhoujr on 20-4-5.
 * 原理:PublishSubject本身作为观察者和被观察者。
 */

public class RxBus {
    private static volatile RxBus sInstance;
    private final PublishSubject<Object> mEventBus = PublishSubject.create();
    private final Map<String, CompositeDisposable> disposableMap = new HashMap<>();

    public static RxBus getInstance(){
        if (sInstance == null){
            synchronized (RxBus.class){
                if (sInstance == null){
                    sInstance = new RxBus();
                }
            }
        }
        return sInstance;
    }

    /**
     * 发送事件(post event)
     * @param event : event object(事件的内容)
     */
    public void post(Object event){
        mEventBus.onNext(event);
    }

    /**
     *
     * @param code
     * @param event
     */
    public void post(int code, Object event){
        Message msg = new Message(code,event);
        mEventBus.onNext(msg);
    }

    /**
     * 返回Event的管理者,进行对事件的接受
     * @return
     */
    public Observable toObservable(){
        return mEventBus;
    }

    /**
     *
     * @param cls :保证接受到制定的类型
     * @param <T>
     * @return
     */
    public <T> Observable<T> toObservable(Class<T> cls){
        //ofType起到过滤的作用,确定接受的类型
        return mEventBus.ofType(cls);
    }

    public <T> Observable<T> toObservable(int code,Class<T> cls){
        return mEventBus.ofType(Message.class)
                .filter(msg -> msg.code == code && cls.isInstance(msg.event))
                .map( msg -> (T)msg.event);

    }

    public <T> Flowable<T> getObservable(Class<T> type){
        return mEventBus.toFlowable(BackpressureStrategy.BUFFER).ofType(type);
    }

    public <T> Disposable doSubscribe(Class<T> type, Consumer<T> next, Consumer<Throwable> error){
        return getObservable(type)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(next,error);
    }

    public void addSubscription(Object o, Disposable disposable) {
        String key = String.valueOf(o.hashCode());
        if (disposableMap.get(key) != null) {
            disposableMap.get(key).add(disposable);
        } else {
            CompositeDisposable disposables = new CompositeDisposable();
            disposables.add(disposable);
            disposableMap.put(key, disposables);
        }
    }

    public void unSubscribe(Object o) {
        String key = String.valueOf(o.hashCode());
        if (!disposableMap.containsKey(key)){
            return;
        }
        if (disposableMap.get(key) != null) {
            disposableMap.get(key).dispose();
        }

        disposableMap.remove(key);
    }



    class Message{
        int code;
        Object event;

        public Message(int code,Object event){
            this.code = code;
            this.event = event;
        }
    }
}
