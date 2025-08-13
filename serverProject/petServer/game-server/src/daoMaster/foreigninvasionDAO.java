/**
 * created by tool DAOGenerate
 */
package daoMaster;


import db.core.BaseDAO;
import java.util.List;
import model.foreignInvasion.entity.foreigninvasionEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;


/**
 * created by tool
 */

public interface foreigninvasionDAO extends BaseDAO<foreigninvasionEntity> {

	@Insert("INSERT INTO foreigninvasion(playeridx,info) VALUES (#{playeridx},#{info});")
	public void insert(foreigninvasionEntity entity);

	@Delete("delete from foreigninvasion where playeridx = #{playeridx}")
	public void delete(String idx);

	@Update("update foreigninvasion set playeridx=#{playeridx},info=#{info} where playeridx = #{playeridx}")
	public void update(foreigninvasionEntity entity);

	@Select("SELECT * FROM foreigninvasion")
	public List<foreigninvasionEntity> listAll();
	
	
	@Insert("INSERT INTO foreigninvasion(playeridx,info) VALUES (0,0);")
	public void test();


}