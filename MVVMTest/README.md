
### MVVMTest项目简介
MVVMTest项目是基于Android Architecture架构的展示项目，是一个简单的新闻客户端项目。本项目展示了基于这种架构的两种ViewModel使用方式。

### 优势
Android Architecture架构相比MVP架构有如下优势。
1. 减少接口编写，不会像MVP模式那样，需要定义对应的View和Presenter接口。
2. 相比MVP，代码冗余量更少，比如接口回调方面。
3. MVP可能需要处理Activity#onSaveInstanceState() 和 Activity#onRestoreInstanceState()来处理旋转的情况，而这种MVVM方式内部集成了处理，不需要额外处理，比如当Activity因为旋转而被销毁重建，不需要特殊处理，可以正常获取数据并显示。
4. MVP的耦合性更强，Presenter和View之间存在相互引用，虽然只是持有接口，但是它是必要的，因为View通常需要通知Presenter的生命周期事件,如onStop()或onDestroy()，以便Presenter可以清除RxJava订阅等等。而在MVVM只需覆盖ViewModel的onclear()清除视图不参与。
5. MVP模式，View的刷新需要Presenter主动回调来实现，而MVVM则只需要订阅ViewModel就可以了，回调则由ViewModel负责通知，ViewModel里不需要主动回调，只要更新数据，ViewModel就会主动通知订阅了的对象。
6. MVP模式一个页面通常对应一个Presenter，如果要对应多个Presenter，实现起来不方便，而使用ViewModel订阅的方式，一个页面可以集成多个ViewModel在其中订阅使用。

参考[Implementing MVVM using LiveData, RxJava, Dagger Android](https://proandroiddev.com/mvvm-architecture-using-livedata-rxjava-and-new-dagger-android-injection-639837b1eb6c)

### 不足
Android Architecture架构的这种MVVM架构有它的局限性，不适合非Activity和Fragment的场景，比如全局悬浮窗，就不适合。需要使用其他方式，比如MVP。

![](https://github.com/gujianhesong/Test/blob/master/MVVMTest/screenshot/1.png?raw=true)

>[我的博客](http://blog.csdn.net/hesong1120?ref=toolbar)
<br>[GitHub](https://github.com/gujianhesong)
<br>[我的简书](https://www.jianshu.com/u/75d212bdd107)
<br>微信公众号 **hesong** ，微信扫一扫下方二维码即可关注：
<br>![](https://raw.githubusercontent.com/gujianhesong/hesong/master/%E5%BE%AE%E4%BF%A1%E5%85%AC%E4%BC%97%E5%8F%B7.jpg)

