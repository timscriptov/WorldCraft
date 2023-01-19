package com.solverlabs.worldcraft.multiplayer.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.solverlabs.worldcraft.R;
import com.solverlabs.worldcraft.factories.DescriptionFactory;
import com.solverlabs.worldcraft.srv.util.ObjectCodec;
import com.solverlabs.worldcraft.ui.JoinRoomListAdapter;

import java.util.ArrayList;
import java.util.Collection;


public class SearchRoomDialog extends Dialog {
    private JoinRoomListAdapter adapterSearch;
    private OnCloseListener onCloseListener;
    private OnRoomClickListener onRoomClickListener;
    private EditText searchText;

    public SearchRoomDialog(final Context context) {
        super(context, 16973841);
        this.adapterSearch = null;
        requestWindowFeature(1);
        setContentView(R.layout.search_room);
        getWindow().setFlags(1024, 1024);
        this.searchText = (EditText) findViewById(R.id.searchText);
        Button searchButton = (Button) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchValue = String.valueOf(SearchRoomDialog.this.searchText.getText());
                SearchRoomDialog.this.adapterSearch.setSearchValue(searchValue);
                SearchRoomDialog.this.adapterSearch.loadRoomlist();
                InputMethodManager imm = (InputMethodManager) context.getSystemService("input_method");
                imm.hideSoftInputFromWindow(SearchRoomDialog.this.searchText.getWindowToken(), 0);
            }
        });
        Button cancelButton = (Button) findViewById(R.id.back_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchRoomDialog.this.onClose();
            }
        });
        setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                SearchRoomDialog.this.onClose();
            }
        });
        initSearchRoomList(new ArrayList<>(), null);
        getWindow().setSoftInputMode(2);
    }

    public void setOnCloseListener(OnCloseListener onCloseListener) {
        this.onCloseListener = onCloseListener;
    }

    public void setOnRoomClickListener(OnRoomClickListener onRoomClickListener) {
        this.onRoomClickListener = onRoomClickListener;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onClose() {
        if (this.onCloseListener != null) {
            this.onCloseListener.onClose();
        }
        if (this.adapterSearch != null) {
            this.adapterSearch.clear();
        }
        dismiss();
    }

    private void initSearchRoomList(ArrayList<ObjectCodec.RoomPack> roomsSearch, String searchValue) {
        ListView roomlist = (ListView) findViewById(R.id.roomList);
        this.adapterSearch = new JoinRoomListAdapter(getContext(), roomsSearch, (byte) 0, searchValue);
        roomlist.setAdapter((ListAdapter) this.adapterSearch);
        roomlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                ObjectCodec.RoomPack roomPack = SearchRoomDialog.this.adapterSearch.getItem(position);
                if (SearchRoomDialog.this.onRoomClickListener != null) {
                    SearchRoomDialog.this.onRoomClickListener.onRoomClick(roomPack);
                }
                SearchRoomDialog.this.dismiss();
            }
        });
        roomlist.setOnScrollListener(new JoinRoomListAdapter.JoinRoomOnScrollListener(this.adapterSearch));
    }

    @Override
    public void show() {
        if (this.adapterSearch != null) {
            this.adapterSearch.clear();
        }
        this.searchText.setText(DescriptionFactory.emptyText);
        super.show();
    }

    public void onResultsLoaded(Collection<ObjectCodec.RoomPack> roomsSearch, short initRoomlistSize) {
        if (roomsSearch.size() > 0) {
            this.adapterSearch.roomlistLoaded(roomsSearch, initRoomlistSize);
        } else {
            this.adapterSearch.fullRoomlistLoaded();
        }
    }

    public boolean isRoomlistLoading() {
        return this.adapterSearch != null && this.adapterSearch.isRoomlistLoading();
    }


    public interface OnCloseListener {
        void onClose();
    }


    public interface OnRoomClickListener {
        void onRoomClick(ObjectCodec.RoomPack roomPack);
    }
}
