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
import model.petrune.entity.petruneEntity;

/**
 * created by tool
 */

public interface petruneDAO extends BaseDAO<petruneEntity> {

	@Insert("INSERT INTO petrune(idx,playeridx,rune,bagenlarge,capacity) VALUES (#{idx},#{playeridx},#{rune},#{bagenlarge},#{capacity});")
	public void insert(petruneEntity entity);

	@Delete("delete from petrune where idx = #{idx}")
	public void delete(String idx);

	@Update("update petrune set idx=#{idx},playeridx=#{playeridx},rune=#{rune},bagenlarge=#{bagenlarge},capacity=#{capacity} where idx = #{idx}")
	public void update(petruneEntity entity);

	@Select("SELECT * FROM petrune")
	public List<petruneEntity> listAll();
	
	
	@Insert("INSERT INTO petrune(idx,playeridx,rune,bagenlarge,capacity) VALUES (0,0,0,0,0);")
	public void test();


}