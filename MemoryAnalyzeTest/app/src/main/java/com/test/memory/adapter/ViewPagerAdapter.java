package com.test.memory.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import java.util.List;

/**
 * Created by gujian on 2018-08-12.
 */

public class ViewPagerAdapter extends FragmentPagerAdapter {
  private List<Fragment> mFragmentList;
  private List<String> mTitleList;

  public ViewPagerAdapter(FragmentManager fm, List<Fragment> fragmentList, List<String> titleList) {
    super(fm);
    mFragmentList = fragmentList;
    mTitleList = titleList;
  }

  @Override public Fragment getItem(int position) {
    return mFragmentList.get(position);
  }

  @Override public int getCount() {
    return mFragmentList.size();
  }

  @Override public CharSequence getPageTitle(int position) {
    return mTitleList.get(position);
  }
}
