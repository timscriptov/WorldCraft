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

import com.solverlabs.droid.rugl.util.WorldUtils;
import com.solverlabs.worldcraft.R;
import com.solverlabs.worldcraft.activity.OptionActivity;
import com.solverlabs.worldcraft.factories.DescriptionFactory;
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
    private ListView roomListRatingView;
    private ListView roomListReadOnly;
    private ListView roomListUsersView;
    private ObjectCodec.RoomPack roomPack;
    private SearchRoomDialog searchRoomDialog;

    public RoomlistDialog(Context context) {
        super(context, 16973841);
        this.adapterPlayerNumber = null;
        this.adapterReadOnly = null;
        this.adapterRating = null;
        requestWindowFeature(1);
        setContentView(R.layout.join_room);
        getWindow().setFlags(1024, 1024);
        this.roomListUsersView = (ListView) findViewById(R.id.roomList);
        this.roomListReadOnly = (ListView) findViewById(R.id.roomList1);
        this.roomListRatingView = (ListView) findViewById(R.id.roomList2);
        Button createGameButton = (Button) findViewById(R.id.createGameButton);
        createGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                RoomlistDialog.this.dismiss();
                if (RoomlistDialog.this.onCreateRoomClickListener != null) {
                    if (WorldUtils.getmCreativeModeWorlds() == null || WorldUtils.getmCreativeModeWorlds().size() <= 0) {
                        RoomlistDialog.this.onCreateRoomClickListener.noCreativeModeWorlds();
                    } else {
                        RoomlistDialog.this.onCreateRoomClickListener.onCreateRoomClick();
                    }
                }
            }
        });
        Button refreshButton = (Button) findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (RoomlistDialog.this.onRefreshClickListener != null) {
                    RoomlistDialog.this.onRefreshClickListener.onRefreshClick();
                }
            }
        });
        Button searchButton = (Button) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                RoomlistDialog.this.hide();
                RoomlistDialog.this.showSearchRoomResultDialog();
            }
        });
        Button cancelButton = (Button) findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RoomlistDialog.this.onCancel();
            }
        });
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
        this.roomListUsersView.setAdapter((ListAdapter) this.adapterPlayerNumber);
        this.adapterReadOnly = new JoinRoomListAdapter(getContext(), roomsByEntranceNumber, (byte) 4);
        this.roomListReadOnly.setAdapter((ListAdapter) this.adapterReadOnly);
        this.adapterRating = new JoinRoomListAdapter(getContext(), roomsByRating, (byte) 3);
        this.roomListRatingView.setAdapter((ListAdapter) this.adapterRating);
        this.roomListUsersView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                RoomlistDialog.this.onRoomSelected(RoomlistDialog.this.adapterPlayerNumber.getItem(position));
            }
        });
        this.roomListReadOnly.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                RoomlistDialog.this.onRoomSelected(RoomlistDialog.this.adapterReadOnly.getItem(position));
            }
        });
        this.roomListRatingView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                RoomlistDialog.this.onRoomSelected(RoomlistDialog.this.adapterRating.getItem(position));
            }
        });
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
        builder.setTitle("Enter password").setView(password).setPositiveButton(OptionActivity.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                RoomlistDialog.this.hideKeyBoard(password);
                RoomlistDialog.this.onJoinRoom(password.getText().toString());
            }
        }).setNegativeButton(OptionActivity.CANCEL, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        this.passwordDialog = builder.create();
        this.passwordDialog.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void hideKeyBoard(View v) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService("input_method");
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onJoinRoom(String roomPassword) {
        this.roomPack.password = roomPassword;
        if (this.onRoomClickListener != null) {
            this.onRoomClickListener.onRoomClick(this.roomPack);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onCancel() {
        dismiss();
        if (this.onCancelClickListener != null) {
            this.onCancelClickListener.onCancelClick();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showSearchRoomResultDialog() {
        if (this.searchRoomDialog == null) {
            this.searchRoomDialog = new SearchRoomDialog(getContext());
        }
        this.searchRoomDialog.setOnCloseListener(new SearchRoomDialog.OnCloseListener() {
            @Override
            public void onClose() {
                RoomlistDialog.this.show();
            }
        });
        this.searchRoomDialog.setOnRoomClickListener(new SearchRoomDialog.OnRoomClickListener() {
            @Override
            public void onRoomClick(ObjectCodec.RoomPack roomPack) {
                RoomlistDialog.this.onRoomSelected(roomPack);
            }
        });
        this.searchRoomDialog.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onRoomSelected(ObjectCodec.RoomPack roomPack) {
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


    public interface OnCancelClickListener {
        void onCancelClick();
    }


    public interface OnCreateRoomClickListener {
        void noCreativeModeWorlds();

        void onCreateRoomClick();
    }


    public interface OnRefreshClickListener {
        void onRefreshClick();
    }


    public interface OnRoomClickListener {
        void onRoomClick(ObjectCodec.RoomPack roomPack);
    }
}
