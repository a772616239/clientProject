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
import model.rank.entity.rankEntity;

/**
 * created by tool
 */

public interface rankDAO extends BaseDAO<rankEntity> {

	@Insert("INSERT INTO rank(idx,data) VALUES (#{idx},#{data});")
	public void insert(rankEntity entity);

	@Delete("delete from rank where idx = #{idx}")
	public void delete(String idx);

	@Update("update rank set idx=#{idx},data=#{data} where idx = #{idx}")
	public void update(rankEntity entity);

	@Select("SELECT * FROM rank")
	public List<rankEntity> listAll();
	
	
	@Insert("INSERT INTO rank(idx,data) VALUES (0,0);")
	public void test();


}