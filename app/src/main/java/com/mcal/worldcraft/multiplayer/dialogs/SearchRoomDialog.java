package com.mcal.worldcraft.multiplayer.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.mcal.worldcraft.R;
import com.mcal.worldcraft.factories.DescriptionFactory;
import com.mcal.worldcraft.srv.util.ObjectCodec;
import com.mcal.worldcraft.ui.JoinRoomListAdapter;

import java.util.ArrayList;
import java.util.Collection;

public class SearchRoomDialog extends Dialog {
    private final EditText searchText;
    private JoinRoomListAdapter adapterSearch;
    private OnCloseListener onCloseListener;
    private OnRoomClickListener onRoomClickListener;

    public SearchRoomDialog(final Context context) {
        super(context);
        this.adapterSearch = null;
        requestWindowFeature(1);
        setContentView(R.layout.search_room);
        getWindow().setFlags(1024, 1024);
        this.searchText = findViewById(R.id.searchText);
        Button searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> {
            String searchValue = String.valueOf(searchText.getText());
            adapterSearch.setSearchValue(searchValue);
            adapterSearch.loadRoomlist();
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
        });
        Button cancelButton = findViewById(R.id.back_button);
        cancelButton.setOnClickListener(v -> onClose());
        setOnDismissListener(dialog -> onClose());
        initSearchRoomList(new ArrayList<>(), null);
        getWindow().setSoftInputMode(2);
    }

    public void setOnCloseListener(OnCloseListener onCloseListener) {
        this.onCloseListener = onCloseListener;
    }

    public void setOnRoomClickListener(OnRoomClickListener onRoomClickListener) {
        this.onRoomClickListener = onRoomClickListener;
    }

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
        ListView roomlist = findViewById(R.id.roomList);
        this.adapterSearch = new JoinRoomListAdapter(getContext(), roomsSearch, (byte) 0, searchValue);
        roomlist.setAdapter(this.adapterSearch);
        roomlist.setOnItemClickListener((arg0, arg1, position, arg3) -> {
            ObjectCodec.RoomPack roomPack = adapterSearch.getItem(position);
            if (onRoomClickListener != null) {
                onRoomClickListener.onRoomClick(roomPack);
            }
            dismiss();
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

    public void onResultsLoaded(@NonNull Collection<ObjectCodec.RoomPack> roomsSearch, short initRoomlistSize) {
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
