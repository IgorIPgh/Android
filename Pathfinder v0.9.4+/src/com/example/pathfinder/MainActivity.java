package com.example.pathfinder;

import java.util.ArrayList;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
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
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import databases.Cell;
import databases.Field;
import databases.SQLController;
import dialogs.GridSizeDialog;
import dialogs.WeightDialog;
import dialogs.AlgorithmSelector;
import dialogs.AlgorithmSelector.AlgorithmListener;
import dialogs.ColorPickDialog;
import dialogs.ColorPickDialog.ColorPickListener;
import dialogs.GridSizeDialog.GridSizeListener;
import dialogs.MapDialog;
import dialogs.MapDialog.MapListener;
import dialogs.SaveDialog;
import dialogs.SaveDialog.SaveDialogListener;
import dialogs.ThemeDialog;
import dialogs.ThemeDialog.ThemeListener;
import entities.Entity;
import entities.Obstacle;
import math.Point;
import views.AnimatePath;

public class MainActivity extends Activity implements OnClickListener, ColorPickListener, GridSizeListener,
		AlgorithmListener, ThemeListener, SaveDialogListener, MapListener {
	// компоненты UI
	Button start;
	Button player, obstacle, target;
	TextView displayState, iterationsCount;
	int px, py, tx, ty, pColor, tColor;

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

	// Экземпляр класса для анимаций
	AnimatePath view;

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

		new AnimatePath(getApplicationContext(), iterationsCount);

		// Назначение обработчиков событий
		player.setOnClickListener(this);
		obstacle.setOnClickListener(this);
		target.setOnClickListener(this);

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
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
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
		case R.id.gridSize:
			GridSizeDialog gSizeDialog = new GridSizeDialog();
			gSizeDialog.show(getFragmentManager(), "grid");
			break;
		case R.id.theme:
			ThemeDialog themeDialog = new ThemeDialog();
			themeDialog.show(getFragmentManager(), "theme");
			break;
		case R.id.algorithm:
			AlgorithmSelector algDialog = new AlgorithmSelector();
			algDialog.show(getFragmentManager(), "algorithm");
			break;
		case R.id.actionDelete:
			state = State.DELETE;
			displayState.setText("Удаление");
			break;
		}

		return super.onOptionsItemSelected(item);
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

//	public int getAlgorithm() {
//		return algorithm;
//	}

	public void setAlgorithm(int algorithm) {
		this.algorithm = algorithm;
	}

//	public int getId() {
//		return id;
//	}

