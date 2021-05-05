package dialogs;

import com.example.pathfinder.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import databases.DBHelper;
import databases.SQLController;

public class SaveDialog extends DialogFragment {
	private SQLController controller;
	private SimpleCursorAdapter adapter;
	private EditText mapEt;
	private TextView savedMapsStatus;
	private ListView mapLv;

	public interface SaveDialogListener {
		public void saveMap(String mapName);
	}	
	private SaveDialogListener listener;

	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder db = new AlertDialog.Builder(getActivity());
        LayoutInflater i = getActivity().getLayoutInflater();
        
        db.setTitle("Сохранение карты");
        View v = i.inflate(R.layout.save_dialog, null);        
        mapEt = (EditText) v.findViewById(R.id.editText1);
        savedMapsStatus = (TextView) v.findViewById(R.id.textView2);
        mapLv = (ListView) v.findViewById(R.id.saveMapLv);
        
        controller = new SQLController(getActivity());
        controller.open();
        
        int ids = controller.getProfilesCount();
        if(ids > 1) {
        	Cursor cursor = controller.readMaps();
     		String[] from = new String[] { DBHelper.ID, DBHelper.MAP_NAME };
     		int[] to = new int[] { R.id.rowid, R.id.mapname };
     		adapter = new SimpleCursorAdapter(getActivity(), R.layout.map_list, cursor, from, to, 1);
    		adapter.notifyDataSetChanged();
     		mapLv.setAdapter(adapter);
     		controller.close();
        } else {
        	savedMapsStatus.setText("К сожалению, у вас ещё нет карт. Сохраните текущее поле.");
        	controller.close();
        }
        
		db.setView(v);
		db.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
		    	if(mapEt.getText().toString().length() == 0) {
		    		Toast.makeText(getActivity().getApplicationContext(), "Введите название!", Toast.LENGTH_SHORT);
		    		return;}
		    	listener.saveMap(mapEt.getText().toString());
		    }
		});
		db.setNegativeButton("Закрыть", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
				getDialog().dismiss();
		    }
		});
		db.setCancelable(false);
        
        return db.create();
    }
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Проверить, что activity реализовала interface и получить на него ссылку
        try {
            listener = (SaveDialogListener) activity;
        } catch (ClassCastException e) {
            // activity не реализовала интерфейс 
            throw new ClassCastException(activity.toString() + " реализуйте SaveDialog");
        }
	}

}