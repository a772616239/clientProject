/**
 * created by tool DAOGenerate
 */
package daoMaster;


import db.core.BaseDAO;
import java.util.List;
import model.stoneRift.stoneriftEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * created by tool
 */

public interface stoneriftDAO extends BaseDAO<stoneriftEntity> {

	@Insert("INSERT INTO stonerift(idx,data) VALUES (#{idx},#{data});")
	public void insert(stoneriftEntity entity);

	@Delete("delete from stonerift where idx = #{idx}")
	public void delete(String idx);

	@Update("update stonerift set idx=#{idx},data=#{data} where idx = #{idx}")
	public void update(stoneriftEntity entity);

	@Select("SELECT * FROM stonerift")
	public List<stoneriftEntity> listAll();
	
	
	@Insert("INSERT INTO stonerift(idx,data) VALUES (0,0);")
	public void test();


}