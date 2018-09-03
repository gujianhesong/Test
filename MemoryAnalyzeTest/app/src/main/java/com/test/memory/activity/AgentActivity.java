package com.test.memory.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import com.test.memory.R;

/**
 * Fragment的代理类
 */
public class AgentActivity extends AppCompatActivity {
  public static final String EXTRA_FRAGMENT_CLASSNAME = "extra_fragment_classname";
  public static final String EXTRA_FRAGMENT_BUNDLE = "extra_fragment_bundle";

  private Fragment mFragment;

  protected Class<? extends Fragment> provideFragmentClass() throws ClassNotFoundException {
    return null;
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_base);

    // 获取Fragment类名或者绑定的ID
    Intent data = getIntent();
    String fragmentClass = data.getStringExtra(EXTRA_FRAGMENT_CLASSNAME);

    Bundle bundle = getIntent().getExtras();
    Fragment fragment = null;

    try {
      // 指定Fragment类名打开
      if (!TextUtils.isEmpty(fragmentClass)) {
        fragment = (Fragment) Class.forName(fragmentClass).newInstance();
      }

      // 指定Fragment类打开
      if (fragment == null && provideFragmentClass() != null) {
        fragment = provideFragmentClass().newInstance();
      }
    } catch (Exception e) {
    }

    if (fragment != null) {
      // 传递参数到Fragment
      fragment.setArguments(bundle);
      // 为Activity设置内部Fragment
      setMainFragment(fragment);
    } else {
      finish();
    }
  }

  /**
   * 打开Fragment
   *
   * @param fragmentClass 类名，需要指定完整的包名
   */
  public static void gotoFragment(Context context, Class<? extends Fragment> fragmentClass) {
    gotoFragment(context, fragmentClass.getName());
  }

  /**
   * 打开Fragment
   *
   * @param fragmentClass 类名，需要指定完整的包名
   */
  public static void gotoFragment(Context context, String fragmentClass) {
    Intent intent = new Intent(context, AgentActivity.class);
    intent.putExtra(EXTRA_FRAGMENT_CLASSNAME, fragmentClass);
    context.startActivity(intent);
  }

  /**
   *
   * @param context
   * @param fragmentClass
   * @param bundle
   */
  public static void gotoFragment(Context context, Class<? extends Fragment> fragmentClass,
      Bundle bundle) {
    gotoFragment(context, fragmentClass.getName(), bundle);
  }

  /**
   *
   * @param context
   * @param fragmentClass
   * @param bundle
   */
  public static void gotoFragment(Context context, String fragmentClass, Bundle bundle) {
    Intent intent = new Intent(context, AgentActivity.class);
    intent.putExtra(EXTRA_FRAGMENT_CLASSNAME, fragmentClass);
    intent.putExtra(EXTRA_FRAGMENT_BUNDLE, bundle);
    context.startActivity(intent);
  }

  /**
   *
   * @param context
   * @param fragmentClass
   * @param requestCode
   * @param bundle
   */
  public static void gotoFragment(Activity context, Class<? extends Fragment> fragmentClass,
      int requestCode, Bundle bundle) {
    gotoFragment(context, fragmentClass.getName(), requestCode, bundle);
  }

  /**
   *
   * @param context
   * @param fragmentClass
   * @param requestCode
   * @param bundle
   */
  public static void gotoFragment(Activity context, String fragmentClass, int requestCode,
      Bundle bundle) {
    Intent intent = new Intent(context, AgentActivity.class);
    intent.putExtra(EXTRA_FRAGMENT_CLASSNAME, fragmentClass);
    intent.putExtra(EXTRA_FRAGMENT_BUNDLE, bundle);
    context.startActivityForResult(intent, requestCode);
  }

  @Override public void finish() {
    super.finish();
    //overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);

  }

  //@Override
  //public void onBackPressed() {
  //    if(mFragment != null){
  //        mFragment.onBackPressed();
  //    }
  //}

  @Override protected void onDestroy() {
    super.onDestroy();
  }

  //设置fragment
  public void setMainFragment(Fragment fragment) {
    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

    mFragment = fragment;

    transaction.replace(R.id.base, fragment);
    transaction.commitAllowingStateLoss();
  }



  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (mFragment != null) {
      mFragment.onActivityResult(requestCode, resultCode, data);
    }
  }
}