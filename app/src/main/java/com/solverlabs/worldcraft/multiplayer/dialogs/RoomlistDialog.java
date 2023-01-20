package com.solverlabs.worldcraft.multiplayer.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.util.WorldUtils;
import com.solverlabs.worldcraft.activity.OptionActivity;
import com.solverlabs.worldcraft.factories.DescriptionFactory;
import com.solverlabs.worldcraft.R;
import com.solverlabs.worldcraft.multiplayer.dialogs.SearchRoomDialog;
import com.solverlabs.worldcraft.srv.util.ObjectCodec;
import com.solverlabs.worldcraft.ui.JoinRoomListAdapter;
import java.util.ArrayList;
import java.util.Collection;

public class RoomlistDialog extends Dialog {
    private JoinRoomListAdapter adapterPlayerNumber;
    private JoinRoomListAdapter adapterRating;
    private JoinRoomListAdapter adapterReadOnly;
    private OnCancelClickListener onCancelClickListener;
    private OnCreateRoomClickListener onCreateRoomClickListener;
    private OnRefreshClickListener onRefreshClickListener;
    private OnRoomClickListener onRoomClickListener;
    private AlertDialog passwordDialog;
    private final ListView roomListRatingView;
    private final ListView roomListReadOnly;
    private final ListView roomListUsersView;
    private ObjectCodec.RoomPack roomPack;
    private SearchRoomDialog searchRoomDialog;

    /* loaded from: classes.dex */
    public interface OnCancelClickListener {
        void onCancelClick();
    }

    /* loaded from: classes.dex */
    public interface OnCreateRoomClickListener {
        void noCreativeModeWorlds();

        void onCreateRoomClick();
    }

    /* loaded from: classes.dex */
    public interface OnRefreshClickListener {
        void onRefreshClick();
    }

    /* loaded from: classes.dex */
    public interface OnRoomClickListener {
        void onRoomClick(ObjectCodec.RoomPack roomPack);
    }

