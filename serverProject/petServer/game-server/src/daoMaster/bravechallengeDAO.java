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
import model.bravechallenge.entity.bravechallengeEntity;

/**
 * created by tool
 */

public interface bravechallengeDAO extends BaseDAO<bravechallengeEntity> {

	@Insert("INSERT INTO bravechallenge(idx,playeridx,challengeprogress) VALUES (#{idx},#{playeridx},#{challengeprogress});")
	public void insert(bravechallengeEntity entity);

	@Delete("delete from bravechallenge where idx = #{idx}")
	public void delete(String idx);

	@Update("update bravechallenge set idx=#{idx},playeridx=#{playeridx},challengeprogress=#{challengeprogress} where idx = #{idx}")
	public void update(bravechallengeEntity entity);

	@Select("SELECT * FROM bravechallenge")
	public List<bravechallengeEntity> listAll();
	
	
	@Insert("INSERT INTO bravechallenge(idx,playeridx,challengeprogress) VALUES (0,0,0);")
	public void test();


}