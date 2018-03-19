package com.pinery.test.util;

import android.os.Handler;
import android.os.Looper;
import com.pinery.test.App;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 多线程任务管理,Runnable任务可提交到这里执行
 *
 */
public class TaskManager {
    private ExecutorService mExecutor;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private static TaskManager manager;
    public static TaskManager getInstance(){
        if(manager == null){
            manager = new TaskManager();
        }
        return manager;
    }

    private TaskManager(){
        mExecutor = Executors.newFixedThreadPool(2);
    }
    
    public void post(Runnable runnable){
    	if(mExecutor == null || runnable == null){
    		return;
    	}
    	
        mExecutor.submit(runnable);
    }

    public void postDelay(final Runnable runnable, long delayTime){
    	if(mExecutor == null || runnable == null){
    		return;
    	}
    	
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mExecutor.submit(runnable);

            }
        }, delayTime);

    }

    public void post(final Runnable runnable, final Callback callback){
    	if(mExecutor == null || runnable == null){
    		return;
    	}
    	
        mExecutor.submit(new Runnable() {
			
			@Override
			public void run() {
				//当前线程执行逻辑
				runnable.run();
				
				//交给主线程处理
				if(callback != null){
					App.getInstance().mHandler.post(new Runnable() {
						
						@Override
						public void run() {
							callback.onRunnableFinished();
						}
					});
					
				}
			}
		});
    }
    
    public void postDelay(final Runnable runnable, final long delayTime, final Callback callback){
    	if(mExecutor == null || runnable == null){
    		return;
    	}
    	
    	mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
               
                mExecutor.submit(new Runnable() {
    				
    				@Override
    				public void run() {
    					//当前线程执行逻辑
    					runnable.run();
    					
    					//交给主线程处理
    					if(callback != null){
                App.getInstance().mHandler.post(new Runnable() {
    							
    							@Override
    							public void run() {
    								callback.onRunnableFinished();
    							}
    						});
    						
    					}
    				}
    			});
            	
            }
            
        }, delayTime);
        
    }
    
    public void destory(){
        if(mExecutor != null){
            mExecutor.shutdown();
        }
        mExecutor = null;
    }
    
    /**
     * 任务执行回调
     */
    public static interface Callback{
    	/** 任务执行完后的调用,该调用在主线程,可用于具体界面更新 */
    	public void onRunnableFinished();
    }

}
