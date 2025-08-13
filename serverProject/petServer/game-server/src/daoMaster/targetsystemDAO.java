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
import model.targetsystem.entity.targetsystemEntity;

/**
 * created by tool
 */

public interface targetsystemDAO extends BaseDAO<targetsystemEntity> {

	@Insert("INSERT INTO targetsystem(idx,linkplayeridx,targetdata) VALUES (#{idx},#{linkplayeridx},#{targetdata});")
	public void insert(targetsystemEntity entity);

	@Delete("delete from targetsystem where idx = #{idx}")
	public void delete(String idx);

	@Update("update targetsystem set idx=#{idx},linkplayeridx=#{linkplayeridx},targetdata=#{targetdata} where idx = #{idx}")
	public void update(targetsystemEntity entity);

	@Select("SELECT * FROM targetsystem")
	public List<targetsystemEntity> listAll();
	
	
	@Insert("INSERT INTO targetsystem(idx,linkplayeridx,targetdata) VALUES (0,0,0);")
	public void test();


}