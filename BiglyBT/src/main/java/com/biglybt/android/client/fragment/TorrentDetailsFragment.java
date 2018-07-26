/*
 * Copyright (c) Azureus Software, Inc, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.biglybt.android.client.fragment;

import java.util.List;

import com.astuetz.PagerSlidingTabStrip;
import com.biglybt.android.client.*;
import com.biglybt.android.client.activity.TorrentDetailsActivityTV;
import com.biglybt.android.client.activity.TorrentViewActivity;
import com.biglybt.android.client.adapter.TorrentDetailsPagerAdapter;
import com.biglybt.android.client.adapter.TorrentPagerAdapter;
import com.biglybt.android.client.session.SessionManager;
import com.biglybt.util.Thunk;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.view.ActionMode;
import android.view.*;

/**
 * Torrent Details Fragment<br>
 * - Contains {@link PeersFragment}, {@link FilesFragment}, {@link TorrentInfoFragment}<br>
 * - Contained in {@link TorrentViewActivity} for wide screens<br>
 * - Contained in {@link TorrentDetailsActivityTV} for narrow screens<br>
 */
public class TorrentDetailsFragment
	extends FragmentM
	implements ActionModeBeingReplacedListener, View.OnKeyListener
{
	private static final String TAG = "TorrentDetailsFrag";

	private TorrentPagerAdapter pagerAdapter;

	@Thunk
	long torrentID;

	@Override
	public void onStart() {
		super.onStart();
		AnalyticsTracker.getInstance(this).fragmentResume(this, TAG);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.frag_torrent_details, container,
				false);
		assert view != null;

		setHasOptionsMenu(true);

		ViewPager viewPager = view.findViewById(R.id.pager);
		PagerSlidingTabStrip tabs = view.findViewById(R.id.pager_title_strip);

		if (viewPager != null) {
			viewPager.setOnKeyListener(this);
		}
		view.setOnKeyListener(this);

		// adapter will bind pager, tabs and adapter together
		String remoteProfileID = SessionManager.findRemoteProfileID(getActivity(),
				TAG);
		pagerAdapter = new TorrentDetailsPagerAdapter(getFragmentManager(),
				viewPager, tabs, remoteProfileID);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		if (pagerAdapter != null) {
			pagerAdapter.onResume();
		}
	}

	@Override
	public void onPause() {
		if (pagerAdapter != null) {
			pagerAdapter.onPause();
		}

		super.onPause();
	}

	// Called from Activity
	public void setTorrentIDs(@Nullable long[] newIDs) {
		this.torrentID = newIDs != null && newIDs.length == 1 ? newIDs[0] : -1;
		if (pagerAdapter != null) {
			pagerAdapter.setSelection(torrentID);
		}
		AndroidUtilsUI.runOnUIThread(this, new Runnable() {
			public void run() {
				List<Fragment> fragments = AndroidUtilsUI.getFragments(
						getFragmentManager());
				for (Fragment item : fragments) {
					if (item instanceof SetTorrentIdListener) {
						((SetTorrentIdListener) item).setTorrentID(torrentID);
					}
				}
			}
		});
	}

	public void onCreateActionMode(@Nullable ActionMode mode, Menu menu) {
		MenuInflater inflater;
		if (mode == null) {
			FragmentActivity activity = getActivity();
			if (activity == null) {
				return;
			}
			inflater = activity.getMenuInflater();
		} else {
			inflater = mode.getMenuInflater();
		}
		List<Fragment> fragments = AndroidUtilsUI.getFragments(
				getFragmentManager());
		for (Fragment frag : fragments) {
			if (frag instanceof FragmentPagerListener) {
				frag.onCreateOptionsMenu(menu, inflater);
			}
		}
	}

	public void onPrepareActionMode(Menu menu) {
		List<Fragment> fragments = AndroidUtilsUI.getFragments(
				getFragmentManager());
		for (Fragment frag : fragments) {
			if (frag instanceof FragmentPagerListener) {
				frag.onPrepareOptionsMenu(menu);
			}
		}
	}

	public boolean onActionItemClicked(MenuItem item) {
		List<Fragment> fragments = AndroidUtilsUI.getFragments(
				getFragmentManager());
		for (Fragment frag : fragments) {
			if (frag instanceof FragmentPagerListener) {
				if (frag.onOptionsItemSelected(item)) {
					return true;
				}
			}
		}
		return false;
	}

	public void playVideo() {
		List<Fragment> fragments = AndroidUtilsUI.getFragments(
				getFragmentManager());
		for (Fragment frag : fragments) {
			if (frag instanceof FilesFragment) {
				((FilesFragment) frag).launchOrStreamFile();
			}
		}
	}

	@Override
	public void setActionModeBeingReplaced(ActionMode actionMode,
			boolean actionModeBeingReplaced) {
	}

	@Override
	public void actionModeBeingReplacedDone() {
	}

	@Override
	public void rebuildActionMode() {
	}

	@Override
	public ActionMode getActionMode() {
		if (pagerAdapter == null) {
			return null;
		}
		Fragment frag = pagerAdapter.getCurrentFragment();
		if (frag instanceof ActionModeBeingReplacedListener) {
			return ((ActionModeBeingReplacedListener) frag).getActionMode();
		}
		return null;
	}

	@Override
	public ActionMode.Callback getActionModeCallback() {
		if (pagerAdapter == null) {
			return null;
		}
		Fragment frag = pagerAdapter.getCurrentFragment();
		if (frag instanceof ActionModeBeingReplacedListener) {
			return ((ActionModeBeingReplacedListener) frag).getActionModeCallback();
		}
		return null;
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (pagerAdapter == null) {
			return false;
		}
		Fragment frag = pagerAdapter.getCurrentFragment();
		if (frag instanceof View.OnKeyListener) {
			return ((View.OnKeyListener) frag).onKey(v, keyCode, event);
		}
		return false;
	}
}
