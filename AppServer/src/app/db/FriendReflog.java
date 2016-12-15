package app.db;

import engine.java.dao.annotation.DAOPrimaryKey;
import engine.java.dao.annotation.DAOProperty;
import engine.java.dao.annotation.DAOTable;

/**
 * 好友操作日志
 */
@DAOTable
public class FriendReflog {

    @DAOPrimaryKey(autoincrement=true)
    private long id;
    
    @DAOProperty
    public long uid;                        // 用户ID

    @DAOProperty
    public long friend_id;                  // 好友ID
    
    /**
     * 1：增加
     * 2：删除
     * 3：更新
     */
    @DAOProperty
    public int op;                          // 操作指令

    @DAOProperty
    public long time;                       // 记录时间

    @DAOProperty
    public boolean isFriend;                // 记录当前状态是否为好友
}