package dialogs;

import com.example.pathfinder.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

//public class SaveDialog extends DialogFragment {
//	
//	private EditText mapEt;
//	private int colors[] = new int[3];
//	private ArrayList<Point> oPositions = new ArrayList<Point>();
//	private ArrayList<Obstacle> obstacles;
//
//	public SaveDialog(int[] colors, ArrayList<Obstacle> obstacles) {
//		this.colors = colors;
//		this.obstacles = obstacles;
//		init();
//	}
//	
//	protected void init() {
//		if(obstacles.isEmpty())
//			return;
//		oPositions.clear();
//		for(Obstacle o : obstacles) {
//			oPositions.add(new Point(o.getGx(), o.getGy()));
//		}
//	}
//	
//	public interface SaveDialogListener {
//		public void saveMap(int[] colors, String mapName, ArrayList<Point> positions);
//		public void saveMap(String mapName);
//	}
//	
//	private SaveDialogListener listener;
//
//	@Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        // Dialog dialog = super.onCreateDialog(savedInstanceState);
//        AlertDialog.Builder db = new AlertDialog.Builder(getActivity());
//        LayoutInflater i = getActivity().getLayoutInflater();
//        
//        db.setTitle("Сохранение карты");
//        View v = i.inflate(R.layout.save_dialog, null);
//        
//        mapEt = (EditText) v.findViewById(R.id.editText1);
//        
//		db.setView(v);
//		db.setPositiveButton("Создать новую", new DialogInterface.OnClickListener() {
//		    public void onClick(DialogInterface dialog, int which) {
//		    	if(mapEt.getText().toString().length() == 0) {
//		    		Toast.makeText(getActivity().getApplicationContext(), "Введите название!", Toast.LENGTH_LONG);
//		    		return;}
//		    	listener.saveMap(colors, mapEt.getText().toString(), oPositions);
//		    }
//		});
//		db.setNegativeButton("Закрыть", new DialogInterface.OnClickListener() {
//		    public void onClick(DialogInterface dialog, int which) {
//				getDialog().dismiss();
//		    }
//		});
//		db.setCancelable(false);
//        
//        return db.create();
//    }
//	
//	@Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        // Проверить, что activity реализовала interface 
//        try {
//            listener = (SaveDialogListener) activity;
//        } catch (ClassCastException e) {
//            // activity не реализовала интерфейс 
//            throw new ClassCastException(activity.toString()
//                    + " реализуйте NoticeDialogListener");
//        }
//	}
//
//}
public class SaveDialog extends DialogFragment {	
	private EditText mapEt;

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
        
		db.setView(v);
		db.setPositiveButton("Создать новую", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
		    	if(mapEt.getText().toString().length() == 0) {
		    		Toast.makeText(getActivity().getApplicationContext(), "Введите название!", Toast.LENGTH_LONG);
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