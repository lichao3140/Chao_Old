package com.runvision.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class FaceProvider {
    private final static String TAG = "FaceProvider";
    private final static String FACE_DB = "/mnt/sdcard/FaceAndroid/FaceTemplate.db";
    private FaceDataHelper faceHelper;

    public FaceProvider(Context context) {
        faceHelper = new FaceDataHelper(context, FACE_DB, null, 1);
    }

    /**
     * 添加用户记录
     *
     * @param user 用户信息
     * @return 添加记录的id号, 小于0表示失败。
     */
    public synchronized int addUserOutId(User user) {
        int ret = -1;
        if (null != faceHelper) {
            SQLiteDatabase db = faceHelper.getWritableDatabase();
            if (null != db) {
                //name TEXT,type TEXT, sex TEXT, age INTEGER,workNo TEXT,cardNo TEXT,templateImageID TEXT,createTime TEXT
                //userName, identityNo, identityImg, cameraImg, similarity, createTime
                db.execSQL("insert into tUser (name,type, sex, age,workNo,cardNo,templateImageID,createTime) values(?,?,?,?,?,?,?,?)",
                        new Object[]{
                                user.getName(),
                                user.getType(),
                                user.getSex(),
                                user.getAge(),
                                user.getWordNo(),
                                user.getCardNo(),
                                user.getTemplateImageID(),
                                user.getTime()
                        });
                Cursor cursor = db.rawQuery("select last_insert_rowid() from tUser", null);
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        ret = cursor.getInt(0);
                    } else {
                        ret = -4;
                    }
                    cursor.close();
                } else {
                    ret = -3;
                }
            } else {
                ret = -2;
            }
        } else {
            ret = -1;
        }
        return ret;
    }

    public int updateUserById(User user) {
        if (null != faceHelper) {
            SQLiteDatabase db = faceHelper.getWritableDatabase();
            if (null != db) {
                db.execSQL("update tUser set name=?,type=?, sex=?, age=?,workNo=?,cardNo=?,templateImageID=? where id = ?",
                        new Object[]{
                                user.getName(),
                                user.getType(),
                                user.getSex(),
                                user.getAge(),
                                user.getWordNo(),
                                user.getCardNo(),
                                user.getTemplateImageID(),
                                user.getId()
                        });
                return 0;
            } else {
                return -2;
            }
        } else {
            return -1;
        }
    }

    public int deleteUserById(int id) {
        if (null != faceHelper) {
            SQLiteDatabase db = faceHelper.getWritableDatabase();
            if (null != db) {

                db.execSQL("delete from tUser where id =" + id);
                return 0;
            } else {
                return -2;
            }
        } else {
            return -1;
        }
    }

    public int deleteAllUser() {
        if (null != faceHelper) {
            SQLiteDatabase db = faceHelper.getWritableDatabase();
            if (null != db) {

                db.execSQL("delete from tUser");
                return 0;
            } else {
                return -2;
            }
        } else {
            return -1;
        }
    }

    public User getUserByUserId(int id) {
        User user = null;
        if (null != faceHelper) {
            SQLiteDatabase db = faceHelper.getWritableDatabase();
            if (null != db) {
                String sql = "select * from tUser where id = " + id;
                Cursor cursor = db.rawQuery(sql, null);
                if (null != cursor && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    user = new User();
                    //name,type, sex, age,workNo,cardNo,templateImageID,createTime
                    user.setId(cursor.getInt(cursor.getColumnIndex("id")));
                    user.setName(cursor.getString(cursor.getColumnIndex("name")));
                    user.setType(cursor.getString(cursor.getColumnIndex("type")));
                    user.setSex(cursor.getString(cursor.getColumnIndex("sex")));
                    user.setAge(cursor.getInt(cursor.getColumnIndex("age")));
                    user.setWordNo(cursor.getString(cursor.getColumnIndex("workNo")));
                    user.setCardNo(cursor.getString(cursor.getColumnIndex("cardNo")));
                    user.setTemplateImageID(cursor.getString(cursor.getColumnIndex("templateImageID")));
                    user.setTime(cursor.getLong(cursor.getColumnIndex("createTime")));
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return user;
    }




    public User getUserByUserpath(String Imagepath) {
        User user = null;
        if (null != faceHelper) {
            SQLiteDatabase db = faceHelper.getWritableDatabase();
            if (null != db) {
                String sql = "select * from tUser where templateImageID = " + Imagepath;
                Cursor cursor = db.rawQuery( sql, null);
                if (null != cursor && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    user = new User();
                    user.setId(cursor.getInt(cursor.getColumnIndex("id")));
                    user.setName(cursor.getString(cursor.getColumnIndex("name")));
                    user.setType(cursor.getString(cursor.getColumnIndex("type")));
                    user.setSex(cursor.getString(cursor.getColumnIndex("sex")));
                    user.setAge(cursor.getInt(cursor.getColumnIndex("age")));
                    user.setWordNo(cursor.getString(cursor.getColumnIndex("workNo")));
                    user.setCardNo(cursor.getString(cursor.getColumnIndex("cardNo")));
                    user.setTemplateImageID(cursor.getString(cursor.getColumnIndex("templateImageID")));
                    user.setTime(cursor.getLong(cursor.getColumnIndex("createTime")));
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return user;
    }

    public List<User> getAllPoints() {

        List<User> list = null;
        String sql = "select * from tRecord";
        if (null != faceHelper) {
            SQLiteDatabase db = faceHelper.getWritableDatabase();
            if (null != db) {
                Cursor cursor = db.rawQuery(sql, null);
                if (null != cursor && cursor.getCount() > 0) {
                    Log.i("zhuhuilong","cursor："+cursor.getCount());
                    list = new ArrayList<User>();
                    User user = null;
                    while (cursor.moveToNext()) {
                        user = new User();
                        user.setId(cursor.getInt(cursor.getColumnIndex("id")));
                        user.setTime(cursor.getLong(cursor.getColumnIndex("createTime")));
                        list.add(user);
                        Log.i("zhuhuilong","数据库数据："+cursor.getColumnIndex("id"));
                        Log.i("zhuhuilong","数据库数据："+cursor.getColumnIndex("createTime"));

                    }
                    cursor.close();
                }
            }

        }
        return list;
    }


    public List<User> queryUsers(int pageSize, int limit) {
        List<User> list = null;
        if (null != faceHelper) {
            SQLiteDatabase db = faceHelper.getWritableDatabase();
            if (null != db) {
                String sql = "select * from tUser order by id desc limit " + pageSize + " offset " + limit;
                Cursor cursor = db.rawQuery(sql, null);
                if (null != cursor) {
                    list = new ArrayList<User>();
                    User user;
                    while (cursor.moveToNext()) {
                        user = new User();
                        user.setId(cursor.getInt(cursor.getColumnIndex("id")));
                        user.setName(cursor.getString(cursor.getColumnIndex("name")));
                        user.setType(cursor.getString(cursor.getColumnIndex("type")));
                        user.setSex(cursor.getString(cursor.getColumnIndex("sex")));
                        user.setAge(cursor.getInt(cursor.getColumnIndex("age")));
                        user.setWordNo(cursor.getString(cursor.getColumnIndex("workNo")));
                        user.setCardNo(cursor.getString(cursor.getColumnIndex("cardNo")));
                        user.setTemplateImageID(cursor.getString(cursor.getColumnIndex("templateImageID")));
                        user.setTime(cursor.getLong(cursor.getColumnIndex("createTime")));
                        list.add(user);
                    }
                    cursor.close();
                }
            }
        }
        return list;
    }

    public List<User> queryUsers(String sql) {
        List<User> list = null;
        if (null != faceHelper) {
            SQLiteDatabase db = faceHelper.getWritableDatabase();
            if (null != db) {
                Cursor cursor = db.rawQuery(sql, null);
                if (null != cursor) {
                    list = new ArrayList<User>();
                    User user;
                    while (cursor.moveToNext()) {
                        user = new User();
                        user.setId(cursor.getInt(cursor.getColumnIndex("id")));
                        user.setName(cursor.getString(cursor.getColumnIndex("name")));
                        user.setType(cursor.getString(cursor.getColumnIndex("type")));
                        user.setSex(cursor.getString(cursor.getColumnIndex("sex")));
                        user.setAge(cursor.getInt(cursor.getColumnIndex("age")));
                        user.setWordNo(cursor.getString(cursor.getColumnIndex("workNo")));
                        user.setCardNo(cursor.getString(cursor.getColumnIndex("cardNo")));
                        user.setTemplateImageID(cursor.getString(cursor.getColumnIndex("templateImageID")));
                        user.setTime(cursor.getLong(cursor.getColumnIndex("createTime")));
                        list.add(user);
                    }
                    cursor.close();
                }
            }
        }
        return list;
    }

    /**
     * 查询管理员是否存在
     *
     * @param admin
     * @return
     */
    public Admin queryAdmin(Admin admin) {
        Admin admin1 = null;
        if (null != faceHelper) {
            SQLiteDatabase db = faceHelper.getWritableDatabase();
            if (null != db) {
                String sql = "select * from tAdmin where username='" + admin.getUsername() + "' and password='" + admin.getPassword()+"'";
                System.out.println("sql=" + sql);
                Cursor cursor = db.rawQuery(sql, null);
                if (null != cursor && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    admin1 = new Admin();
                    admin1.setId(cursor.getInt(cursor.getColumnIndex("id")));
                    admin1.setUsername(cursor.getString(cursor.getColumnIndex("username")));
                    admin1.setPassword(cursor.getString(cursor.getColumnIndex("password")));

                }
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return admin;
    }


    /**
     * 新增管理员帐号
     *
     * @param admin
     * @return
     */
    public int addAdmin(Admin admin) {
        int ret = -1;
        if (null != faceHelper) {
            SQLiteDatabase db = faceHelper.getWritableDatabase();
            if (null != db) {
                //name TEXT,type TEXT, sex TEXT, age INTEGER,workNo TEXT,cardNo TEXT,templateImageID TEXT,createTime TEXT
                //userName, identityNo, identityImg, cameraImg, similarity, createTime
                db.execSQL("insert into tAdmin(username,password) values(?,?)",
                        new Object[]{
                                admin.getUsername(),
                                admin.getPassword()
                        });
                return 0;
            } else {
                ret = -2;
            }
        } else {
            ret = -1;
        }
        return ret;
    }

    public int querAdminSize() {
        int count = 0;
        String sql = "select count(id) from tAdmin";
        if (null != faceHelper) {
            SQLiteDatabase db = faceHelper.getWritableDatabase();
            if (null != db) {
                Cursor cursor = db.rawQuery(sql, null);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    count = cursor.getInt(0);
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return count;
    }


    /**
     * 查询数据库中的总条数.
     *
     * @return
     */
    public int quaryUserTableRowCount(String sql) {
        int count = 0;
        if (null != faceHelper) {
            SQLiteDatabase db = faceHelper.getWritableDatabase();
            if (null != db) {
                Cursor cursor = db.rawQuery(sql, null);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    count = cursor.getInt(0);
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return count;
    }


    /**
     * 查询数据库中的总条数.
     *
     * @return
     */
    public int quaryRecordTableRowCount(String sql) {
        int count = 0;
//        String sql = "select count(id) from tRecord";
        if (null != faceHelper) {
            SQLiteDatabase db = faceHelper.getWritableDatabase();
            if (null != db) {
                Cursor cursor = db.rawQuery(sql, null);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    count = cursor.getInt(0);
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return count;
    }

    public List<User> queryRecord(String sql) {
        List<User> list = null;
        if (null != faceHelper) {
            SQLiteDatabase db = faceHelper.getWritableDatabase();
            if (null != db) {
                Cursor cursor = db.rawQuery(sql, null);
                if (null != cursor) {
                    list = new ArrayList<User>();
                    while (cursor.moveToNext()) {

                        User user = new User();
                        //name,type, sex, age,workNo,cardNo,templateImageID,createTime
                        user.setId(cursor.getInt(cursor.getColumnIndex("id")));
                        user.setName(cursor.getString(cursor.getColumnIndex("name")));
                        user.setType(cursor.getString(cursor.getColumnIndex("type")));
                        user.setSex(cursor.getString(cursor.getColumnIndex("sex")));
                        user.setAge(cursor.getInt(cursor.getColumnIndex("age")));
                        user.setWordNo(cursor.getString(cursor.getColumnIndex("workNo")));
                        user.setCardNo(cursor.getString(cursor.getColumnIndex("cardNo")));
                        user.setTemplateImageID(cursor.getString(cursor.getColumnIndex("templateImageID")));
                        user.setTime(cursor.getLong(cursor.getColumnIndex("createTime")));
                        //id INTEGER primary key autoincrement,name TEXT,type TEXT, sex TEXT, age INTEGER,workNo TEXT,cardNo TEXT,createTime TEXT,snapImageID TEXT,templateImageID TEXT,score TEXT,comperResult TEXT,comperType)"
                        Record record = new Record();
                        record.setSnapImageID(cursor.getString(cursor.getColumnIndex("snapImageID")));
                        record.setCompertResult(cursor.getString(cursor.getColumnIndex("comperResult")));
                        record.setScore(cursor.getString(cursor.getColumnIndex("score")));
                        record.setComperType(cursor.getString(cursor.getColumnIndex("comperType")));
                        user.setRecord(record);
                        list.add(user);
                    }
                    cursor.close();
                }
            }
        }
        return list;
    }

    /**
     * 添加用户记录
     *
     * @param user 用户信息
     * @return 添加记录的id号, 小于0表示失败。
     */
    public int addRecord(User user) {
        if (null != faceHelper) {
            SQLiteDatabase db = faceHelper.getWritableDatabase();
            if (null != db) {
                //id INTEGER primary key autoincrement,name TEXT,type TEXT, sex TEXT, age INTEGER,workNo TEXT,cardNo TEXT,createTime TEXT,snapImageID TEXT,templateImageID TEXT,score TEXT,comperResult TEXT,comperType)";
                db.execSQL("insert into tRecord (name,type, sex,age,workNo,cardNo,createTime,snapImageID,templateImageID,score,comperResult,comperType) values(?,?,?,?,?,?,?,?,?,?,?,?)",
                        new Object[]{
                                user.getName(),
                                user.getType(),
                                user.getSex(),
                                user.getAge(),
                                user.getWordNo(),
                                user.getCardNo(),
                                user.getTime(),
                                user.getRecord().getSnapImageID(),
                                user.getTemplateImageID(),
                                user.getRecord().getScore(),
                                user.getRecord().getCompertResult(),
                                user.getRecord().getComperType()
                        });
                return 0;
            } else {
                return -2;
            }
        } else {
            return -1;
        }
    }

    /**
     * 删除记录
     * @param id
     * @return
     */
    public int deleteRecord(int id){
        if (null != faceHelper) {
            SQLiteDatabase db = faceHelper.getWritableDatabase();
            if (null != db) {

                db.execSQL("delete from tRecord where id =" + id);
                return 0;
            } else {
                return -2;
            }
        } else {
            return -1;
        }
    }

    private void log(String msg) {
        Log.e(TAG, msg);
    }
}
