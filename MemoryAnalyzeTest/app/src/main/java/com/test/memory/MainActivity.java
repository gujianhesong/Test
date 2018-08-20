package com.test.memory;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import com.test.memory.activity.AgentActivity;
import com.test.memory.fragment.HomeFragment;

public class MainActivity extends AgentActivity {

  public static void navigate(Context context){
    context.startActivity(new Intent(context, MainActivity.class));
  }

  @Override protected Class<? extends Fragment> provideFragmentClass() {
    return HomeFragment.class;
  }
}
