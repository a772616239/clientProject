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
import model.mainLine.entity.mainlineEntity;

/**
 * created by tool
 */

public interface mainlineDAO extends BaseDAO<mainlineEntity> {

	@Insert("INSERT INTO mainline(idx,linkplayeridx,mainlinedata) VALUES (#{idx},#{linkplayeridx},#{mainlinedata});")
	public void insert(mainlineEntity entity);

	@Delete("delete from mainline where idx = #{idx}")
	public void delete(String idx);

	@Update("update mainline set idx=#{idx},linkplayeridx=#{linkplayeridx},mainlinedata=#{mainlinedata} where idx = #{idx}")
	public void update(mainlineEntity entity);

	@Select("SELECT * FROM mainline")
	public List<mainlineEntity> listAll();
	
	
	@Insert("INSERT INTO mainline(idx,linkplayeridx,mainlinedata) VALUES (0,0,0);")
	public void test();


}