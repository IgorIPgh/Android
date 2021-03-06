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
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import entities.Node;

public class WeightDialog extends DialogFragment {
	
	NumberPicker np1, np2;
	EditText et1, et2;
	TextView displayWeight;
	private int dCost = 14, hCost = 10;
	
	Node node = new Node();
	
	public interface WeightListener {
		public void onWeightSelected(int d, int h);
	}
	
	private WeightListener listener;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Dialog dialog = super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder db = new AlertDialog.Builder(getActivity());
        LayoutInflater i = getActivity().getLayoutInflater();
        
        db.setTitle("Изменение весов (A*)");
        View v = i.inflate(R.layout.weight_dialog, null);
        
        np1 = (NumberPicker) v.findViewById(R.id.numberPicker1);
        np2 = (NumberPicker) v.findViewById(R.id.numberPicker2);
        
        if(np1 != null) {
        	np1.setMinValue(1);
        	np1.setMaxValue(30);
        	np1.setValue(Node.DIAGONAL_COST);
        	np1.setWrapSelectorWheel(true);
        	np1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
				@Override
				public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
					dCost = newVal;
			    	/* et1.setText(String.valueOf(dCost));
			    	et2.setText(String.valueOf(hCost)); */
					displayWeight.setText("Текущие размеры: " + dCost + "d" + " " + hCost + "h");
					// Toast.makeText(getActivity(), "Стоимости: " + dCost + " " + hCost, Toast.LENGTH_SHORT).show();;
				}
			});
        }
        
        if(np2 != null) {
        	np2.setMinValue(1);
        	np2.setMaxValue(30);
        	np2.setValue(Node.CELL_COST);
        	np2.setWrapSelectorWheel(true);
        	np2.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
				@Override
				public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
					hCost = newVal;
					displayWeight.setText("Текущие размеры: " + dCost + "d" + " " + hCost + "h");
				}
			});
        }
        
        displayWeight = (TextView) v.findViewById(R.id.textView1);
        
		db.setView(v);
		db.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
		    	listener.onWeightSelected(dCost, hCost);
		    	Node.CELL_COST = hCost;
		    	Node.DIAGONAL_COST = dCost;
				getDialog().dismiss();
		    }
		});
		/* db.setNeutralButton("Сохранить (ввод)", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(et1.getText().toString().length() == 0 || et2.getText().toString().length() == 0) {
					et1.setText(String.valueOf(dCost));
					et2.setText(String.valueOf(hCost));
					return;}
				Node.DIAGONAL_COST = Integer.parseInt(et1.getText().toString());
				Node.CELL_COST = Integer.parseInt(et2.getText().toString());
				Toast.makeText(getActivity(), "Изменены размеры сетки: " + Node.DIAGONAL_COST + "d" + " " + Node.CELL_COST + "h", Toast.LENGTH_LONG).show();
				getDialog().dismiss();
			}
		}); */
		db.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
				getDialog().dismiss();
		    }
		});
        
        return db.create();
    }
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Проверить, что activity реализовала interface 
        try {
            // Получаем ссылку на NoticeDialogListener для доставки событий в activity 
            listener = (WeightListener) activity;
        } catch (ClassCastException e) {
            // activity не реализовала интерфейс 
            throw new ClassCastException(activity.toString()
                    + " реализуйте NoticeDialogListener");
        }
    }
	
}
