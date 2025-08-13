/**
 * created by tool DAOGenerate
 */
package daoMaster;


import java.util.List;


import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import db.core.BaseDAO;
import model.recentpassed.entity.recentpassedEntity;

/**
 * created by tool
 */

public interface recentpassedDAO extends BaseDAO<recentpassedEntity> {

    @Insert("INSERT INTO recentpassed(idx,info) VALUES (#{idx},#{info});")
    public void insert(recentpassedEntity entity);

    @Delete("delete from recentpassed where idx = #{idx}")
    public void delete(String idx);

    @Update("update recentpassed set idx=#{idx},info=#{info} where idx = #{idx}")
    public void update(recentpassedEntity entity);

    @Select("SELECT * FROM recentpassed")
    public List<recentpassedEntity> listAll();


    @Insert("INSERT INTO recentpassed(idx,info) VALUES (0,0);")
    public void test();


}