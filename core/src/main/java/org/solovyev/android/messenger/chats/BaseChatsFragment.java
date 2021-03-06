/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger.chats;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import org.solovyev.android.fragments.DetachableFragment;
import org.solovyev.android.menu.ActivityMenu;
import org.solovyev.android.menu.IdentifiableMenuItem;
import org.solovyev.android.menu.ListActivityMenu;
import org.solovyev.android.messenger.BaseAsyncListFragment;
import org.solovyev.android.messenger.SyncRefreshListener;
import org.solovyev.android.messenger.ToggleFilterInputMenuItem;
import org.solovyev.android.messenger.UiThreadEventListener;
import org.solovyev.android.messenger.accounts.AccountEvent;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.MessagesFragment;
import org.solovyev.android.messenger.sync.SyncTask;
import org.solovyev.android.messenger.users.BaseUserFragment;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.sherlock.menu.SherlockMenuHelper;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseChatsFragment extends BaseAsyncListFragment<UiChat, ChatListItem> implements DetachableFragment {

	@Nonnull
	protected static final String TAG = "ChatsFragment";

	@Nullable
	private JEventListener<ChatEvent> chatEventListener;

	public BaseChatsFragment() {
		super(TAG, R.string.mpp_messages, true, true);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
	}

	@Override
	protected boolean canReuseFragment(@Nonnull Fragment fragment, @Nonnull ChatListItem selectedItem) {
		boolean canReuse = false;
		final Chat chat = selectedItem.getChat();
		if (fragment instanceof MessagesFragment) {
			canReuse = ((MessagesFragment) fragment).getChat().equals(chat);
		} else if (fragment instanceof BaseUserFragment && chat.isPrivate()) {
			final Entity contact = chat.getSecondUser();
			final User fragmentUser = ((BaseUserFragment) fragment).getUser();
			canReuse = fragmentUser != null && fragmentUser.getEntity().equals(contact);
		}
		return canReuse;
	}

	@Override
	protected void onListLoaded() {
		super.onListLoaded();

		chatEventListener = UiThreadEventListener.onUiThread(this, new ChatEventListener());
		getChatService().addListener(chatEventListener);
	}

	@Override
	public void onStop() {
		if (chatEventListener != null) {
			getChatService().removeListener(chatEventListener);
		}

		super.onStop();
	}

	private class ChatEventListener extends AbstractJEventListener<ChatEvent> {

		private ChatEventListener() {
			super(ChatEvent.class);
		}

		@Override
		public void onEvent(@Nonnull final ChatEvent event) {
			getAdapter().onEvent(event);
		}
	}

	@Nonnull
	@Override
	protected abstract BaseChatsAdapter createAdapter();

	@Nonnull
	protected BaseChatsAdapter getAdapter() {
		return (BaseChatsAdapter) super.getAdapter();
	}

	@Nullable
	@Override
	protected ListViewAwareOnRefreshListener getBottomPullRefreshListener() {
		return null;
	}

	@Nullable
	@Override
	protected ListViewAwareOnRefreshListener getTopPullRefreshListener() {
		return new SyncRefreshListener(SyncTask.user_chats);
	}

	@Override
	protected void onEvent(@Nonnull AccountEvent event) {
		super.onEvent(event);
		switch (event.getType()) {
			case state_changed:
				postReload();
				break;
		}
	}

    /*
	**********************************************************************
    *
    *                           MENU
    *
    **********************************************************************
    */

	private ActivityMenu<Menu, MenuItem> menu;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return this.menu.onOptionsItemSelected(this.getActivity(), item);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		this.menu.onPrepareOptionsMenu(this.getActivity(), menu);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		final List<IdentifiableMenuItem<MenuItem>> menuItems = new ArrayList<IdentifiableMenuItem<MenuItem>>();

		menuItems.add(new ToggleFilterInputMenuItem(this));

		this.menu = ListActivityMenu.fromResource(R.menu.mpp_menu_chats, menuItems, SherlockMenuHelper.getInstance());
		this.menu.onCreateOptionsMenu(this.getActivity(), menu);
	}
}
