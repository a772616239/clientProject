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
import model.playerrecentpass.entity.playerrecentpassEntity;

/**
 * created by tool
 */

public interface playerrecentpassDAO extends BaseDAO<playerrecentpassEntity> {

	@Insert("INSERT INTO playerrecentpass(playeridx,data) VALUES (#{playeridx},#{data});")
	public void insert(playerrecentpassEntity entity);

	@Delete("delete from playerrecentpass where playeridx = #{playeridx}")
	public void delete(String idx);

	@Update("update playerrecentpass set playeridx=#{playeridx},data=#{data} where playeridx = #{playeridx}")
	public void update(playerrecentpassEntity entity);

	@Select("SELECT * FROM playerrecentpass")
	public List<playerrecentpassEntity> listAll();
	
	
	@Insert("INSERT INTO playerrecentpass(playeridx,data) VALUES (0,0);")
	public void test();


}