//	public void setId(int id) {
//		this.id = id;
//	}

	@Override
	public void changeGridSize(DialogFragment dialog, int gridSizeValue) {
		if (view.getPlayer() == null || view.getTarget() == null) {
			Toast.makeText(getApplicationContext(), "Сначала поставьте начальную или конечную точку!",
					Toast.LENGTH_LONG).show();
			return;
		}
		view.setGridSize(gridSizeValue/* , view.getGridSize() */);
		view.revertAllPositions(view.getPlayer().getGPosition(), view.getTarget().getGPosition(), view.getObstacles());
	}

	@Override
	public void invalidName(DialogFragment dialog) {

	}

	@Override
	public void algorithmSelected(int algorithm) {
		setAlgorithm(algorithm);
		if (algorithm == 1) { // Если выбран алгоритм best-first
			cb.setClickable(false);
			cb.setChecked(false);
			cb.setAlpha(0.5f);
			distanceCb.setClickable(false);
			distanceCb.setChecked(false);
			distanceCb.setAlpha(0.5f);
		} else if (algorithm == 0) {
			cb.setClickable(true);
			cb.setChecked(true);
			cb.setAlpha(1.0f);
			distanceCb.setClickable(true);
			distanceCb.setAlpha(1.0f);
			nbCheckBox.setClickable(true);
			nbCheckBox.setAlpha(1.0f);
		} else if (algorithm == 2) {
			cb.setClickable(false);
			cb.setChecked(false);
			cb.setAlpha(0.5f);
			distanceCb.setClickable(false);
			distanceCb.setChecked(false);
			distanceCb.setAlpha(0.5f);
			nbCheckBox.setClickable(false);
			nbCheckBox.setAlpha(0.5f);
		}
	}

	@Override
	public void onPickedColor(int pColor, int tColor, int pathColor) {
		view.setPlayerColor(pColor);
		view.setTargetColor(tColor);
		view.setPathColor(pathColor);
	}

	@Override
	public void onThemeSelected(int theme) {
		switch (theme) {
		case 0: // Выбрана космическая тема
			view.setClassic(false);

			// HEX-код фиолетового цвета
			int purpleColor = Color.rgb(247, 2, 141);

			// Изменение цвета текста
			start.setTextColor(purpleColor);
			player.setTextColor(purpleColor);
			target.setTextColor(purpleColor);
			obstacle.setTextColor(purpleColor);
			iterationsCount.setTextColor(Color.WHITE);
			displayState.setTextColor(purpleColor);
			cb.setTextColor(Color.WHITE);
			distanceCb.setTextColor(Color.WHITE);
			nbCheckBox.setTextColor(Color.WHITE);

			// Изменение заднего фона
			start.setBackgroundResource(R.drawable.start_btn);
			player.setBackgroundResource(R.drawable.rounded_shape);
			target.setBackgroundResource(R.drawable.rounded_shape);
			obstacle.setBackgroundResource(R.drawable.rounded_shape);
			background.setBackgroundResource(R.drawable.universe1);
			break;

		case 1: // Выбрана классическая тема
			view.setClassic(true);

			// Изменение цвета текста
			start.setTextColor(Color.BLACK);
			player.setTextColor(Color.BLACK);
			target.setTextColor(Color.BLACK);
			obstacle.setTextColor(Color.BLACK);
			iterationsCount.setTextColor(Color.BLACK);
			displayState.setTextColor(Color.BLACK);
			cb.setTextColor(Color.BLACK);
			distanceCb.setTextColor(Color.BLACK);
			nbCheckBox.setTextColor(Color.BLACK);

			// Изменение заднего фона
			start.setBackgroundResource(R.drawable.classic_start_btn);
			player.setBackgroundResource(R.drawable.classic_shape);
			obstacle.setBackgroundResource(R.drawable.classic_shape);
			target.setBackgroundResource(R.drawable.classic_shape);
			background.setBackgroundResource(R.drawable.classic_shape);
			background.setBackgroundColor(Color.WHITE);
			break;
		}
	}

//	@Override
//	public void saveMap(int[] colors, String mapName, ArrayList<Point> positions) {
////		controller = new SQLController(getApplicationContext());
////		controller.open();
//		
////		displayState.setText("Сохранение...");
//		
//		// Перевод позиции начальной и конечной точки в координаты сетки
//		Point pPos = new Point(view.getPlayer().getGx(), view.getPlayer().getGy());
//		Point tPos = new Point(view.getTarget().getGx(), view.getTarget().getGy());
//
//		int id = 0;
//		if(!controller.nameExists(mapName)) {
//			id = (int) controller.insertMap(mapName, view.getGridSize(), colors[0], colors[1], colors[2]);
//			if(!(positions == null || positions.isEmpty())) {
//				for(Point p : positions) {
//					controller.insertElement((int) p.getX(), (int) p.getY(), 3, id);
//				}
//			}
//			controller.insertElement((int) pPos.getX(), (int) pPos.getY(), 1, id);
//			controller.insertElement((int) tPos.getX(), (int) tPos.getY(), 2, id);
//		} else {
//			int mapID = (int) controller.getIdByName(mapName);
//			id = (int) controller.updateMap(mapID, mapName, view.getGridSize(), colors[0], colors[1], colors[2]);
//			ArrayList<Cell> obstacleCells = controller.readObstacles(mapID);
//			for(Cell cell : obstacleCells) {
//				controller.deleteElements(cell.cx, cell.cy, 3, mapID);
//			}
//			if(!(positions == null || positions.isEmpty())) {
//				for(Point p : positions) {
//					controller.insertElement((int) p.getX(), (int) p.getY(), 3, (int) mapID);
//				}
//			}
//			id = (int) controller.getIdByMapId(1, (int) mapID);
//			controller.updateElements(id, (int) pPos.getX(), (int) pPos.getY(), 1, (int) mapID);
//			id = (int) controller.getIdByMapId(2, (int) mapID);
//			controller.updateElements(id, (int) tPos.getX(), (int) tPos.getY(), 2, (int) mapID);
//		}
//		
////		controller.close();
//		displayState.setText("Сохранено!");
//	}

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