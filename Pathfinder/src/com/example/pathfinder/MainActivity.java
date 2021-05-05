package com.example.pathfinder;

import java.util.ArrayList;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import databases.Cell;
import databases.Field;
import databases.SQLController;
import dialogs.GridSizeDialog;
import dialogs.WeightDialog;
import dialogs.AlgorithmSelector.AlgorithmListener;
import dialogs.AlgorithmSelector;
import dialogs.ColorPickDialog;
import dialogs.ColorPickDialog.ColorPickListener;
import dialogs.GridSizeDialog.GridSizeListener;
import dialogs.MapDialog;
import dialogs.MapDialog.MapListener;
import dialogs.SaveDialog;
import dialogs.ThemeDialog;
import dialogs.ThemeDialog.ThemeListener;
import dialogs.SaveDialog.SaveDialogListener;
import dialogs.WeightDialog.WeightListener;
import entities.Entity;
import entities.Obstacle;
import math.Point;
import views.AnimatePath;

public class MainActivity extends Activity implements OnClickListener, ColorPickListener,
		AlgorithmListener, SaveDialogListener, MapListener, WeightListener, GridSizeListener, ThemeListener {
	// компоненты UI
	Button start, algBtn;
	Button player, obstacle, target;
	TextView displayState, iterationsCount;
	int px, py, tx, ty, pColor, tColor, pathColor;

	ArrayList<Integer> oPosX, oPosY;
	CheckBox cb, nbCheckBox, distanceCb;
	LinearLayout background;
	SQLController controller;
	GridSizeDialog gridSizeDialog = new GridSizeDialog();

	// Перечисление
	State state;

	// Экземпляры классов игрока, цели и препятствий
	Entity p;
	Obstacle o;
	Entity t;

	private int iterations = 0;
	private int algorithm = 0;
	private int theme = 0;
	private boolean devMode = false; // false - использовать updated_menu.xml, true - menu.xml

	// Экземпляр класса для анимаций
	AnimatePath view;
	
	// Другое
	private int currentAlgState = 0;
	public static final int MAX_ALGORITHM_SIZE = 2;

	// Координаты нажатий
	private float xCoord, yCoord, x, y;

	// Массив координат сетки
	Point coords, screenCoords;

	// Поле карты
	Field field = new Field(1, "default", 16, Color.GREEN, Color.RED, Color.GRAY);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_star_layout);

		// Нахождение компонентов UI по ID
		start = (Button) findViewById(R.id.start);
		player = (Button) findViewById(R.id.player);
		obstacle = (Button) findViewById(R.id.obstacle);
		target = (Button) findViewById(R.id.target);
		algBtn = (Button) findViewById(R.id.algBtn);
		displayState = (TextView) findViewById(R.id.textView1);
		iterationsCount = (TextView) findViewById(R.id.textView2);
		background = (LinearLayout) findViewById(R.id.LinearLayout1);
		cb = (CheckBox) findViewById(R.id.checkBox1);
		nbCheckBox = (CheckBox) findViewById(R.id.checkBox2);
		distanceCb = (CheckBox) findViewById(R.id.checkBox3);
		cb.setChecked(true);

		controller = new SQLController(getApplicationContext());
		view = (AnimatePath) findViewById(R.id.pathAnimator1);
		view.setMap(field);
		new AnimatePath(getApplicationContext());

		// Назначение обработчиков событий
		player.setOnClickListener(this);
		obstacle.setOnClickListener(this);
		target.setOnClickListener(this);
		algBtn.setOnClickListener(this);

		state = State.PLAYER;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.player:
			state = State.PLAYER;
			displayState.setText("Игрок");
			break;
		case R.id.obstacle:
			state = State.OBSTACLE;
			displayState.setText("Препятствие");
			break;
		case R.id.target:
			state = State.TARGET;
			displayState.setText("Цель");
			break;
		case R.id.algBtn:
			currentAlgState++;
			if(currentAlgState > MAX_ALGORITHM_SIZE)
				currentAlgState = 0;
			checkAlgorithm(currentAlgState);
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(devMode == true ? R.menu.menu : R.menu.updated_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.loadMap:
			MapDialog loadDialog = new MapDialog(displayState);
			loadDialog.show(getFragmentManager(), "load");
			break;
		case R.id.saveMap:
			if (view.getPlayer() == null || view.getPlayer().getDeleted() // 
			 || view.getTarget() == null || view.getTarget().getDeleted())
				break;
			SaveDialog saveDialog = new SaveDialog();
			saveDialog.show(getFragmentManager(), "save");
			break;
		case R.id.clearMap:
			view.clearMap();
			break;
		case R.id.settings:
			Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
			settingsIntent.putExtra("gridSize", view.getGridSize());
			settingsIntent.putExtra("theme", theme);
			settingsIntent.putExtra("dev_mode", devMode);
			startActivityForResult(settingsIntent, 1);
			break;
		case R.id.bluetooth:
			Intent btIntent = new Intent(getApplicationContext(), ExchangeActivity.class);
			startActivity(btIntent);
			break;
		case R.id.selectColor:
			ColorPickDialog newFragment = new ColorPickDialog();
			newFragment.show(getFragmentManager(), "color");
			break;
		case R.id.changeWeight:
			WeightDialog wd = new WeightDialog();
			wd.show(getFragmentManager(), "ncost");
			break;
		case R.id.theme:
			ThemeDialog themeDialog = new ThemeDialog();
			themeDialog.show(getFragmentManager(), "theme");
			break;
		case R.id.algorithm:
			AlgorithmSelector algDialog = new AlgorithmSelector();
			algDialog.show(getFragmentManager(), "algorithm");
			break;
		case R.id.gridSize:
		    GridSizeDialog gSizeDialog = new GridSizeDialog();
		    gSizeDialog.show(getFragmentManager(), "grid");
		    break;
		case R.id.actionDelete:
			state = State.DELETE;
			displayState.setText("Удаление");
			break;
		}

		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data == null) {return;}
		if (resultCode == RESULT_OK) {
			changeGridSize(data.getIntExtra("gSize", 16));
		    theme = data.getIntExtra("tcode", 0);
		    onThemeSelected(theme);
		    pColor = data.getIntExtra("pcolor", -1);
		    tColor = data.getIntExtra("tcolor", -1);
		    pathColor = data.getIntExtra("path_color", -1);
		    onPickedColor(pColor, tColor, pathColor);
		    devMode = data.getBooleanExtra("dev_mode", false);
		    invalidateOptionsMenu();
		} else
			Toast.makeText(getApplicationContext(), "Произошла ошибка!", Toast.LENGTH_SHORT).show();
	}

	// Начало анимации
	public void onStart(View v) {
		view.setEnableNeighbours(nbCheckBox.isChecked());
		view.setDiagonal(cb.isChecked());
		view.setEDistance(distanceCb.isChecked());

		iterations = view.startButton(algorithm);
		if (iterations == 0)
			return;
		setDebugInfo(iterations, view);
	}

	public void setDebugInfo(int iterations, AnimatePath view) {
		iterationsCount.setText("iterations: " + iterations //
				+ " angle: " + view.angle() + "°" //
				+ " p: " + view.getPlayer().getPosition().getX() + " " //
				+ view.getPlayer().getPosition().getY() //
				+ " t: " + view.getTarget().getPosition().getX() + " " //
				+ view.getTarget().getPosition().getY() //
				+ " time: " + view.getTime() + "ms");
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// Определение координат нажатия

			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);

			Rect rectangle = new Rect();
			Window window = getWindow();
			window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
			int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();

			// int statusBarHeight = rectangle.top;
			// int height = metrics.heightPixels;
			// int h = contentViewTop - statusBarHeight;

			xCoord = event.getRawX();
			yCoord = event.getRawY() - contentViewTop;

			if (xCoord <= view.getX0() || xCoord > (view.getCanvasWidth() - view.getXoffset())
					|| view.convertToGridCoords(0, yCoord).getY() >= view.getGridSize() || yCoord <= view.getYoffset())
				return false;

			coords = view.convertToGridCoords(xCoord, yCoord);
			screenCoords = view.convertToScreenCoords(coords.getX(), coords.getY(), true);

			x = screenCoords.getX();
			y = screenCoords.getY();

			switch (state) {
			case PLAYER:
				// Удаление текущего игрока и прорисовка нового на поле
				view.drawPlayer(x, y);

				if (view.getPlayer() == null || view.getTarget() == null)
					break;
				iterations = view.getIterations();
				setDebugInfo(iterations, view);
				break;

			case OBSTACLE:
				view.drawObstacle(x, y);
				break;

			case TARGET:
				view.drawEndPoint(x, y);
				if (view.getPlayer() == null || view.getTarget() == null)
					break;
				iterations = view.getIterations();
				setDebugInfo(iterations, view);
				break;

			case DELETE:
				view.deletePoint(x, y);
				break;
			}
		}

		return super.onTouchEvent(event);
	}
	
	public void setAlgorithm(int algorithm) {
		this.algorithm = algorithm;
	}

	public TextView getDisplayState() {
		return displayState;
	}

	public void changeGridSize(int gridSizeValue) {
		if (view.getPlayer() == null || view.getTarget() == null) {
			Toast.makeText(getApplicationContext(), "Сначала поставьте начальную или конечную точку!",
					Toast.LENGTH_LONG).show();
			return;
		}
		view.setGridSize(gridSizeValue);
		view.revertAllPositions(view.getPlayer().getGPosition(), view.getTarget().getGPosition(), view.getObstacles());
	}

	@Override
	public void algorithmSelected(int algorithm) {
		checkAlgorithm(algorithm);
	}
	
	public void checkAlgorithm(int algorithm) {
		setAlgorithm(algorithm);
		if (algorithm == 1) { // Если выбран алгоритм best-first
			cb.setClickable(false);
			cb.setChecked(false);
			cb.setAlpha(0.5f);
			distanceCb.setClickable(false);
			distanceCb.setChecked(false);
			distanceCb.setAlpha(0.5f);
			algBtn.setText("Best-first");
		} else if (algorithm == 0) {
			cb.setClickable(true);
			cb.setChecked(true);
			cb.setAlpha(1.0f);
			distanceCb.setClickable(true);
			distanceCb.setAlpha(1.0f);
			nbCheckBox.setClickable(true);
			nbCheckBox.setAlpha(1.0f);
			algBtn.setText("Алгоритм A*");
		} else if (algorithm == 2) {
			cb.setClickable(false);
			cb.setChecked(false);
			cb.setAlpha(0.5f);
			distanceCb.setClickable(false);
			distanceCb.setChecked(false);
			distanceCb.setAlpha(0.5f);
			nbCheckBox.setClickable(false);
			nbCheckBox.setAlpha(0.5f);
			algBtn.setText("Дейкстра");
		}
	}

	@Override
	public void onPickedColor(int pColor, int tColor, int pathColor) {
		if(pColor != -1)
			view.setPlayerColor(pColor);
		if(tColor != -1)
			view.setTargetColor(tColor);
		if(pathColor != -1)
			view.setPathColor(pathColor);
	}
	
	@Override
	public void onWeightSelected(int d, int h) {
		Toast.makeText(getApplicationContext(), "Изменены веса: " + d + "d " + h + "h", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void changeGridSize(DialogFragment dialog, int gridSizeValue) {
		changeGridSize(gridSizeValue);
	}
	
	@SuppressWarnings("deprecation")
	public void setTheme(int[] ids) {
		// Изменение цвета текста
		int mainColor = getResources().getColor(ids[0]);
		int secondColor = getResources().getColor(ids[1]);
		start.setTextColor(mainColor);
		player.setTextColor(mainColor);
		target.setTextColor(mainColor);
		obstacle.setTextColor(mainColor);
		algBtn.setTextColor(mainColor);
		iterationsCount.setTextColor(secondColor);
		displayState.setTextColor(mainColor);
		cb.setTextColor(secondColor);
		distanceCb.setTextColor(secondColor);
		nbCheckBox.setTextColor(secondColor);

		// Изменение заднего фона
		start.setBackgroundResource(ids[3]);
		player.setBackgroundResource(ids[4]);
		target.setBackgroundResource(ids[4]);
		obstacle.setBackgroundResource(ids[4]);
		background.setBackgroundResource(ids[5]);
		algBtn.setBackgroundResource(ids[4]);
		if(ids[2] != -1)
			background.setBackgroundColor(getResources().getColor(ids[2]));
	}

	public void onThemeSelected(int theme) {
		this.theme = theme;
		switch (theme) {
		case 0: // Выбрана космическая тема
			view.setClassic(false);
			// RGB: 247 2 141 | HEX: #f7028d
			int ids[] = new int[] {R.color.purple, R.color.white, -1, R.drawable.start_btn, R.drawable.rounded_shape, 
					R.drawable.universe};
			setTheme(ids);
			break;
		case 1: // Выбрана классическая тема
			view.setClassic(true);
			int classicIDs[] = new int[] {R.color.black, R.color.black, R.color.white, 
					R.drawable.classic_start_btn, R.drawable.classic_shape, R.drawable.classic_shape};
			setTheme(classicIDs);
			break;
		}
	}

	@Override
	public void saveMap(String mapName) {
		ArrayList<Point> positions = new ArrayList<Point>();
		ArrayList<Obstacle> obstacles = view.getObstacles();
		for (Obstacle o : obstacles) {
			positions.add(new Point(o.getGx(), o.getGy()));
		}

		Point pPos = new Point(view.getPlayer());
		Point tPos = new Point(view.getTarget());

		int id = 0;
		if (!controller.nameExists(mapName)) { // вставка - такой карты ещё не было
			id = (int) controller.insertMap(mapName, view.getGridSize(), view.getPlayerColor(), view.getTargetColor(),
					view.getPathColor());
			controller.insertElement((int) pPos.getX(), (int) pPos.getY(), 1, id);
			controller.insertElement((int) tPos.getX(), (int) tPos.getY(), 2, id);

			if (!(positions == null || positions.isEmpty())) {
				for (Point p : positions) {
					controller.insertElement((int) p.getX(), (int) p.getY(), 3, id);
				}
			}

		} else { // обновление карты
			int mapID = (int) controller.getIdByName(mapName);
			controller.updateMap(mapID, mapName, view.getGridSize(), view.getPlayerColor(), view.getTargetColor(),
					view.getPathColor());

			id = (int) controller.getIdByMapId(1, mapID);
			controller.updateElements(id, (int) pPos.getX(), (int) pPos.getY(), 1, mapID);
			id = (int) controller.getIdByMapId(2, mapID);
			controller.updateElements(id, (int) tPos.getX(), (int) tPos.getY(), 2, mapID);

			ArrayList<Cell> obstacleCells = controller.readObstacles(mapID);
			for (Cell cell : obstacleCells) {
				controller.deleteElements(cell.cx, cell.cy, 3, mapID);
			}
			if (!(positions == null || positions.isEmpty())) {
				for (Point p : positions) {
					controller.insertElement((int) p.getX(), (int) p.getY(), 3, mapID);
				}
			}
		}

		displayState.setText("Карта Сохранена");
	}

	@Override
	public void openMap(Field field) {
		this.field = field;
		view.getPath().reset();
		view.invalidate();
		view.setGridSize(field.gsize);
		controller.loadConfiguration(field.mapName, view);
		displayState.setText("Карта Загружена");
	}

}

// Перечисление выбранных элементов
enum State {
	PLAYER, OBSTACLE, TARGET, DELETE,
}