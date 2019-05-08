package com.sjxz.moji_weather.mvp.presenter.impl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.sjxz.moji_weather.helper.WeatherService;
import com.sjxz.moji_weather.mvp.interactor.MainInteractor;
import com.sjxz.moji_weather.mvp.interactor.impl.MainInteractorImpl;
import com.sjxz.moji_weather.mvp.presenter.MainPresenter;
import com.sjxz.moji_weather.mvp.view.MainView;

/**
 * @author WYH_Healer
 * @email 3425934925@qq.com
 * Created by xz on 2017/2/8.
 * Role:
 */
public class MainPresenterImpl implements MainPresenter{

    private Context context;

    private MainInteractor mainInteractor;

    private MainView mainView;

    private WeatherService weatherService;


    private boolean mIsServiceBound;

    public MainPresenterImpl(MainView mainView, Context context) {
        this.mainView = mainView;//在Activity中实例化数据
        this.context = context;

        mainInteractor= new MainInteractorImpl();
    }

    @Override
    public void initialMain() {
        mainView.intialCityName(mainInteractor.initialCity(context));
        mainView.animAlpha(mainInteractor.initialAnim(context));
        mainView.initialTime(mainInteractor.getTodayTime());

        //bind数据
        bindPlaybackService();
    }


    public void bindPlaybackService() {
        //为了防止内存泄漏
        //Activity对象包括大量的布局和资源文件， 一旦它被该单例持有，它所持有的资源在应用结束前都不会被释放。修改的方法很简单：传进来的Context用ApplicationContext就可以了。ApplicationContext对象在应用整个生命周期中有且只有一个对象。持有它的引用不会占用更多资源。
        context.getApplicationContext().bindService(new Intent(context.getApplicationContext(), WeatherService.class), mConnect, Context.BIND_AUTO_CREATE);
        mIsServiceBound = true;
    }


    public void unbindPlaybackService() {
        if (mIsServiceBound) {
            context.getApplicationContext().unbindService(mConnect);
            mIsServiceBound = false;
        }
    }

    //用于bind数据的connect
    private ServiceConnection mConnect=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            weatherService=((WeatherService.LocalBinder)service).getService();
            mainView.initNotify(weatherService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
}
