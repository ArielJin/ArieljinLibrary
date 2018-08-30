package com.arieljin.library.abs;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @time 2018/8/16.
 * @email ariel.jin@tom.com
 */
public class AbsDbHelper<T extends AbsDBModel> extends SQLiteOpenHelper {


    public static final String NOT_NULL = "not_null";
    private final String TAB_NAME;
    protected Context context;
    protected String byUser;

    public static Map<Class<? extends AbsDBModel>, Class<? extends AbsDbHelper<?>>> allDBHelpers = new HashMap<>();

    public AbsDbHelper(Context context, String tab_name) {
        super(context, AbsApplication.getInstance().getDB_NAME(), null, AbsApplication.VERSION_CODE);
        this.context = context;
        TAB_NAME = tab_name;
    }

    public AbsDbHelper(String byUser, Context context, String tab_name) {
        super(context, AbsApplication.getInstance().getDB_NAME(), null, AbsApplication.VERSION_CODE);
        this.byUser = byUser;
        this.context = context;
        TAB_NAME = tab_name;
    }

    private AbsDbHelper(String byUser, Context context, SQLiteDatabase.CursorFactory factory, int version, String tab_name) {
        super(context, AbsApplication.getInstance().getDB_NAME(), factory, AbsApplication.VERSION_CODE);
        this.byUser = byUser;
        this.context = context;
        TAB_NAME = tab_name;
    }

    protected String getDefaultOrderBy() {
        return null;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (AbsApplication.getInstance().isOnMainProcess()) {
            Log.i("ariel", "AbsDbHelper:onCreate");
            create(db);
        }
    }

