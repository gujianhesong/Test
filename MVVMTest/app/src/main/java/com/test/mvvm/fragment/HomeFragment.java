package com.test.mvvm.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.test.mvvm.R;
import com.test.mvvm.adapter.ViewPagerAdapter;
import com.test.mvvm.common.CommonInfo;
import com.test.mvvm.widget.ViewPagerTabs;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gujian on 2018-08-12.
 */

public class HomeFragment extends Fragment {
  private ViewPagerTabs mIndicator;
  private ViewPager mViewPager;
  private List<Fragment> mFragmentList;
  private List<String> mTitleList;

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_home, null);

    return view;
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mIndicator = view.findViewById(R.id.indicator);
    mViewPager = view.findViewById(R.id.viewPager);

    initTitile();
    initFragment();
    initViewPager();
  }

  private void initTitile() {
    mTitleList = new ArrayList<>();
    for (int titleId : CommonInfo.initNewsTitles) {
      mTitleList.add(getString(titleId));
    }
  }

  private void initFragment() {
    mFragmentList = new ArrayList<>();
    for (int i = 0; i < mTitleList.size(); i++) {
      mFragmentList.add(NewsListFragment.newInstance().setNewsType(CommonInfo.initNewsIds[i]));
    }
  }

  private void initViewPager() {
    //设置适配器
    mViewPager.setAdapter(new ViewPagerAdapter(getFragmentManager(), mFragmentList, mTitleList));
    mIndicator.setViewPager(mViewPager);
    mViewPager.addOnPageChangeListener(mIndicator);
  }
}
