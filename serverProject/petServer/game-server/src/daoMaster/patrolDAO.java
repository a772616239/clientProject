/**
 * created by tool DAOGenerate
 */
package daoMaster;


import db.core.BaseDAO;
import model.patrol.entity.patrolEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * created by tool
 */

public interface patrolDAO extends BaseDAO<patrolEntity> {

    @Insert("INSERT INTO patrol(idx,playeridx,map,patrolstatus,finish) VALUES (#{idx},#{playeridx},#{map},#{patrolstatus},#{finish});")
    public void insert(patrolEntity entity);

    @Delete("delete from patrol where idx = #{idx}")
    public void delete(String idx);

    @Update("update patrol set idx=#{idx},playeridx=#{playeridx},map=#{map},patrolstatus=#{patrolstatus},finish=#{finish} where idx = #{idx}")
    public void update(patrolEntity entity);

    @Select("SELECT * FROM patrol")
    public List<patrolEntity> listAll();


    @Insert("INSERT INTO patrol(idx,playeridx,map,patrolstatus,finish) VALUES (0,0,0,0,0);")
    public void test();


}