/**
 * created by tool DAOGenerate
 */
package daoMaster;


import db.core.BaseDAO;
import model.gameplay.entity.gameplayEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * created by tool
 */

public interface gameplayDAO extends BaseDAO<gameplayEntity> {

	@Insert("INSERT INTO gameplay(idx,gameplayinfo) VALUES (#{idx},#{gameplayinfo});")
	public void insert(gameplayEntity entity);

	@Delete("delete from gameplay where idx = #{idx}")
	public void delete(String idx);

	@Update("update gameplay set idx=#{idx},gameplayinfo=#{gameplayinfo} where idx = #{idx}")
	public void update(gameplayEntity entity);

	@Select("SELECT * FROM gameplay")
	public List<gameplayEntity> listAll();
	
	
	@Insert("INSERT INTO gameplay(idx,gameplayinfo) VALUES (0,0);")
	public void test();


}