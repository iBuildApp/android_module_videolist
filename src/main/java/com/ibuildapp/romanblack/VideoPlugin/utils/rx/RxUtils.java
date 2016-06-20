package com.ibuildapp.romanblack.VideoPlugin.utils.rx;


import rx.Observable;
import rx.Scheduler;

public class RxUtils {
    public static <T> Observable.Transformer<T, T> applyCustomSchedulers(final Scheduler subscribeHandler, final Scheduler observeHandler) {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable.subscribeOn(subscribeHandler)
                        .observeOn(observeHandler);
            }
        };
    }
}
