
### 前言
在android开发中，我们都或多或少的会遇到一些内存泄漏的问题，虽然大都知道哪些情况会导致内存泄露，但是还是不可避免的会遇到类似的问题，因此，知道如何去查找内存泄露就显得非常重要了。本篇和大家分享下如何进行内存泄漏的定位分析，以及对内存占用的优化分析。相信大家看了之后会有所收获。

为了有一个良好的分析体验，我特意新建了一个用于分析内存方面的项目，该项目是一个简易的新闻客户端，结构上大致是这样的，mvp开发模式，网络数据方面采用Retrofit + rxjava，列表使用LRecyclerView，新闻页面由ViewPager将十几个不同类型的新闻列表Fragment页面组合在一起。种情况由于页面的切换，以及数据列表的刷新加载等，在开发中还是比较典型的，在内存控制上也是有较高要求的，因此是比较适合用来做内存分析的。

[项目地址](https://github.com/gujianhesong/Test/tree/master/MemoryAnalyzeTest)
![](https://github.com/gujianhesong/Test/blob/master/MemoryAnalyzeTest/screenshot/0.png?raw=true)
![](https://github.com/gujianhesong/Test/blob/master/MemoryAnalyzeTest/screenshot/1.png?raw=true)

### 内存快照分析方法
这里我们直接使用Android Studio的内存分析工具进行分析。打开Android Monitor，可看到Logcat，切换到Monitors，可看到内存，CPU相关信息。
1. 找到当前分析的应用，这里为com.test.memory。
2. 点击几次Initiate GC，用于通知垃圾收集进行垃圾回收，避免无效的内存分析。
3. 点击Dump Java Heap，过一会就会打开内存快照。
![](https://github.com/gujianhesong/Test/blob/master/MemoryAnalyzeTest/screenshot/3.png?raw=true)

接下来分析内存快照
1. 点击选择PackageTreeView，这样就可以按包名层级进行类的查找。
2. 左上部分就是应用相关的类的内存信息了。通常我们只需按包名com.test.memory找到自己应用下的类进行分析，这里找到NewsListFragment进行分析，它代表一种类型的新闻列表页面。
3. 可以看到TotalCount这一列是12，也就是说当前有12个NewsListFragment对象，也就是12个NewsListFragment新闻列表页面了，因为之前有将所有类型的页面都打开过了。
4. Shallow Size这一栏，可以看到是2880，代表的意思就是NewsListFragment的所有对象占用了多少内存，这里是12个的总大小，因此一个NewsListFragment的大小是240。注意，这里仅仅是指NewsListFragment本身占用的内存，而作为它的引用属性对象所占的内存是不算其中的，比如它持有的视图View的大小是不算其中的，而只算一个int类型引用的大小，4字节。所以Shallow Size通常并不大，因为它只是当前对象本身的大小，不算它引用对象的大小在其中。
5. Retained Size这一栏，是1757826，也就是1.75M大小了，也就是说12个NewsListFragment所持有的总大小是1.75M，这里的持有大小，它不但包括NewsListFragment本身的大小，还包括它持有对象的大小，并且是它持有对象可被回收的大小。因此Retained Size是指，如果NewsListFragment这个对象被回收时，它最终能被回收的内存，也就是它本身的内存，和一部分只有被它引用的对象的内存，而还被其他对象持有的内存是不算在其中的，例如context对象，它不仅被NewsListFragment引用，所以它的内存大小是不算入在Retained Size中的。
6. 右上部分代表的是所选择类的所有对象和其属性所占内存情况。例如这里是NewsListFragment类的12个对象的具体内存情况，和它其中各个属性引用对象的内存情况。这里可以分析其中的哪些属性或引用对象占用的内存较高。
7. 下面的部分指的是当前的NewsListFragment对象被哪些对象引用了，可以查看它的引用树，可用于查找最终导致内存泄露无法被释放的最终根源。
![](https://github.com/gujianhesong/Test/blob/master/MemoryAnalyzeTest/screenshot/2.png?raw=true)

### 内存泄露分析
明白了如何查看内存快照信息，知道它们代表的含义之后，接下来举一个例子来分析下内存泄露问题。场景是这样的，在每个新闻列表页面个NewsListFragment的onCreateView方法时，我会添加一个LeakAnimView在其上，并开始执行缩放动画，当onDestoryView时移除LeakAnimView，并停止它的动画。代码如下：
```
public class NewsListFragment extends BaseListFragment<NewsPresenter>
    implements NewsContract.View {

  private LeakAnimView animView;

    @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    if(ControInfos.isTestLeak){
      //如果测试内存泄露问题，则执行
      animView = new LeakAnimView(view.getContext());
      RelativeLayout parent = (RelativeLayout) view;
      RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
      params.addRule(RelativeLayout.CENTER_IN_PARENT);
      parent.addView(animView, params);
      animView.start();
    }

  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();

    if(ControInfos.isTestLeak){
      //如果测试内存泄露问题，则执行
      if(animView != null && animView.getParent() != null){
        RelativeLayout parent = (RelativeLayout) animView.getParent();
        animView.cancel();
        parent.removeView(animView);
        animView = null;
      }
    }
  }

  ...

}
```
下面是LeakAnimView的实现：
```
/**
 * 存在内存泄露的动画View，由于动画Cancel之后，还是会回调onAnimationEnd，所以需要额外判断是否取消状态，否则动画会一直执行下去，导致内存泄露问题
 */
public class LeakAnimView extends View{
    private static final String TAG = "AnimView";

    private AnimatorSet animatorSet, animatorSet2;
    private ObjectAnimator scaleX, scaleY;
    private ObjectAnimator scaleX2, scaleY2;

    private boolean isAnimating;

    public LeakAnimView(Context context) {
        super(context);

        init();
    }

    public LeakAnimView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init(){
        setBackgroundColor(Color.RED);

        animatorSet = new AnimatorSet();
        scaleX = ObjectAnimator.ofFloat(this, "scaleX", 0.5f, 1f);
        scaleY = ObjectAnimator.ofFloat(this, "scaleY", 0.5f, 1f);
        animatorSet.play(scaleX).with(scaleY);
        animatorSet.setDuration(500);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.d(TAG, "animatorSet onAnimationStart");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.d(TAG, "animatorSet onAnimationEnd");

                //取消动画时，该方法依然会被回调，所以下个动画会执行，存在内存泄露问题，所以要做状态的判断

                if(ControInfos.exitstLeak){
                    //这里存在内存泄露问题
                    animatorSet2.start();
                }else{
                    //这里解决了内存泄露问题
                    if(isAnimating){
                        animatorSet2.start();
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Log.d(TAG, "animatorSet onAnimationCancel");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animatorSet2 = new AnimatorSet();
        scaleX2 = ObjectAnimator.ofFloat(this, "scaleX", 1f, 0.5f);
        scaleY2 = ObjectAnimator.ofFloat(this, "scaleY", 1f, 0.5f);
        animatorSet2.play(scaleX2).with(scaleY2);
        animatorSet2.setDuration(500);
        animatorSet2.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.d(TAG, "animatorSet2 onAnimationStart");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.d(TAG, "animatorSet2 onAnimationEnd");

                //取消动画时，该方法依然会被回调，所以下个动画会执行，存在内存泄露问题，所以要做状态的判断

                if(ControInfos.exitstLeak){
                    //这里存在内存泄露问题
                    animatorSet.start();
                }else{
                    //这里解决了内存泄露问题
                    if(isAnimating){
                        animatorSet.start();
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Log.d(TAG, "animatorSet2 onAnimationCancel");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

    }

    public void start(){
        if(isAnimating){
            return;
        }
        isAnimating = true;
        animatorSet.start();
    }

    public void cancel(){
        if(isAnimating){
            isAnimating = false;
            if(animatorSet.isRunning() || animatorSet.isStarted()){
                animatorSet.cancel();
            }
            if(animatorSet2.isRunning() || animatorSet2.isStarted()){
                animatorSet2.cancel();
            }
        }
    }
}
```
上面只给出测试导致内存泄露的部分，其他代码实现可以看项目源码。其实导致内存泄露的原因也比较简单，但是如果对动画不是很熟悉的话，容易踩这个坑，做一个循环动画，动画1执行完后执行动画2，动画2执行完后执行动画1，如此循环。重点是取消的时候，除了会回调onAnimationCancel之外，仍然会回调onAnimationEnd，而如果不在其中做标记判断的话，那么又会去执行下一个动画，那么取消方法并不能停止动画，动画会一直持有LeakAnimView，然后导致NewsListFragment即便是所属的Activity页面关闭了也不能被释放，这时就存在内存泄露问题了。
![](https://github.com/gujianhesong/Test/blob/master/MemoryAnalyzeTest/screenshot/8.png?raw=true)
如图所示，NewsListFragment对象是12个，而LeakAnimView却有62个之多，如果左右滑动更多的话，会一直增加，而从底部引用树中也可以看出是动画导致的内存泄露。那么关闭页面之后，看看这些NewsListFragment和LeakAnimView能不能被回收
![](https://github.com/gujianhesong/Test/blob/master/MemoryAnalyzeTest/screenshot/9.png?raw=true)
发现这些对象并没有随着所属Activity页面的关闭而被回收。那么在修改了内存泄露问题之后，看看效果是怎么样的
![](https://github.com/gujianhesong/Test/blob/master/MemoryAnalyzeTest/screenshot/5.png?raw=true)
可以看到，LeakAnimView变成了12个，无论怎么样左右滑动页面，它都只保存在12以内，这说明内存泄露不存在了，同时当将页面关闭时，可以看到LeakAnimView和NewsListFragment对象数量都为0，都被回收了。
![](https://github.com/gujianhesong/Test/blob/master/MemoryAnalyzeTest/screenshot/6.png?raw=true)

### 内存占用分析
上面我们通过分析将内存泄露的问题解决了，但是我们深知当前的状态并不是完美的。虽然不存在内存泄露，但是内存占用的问题还是可以进行优化的。特别是在每个列表页面数据量大，页面的布局复杂，带有重量级的控件在其中时，如果这些不能随着PageAdapter的滑动进行一定的释放的话，内存占用也是会非常高，导致内存溢出的问题。这里我们还是以LeakAnimView来做个例子吧，我们知道，当我们浏览过所有的NewsListFragment页面后，NewsListFragment的对象数量维持在12，相应的LeakAnimView也是在12个。

但是不觉得有点奇怪吗？我在NewsListFragment的onDestroyView中是做了移除操作的，并且将animView设为null了，照理说应该没有被其他对象引用了，应该是可以被回收的，这样的话，除了有两三个LeakAnimView对象还存在之外，其他应该都是被回收的啦，但是为啥没有呢？我们看下其中一个LeakAnimView对象的引用树，发现了问题。
![](https://github.com/gujianhesong/Test/blob/master/MemoryAnalyzeTest/screenshot/10.png?raw=true)
当前的LeakAnimView对象被android.widget.RelativeLayout.DependencyGraph.Node@318059736 (0x12f534d8)给引用了，当然它还有被其他给引用，不过经分析，有效的引用是属于RelativeLayout.DependencyGraph.Node的，那这个是干嘛用的，跟进代码发现，原来RelativeLayout中有个Node来管理它的子View，每个子View作为一个节点Node，DependencyGraph则是用来管理节点Node，Node还持有的当前的LeakAnimView对象的话，说明Node没有被释放，执行release方法,也就是DependencyGraph没有执行clear方法。
```
public class RelativeLayout{
    ...

    private static class DependencyGraph {
        ...

        void clear() {
            final ArrayList<Node> nodes = mNodes;
            final int count = nodes.size();

            for (int i = 0; i < count; i++) {
                nodes.get(i).release();
            }
            nodes.clear();

            mKeyNodes.clear();
            mRoots.clear();
        }

        static class Node {
            ...

            void release() {
                view = null;
                dependents.clear();
                dependencies.clear();

                sPool.release(this);
            }
        }
    }
}
```
再找哪里调用了DependencyGraph的clear方法，发现是在RelativeLayout的sortChildren方法中，而sortChildren是在onMeasure方法中被调用的
```
public class RelativeLayout{
    ...

    private void sortChildren() {
        final int count = getChildCount();
        if (mSortedVerticalChildren == null || mSortedVerticalChildren.length != count) {
            mSortedVerticalChildren = new View[count];
        }

        if (mSortedHorizontalChildren == null || mSortedHorizontalChildren.length != count) {
            mSortedHorizontalChildren = new View[count];
        }

        final DependencyGraph graph = mGraph;
        graph.clear();

        for (int i = 0; i < count; i++) {
            graph.add(getChildAt(i));
        }

        graph.getSortedViews(mSortedVerticalChildren, RULES_VERTICAL);
        graph.getSortedViews(mSortedHorizontalChildren, RULES_HORIZONTAL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mDirtyHierarchy) {
            mDirtyHierarchy = false;
            sortChildren();
        }
        ...
    }
}
```
也就是说onMeasure没有在NewsListFragment执行onDestroyView时执行。那这个怎么解决，我现在也没有比较好的解决方案，想了一个做验证性的方法，通过反射主动调用RelativeLayout的sortChildren方法
```
public class NewsListFragment extends BaseListFragment<NewsPresenter>
    implements NewsContract.View {
    ...
     @Override
    public void onDestroyView() {
        super.onDestroyView();

        if(ControInfos.isTestLeak){
          //如果测试内存泄露问题，则执行
          if(animView != null && animView.getParent() != null){
            Log.e("NewsListFragment", "remove pre, animView parent : " + animView.getParent());
            RelativeLayout parent = (RelativeLayout) animView.getParent();
            animView.cancel();
            parent.removeView(animView);

            //这里通过反射主动调用RelativeLayout的sortChildren方法，达到清除animView被RelativeLayout.DependencyGraph.Node持有引用的问题
            ReflectUtil.invokeMethod(parent.getClass().getName(), "sortChildren", parent, null, new Object[]{});

            Log.e("NewsListFragment", "remove post, animView parent : " + animView.getParent());
            Log.e("NewsListFragment", "remove post, parent size : " + parent.getChildCount());

            animView = null;
          }
        }
    }
}
```
现在测试一下看看效果。
![](https://github.com/gujianhesong/Test/blob/master/MemoryAnalyzeTest/screenshot/11.png?raw=true)
很欣喜的看到，这个只有2个LeakAnimView对象了（当前的NewsListFragment和旁边的NewsListFragment所持有的LeakAnimView对象）。说明确实是由于被RelativeLayout.DependencyGraph.Node持有的引用导致LeakAnimView对象不能被回收了。当然通过反射去实现不一定是合适的办法，大家可以想想其他更合适的方法去实现。

显然，这样省去了10个LeakAnimView对象所占用的内存，那么再延伸到NewsListFragment持有的View的话，是不是可以想办法去实现回收其他10个NewsListFragment中的View的内存呢，那么想想，内存占用是不是会减少很多？具体怎么去做需要大家自己去做尝试和验证。

### 总结
好啦，到总结的时候了。无论是内存泄露的检测分析，还是内存占用的优化分析，都可以通过查看Android Studio导出的内存快照进行分析。内存泄露问题着重看类对象的数量Total Size，看是否符合预期，而内存占用则更注重去找内存占用较大的对象Shallow Size，分析它的数量，以及哪里占用了较大内存，分析是否合理，然后进行针对性的优化，更深的体会就得自己亲自尝试了，

>[我的GitHub](https://github.com/gujianhesong)
<br>微信公众号 **hesong** ，微信扫一扫下方二维码即可关注：
<br>![](https://raw.githubusercontent.com/gujianhesong/hesong/master/%E5%BE%AE%E4%BF%A1%E5%85%AC%E4%BC%97%E5%8F%B7.jpg)

