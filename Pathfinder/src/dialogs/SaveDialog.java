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
	private EditText mapEt;

	public interface SaveDialogListener {
		public void saveDevMap(String mapName);
	}	
	private SaveDialogListener listener;

	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder db = new AlertDialog.Builder(getActivity());
        LayoutInflater i = getActivity().getLayoutInflater();
        
        db.setTitle("Сохранение карты");
        View v = i.inflate(R.layout.save_dialog, null);        
        mapEt = (EditText) v.findViewById(R.id.editText1);
        
        controller = new SQLController(getActivity());
        controller.open();
        
		db.setView(v);
		db.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
		    	if(mapEt.getText().toString().length() == 0) {
		    		Toast.makeText(getActivity().getApplicationContext(), "Введите название!", Toast.LENGTH_SHORT).show();
		    		return;}
		    	listener.saveDevMap(mapEt.getText().toString());
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