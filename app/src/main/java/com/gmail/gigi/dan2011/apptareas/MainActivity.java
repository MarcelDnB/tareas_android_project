package com.gmail.gigi.dan2011.apptareas;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private ListView mListView;
    private AvisosDBAdapter mDbAdapter;
    private AvisosSimpleCursorAdapter mCursorAdapter;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher);

        mListView = (ListView) findViewById(R.id.avisos_list_view);
        findViewById(R.id.avisos_list_view);
        mListView.setDivider(null);
        mDbAdapter = new AvisosDBAdapter(this);
        mDbAdapter.open();


        // cuando pulsamos un item individual en la  listview
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, final int masterListPosition, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                ListView modeListView = new ListView(MainActivity.this);
                String[] modes = new String[]{"Ver Tarea","Editar Tarea", "Borrar Tarea"};
                ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(MainActivity.this,
                        android.R.layout.simple_list_item_1, android.R.id.text1, modes);
                modeListView.setAdapter(modeAdapter);
                modeListView.setBackgroundColor(Color.parseColor("#919191"));
                builder.setView(modeListView);
                final Dialog dialog = builder.create();
                dialog.show();
                modeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        //editar aviso
                        if(position == 0) {
                            int nId = getIdFromPosition(masterListPosition);
                            Aviso aviso = mDbAdapter.fetchReminderById(nId);
                            fireCustomDialog(aviso,1);

                        }
                        if (position == 1) {
                            int nId = getIdFromPosition(masterListPosition);
                            Aviso aviso = mDbAdapter.fetchReminderById(nId);
                            fireCustomDialog(aviso,0);

                        }
                        if(position == 2) {
                            mDbAdapter.deleteReminderById(getIdFromPosition(masterListPosition));
                            mCursorAdapter.changeCursor(mDbAdapter.fetchAllReminders());
                        }
                        dialog.dismiss();
                    }
                });
            }
        });


        Cursor cursor = mDbAdapter.fetchAllReminders();

        //desde las columnas definidas en la base de datos
        String[] from = new String[]{
                AvisosDBAdapter.COL_CONTENT
        };

        //a la id de views en el layout
        int[] to = new int[]{
                R.id.row_text
        };

        mCursorAdapter = new AvisosSimpleCursorAdapter(
                //context
                MainActivity.this,
                //el layout de la fila
                R.layout.avisos_row,
                //cursor
                cursor,
                //desde columnas definidas en la base de datos
                from,
                //a las ids de views en el layout
                to,
                //flag - no usado
                0);

        //el cursorAdapter (controller) está ahora actualizando la listView (view)
        //con datos desde la base de datos (modelo)
        mListView.setAdapter(mCursorAdapter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                }

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.cam_menu, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_item_delete_aviso:
                            for (int nC = mCursorAdapter.getCount() - 1; nC >= 0; nC--) {
                                if (mListView.isItemChecked(nC)) {
                                    mDbAdapter.deleteReminderById(getIdFromPosition(nC));
                                }
                            }
                            mode.finish();
                            mCursorAdapter.changeCursor(mDbAdapter.fetchAllReminders());
                            return true;
                    }
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                }
            });

        }
    }

    private int getIdFromPosition(int nC) {
        return (int) mCursorAdapter.getItemId(nC);
    }

    private void fireCustomDialog(final Aviso aviso,final int aux) {
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (aux == 1) {
            dialog.setContentView(R.layout.ver_activity);
        }else if(aux==2) {
            dialog.setContentView(R.layout.acercade_activity);
        }else {
            dialog.setContentView(R.layout.dialog_custom);
        }
        TextView titleView = (TextView) dialog.findViewById(R.id.custom_title);
        final EditText editCustom = (EditText) dialog.findViewById(R.id.custom_edit_reminder);
        final TextView showText = (TextView) dialog.findViewById(R.id.ver_texto);
        final TextView acercade_text = (TextView) dialog.findViewById(R.id.acercade_text);
        Button commitButton = (Button) dialog.findViewById(R.id.custom_button_commit);
        Button verButton = (Button) dialog.findViewById(R.id.ver_button);
        Button acercadeButton = (Button) dialog.findViewById(R.id.acercade_button);
        final CheckBox checkBox = (CheckBox) dialog.findViewById(R.id.custom_check_box);
        LinearLayout rootLayout = (LinearLayout) dialog.findViewById(R.id.custom_root_layout);
        final boolean isEditOperation = (aviso != null);
        final boolean isShowOperation = (aux == 1);
        final boolean isAcercadeOperation = (aux==2);

        //esto es para un edit
        if (isEditOperation && !isShowOperation) {
            titleView.setText("Editar Tarea:");
            checkBox.setChecked(aviso.getImportant() == 1);
            editCustom.setText(aviso.getContent());
            rootLayout.setBackgroundColor(getResources().getColor(R.color.prueba3));
        }

        if (isShowOperation) {
            showText.setText(aviso.getContent());
            verButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }else if(isAcercadeOperation) {
            acercade_text.setText("Este es un proyecto free-source, si me quieres ayudar o tienes alguna idea de una posible implementacion descarga mi proyecto de github: \n\n https://github.com/MarcelDnB\n\n Ó habla conmigo:\n\n Contacto: gigi.dan2011@gmail.com");
            acercadeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
        else {
            commitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String reminderText = editCustom.getText().toString();
                    if (isEditOperation && !isShowOperation) {
                        Aviso reminderEdited = new Aviso(aviso.getId(),
                                reminderText, checkBox.isChecked() ? 1 : 0);
                        mDbAdapter.updateReminder(reminderEdited);
                        //esto es para nuevo aviso
                    }
                    if (!isEditOperation && !isShowOperation) {
                        mDbAdapter.createReminder(reminderText, checkBox.isChecked());
                    }
                    mCursorAdapter.changeCursor(mDbAdapter.fetchAllReminders());
                    dialog.dismiss();
                }
            });
            Button buttonCancel = (Button) dialog.findViewById(R.id.custom_button_cancel);
            buttonCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_nuevo:
                //crear nuevo Aviso
                fireCustomDialog(null,3);
                return true;
            case R.id.action_salir:
                finish();
                return true;
            case R.id.action_acercade:
                fireCustomDialog(null,2);
                return true;
            default:
                return false;
        }
    }
}
