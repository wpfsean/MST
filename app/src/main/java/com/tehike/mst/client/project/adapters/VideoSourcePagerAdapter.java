package com.tehike.mst.client.project.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class VideoSourcePagerAdapter extends FragmentPagerAdapter {

	private List<Fragment> list;

	public VideoSourcePagerAdapter(FragmentManager fm, List<Fragment> list) {
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
