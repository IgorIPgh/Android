package databases;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import entities.Entity;
import entities.Obstacle;
import math.Point;
import views.AnimatePath;

public class SQLController {

	public String gridFormat = "INSERT INTO" + DBHelper.TABLE_ELEMENTS + "(" //
			+ DBHelper.MAP_NAME + ", " + DBHelper.GRID_SIZE + ", " //
			+ DBHelper.PLAYER_COLOR + ", " + DBHelper.TARGET_COLOR + ", " //
			+ DBHelper.PATH_COLOR + ") VALUES ('%s', %s, %s, %s, %s);";
	public String cellFormat = "INSERT INTO" + DBHelper.TABLE_GRIDS + "(" //
			+ DBHelper.CELL_X + ", " + DBHelper.CELL_Y + ", " //
			+ DBHelper.CELL_CODE + "," //
			+ DBHelper.MAP_ID + ") VALUES (%s, %s, %s, %d);";

	private DBHelper dbhelper;
	private Context ourcontext;
	private SQLiteDatabase database;

	public SQLController(Context c) {
		ourcontext = c;
		dbhelper = new DBHelper(ourcontext);
	}

	public SQLController open() throws SQLException {
		database = dbhelper.getWritableDatabase();
		return this;
	}

	public void close() {
		dbhelper.close();
	}

	public int getProfilesCount() {
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		int count = (int) DatabaseUtils.queryNumEntries(db, DBHelper.TABLE_GRIDS);
		db.close();
		return count;
	}

	public static Field cursorToField(Cursor c) {
		return new Field(c.getInt(0), c.getString(1), c.getInt(2), c.getInt(3), c.getInt(4), c.getInt(5));
	}

