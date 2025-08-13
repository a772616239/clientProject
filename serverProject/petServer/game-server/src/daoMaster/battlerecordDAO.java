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
import model.battlerecord.entity.battlerecordEntity;

/**
 * created by tool
 */

public interface battlerecordDAO extends BaseDAO<battlerecordEntity> {

    @Insert("INSERT INTO battlerecord(battleid,version,data) VALUES (#{battleid},#{version},#{data});")
    public void insert(battlerecordEntity entity);

    @Delete("delete from battlerecord where battleid = #{battleid}")
    public void delete(String idx);

    @Update("update battlerecord set battleid=#{battleid},version=#{version},data=#{data} where battleid = #{battleid}")
    public void update(battlerecordEntity entity);

    @Select("SELECT * FROM battlerecord")
    public List<battlerecordEntity> listAll();


    @Insert("INSERT INTO battlerecord(battleid,version,data) VALUES (0,0,0);")
    public void test();

    @Select("SELECT * FROM battlerecord WHERE battleid = #{idx};")
    public battlerecordEntity selectByIdx(String idx);
}