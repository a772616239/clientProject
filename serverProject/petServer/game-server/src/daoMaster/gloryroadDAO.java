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
import model.gloryroad.entity.gloryroadEntity;

/**
 * created by tool
 */

public interface gloryroadDAO extends BaseDAO<gloryroadEntity> {

	@Insert("INSERT INTO gloryroad(playeridx,data) VALUES (#{playeridx},#{data});")
	public void insert(gloryroadEntity entity);

	@Delete("delete from gloryroad where playeridx = #{playeridx}")
	public void delete(String idx);

	@Update("update gloryroad set playeridx=#{playeridx},data=#{data} where playeridx = #{playeridx}")
	public void update(gloryroadEntity entity);

	@Select("SELECT * FROM gloryroad")
	public List<gloryroadEntity> listAll();
	
	
	@Insert("INSERT INTO gloryroad(playeridx,data) VALUES (0,0);")
	public void test();


}