	public int getNameCount(String name) {
		database = dbhelper.getReadableDatabase();
		Cursor cursor = database.query(DBHelper.TABLE_GRIDS, new String[] { DBHelper.MAP_NAME },
				DBHelper.MAP_NAME + "=?", new String[] { name }, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();

		int count = cursor.getCount();

		database.close();
		return count;
	}

	public String getNameByID(int mapID) {
		database = dbhelper.getWritableDatabase();
		Cursor cursor = database.query(DBHelper.TABLE_GRIDS, new String[] { DBHelper.MAP_NAME }, DBHelper.ID + "=?",
				new String[] { String.valueOf(mapID) }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();

		String name;
		try {
			name = cursor.getString(0);
		} catch (Exception e) {
			name = null;
		}

		database.close();
		return name;
	}

//	public Field getDefaultMap() {
//		Field field = null;
//		database = dbhelper.getReadableDatabase();
//		String sql = "SELECT * FROM " + DBHelper.TABLE_GRIDS //
//				+ " WHERE " + DBHelper.MAP_NAME + "='default';";
//		Cursor c = database.rawQuery(sql, null);
//		if (c.moveToFirst()) {
//			field = new Field(c.getInt(0), c.getString(1), c.getInt(2), c.getInt(3), c.getInt(4), c.getInt(5));
//		}
//		database.close();
//		return field;
//	}

	public void insertElement(int cx, int cy, int ct, int mapID) {
		database = dbhelper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(DBHelper.CELL_X, cx);
		cv.put(DBHelper.CELL_Y, cy);
		cv.put(DBHelper.CELL_CODE, ct);
		cv.put(DBHelper.MAP_ID, mapID);
		database.insert(DBHelper.TABLE_ELEMENTS, null, cv);
		database.close();
	}

	public long insertMap(String mapName, int gSize, int pColor, int tColor, int pathColor) {
		database = dbhelper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(DBHelper.MAP_NAME, mapName);
		cv.put(DBHelper.GRID_SIZE, gSize);
		cv.put(DBHelper.PLAYER_COLOR, pColor);
		cv.put(DBHelper.TARGET_COLOR, tColor);
		cv.put(DBHelper.PATH_COLOR, pathColor);
		long id = database.insert(DBHelper.TABLE_GRIDS, null, cv);
		database.close();
		return id;
	}

	// data - полученные через bluetooth данные
	// парсим данные и записываем сразу в БД
	public void insertConfig(String data) {
		database = dbhelper.getWritableDatabase();
		String[] sa = data.split("[|]");
		String ar[];
		long newMapId = 0;
		ContentValues cv = new ContentValues();
		for (int i = 0; i < sa.length; i++) {
			if (sa[i].startsWith("#DATA:"))
				continue;
			if (sa[i].startsWith("GRID:")) {
				int pos = sa[i].indexOf(":") + 1;
				ar = sa[i].substring(pos).split("[;]");

				cv.put(DBHelper.MAP_NAME, ar[0]);
				cv.put(DBHelper.GRID_SIZE, ar[1]);
				cv.put(DBHelper.PLAYER_COLOR, ar[2]);
				cv.put(DBHelper.TARGET_COLOR, ar[3]);
				cv.put(DBHelper.PATH_COLOR, ar[4]);
				newMapId = database.insert(DBHelper.TABLE_GRIDS, null, cv);
			}
			if (sa[i].startsWith("CELL:")) {
				int pos = sa[i].indexOf(":") + 1;
				ar = sa[i].substring(pos).split("[;]");

				cv.clear();
				cv.put(DBHelper.CELL_X, ar[0]);
				cv.put(DBHelper.CELL_Y, ar[1]);
				cv.put(DBHelper.CELL_CODE, ar[2]);
				cv.put(DBHelper.MAP_ID, newMapId);
				database.insert(DBHelper.TABLE_ELEMENTS, null, cv);
			}
		}
		database.close();
	}

	public int updateElements(long memberID, int cx, int cy, int ct, int mapID) {
		database = dbhelper.getWritableDatabase();
		ContentValues cvUpdate = new ContentValues();
		cvUpdate.put(DBHelper.CELL_X, cx);
		cvUpdate.put(DBHelper.CELL_Y, cy);
		cvUpdate.put(DBHelper.CELL_CODE, ct);
		cvUpdate.put(DBHelper.MAP_ID, mapID);
		int i = database.update(DBHelper.TABLE_ELEMENTS, cvUpdate, DBHelper.ID + " = " + memberID, null);
		database.close();
		return i;
	}

	public int updateMap(long memberID, String mapName, int gSize, int pColor, int tColor, int pathColor)
			throws SQLException {
		database = dbhelper.getWritableDatabase();
		ContentValues cvUpdate = new ContentValues();
		cvUpdate.put(DBHelper.MAP_NAME, mapName);
		cvUpdate.put(DBHelper.GRID_SIZE, gSize);
		cvUpdate.put(DBHelper.PLAYER_COLOR, pColor);
		cvUpdate.put(DBHelper.TARGET_COLOR, tColor);
		cvUpdate.put(DBHelper.PATH_COLOR, pathColor);
		int i = database.update(DBHelper.TABLE_GRIDS, cvUpdate, DBHelper.ID + " = " + memberID, null);
		database.close();
		return i;
	}

//	public Cursor readElements() {
//		Cursor c = database.query(DBHelper.TABLE_ELEMENTS, null, null, null, null, null, null);
//		if (c != null) {
//			c.moveToFirst();
//		}
//		return c;
//	}

//	public int[] getColors(int id) {
//		database = dbhelper.getReadableDatabase();
//		Cursor cursor = database.query(DBHelper.TABLE_GRIDS,
//				new String[] { DBHelper.PLAYER_COLOR, DBHelper.TARGET_COLOR, DBHelper.PATH_COLOR },
//				DBHelper.ID + "=" + id, null, null, null, null, null);
//		if (cursor != null)
//			cursor.moveToFirst();
//
//		int playerColor = cursor.getInt(cursor.getColumnIndex(DBHelper.PLAYER_COLOR));
//		int targetColor = cursor.getInt(cursor.getColumnIndex(DBHelper.TARGET_COLOR));
//		int pathColor = cursor.getInt(cursor.getColumnIndex(DBHelper.PATH_COLOR));
//
//		database.close();
//		return new int[] { playerColor, targetColor, pathColor };
//	}

	public int getIdByName(String name) {
		database = dbhelper.getReadableDatabase();
		Cursor cursor = database.query(DBHelper.TABLE_GRIDS, new String[] { DBHelper.ID }, DBHelper.MAP_NAME + "=?",
				new String[] { name }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();

		database.close();
		int id = cursor.getInt(cursor.getColumnIndex(DBHelper.ID));
		return id;
	}

//	public long getIdByMapId(int cx, int cy, int ct, int mapID) {
//		database = dbhelper.getReadableDatabase();
//		Cursor cursor = database.query(DBHelper.TABLE_ELEMENTS, new String[] { DBHelper.ID },
//				DBHelper.MAP_ID + "=?" + " AND " + DBHelper.CELL_X + "=?" + " AND " + DBHelper.CELL_Y + "=?" + " AND "
//						+ DBHelper.CELL_CODE + "=?",
//				new String[] { String.valueOf(mapID), String.valueOf(cx), String.valueOf(cy), String.valueOf(ct) },
//				null, null, null, null);
//		if (cursor != null)
//			cursor.moveToFirst();
//
//		database.close();
//		int id = cursor.getInt(0);
//		return id;
//	}

	public long getIdByMapId(int ct, int mapID) {
		database = dbhelper.getReadableDatabase();
		Cursor cursor = database.query(DBHelper.TABLE_ELEMENTS, new String[] { DBHelper.ID },
				DBHelper.MAP_ID + "=?" + " AND " + DBHelper.CELL_CODE + "=?",
				new String[] { String.valueOf(mapID), String.valueOf(ct) }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();

		database.close();
		int id = cursor.getInt(0);
		return id;
	}

	public boolean nameExists(String name) {
//		if (getNameCount(name) <= 0)
//			return false;
//		return true;
		return getNameCount(name) > 0;
	}

	public int loadConfiguration(String name, AnimatePath ap) {
		int map_id;
		database = dbhelper.getReadableDatabase();

		// чтение карты
		// _id, mname, gsize, pcolor, tcolor, lcolor
		String sql = "SELECT * FROM " + DBHelper.TABLE_GRIDS //
				+ " WHERE " + DBHelper.MAP_NAME + "='" + name + "';";
		Cursor c = database.rawQuery(sql, null);
		if (c.moveToFirst()) {
			map_id = c.getInt(0);
			ap.setMapParams(new Field(c.getInt(0), c.getString(1), c.getInt(2), c.getInt(3), c.getInt(4), c.getInt(5)));
		} else
			return 1;

		// чтение ячеек:
		// _id, cx, cy, ct, map_id
		sql = "SELECT * FROM " + DBHelper.TABLE_ELEMENTS //
				+ " WHERE " + DBHelper.MAP_ID + "=" + map_id + " ORDER BY ct;";
		c = database.rawQuery(sql, null);
		if (c.moveToFirst()) {
			ArrayList<Obstacle> obstacles = ap.getObstacles();
			obstacles.clear();

			do {
				int cx = c.getInt(c.getColumnIndex(DBHelper.CELL_X));
				int cy = c.getInt(c.getColumnIndex(DBHelper.CELL_Y));
				int ct = c.getInt(c.getColumnIndex(DBHelper.CELL_CODE));

				if (ct == 1) {
					Point pPos = ap.convertToScreenCoords(cx, cy, true);
					ap.setPlayer(new Entity((int) pPos.getX(), (int) pPos.getY(), ap));
				} else if (ct == 2) {
					Point tPos = ap.convertToScreenCoords(cx, cy, true);
					ap.setTarget(new Entity((int) tPos.getX(), (int) tPos.getY(), ap));
				} else if (ct == 3) {
					Point oPos = ap.convertToScreenCoords(cx, cy, true);
					obstacles.add(new Obstacle((int) oPos.getX(), (int) oPos.getY(), ap));
				}

			} while (c.moveToNext());

		} else
			return 2;

		database.close();
		ap.invalidate();
		return 0;
	}

	public int saveConfiguration(String name, int id, AnimatePath ap) {
		database = dbhelper.getWritableDatabase();

		int mapId = (int) insertMap(name, ap.getGridSize(), ap.getPlayerColor(), ap.getTargetColor(),
				ap.getPathColor());

		Entity player = ap.getPlayer();
		insertElement(player.getGx(), player.getGy(), 1, mapId);

		Entity target = ap.getTarget();
		insertElement(target.getGx(), target.getGy(), 2, mapId);

		ArrayList<Obstacle> obstacles = ap.getObstacles();
		for (Obstacle o : obstacles) {
			insertElement(o.getGx(), o.getGy(), 3, mapId);
		}
		database.close();
		return 0;
	}

	public int updateConfiguration(int mapId, String mapName, AnimatePath ap) {
		database = dbhelper.getWritableDatabase();

		updateMap(mapId, mapName, ap.getGridSize(), ap.getPlayerColor(), ap.getTargetColor(), ap.getPathColor());
		deleteCells(mapId);

		Entity player = ap.getPlayer();
		insertElement(player.getGx(), player.getGy(), 1, mapId);

		Entity target = ap.getTarget();
		insertElement(target.getGx(), target.getGy(), 2, mapId);

		ArrayList<Obstacle> obstacles = ap.getObstacles();
		for (Obstacle o : obstacles) {
			insertElement(o.getGx(), o.getGy(), 3, mapId);
		}
		database.close();
		return 0;
	}

	// удаление
//	public void deleteCell(int cell_id) {
//		database = dbhelper.getWritableDatabase();
//		database.delete(DBHelper.TABLE_ELEMENTS, DBHelper.ID + cell_id, null);
//		database.close();
//	}

	public void deleteCells(int map_id) {
		database = dbhelper.getWritableDatabase();
		database.delete(DBHelper.TABLE_ELEMENTS, DBHelper.MAP_ID + "=" + map_id, null);
		database.close();
	}

//	public void deleteCell(int oX, int oY, int map_id) {
//		database = dbhelper.getWritableDatabase();
//		database.delete(DBHelper.TABLE_ELEMENTS, DBHelper.CELL_X + "=" + oY + " AND " + DBHelper.MAP_ID + "=" + map_id
//				+ " AND " + DBHelper.CELL_Y + "=" + oY, null);
//		database.close();
//	}

//	public void deleteObstacles(long mapID) {
//		database = dbhelper.getWritableDatabase();
//		database.delete(DBHelper.TABLE_ELEMENTS, DBHelper.MAP_ID + "=" + mapID + " AND " + DBHelper.CELL_CODE + ">2",
//				null);
//		database.close();
//	}

	public int deleteMap(long id) {
		database = dbhelper.getWritableDatabase();
		database.execSQL("PRAGMA foreign_keys=on;");
		int i = database.delete(DBHelper.TABLE_GRIDS, DBHelper.ID + "=" + id, null);
		database.close();
		return i;
	}

	public int deleteElements(int cx, int cy, int ct, int map_id) {
		database = dbhelper.getWritableDatabase();
		int i = database.delete(DBHelper.TABLE_ELEMENTS, DBHelper.CELL_X + "=" + cx + " AND " + DBHelper.MAP_ID + "="
				+ map_id + " AND " + DBHelper.CELL_Y + "=" + cy + " AND " + DBHelper.CELL_CODE + "=" + ct, null);
		database.close();
		return i;
	}

	public int getGSize(int map_id) {
		database = dbhelper.getReadableDatabase();
		Cursor cursor = database.query(DBHelper.TABLE_GRIDS, new String[] { DBHelper.GRID_SIZE }, DBHelper.ID + "=?",
				new String[] { String.valueOf(map_id) }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();

		database.close();
		return cursor.getInt(0);
	}
//	public Field getField(int map_id) {
//		database = dbhelper.getReadableDatabase();
//		Cursor c = database.query(DBHelper.TABLE_GRIDS, new String[] { DBHelper.GRID_SIZE }, DBHelper.ID + "=?",
//				new String[] { String.valueOf(map_id) }, null, null, null, null);
//		if (c != null)
//			c.moveToFirst();
//
//		database.close();
//		return new Field(c.getInt(0), c.getString(1), c.getInt(2), c.getInt(3), c.getInt(4), c.getInt(5));
//	}

	public Point getEntityPosition(int mapID, int type) {
//		database = dbhelper.getReadableDatabase();
		Cursor cursor = database.query(DBHelper.TABLE_ELEMENTS, new String[] { DBHelper.CELL_X, DBHelper.CELL_Y },
				DBHelper.MAP_ID + "=" + mapID + " AND " + DBHelper.CELL_CODE + "=" + type, null, null, null, null,
				null);
		if (cursor != null)
			cursor.moveToFirst();

		int x = cursor.getInt(cursor.getColumnIndex(DBHelper.CELL_X));
		int y = cursor.getInt(cursor.getColumnIndex(DBHelper.CELL_Y));

//		database.close();
		return new Point(x, y);
	}

	public ArrayList<Cell> readObstacles(int map_id) {
		ArrayList<Cell> alc = new ArrayList<Cell>();
		database = dbhelper.getReadableDatabase();
		// _id, cx, cy, ct, map_id
		Cursor c = database.query(DBHelper.TABLE_ELEMENTS, null, //
				DBHelper.MAP_ID + "=" + map_id + " AND " + DBHelper.CELL_CODE + "=3", //
				null, null, null, null, null);
		if (c.moveToFirst()) {
			do {
				Cell cell = new Cell(c.getInt(0), c.getInt(1), c.getInt(2), c.getInt(3), c.getInt(4));
				alc.add(cell);
			} while (c.moveToNext());
		}
		database.close(); // закрываем БД
		return alc; // возвращаем список ячеек
	}

	public Cursor readMaps() {
		database = dbhelper.getReadableDatabase();
		Cursor c = database.query(DBHelper.TABLE_GRIDS, null, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		database.close();
		return c;
	}

	// Формирование данных для передачи по bluetooth
//	public String makeData(String mapname, int gSize, int[] colors, Point pPos, Point tPos, ArrayList<Cell> obstacles) {
//		StringBuilder data = new StringBuilder();
//		data.append("#DATA:|");
//		data.append(String.format("GRID:%s;%d;%d;%d;%d|", // у тебя целые числа !
//				mapname, gSize, colors[0], colors[1], colors[2]));
//		data.append(String.format("CELL:%d;%d;%d|", (int) pPos.getX(), (int) pPos.getY(), 1));
//		data.append(String.format("CELL:%d;%d;%d|", (int) tPos.getX(), (int) tPos.getY(), 2));
//		for (Cell c : obstacles) {
//			data.append(String.format("CELL:%d;%d;%d|", c.cx, c.cy, 3));
//		}
//		return data.toString();
//	}

	// Формирование данных для передачи по bluetooth
	public String makeData(Field field) {
		database = dbhelper.getReadableDatabase();
		Point pPos = getEntityPosition(field.id, 1);
		Point tPos = getEntityPosition(field.id, 2);
		ArrayList<Cell> obstacles = readObstacles(field.id);
		StringBuilder data = new StringBuilder();
		data.append("#DATA:|");
		data.append(String.format("GRID:%s;%d;%d;%d;%d|", // у тебя целые числа !
				field.mapName, field.gsize, field.pcolor, field.tcolor, field.lcolor));
		data.append(String.format("CELL:%d;%d;%d|", (int) pPos.getX(), (int) pPos.getY(), 1));
		data.append(String.format("CELL:%d;%d;%d|", (int) tPos.getX(), (int) tPos.getY(), 2));
		for (Cell c : obstacles) {
			data.append(String.format("CELL:%d;%d;%d|", c.cx, c.cy, 3));
		}
		database.close();
		return data.toString();
	}
	
	// формирование списка карт
//	public ArrayList<Field> makeMapList(int maps) {
//		ArrayList<Field> mapList = new ArrayList<Field>();
//		Cursor cur = readMaps();
//		do {
//			mapList.add(SQLController.cursorToField(cur));
//		} while (cur.moveToNext());
//		return mapList;
//	}

}