    public RoomlistDialog(Context context) {
        super(context);
        this.adapterPlayerNumber = null;
        this.adapterReadOnly = null;
        this.adapterRating = null;
        requestWindowFeature(1);
        setContentView(R.layout.join_room);
        getWindow().setFlags(1024, 1024);
        this.roomListUsersView = findViewById(R.id.roomList);
        this.roomListReadOnly = findViewById(R.id.roomList1);
        this.roomListRatingView = findViewById(R.id.roomList2);
        Button createGameButton = findViewById(R.id.createGameButton);
        createGameButton.setOnClickListener(arg0 -> {
            dismiss();
            if (onCreateRoomClickListener != null) {
                if (WorldUtils.getCreativeModeWorlds() == null || WorldUtils.getCreativeModeWorlds().size() <= 0) {
                    onCreateRoomClickListener.noCreativeModeWorlds();
                } else {
                    onCreateRoomClickListener.onCreateRoomClick();
                }
            }
        });
        Button refreshButton = findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(arg0 -> {
            if (onRefreshClickListener != null) {
                onRefreshClickListener.onRefreshClick();
            }
        });
        Button searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(arg0 -> {
            hide();
            showSearchRoomResultDialog();
        });
        Button cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> onCancel());
    }

    public void setOnCreateRoomClickListener(OnCreateRoomClickListener onCreateRoomClickListener) {
        this.onCreateRoomClickListener = onCreateRoomClickListener;
    }

    public void setOnRefreshClickListener(OnRefreshClickListener onRefreshClickListener) {
        this.onRefreshClickListener = onRefreshClickListener;
    }

    public void setOnCancelClickListener(OnCancelClickListener onCancelClickListener) {
        this.onCancelClickListener = onCancelClickListener;
    }

    public void setOnRoomClickListener(OnRoomClickListener onRoomClickListener) {
        this.onRoomClickListener = onRoomClickListener;
    }

    public void initData(ArrayList<ObjectCodec.RoomPack> roomsByEntranceNumber, ArrayList<ObjectCodec.RoomPack> roomsByPlayerNumber, ArrayList<ObjectCodec.RoomPack> roomsByRating) {
        this.adapterPlayerNumber = new JoinRoomListAdapter(getContext(), roomsByPlayerNumber, (byte) 1);
        this.roomListUsersView.setAdapter(this.adapterPlayerNumber);
        this.adapterReadOnly = new JoinRoomListAdapter(getContext(), roomsByEntranceNumber, (byte) 4);
        this.roomListReadOnly.setAdapter(this.adapterReadOnly);
        this.adapterRating = new JoinRoomListAdapter(getContext(), roomsByRating, (byte) 3);
        this.roomListRatingView.setAdapter(this.adapterRating);
        this.roomListUsersView.setOnItemClickListener((arg0, arg1, position, arg3) -> onRoomSelected(adapterPlayerNumber.getItem(position)));
        this.roomListReadOnly.setOnItemClickListener((arg0, arg1, position, arg3) -> onRoomSelected(adapterReadOnly.getItem(position)));
        this.roomListRatingView.setOnItemClickListener((arg0, arg1, position, arg3) -> onRoomSelected(adapterRating.getItem(position)));
        this.roomListUsersView.setOnScrollListener(new JoinRoomListAdapter.JoinRoomOnScrollListener(this.adapterPlayerNumber));
        this.roomListReadOnly.setOnScrollListener(new JoinRoomListAdapter.JoinRoomOnScrollListener(this.adapterReadOnly));
        this.roomListRatingView.setOnScrollListener(new JoinRoomListAdapter.JoinRoomOnScrollListener(this.adapterRating));
    }

    public void onRoomlistLoaded(Collection<ObjectCodec.RoomPack> roomsReadOnly, Collection<ObjectCodec.RoomPack> roomsByPlayerNumber, Collection<ObjectCodec.RoomPack> roomsByRating, Collection<ObjectCodec.RoomPack> roomsSearch, short initRoomlistSize) {
        if (this.searchRoomDialog != null && this.searchRoomDialog.isRoomlistLoading()) {
            this.searchRoomDialog.onResultsLoaded(roomsSearch, initRoomlistSize);
        } else if (roomsReadOnly.size() > 0 || roomsByPlayerNumber.size() > 0 || roomsByRating.size() > 0) {
            if (roomsByPlayerNumber.size() > 0) {
                this.adapterPlayerNumber.roomlistLoaded(roomsByPlayerNumber, initRoomlistSize);
            }
            if (roomsReadOnly.size() > 0) {
                this.adapterReadOnly.roomlistLoaded(roomsReadOnly, initRoomlistSize);
            }
            if (roomsByRating.size() > 0) {
                this.adapterRating.roomlistLoaded(roomsByRating, initRoomlistSize);
            }
        } else if (this.adapterReadOnly.isRoomlistLoading()) {
            this.adapterReadOnly.fullRoomlistLoaded();
        } else if (this.adapterPlayerNumber.isRoomlistLoading()) {
            this.adapterPlayerNumber.fullRoomlistLoaded();
        } else if (this.adapterRating.isRoomlistLoading()) {
            this.adapterRating.fullRoomlistLoaded();
        }
    }

    @Override 
    public void show() {
        super.show();
    }

    @Override 
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4) {
            onCancel();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final EditText password = new EditText(getContext());
        password.setInputType(129);
        builder.setTitle("Enter password").setView(password).setPositiveButton(OptionActivity.OK, (dialog, id) -> {
            hideKeyBoard(password);
            onJoinRoom(password.getText().toString());
        }).setNegativeButton(OptionActivity.CANCEL, (dialog, id) -> dialog.dismiss());
        this.passwordDialog = builder.create();
        this.passwordDialog.show();
    }

    public void hideKeyBoard(@NonNull View v) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public void onJoinRoom(String roomPassword) {
        this.roomPack.password = roomPassword;
        if (this.onRoomClickListener != null) {
            this.onRoomClickListener.onRoomClick(this.roomPack);
        }
    }

    public void onCancel() {
        dismiss();
        if (this.onCancelClickListener != null) {
            this.onCancelClickListener.onCancelClick();
        }
    }

    public void showSearchRoomResultDialog() {
        if (this.searchRoomDialog == null) {
            this.searchRoomDialog = new SearchRoomDialog(getContext());
        }
        this.searchRoomDialog.setOnCloseListener(this::show);
        this.searchRoomDialog.setOnRoomClickListener(this::onRoomSelected);
        this.searchRoomDialog.show();
    }

    public void onRoomSelected(@NonNull ObjectCodec.RoomPack roomPack) {
        this.roomPack = roomPack;
        if (roomPack.hasPassword) {
            showPasswordDialog();
        } else {
            onJoinRoom(DescriptionFactory.emptyText);
        }
    }

    @Override 
    public void dismiss() {
        if (this.passwordDialog != null) {
            this.passwordDialog.dismiss();
        }
        if (this.searchRoomDialog != null) {
            this.searchRoomDialog.dismiss();
        }
        super.dismiss();
    }
}
