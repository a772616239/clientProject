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
import model.arena.entity.arenaEntity;

/**
 * created by tool
 */

public interface arenaDAO extends BaseDAO<arenaEntity> {

	@Insert("INSERT INTO arena(playeridx,arenainfo) VALUES (#{playeridx},#{arenainfo});")
	public void insert(arenaEntity entity);

	@Delete("delete from arena where playeridx = #{playeridx}")
	public void delete(String idx);

	@Update("update arena set playeridx=#{playeridx},arenainfo=#{arenainfo} where playeridx = #{playeridx}")
	public void update(arenaEntity entity);

	@Select("SELECT * FROM arena")
	public List<arenaEntity> listAll();
	
	
	@Insert("INSERT INTO arena(playeridx,arenainfo) VALUES (0,0);")
	public void test();


}