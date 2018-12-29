package com.tehike.mst.client.project.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import java.util.List;

/**
 * fragment滑动的布局的适配器
 *
 * 竖屏主界面 的滑动适配器
 *
 *
 */
public class SlidingPagerAdapter extends FragmentPagerAdapter {

	private List<Fragment> list;
	public SlidingPagerAdapter(FragmentManager fm, List<Fragment> list) {
		super(fm);
		this.list = list;

	}
	@Override
	public Fragment getItem(int arg0) {
		return list.get(arg0);
	}

	@Override
	public int getCount() {
		return list == null ? 0 : list.size();
	}

}
