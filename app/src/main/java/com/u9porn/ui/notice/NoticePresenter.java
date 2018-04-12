package com.u9porn.ui.notice;

import android.arch.lifecycle.Lifecycle;
import android.support.annotation.NonNull;

import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter;
import com.trello.rxlifecycle2.LifecycleProvider;
import com.u9porn.data.DataManager;
import com.u9porn.data.model.Notice;
import com.u9porn.di.PerActivity;
import com.u9porn.rxjava.CallBackWrapper;
import com.u9porn.rxjava.RxSchedulersHelper;

import javax.inject.Inject;

/**
 * @author flymegoc
 * @date 2018/1/26
 */
@PerActivity
public class NoticePresenter extends MvpBasePresenter<NoticeView> implements INotice {

    private final static String CHECK_NEW_NOTICE_URL = "https://github.com/techGay/91porn/blob/master/notice.txt";
    private LifecycleProvider<Lifecycle.Event> provider;

    private DataManager dataManager;

    @Inject
    public NoticePresenter(LifecycleProvider<Lifecycle.Event> provider, DataManager dataManager) {
        this.provider = provider;
        this.dataManager = dataManager;
    }

    @Override
    public void checkNewNotice(int versionCode) {
        checkNewNotice(versionCode, null);
    }

    public void checkNewNotice(final int versionCode, final CheckNewNoticeListener checkNewNoticeListener) {
        dataManager.checkNewNotice(CHECK_NEW_NOTICE_URL)
                .compose(RxSchedulersHelper.<Notice>ioMainThread())
                .compose(provider.<Notice>bindUntilEvent(Lifecycle.Event.ON_DESTROY))
                .subscribe(new CallBackWrapper<Notice>() {
                    @Override
                    public void onSuccess(final Notice notice) {
                        if (notice.getVersionCode() > versionCode) {
                            if (checkNewNoticeListener == null) {
                                ifViewAttached(new ViewAction<NoticeView>() {
                                    @Override
                                    public void run(@NonNull NoticeView view) {
                                        view.haveNewNotice(notice);
                                    }
                                });
                            } else {
                                checkNewNoticeListener.haveNewNotice(notice);
                            }
                        } else {
                            if (checkNewNoticeListener == null) {
                                ifViewAttached(new ViewAction<NoticeView>() {
                                    @Override
                                    public void run(@NonNull NoticeView view) {
                                        view.noNewNotice();
                                    }
                                });
                            } else {
                                checkNewNoticeListener.noNewNotice();
                            }
                        }
                    }

                    @Override
                    public void onError(final String msg, int code) {
                        if (checkNewNoticeListener == null) {
                            ifViewAttached(new ViewAction<NoticeView>() {
                                @Override
                                public void run(@NonNull NoticeView view) {
                                    view.checkNewNoticeError(msg);
                                }
                            });
                        } else {
                            checkNewNoticeListener.checkNewNoticeError(msg);
                        }
                    }
                });
    }

    @Override
    public void checkUpdate(int versionCode) {

    }

    public interface CheckNewNoticeListener {
        void haveNewNotice(Notice notice);

        void noNewNotice();

        void checkNewNoticeError(String message);
    }
}
