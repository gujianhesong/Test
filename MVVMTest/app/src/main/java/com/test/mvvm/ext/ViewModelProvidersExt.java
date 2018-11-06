package com.test.mvvm.ext;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

/**
 * Created by hesong-os on 2018/9/13.
 */

public class ViewModelProvidersExt {

    /**
     * @deprecated This class should not be directly instantiated
     */
    @Deprecated
    public ViewModelProvidersExt() {
    }

    /**
     * Creates a {@link ViewModelProvider}, which retains ViewModels while a scope of given
     * {@code fragment} is alive. More detailed explanation is in {@link ViewModel}.
     * <p>
     * It uses {@link ViewModelProvider.AndroidViewModelFactory} to instantiate new ViewModels.
     *
     * @param fragment a fragment, in whose scope ViewModels should be retained
     * @return a ViewModelProvider instance
     */
    @NonNull
    @MainThread
    public static ViewModelProvider of(@NonNull Fragment fragment) {
        return ViewModelProviders.of(fragment);
    }

    /**
     * Creates a {@link ViewModelProvider}, which retains ViewModels while a scope of given Activity
     * is alive. More detailed explanation is in {@link ViewModel}.
     * <p>
     * It uses {@link ViewModelProvider.AndroidViewModelFactory} to instantiate new ViewModels.
     *
     * @param activity an activity, in whose scope ViewModels should be retained
     * @return a ViewModelProvider instance
     */
    @NonNull
    @MainThread
    public static ViewModelProvider of(@NonNull FragmentActivity activity) {
        return ViewModelProviders.of(activity);
    }

    /**
     * Creates a {@link ViewModelProvider}, which retains ViewModels while a scope of given
     * {@code fragment} is alive. More detailed explanation is in {@link ViewModel}.
     * <p>
     * It uses the given {@link ViewModelProvider.Factory} to instantiate new ViewModels.
     *
     * @param fragment a fragment, in whose scope ViewModels should be retained
     * @param factory  a {@code Factory} to instantiate new ViewModels
     * @return a ViewModelProvider instance
     */
    @NonNull
    @MainThread
    public static ViewModelProvider of(@NonNull Fragment fragment, @Nullable ViewModelProvider.Factory factory) {
        return ViewModelProviders.of(fragment, factory);
    }

    /**
     * Creates a {@link ViewModelProvider}, which retains ViewModels while a scope of given Activity
     * is alive. More detailed explanation is in {@link ViewModel}.
     * <p>
     * It uses the given {@link ViewModelProvider.Factory} to instantiate new ViewModels.
     *
     * @param activity an activity, in whose scope ViewModels should be retained
     * @param factory  a {@code Factory} to instantiate new ViewModels
     * @return a ViewModelProvider instance
     */
    @NonNull
    @MainThread
    public static ViewModelProvider of(@NonNull FragmentActivity activity,
                                       @Nullable ViewModelProvider.Factory factory) {
        return ViewModelProviders.of(activity, factory);
    }

    /**
     * Creates a {@link ViewModelProvider}, which retains ViewModels until invoke {@link #removeDefault()}.
     * More detailed explanation is in {@link ViewModel}.
     *
     * @param context
     * @return
     */
    @NonNull
    @MainThread
    public static ViewModelProvider ofContext(@NonNull Context context) {
        if (context instanceof FragmentActivity) {
            return of((FragmentActivity) context);
        }

        Application application = (Application) context.getApplicationContext();

        return new ViewModelProvider(ViewModelStoreHolder.getInstance().getDefault(), ViewModelProvider.AndroidViewModelFactory.getInstance(application));
    }

    /**
     * Creates a {@link ViewModelProvider}, which retains ViewModels until invoke {@link #remove(String)} ()}.
     * More detailed explanation is in {@link ViewModel}.
     *
     * @param context
     * @param tag
     * @return
     */
    @NonNull
    @MainThread
    public static ViewModelProvider ofContext(@NonNull Context context, String tag) {
        if (context instanceof FragmentActivity) {
            return of((FragmentActivity) context);
        }

        Application application = (Application) context.getApplicationContext();

        return new ViewModelProvider(ViewModelStoreHolder.getInstance().get(tag), ViewModelProvider.AndroidViewModelFactory.getInstance(application));
    }

    /**
     * Creates a {@link ViewModelProvider}, which retains ViewModels until invoke {@link #removeDefault()}.
     * More detailed explanation is in {@link ViewModel}.
     *
     * @param context
     * @param factory
     * @return
     */
    @NonNull
    @MainThread
    public static ViewModelProvider ofContext(@NonNull Context context,
                                              @Nullable ViewModelProvider.Factory factory) {
        if (context instanceof FragmentActivity) {
            return of((FragmentActivity) context, factory);
        }

        Application application = (Application) context.getApplicationContext();
        if (factory == null) {
            factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application);
        }
        return new ViewModelProvider(ViewModelStoreHolder.getInstance().getDefault(), factory);
    }

    /**
     * Creates a {@link ViewModelProvider}, which retains ViewModels until invoke {@link #remove(String)} ()}.
     * More detailed explanation is in {@link ViewModel}.
     *
     * @param context
     * @param tag
     * @param factory
     * @return
     */
    @NonNull
    @MainThread
    public static ViewModelProvider ofContext(@NonNull Context context, String tag,
                                              @Nullable ViewModelProvider.Factory factory) {
        if (context instanceof FragmentActivity) {
            return of((FragmentActivity) context, factory);
        }

        Application application = (Application) context.getApplicationContext();
        if (factory == null) {
            factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application);
        }
        return new ViewModelProvider(ViewModelStoreHolder.getInstance().get(tag), factory);
    }

    /**
     * Remove the Default ViewModelStore and then release its ViewModels
     */
    @NonNull
    @MainThread
    public static void removeDefault() {
        ViewModelStoreHolder.getInstance().removeDefault();
    }

    /**
     * Remove a ViewModelStore with tag and then release its ViewModels
     *
     * @param tag
     */
    @NonNull
    @MainThread
    public static void remove(String tag) {
        ViewModelStoreHolder.getInstance().remove(tag);
    }

}