    private static void create(SQLiteDatabase db) {
        synchronized (NOT_NULL) {
            for (Map.Entry<Class<? extends AbsDBModel>, Class<? extends AbsDbHelper<?>>> entry : allDBHelpers.entrySet()) {
                String tabName;
                try {
                    tabName = (String) entry.getValue().getField("TAB_NAME").get(null);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                createTable(db, entry.getValue(), tabName);
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (AbsApplication.getInstance().isOnMainProcess()) {
            Log.i("ariel", "AbsDbHelper:onUpgrade");
            upgrade(db);
            // MApplication.getInstance().renameFolder();
        }
    }

    private static void upgrade(SQLiteDatabase db) {
        synchronized (NOT_NULL) {
            Set<String> created_tables = new HashSet<String>();
            Cursor cursor = db.rawQuery("select name from sqlite_master where type='table';", null);
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                created_tables.add(name);
            }
            for (Map.Entry<Class<? extends AbsDBModel>, Class<? extends AbsDbHelper<?>>> entry : allDBHelpers.entrySet()) {
                String tabName;
                try {
                    tabName = (String) entry.getValue().getField("TAB_NAME").get(null);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }

                if (!created_tables.contains(tabName)) {
                    createTable(db, entry.getValue(), tabName);
                    Log.i("ariel", "create:" + tabName);
                    continue;
                }

                created_tables.remove(tabName);

                cursor.close();

                cursor = db.query(tabName, null, null, null, null, null, null, "0");

                HashSet<String> oldNames = new HashSet<String>();
                for (String string : cursor.getColumnNames()) {
                    oldNames.add(string);
                }
                cursor.close();

                db.execSQL("ALTER TABLE " + tabName + " RENAME TO __temp__" + tabName);
                Set<String> newNames = createTable(db, entry.getValue(), tabName);

                StringBuilder builder = new StringBuilder(128);
                builder.append("INSERT INTO ");
                builder.append(tabName);
                builder.append("(");

                StringBuilder selectBuilder = new StringBuilder(128);
                for (String string : newNames) {
                    if (oldNames.contains(string)) {
                        builder.append(string);
                        builder.append(",");
                        selectBuilder.append(string);
                        selectBuilder.append(",");
                    }
                }

                if (builder.length() > 0) {
                    builder.deleteCharAt(builder.length() - 1);
                    selectBuilder.deleteCharAt(selectBuilder.length() - 1);
                }

                builder.append(")");
                builder.append(" SELECT ");
                builder.append(selectBuilder);
                builder.append(" FROM __temp__");
                builder.append(tabName);

                // Log.e("ariel", builder.toString());

                db.execSQL(builder.toString());
                db.execSQL("DROP TABLE __temp__" + tabName);
            }

            for (String tabName : created_tables) {
                db.execSQL("DROP TABLE " + tabName);
                Log.i("ariel", "DROP:" + tabName);
            }
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
    }

    @SuppressWarnings("unchecked")
    private Class<T> getEntityClass() {
        Type genType = getClass().getGenericSuperclass();
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        Class<T> entityClass = (Class<T>) params[0];

        return entityClass;
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Serializable> getEntityClass(Class<? extends SQLiteOpenHelper> clz) {
        Type genType = clz.getGenericSuperclass();
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        Class<? extends AbsDBModel> entityClass = (Class<? extends AbsDBModel>) params[0];

        return entityClass;
    }

    private static Set<String> createTable(SQLiteDatabase db, Class<? extends SQLiteOpenHelper> clz, String tabName) {
        StringBuilder builder = new StringBuilder(128);

        builder.append("create table if not exists " + tabName);
        builder.append("(");

        Boolean needAutoincrement = getNeedAutoincrement(clz);
        if (needAutoincrement == null) {
            return null;
        }

        Set<String> result = new HashSet<String>();

        for (Field field : getEntityClass(clz).getFields()) {
            String name = field.getName();

            if (name.equals("uid")) {
                builder.append(name);

                result.add(name);

                if (needAutoincrement) {
                    builder.append(" integer primary key autoincrement,");
                } else {
                    builder.append(" varchar,");
                }
                continue;
            }

            if (name.equals("serialVersionUID"))
                continue;

            Class<?> modelClz = field.getType();

            String type = "";

            if (String.class.isAssignableFrom(modelClz)) {
                type = "varchar";
            } else if (Enum.class.isAssignableFrom(modelClz)) {
                type = "varchar";
            } else if (modelClz == boolean.class || modelClz == Boolean.class) {
                type = "integer";
            } else if (modelClz == int.class || modelClz == Integer.class) {
                type = "integer";
            } else if (modelClz == long.class || modelClz == Long.class) {
                type = "long";
            } else if (modelClz == float.class || modelClz == Float.class) {
                type = "float";
            } else if (modelClz == double.class || modelClz == Double.class) {
                type = "double";
            } else if (Date.class.isAssignableFrom(modelClz)) {
                type = "long";
            } else if (AbsDBModel.class.isAssignableFrom(modelClz)) {
                type = "varchar";
            } else if (modelClz.isArray()) {
                type = "varchar";
            } else {
                // Log.i("nero", name + ":" + modelClz);
                continue;
            }

            result.add(name);

            builder.append(name);
            builder.append(" ");
            builder.append(type);
            builder.append(",");
        }

        if (!needAutoincrement) {
            builder.append("primary key (byUser,uid))");
        } else {
            builder.deleteCharAt(builder.lastIndexOf(","));
            builder.append(")");
        }

        // Log.i("ariel", builder.toString());

        db.execSQL(builder.toString());

        return result;
    }

    private static Boolean getNeedAutoincrement(Class<? extends SQLiteOpenHelper> clz) {
        try {
            return (Boolean) clz.getField("needAutoincrement").get(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean saveOrUpdate(T t, boolean saveChildren) {
        if (byUser != null && !byUser.isEmpty()) {
            t.byUser = byUser;
        }

        if (queryOne(t.uid) != null) {
            int count = 0;

            try {
                synchronized (NOT_NULL) {
                    if (byUser != null && !byUser.isEmpty()) {
                        count = getWritableDatabase().update(TAB_NAME, createContentValues(t, saveChildren), "uid=? and byUser=?", new String[]{t.uid, byUser});
                    } else {
                        count = getWritableDatabase().update(TAB_NAME, createContentValues(t, saveChildren), "uid=?", new String[]{t.uid});
                    }
                }
            } catch (SQLiteException e) {
                e.printStackTrace();
                if (e.getMessage() != null && e.getMessage().contains("no such column")) {
                    upgrade(getWritableDatabase());
                    return saveOrUpdate(t, saveChildren);
                }
            }
            return count > 0;
        } else {
            ContentValues values = createContentValues(t, saveChildren);
            synchronized (NOT_NULL) {
                try {
                    return getWritableDatabase().insertWithOnConflict(TAB_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE) > 0;
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e.getMessage() != null && e.getMessage().contains("no such column")) {
                        upgrade(getWritableDatabase());
                        return saveOrUpdate(t, saveChildren);
                    }
                    return false;
                }
            }
        }
    }

    public int saveOrUpdate(Collection<T> collection, boolean saveChildren) {
        int count = 0;

        for (T t : collection) {
            if (saveOrUpdate(t, saveChildren)) {
                count++;
            }
        }

        return count;
    }

    public boolean update(String columnName, Object newValue) {
        ContentValues values = new ContentValues();

        if (newValue instanceof Boolean) {
            values.put(columnName, (Boolean) newValue ? 1 : 0);
        } else {
            values.put(columnName, newValue.toString());
        }

        int count;
        synchronized (NOT_NULL) {
            if (byUser != null && !byUser.isEmpty()) {
                count = getWritableDatabase().update(TAB_NAME, values, "byUser=?", new String[]{byUser});
            } else {
                count = getWritableDatabase().update(TAB_NAME, values, null, null);
            }
        }
        if (count > 0) {
            return true;
        }
        return false;
    }

    public boolean update(String columnName, Object oldValue, Object newValue) {
        ContentValues values = new ContentValues();

        if (newValue instanceof Boolean) {
            values.put(columnName, (Boolean) newValue ? 1 : 0);
        } else {
            values.put(columnName, newValue.toString());
        }

        if (oldValue instanceof Boolean) {
            oldValue = (Boolean) oldValue ? 1 : 0;
        }

        int count;
        synchronized (NOT_NULL) {
            if (byUser != null && !byUser.isEmpty()) {
                count = getWritableDatabase().update(TAB_NAME, values, "byUser=?" + " and " + columnName + "=?", new String[]{byUser, oldValue.toString()});
            } else {
                count = getWritableDatabase().update(TAB_NAME, values, columnName + "=?", new String[]{oldValue.toString()});
            }
        }
        if (count > 0) {
            return true;
        }
        return false;
    }

    public boolean update(String uid, Map<String, Object> map) {
        ContentValues values = new ContentValues();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Boolean) {
                values.put(key, (Boolean) value ? 1 : 0);
            } else {
                values.put(key, value.toString());
            }
        }

        int count;
        synchronized (NOT_NULL) {
            if (byUser != null && !byUser.isEmpty()) {
                count = getWritableDatabase().update(TAB_NAME, values, "byUser=? and uid=?", new String[]{byUser, uid});
            } else {
                count = getWritableDatabase().update(TAB_NAME, values, "uid=?", new String[]{uid});
            }
        }
        if (count > 0) {
            return true;
        }
        return false;
    }

    public boolean updateIn(String columnName, String inValue, Map<String, Object> map) {
        ContentValues values = new ContentValues();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Boolean) {
                values.put(key, (Boolean) value ? 1 : 0);
            } else {
                values.put(key, value.toString());
            }
        }

        int count;
        synchronized (NOT_NULL) {
            if (byUser != null && !byUser.isEmpty()) {
                count = getWritableDatabase().update(TAB_NAME, values, "byUser=? and " + columnName + " in (" + inValue + ")", new String[]{byUser});
            } else {
                count = getWritableDatabase().update(TAB_NAME, values, new String(columnName + " in (" + inValue + ")"), null);
            }
        }
        if (count > 0) {
            return true;
        }
        return false;
    }

    public boolean delete(T t) {
        int count;
        synchronized (NOT_NULL) {
            if (byUser != null && !byUser.isEmpty()) {
                count = getWritableDatabase().delete(TAB_NAME, "uid=? and byUser=?", new String[]{t.uid, byUser});
            } else {
                count = getWritableDatabase().delete(TAB_NAME, "uid=?", new String[]{t.uid});
            }
        }
        if (count > 0) {
            return true;
        }
        return false;
    }

    public int delete(Map<String, Object> equal) {
        if (equal == null || equal.isEmpty()) {
            return 0;
        }

        if (byUser != null && !byUser.isEmpty()) {
            equal.put("byUser", byUser);
        }

        StringBuilder selection = new StringBuilder();
        ArrayList<String> selectionList = new ArrayList<String>();

        Iterator<Map.Entry<String, Object>> iter = equal.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Object> entry = iter.next();

            selection.append(entry.getKey());

            Object value = entry.getValue();

            if (value.equals(NOT_NULL)) {
                selection.append("!=?");
            } else {
                selection.append("=?");
            }

            if (iter.hasNext()) {
                selection.append(" and ");
            }

            if (value.equals(NOT_NULL)) {
                selectionList.add("null");
            } else if (value instanceof Boolean) {
                selectionList.add((Boolean) value ? "1" : "0");
            } else {
                selectionList.add(String.valueOf(value));
            }
        }

        String[] selectionArgs = null;
        if (selectionList.size() > 0) {
            selectionArgs = new String[selectionList.size()];
            selectionArgs = (String[]) selectionList.toArray(selectionArgs);
        }

        synchronized (NOT_NULL) {
            return getWritableDatabase().delete(TAB_NAME, selection.toString(), selectionArgs);
        }
    }

    public int deleteAll() {
        synchronized (NOT_NULL) {
            return getWritableDatabase().delete(TAB_NAME, null, null);
        }
    }

    public List<T> query(Map<String, Object> equal, Map<String, Object> or, Map<String, String> like, String orderBy, int start, int count) {
        if (byUser != null && !byUser.isEmpty()) {
            if (equal == null) {
                equal = new HashMap<String, Object>();
            }
            equal.put("byUser", byUser);
        }

        if (orderBy == null) {
            orderBy = getDefaultOrderBy();
        }

        StringBuilder selection = new StringBuilder();
        ArrayList<String> selectionList = new ArrayList<String>();

        if (equal != null && !equal.isEmpty()) {
            Iterator<Map.Entry<String, Object>> iter = equal.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, Object> entry = iter.next();

                String key = entry.getKey().trim();
                Object value = entry.getValue();

                selection.append(key);

                if (value.equals(NOT_NULL)) {
                    selection.append("!=?");
                } else if (key.endsWith("<") || key.endsWith(">") || key.endsWith("=")) {
                    selection.append("?");
                } else {
                    selection.append("=?");
                }

                if (iter.hasNext()) {
                    selection.append(" and ");
                }

                if (value.equals(NOT_NULL)) {
                    selectionList.add("null");
                } else if (value instanceof Boolean) {
                    selectionList.add((Boolean) value ? "1" : "0");
                } else {
                    selectionList.add(String.valueOf(value));
                }
            }
        }

        if (or != null && !or.isEmpty()) {
            Iterator<Map.Entry<String, Object>> iter = or.entrySet().iterator();
            selection.append(" and (");

            while (iter.hasNext()) {
                Map.Entry<String, Object> entry = iter.next();

                String key = entry.getKey().trim();
                Object value = entry.getValue();

                selection.append(key);

                if (value.equals(NOT_NULL)) {
                    selection.append("!=?");
                } else if (key.endsWith("<") || key.endsWith(">") || key.endsWith("=")) {
                    selection.append("?");
                } else {
                    selection.append("=?");
                }

                if (iter.hasNext()) {
                    selection.append(" or ");
                }

                if (value.equals(NOT_NULL)) {
                    selectionList.add("null");
                } else if (value instanceof Boolean) {
                    selectionList.add((Boolean) value ? "1" : "0");
                } else {
                    selectionList.add(String.valueOf(value));
                }
            }
            selection.append(")");
        }

        if (like != null && !like.isEmpty()) {
            Iterator<Map.Entry<String, String>> iter = like.entrySet().iterator();
            selection.append(" and ");

            while (iter.hasNext()) {
                Map.Entry<String, String> entry = iter.next();

                selection.append(entry.getKey());
                selection.append(" like ? ");

                if (iter.hasNext()) {
                    selection.append(" and ");
                }

                selectionList.add("%" + entry.getValue() + "%");
            }
        }

        String[] selectionArgs = null;
        if (selectionList.size() > 0) {
            selectionArgs = new String[selectionList.size()];
            selectionArgs = (String[]) selectionList.toArray(selectionArgs);
        }

        Cursor cursor = null;
        synchronized (NOT_NULL) {
            try {
                if (count > 0) {
                    cursor = getWritableDatabase().query(TAB_NAME, null, selection.toString(), selectionArgs, getDefaultGroupBy(), null, orderBy, start + "," + count);
                } else {
                    cursor = getWritableDatabase().query(TAB_NAME, null, selection.toString(), selectionArgs, getDefaultGroupBy(), null, orderBy);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (e.getMessage() != null && e.getMessage().contains("no such table")) {
                    upgrade(getWritableDatabase());
                    return query(equal, or, like, orderBy, start, count);
                }
                return null;
            }
        }

        List<T> list = handleQuery(cursor);

        return list;
    }

    protected String getDefaultGroupBy() {
        return null;
    }

    public T queryOne(String uid) {
        Cursor cursor = null;
        synchronized (NOT_NULL) {
            try {
                if (byUser != null && !byUser.isEmpty()) {
                    cursor = getWritableDatabase().query(TAB_NAME, null, "uid=? and byUser=?", new String[]{uid, byUser}, null, null, null, 0 + "," + 1);
                } else {
                    cursor = getWritableDatabase().query(TAB_NAME, null, "uid=?", new String[]{uid}, null, null, null, 0 + "," + 1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (e.getMessage() != null && e.getMessage().contains("no such table")) {
                    upgrade(getWritableDatabase());
                    return queryOne(uid);
                }
                return null;
            }
        }

        List<T> list = handleQuery(cursor);

        if (list.size() > 0) {
            return list.get(0);
        }

        return null;
    }

    public List<T> search(Map<String, Object> equal, Map<String, Object> or, Map<String, String> filter) {
        return query(equal, or, filter, null, 0, 0);
    }

    public List<T> search(Map<String, Object> equal, Map<String, String> filter) {
        return query(equal, null, filter, null, 0, 0);
    }

    public List<T> search(Map<String, String> filter) {
        return query(null, null, filter, null, 0, 0);
    }

    public List<T> search(Map<String, String> filter, int start, int count) {
        return query(null, null, filter, null, start, count);
    }

    public List<T> query(Map<String, Object> equal, Map<String, Object> or) {
        return query(equal, or, null, null, 0, 0);
    }

    public List<T> query(Map<String, Object> equal) {
        return query(equal, null, null, null, 0, 0);
    }

    public List<T> query(Map<String, Object> equal, int start, int count) {
        return query(equal, null, null, null, start, count);
    }

    public List<T> query() {
        return query(null, null, null, null, 0, 0);
    }

    public List<T> query(int start, int count) {
        return query(null, null, null, null, start, count);
    }

    @SuppressWarnings("unchecked")
    protected ContentValues createContentValues(T t, boolean saveChildren) {
        ContentValues values = new ContentValues();

        for (Field field : getEntityClass().getFields()) {
            try {
                Object object = field.get(t);
                if (field.getName().equals("serialVersionUID"))
                    continue;
                if (object != null) {
                    if (object instanceof String) {
                        values.put(field.getName(), object.toString());
                    } else if (object instanceof Integer) {
                        values.put(field.getName(), object.toString());
                    } else if (object instanceof Long) {
                        values.put(field.getName(), object.toString());
                    } else if (object instanceof Float) {
                        values.put(field.getName(), object.toString());
                    } else if (object instanceof Double) {
                        values.put(field.getName(), object.toString());
                    } else if (object instanceof Date) {
                        values.put(field.getName(), ((Date) object).getTime());
                    } else if (object instanceof Boolean) {
                        values.put(field.getName(), (Boolean) object ? 1 : 0);
                    } else if (object instanceof Enum<?>) {
                        values.put(field.getName(), object.toString());
                    } else if (object instanceof AbsDBModel) {
                        AbsDBModel obj = (AbsDBModel) object;
                        Class<? extends AbsDbHelper<?>> dbClz = allDBHelpers.get(object.getClass());
                        if (saveChildren) {
                            if (obj.uid == null) {
                                Boolean needAutoincrement = getNeedAutoincrement(dbClz);
                                if (needAutoincrement != null && !needAutoincrement) {
                                    obj.uid = UUID.randomUUID().toString();
                                }
                            }
                            AbsDbHelper<AbsDBModel> dbhelper = (AbsDbHelper<AbsDBModel>) dbClz.getConstructor(String.class, Context.class).newInstance(byUser, context);
                            dbhelper.saveOrUpdate(obj, saveChildren);
                            dbhelper.close();
                            values.put(field.getName(), obj.uid);
                        } else {
                            AbsDbHelper<AbsDBModel> dbhelper = (AbsDbHelper<AbsDBModel>) dbClz.getConstructor(String.class, Context.class).newInstance(byUser, context);
                            if (obj.uid != null && dbhelper.queryOne(obj.uid) == null) {
                                dbhelper.saveOrUpdate(obj, saveChildren);
                                dbhelper.close();
                            }
                            values.put(field.getName(), obj.uid);
                        }
                    } else if (field.getType().isArray()) {
                        Object[] array = (Object[]) object;
                        StringBuilder builder = new StringBuilder();
                        for (Object temp : array) {
                            builder.append(temp.toString());
                            builder.append(",");
                        }
                        builder.deleteCharAt(builder.lastIndexOf(","));

                        values.put(field.getName(), builder.toString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return values;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected List<T> handleQuery(Cursor cursor) {
        ArrayList<T> list = new ArrayList<T>();

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Class<T> entityClass = getEntityClass();

            T t;
            try {
                t = entityClass.getConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                return list;
            }

            for (Field field : entityClass.getFields()) {
                try {
                    if (field.getName().equals("serialVersionUID"))
                        continue;
                    Class<?> clz = field.getType();

                    if (String.class.isAssignableFrom(clz)) {
                        field.set(t, cursor.getString(cursor.getColumnIndex(field.getName())));
                    } else if (Enum.class.isAssignableFrom(clz)) {
                        String temp = cursor.getString(cursor.getColumnIndex(field.getName()));
                        if (temp != null) {
                            field.set(t, Enum.valueOf((Class<Enum>) clz, temp));
                        }
                    } else if (clz == boolean.class || clz == Boolean.class) {
                        field.set(t, cursor.getInt(cursor.getColumnIndex(field.getName())) == 1);
                    } else if (clz == int.class || clz == Integer.class) {
                        field.set(t, cursor.getInt(cursor.getColumnIndex(field.getName())));
                    } else if (clz == long.class || clz == Long.class) {
                        field.set(t, cursor.getLong(cursor.getColumnIndex(field.getName())));
                    } else if (clz == float.class || clz == Float.class) {
                        field.set(t, cursor.getFloat(cursor.getColumnIndex(field.getName())));
                    } else if (clz == double.class || clz == Double.class) {
                        field.set(t, cursor.getDouble(cursor.getColumnIndex(field.getName())));
                    } else if (Date.class.isAssignableFrom(clz)) {
                        long temp = cursor.getLong(cursor.getColumnIndex(field.getName()));
                        if (temp > 0) {
                            field.set(t, new Date(temp));
                        }
                    } else if (AbsDBModel.class.isAssignableFrom(clz)) {
                        String uid = cursor.getString(cursor.getColumnIndex(field.getName()));
                        if (uid != null) {
                            AbsDbHelper<?> dbhelper = allDBHelpers.get(clz).getConstructor(String.class, Context.class).newInstance(byUser, context);
                            field.set(t, dbhelper.queryOne(uid));
                            dbhelper.close();
                        }
                    } else if (clz.isArray()) {
                        String temp = cursor.getString(cursor.getColumnIndex(field.getName()));
                        if (temp != null) {
                            String[] array = temp.split(",");
                            Class<?> componentType = clz.getComponentType();

                            if (String.class.isAssignableFrom(componentType)) {
                                field.set(t, array);
                            } else {
                                // for (String temp : array) {
                                //
                                // }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            list.add(t);

            cursor.moveToNext();
        }

        cursor.close();

        return list;
    }

    public static void clearAllDB(String byUser) {
        Context context = AbsApplication.getInstance();
        for (Map.Entry<Class<? extends AbsDBModel>, Class<? extends AbsDbHelper<?>>> entry : allDBHelpers.entrySet()) {
            try {
                AbsDbHelper<?> dbhelper = entry.getValue().getConstructor(String.class, Context.class).newInstance(byUser, context);
                dbhelper.clear();
                dbhelper.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void clear() {
        String sql = "DELETE FROM " + TAB_NAME + " WHERE byUser='" + byUser + "'";
        synchronized (NOT_NULL) {
            getWritableDatabase().execSQL(sql);
        }
    }


}
