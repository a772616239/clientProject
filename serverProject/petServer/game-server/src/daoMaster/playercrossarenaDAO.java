/**
 * created by tool DAOGenerate
 */
package daoMaster;

import db.core.BaseDAO;
import model.crossarena.entity.playercrossarenaEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * created by tool
 */

public interface playercrossarenaDAO extends BaseDAO<playercrossarenaEntity> {

    @Insert("INSERT INTO playercrossarena(idx,data) VALUES (#{idx},#{data});")
    public void insert(playercrossarenaEntity entity);

    @Delete("delete from playercrossarena where idx = #{idx}")
    public void delete(String idx);

    @Update("update playercrossarena set idx=#{idx},data=#{data} where idx = #{idx}")
    public void update(playercrossarenaEntity entity);

    @Select("SELECT * FROM playercrossarena")
    public List<playercrossarenaEntity> listAll();

    @Insert("INSERT INTO playercrossarena(idx,data) VALUES (0,0);")
    public void test();

}